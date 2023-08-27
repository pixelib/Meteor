package com.meteormsg.transport.redis;

import com.meteormsg.base.RpcTransport;
import com.meteormsg.base.enums.Direction;
import com.meteormsg.base.interfaces.SubscriptionHandler;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.Base64;
import java.util.Locale;
import java.util.logging.Logger;

public class RedisTransport implements RpcTransport {

    private final Logger logger = Logger.getLogger(RedisTransport.class.getSimpleName());

    private final JedisPool jedisPool;
    private final String topic;
    private RedisSubscriptionThread redisSubscriptionThread;

    public RedisTransport(JedisPool jedisPool, String topic) {
        this.jedisPool = jedisPool;
        this.topic = topic;
    }

    public RedisTransport(String url, String topic) {
        this.jedisPool = new JedisPool(url);
        this.topic = topic;
    }

    public RedisTransport(String host, int port, String topic) {
        this.jedisPool = new JedisPool(host, port);
        this.topic = topic;
    }

    @Override
    public void send(Direction direction, byte[] bytes) {
        if (jedisPool.isClosed()) {
            throw new IllegalStateException("Jedis pool is closed");
        }

        try (Jedis connection = jedisPool.getResource()) {
            connection.publish(getTopicName(direction), Base64.getEncoder().encodeToString(bytes));
        }
    }

    @Override
    public void subscribe(Direction direction, SubscriptionHandler onReceive) {
        if (jedisPool.isClosed()) {
            throw new IllegalStateException("Jedis pool is closed");
        }

        if (redisSubscriptionThread == null) {
            redisSubscriptionThread = new RedisSubscriptionThread(onReceive, logger, getTopicName(direction), jedisPool);
            redisSubscriptionThread.start().join();
        } else {
            redisSubscriptionThread.subscribe(getTopicName(direction), onReceive);
        }
    }

    public String getTopicName(Direction direction) {
        return topic + "_" + direction.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public void close() throws IOException {
        if (redisSubscriptionThread != null) {
            redisSubscriptionThread.stop();
        }

        jedisPool.close();
    }
}
