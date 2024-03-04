package dev.pixelib.meteor.transport.redis;

import dev.pixelib.meteor.base.RpcTransport;
import dev.pixelib.meteor.base.enums.Direction;
import dev.pixelib.meteor.base.interfaces.SubscriptionHandler;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class RedisTransport implements RpcTransport {

    private final Logger logger = Logger.getLogger(RedisTransport.class.getSimpleName());

    private final JedisPool jedisPool;
    private final String topic;
    private RedisSubscriptionThread redisSubscriptionThread;
    private final UUID transportId = UUID.randomUUID();
    private boolean ignoreSelf = true;

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

    public RedisTransport withIgnoreSelf(boolean ignoreSelf) {
        this.ignoreSelf = ignoreSelf;
        return this;
    }

    @Override
    public void send(Direction direction, byte[] bytes) {
        if (jedisPool.isClosed()) {
            throw new IllegalStateException("Jedis pool is closed");
        }

        try (Jedis connection = jedisPool.getResource()) {
            connection.publish(
                    getTopicName(direction),
                    transportId + Base64.getEncoder().encodeToString(bytes)
            );
        }
    }

    @Override
    public void subscribe(Direction direction, SubscriptionHandler onReceive) {
        if (jedisPool.isClosed()) {
            throw new IllegalStateException("Jedis pool is closed");
        }

        StringMessageBroker wrappedHandler = (message) -> {
            byte[] bytes = message.getBytes();
            // only split after UUID, which always has a length of 36
            byte[] uuid = new byte[36];
            System.arraycopy(bytes, 0, uuid, 0, 36);

            if (ignoreSelf && transportId.toString().equals(new String(uuid))) {
                return false;
            }

            byte[] data = new byte[bytes.length - 36];
            System.arraycopy(bytes, 36, data, 0, data.length);

            try {
                return onReceive.onPacket(Base64.getDecoder().decode(data));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        if (redisSubscriptionThread == null) {
            redisSubscriptionThread = new RedisSubscriptionThread(wrappedHandler, logger, getTopicName(direction), jedisPool);
            redisSubscriptionThread.start().join();
        } else {
            redisSubscriptionThread.subscribe(getTopicName(direction), wrappedHandler);
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
