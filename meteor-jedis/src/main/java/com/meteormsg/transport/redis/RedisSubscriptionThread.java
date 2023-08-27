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
    private boolean isStopping = false;

    private RedisPacketListener jedisPacketListener;

    private final ExecutorService listenerThread = Executors.newSingleThreadExecutor(r -> new Thread(r, "meteor-redis-listener-thread"));

    public RedisSubscriptionThread(SubscriptionHandler messageBroker, Logger logger, String channel, JedisPool jedisPool) {
        this.messageBroker = messageBroker;
        this.logger = logger;
        this.defaultChannel = channel;
        this.jedisPool = jedisPool;
    }

    public CompletableFuture<Boolean> start() {
        jedisPacketListener = new RedisPacketListener(messageBroker, defaultChannel, logger);

        Runnable runnable = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try (Jedis connection = jedisPool.getResource()) {
                    connection.ping();
                    logger.info("Redis connected!");

                    //Start blocking
                    connection.subscribe(jedisPacketListener, jedisPacketListener.getCustomSubscribedChannels().toArray(new String[]{}));
                    break;
                } catch (JedisConnectionException e) {
                    if (isStopping) {
                        logger.info("Redis connection closed, interrupted by stop");
                        return;
                    }
                    logger.log(Level.SEVERE, "Redis has lost connection", e);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        listenerThread.execute(runnable);

        return isSubscribed();

    }

    public void subscribe(String channel, SubscriptionHandler onReceive) {
        jedisPacketListener.subscribe(channel, onReceive);

    }

    public void stop() {
        if (isStopping) return;
        isStopping = true;
        jedisPacketListener.stop();
        listenerThread.shutdownNow();
    }

    private CompletableFuture<Boolean> isSubscribed() {
        return CompletableFuture.supplyAsync(() -> {
            final int maxAttempts = 5;
            for (int i = 0; i < maxAttempts; i++) {
                if (jedisPacketListener.isSubscribed()) {
                    return true;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread was interrupted while waiting for subscription", e);
                }
            }

            // If it fails to subscribe within 5 attempts (5 seconds), throw an exception
            throw new IllegalStateException("Failed to subscribe within the given timeframe");
        });
    }
}
