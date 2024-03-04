package dev.pixelib.meteor.transport.redis;

import dev.pixelib.meteor.base.interfaces.SubscriptionHandler;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedisSubscriptionThread {

    private final StringMessageBroker messageBroker;
    private final Logger logger;
    private final String defaultChannel;
    private final JedisPool jedisPool;
    private boolean isStopping = false;

    private RedisPacketListener jedisPacketListener;

    private final ExecutorService listenerThread = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "meteor-redis-listener-thread");
        thread.setDaemon(true);
        return thread;
    });

    public RedisSubscriptionThread(StringMessageBroker messageBroker, Logger logger, String channel, JedisPool jedisPool) {
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
                    logger.log(Level.FINE, "Redis connected!");

                    //Start blocking
                    connection.subscribe(jedisPacketListener, jedisPacketListener.getCustomSubscribedChannels().toArray(new String[]{}));
                    break;
                } catch (JedisConnectionException e) {
                    if (isStopping) {
                        logger.log(Level.FINE, "Redis connection closed, interrupted by stop");
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

    public void stop() {
        if (isStopping) return;
        isStopping = true;
        jedisPacketListener.stop();
        listenerThread.shutdownNow();
    }

    public void subscribe(String channel, StringMessageBroker onReceive) {
        jedisPacketListener.subscribe(channel, onReceive);

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
                    throw new CompletionException("Thread was interrupted while waiting for subscription", e);
                }
            }

            // If it fails to subscribe within 5 attempts (5 seconds), throw an exception
            throw new IllegalStateException("Failed to subscribe within the given timeframe");
        });
    }
}
