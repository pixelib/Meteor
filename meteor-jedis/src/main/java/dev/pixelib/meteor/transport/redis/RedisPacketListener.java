package dev.pixelib.meteor.transport.redis;

import dev.pixelib.meteor.base.interfaces.SubscriptionHandler;
import redis.clients.jedis.JedisPubSub;

import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedisPacketListener extends JedisPubSub {

    private final Logger logger;

    private final ExecutorService jedisThreadPool = Executors.newCachedThreadPool();
    private final Map<String, Set<StringMessageBroker>> messageBrokers = new ConcurrentHashMap<>();
    private final Collection<String> customSubscribedChannels = ConcurrentHashMap.newKeySet();

    public RedisPacketListener(StringMessageBroker messageBroker, String startChannel, Logger logger) {
        this.logger = logger;
        registerBroker(startChannel, messageBroker);
        customSubscribedChannels.add(startChannel);
    }

    @Override
    public void onMessage(String channel, String message) {
        messageBrokers.get(channel).forEach(subscriptionHandler -> {
            try {
                subscriptionHandler.onRedisMessage(message);
            } catch (Exception exception) {
                logger.log(Level.SEVERE, "Error while handling packet", exception);
            }
        });
    }

    public void subscribe(String channel, StringMessageBroker onReceive) {
        registerBroker(channel, onReceive);

        if (customSubscribedChannels.add(channel)) {
            super.subscribe(channel);
        }
    }

    public void stop() {
        unsubscribe();
        jedisThreadPool.shutdownNow();
    }

    public Collection<String> getCustomSubscribedChannels() {
        return customSubscribedChannels;
    }

    private void registerBroker(String channel, StringMessageBroker onReceive) {
        messageBrokers.computeIfAbsent(channel, key -> ConcurrentHashMap.newKeySet()).add(onReceive);
    }
}
