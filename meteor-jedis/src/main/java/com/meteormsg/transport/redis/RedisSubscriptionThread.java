package com.meteormsg.transport.redis;

import com.meteormsg.base.interfaces.SubscriptionHandler;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedisSubscriptionThread {

    private final SubscriptionHandler messageBroker;
    private final Logger logger;
    private final String defaultChannel;
    private final JedisPool jedisPool;

    private final ExecutorService pool = Executors.newSingleThreadExecutor(r -> new Thread(r, "meteor-redis-listener-thread"));

    public RedisSubscriptionThread(SubscriptionHandler messageBroker, Logger logger, String channel, JedisPool jedisPool) {
        this.messageBroker = messageBroker;
        this.logger = logger;
        this.defaultChannel = channel;
        this.jedisPool = jedisPool;
    }

    private RedisPacketListener jedisPacketListener;

    public CompletableFuture<Boolean> start() {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        jedisPacketListener = new RedisPacketListener(messageBroker, defaultChannel);

        Runnable runnable = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try (Jedis connection = jedisPool.getResource()) {
                    connection.ping();
                    logger.info("Redis connected!");
                    completableFuture.complete(true);

                    //Start blocking
                    connection.subscribe(jedisPacketListener, jedisPacketListener.getCustomSubscribedChannels().toArray(new String[]{}));
                    break;
                } catch (JedisConnectionException e) {
                    logger.log(Level.SEVERE, "Redis has lost connection", e);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        pool.execute(runnable);

        return completableFuture;
    }

    public void stop() {
        jedisPacketListener.unsubscribe();
        jedisPacketListener.stop();
        pool.shutdownNow();
    }
}
