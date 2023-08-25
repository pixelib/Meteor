package com.meteormsg.transport.redis;

import com.meteormsg.base.interfaces.SubscriptionHandler;
import redis.clients.jedis.JedisPubSub;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisPacketListener extends JedisPubSub {

    private final ExecutorService jedisThreadPool = Executors.newCachedThreadPool();

    private final SubscriptionHandler messageBroker;

    private final Collection<String> customSubscribedChannels = ConcurrentHashMap.newKeySet();

    public RedisPacketListener(SubscriptionHandler messageBroker, String startChannel) {
        this.messageBroker = messageBroker;
        customSubscribedChannels.add(startChannel);
    }

    @Override
    public void onMessage(String channel, String message) {
        jedisThreadPool.submit(() -> messageBroker.onPacket(message.getBytes()));
    }

    @Override
    public void onPMessage(String s, String s1, String s2) {

    }

    @Override
    public void onSubscribe(String s, int i) {

    }

    @Override
    public void onUnsubscribe(String s, int i) {

    }

    @Override
    public void onPUnsubscribe(String s, int i) {

    }

    @Override
    public void onPSubscribe(String s, int i) {

    }

    @Override
    public void subscribe(String... channels) {
        for (String channel : channels) {
            if (customSubscribedChannels.add(channel)) {
                super.subscribe(channel);
            }
        }
    }

    public void stop() {
        jedisThreadPool.shutdownNow();
    }

    public Collection<String> getCustomSubscribedChannels() {
        return customSubscribedChannels;
    }
}
