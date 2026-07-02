package dev.pixelib.meteor.transport.redis;

import com.github.fppt.jedismock.RedisServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.Connection;
import redis.clients.jedis.RedisClient;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisPacketListenerTest {

    @Mock
    StringMessageBroker subscriptionHandler;

    @Test
    void onMessage_withValidChannel() throws Exception {
        String topic = "test";
        String expected = "message";

        RedisPacketListener redisPacketListener = new RedisPacketListener(subscriptionHandler, topic, Logger.getAnonymousLogger());

        redisPacketListener.onMessage(topic, expected);
        verify(subscriptionHandler, times(1)).onRedisMessage(expected);
    }

    @Test
    void onMessage_throwException() throws Exception {
        String topic = "test";
        String expected = "message";

        StringMessageBroker handler = new StringMessageBroker() {
            @Override
            public boolean onRedisMessage(String message) throws Exception {
                throw new NullPointerException();
            }
        };

        StringMessageBroker handlerSub = spy(handler);
        RedisPacketListener redisPacketListener = new RedisPacketListener(handlerSub, topic, Logger.getAnonymousLogger());

        redisPacketListener.onMessage(topic, expected);
        verify(handlerSub, times(1)).onRedisMessage(expected);
    }

    @Test
    void onMessage_withUnKnownChannel() {
        String topic = "test";
        String message = "message";

        RedisPacketListener redisPacketListener = new RedisPacketListener(subscriptionHandler, topic, Logger.getAnonymousLogger());

        assertThrows(NullPointerException.class, () -> redisPacketListener.onMessage("fake-test", message));
    }

    @Test
    @Timeout(10)
    void subscribe_success() throws Exception {
        String topic = "test";
        String newTopic = "newTopic";

        RedisServer server = RedisServer.newRedisServer().start();
        try {
            RedisClient redisClient = new RedisClient.Builder().hostAndPort(server.getHost(), server.getBindPort()).build();

            CountDownLatch subscribedLatch = new CountDownLatch(1);
            RedisPacketListener redisPacketListener = new RedisPacketListener(subscriptionHandler, topic, Logger.getAnonymousLogger()) {
                @Override
                public void onSubscribe(String channel, int subscribedChannels) {
                    super.onSubscribe(channel, subscribedChannels);
                    subscribedLatch.countDown();
                }
            };

            Thread runner = new Thread(() -> {
                Connection connection = redisClient.getPool().getResource();
                redisPacketListener.proceed(connection, topic);
            });
            runner.start();

            subscribedLatch.await();

            redisPacketListener.subscribe(newTopic, subscriptionHandler);
            assertTrue(redisPacketListener.getCustomSubscribedChannels().contains(newTopic));

            redisPacketListener.stop();
            runner.join();

            redisClient.close();
        } finally {
            server.stop();
        }

    }

    @Test
    @Timeout(10)
    void stop_success() throws Exception {
        String topic = "test";

        RedisServer server = RedisServer.newRedisServer().start();
        try {
            RedisClient redisClient = new RedisClient.Builder().hostAndPort(server.getHost(), server.getBindPort()).build();

            CountDownLatch subscribedLatch = new CountDownLatch(1);
            RedisPacketListener redisPacketListener = new RedisPacketListener(subscriptionHandler, topic, Logger.getAnonymousLogger()) {
                @Override
                public void onSubscribe(String channel, int subscribedChannels) {
                    super.onSubscribe(channel, subscribedChannels);
                    subscribedLatch.countDown();
                }
            };

            Thread runner = new Thread(() -> {
                Connection connection = redisClient.getPool().getResource();
                redisPacketListener.proceed(connection, topic);
            });
            runner.start();

            subscribedLatch.await();

            redisPacketListener.stop();
            runner.join();

            redisClient.close();
        } finally {
            server.stop();
        }
    }

    @Test
    void getCustomSubscribedChannels_success() {
        String topic = "test";

        RedisPacketListener redisPacketListener = new RedisPacketListener(subscriptionHandler, topic, Logger.getAnonymousLogger());

        Collection<String> result = redisPacketListener.getCustomSubscribedChannels();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(topic));
    }
}
