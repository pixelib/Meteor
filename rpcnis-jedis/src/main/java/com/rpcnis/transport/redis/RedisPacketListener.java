package com.rpcnis.transport.redis;

import com.rpcnis.base.enums.ReadStatus;
import redis.clients.jedis.JedisPubSub;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class RedisPacketListener extends JedisPubSub {

    private final ExecutorService jedisThreadPool = Executors.newCachedThreadPool();

    private final Function<byte[], ReadStatus> messageBroker;

    private final Collection<String> customSubscribedChannels = ConcurrentHashMap.newKeySet();

    public RedisPacketListener(Function<byte[], ReadStatus> messageBroker, String startChannel) {
        this.messageBroker = messageBroker;
        customSubscribedChannels.add(startChannel);
    }

    @Override
    public void onMessage(String channel, String message) {
        jedisThreadPool.submit(() -> messageBroker.apply(message.getBytes()));
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
