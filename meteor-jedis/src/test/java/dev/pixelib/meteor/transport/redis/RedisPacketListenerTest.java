package dev.pixelib.meteor.transport.redis;

import com.github.fppt.jedismock.RedisServer;
import dev.pixelib.meteor.base.interfaces.SubscriptionHandler;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.JedisPool;

import java.util.Base64;
import java.util.Collection;
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
        verify(subscriptionHandler).onRedisMessage(argThat(argument -> {
            assertEquals(expected,argument);
            return true;
        }));
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
        verify(handlerSub).onRedisMessage(argThat(argument -> {
            assertEquals(expected,argument);
            return true;
        }));
    }

    @Test
    void onMessage_withUnKnownChannel() throws Exception {
        String topic = "test";
        String message = "message";

        RedisPacketListener redisPacketListener = new RedisPacketListener(subscriptionHandler, topic, Logger.getAnonymousLogger());

        assertThrows(NullPointerException.class, () -> redisPacketListener.onMessage("fake-test", message));
    }

    @Test
    @Timeout(10)
    @Disabled
    void subscribe_success() throws Exception{
        String topic = "test";
        String newTopic = "newTopic";

        RedisServer server = RedisServer.newRedisServer().start();

        JedisPool jedisPool = new JedisPool(server.getHost(), server.getBindPort());


        RedisPacketListener redisPacketListener = new RedisPacketListener(subscriptionHandler, topic, Logger.getAnonymousLogger());

        Thread runner = new Thread(() -> {
            jedisPool.getResource().subscribe(redisPacketListener, topic);
        });

        runner.start();

        while(!redisPacketListener.isSubscribed()) {
            Thread.sleep(20);
        }

        redisPacketListener.subscribe(newTopic, subscriptionHandler);

        assertTrue(redisPacketListener.getCustomSubscribedChannels().contains(newTopic));

        redisPacketListener.stop();

        while(redisPacketListener.isSubscribed()) {
            Thread.sleep(20);
        }

        jedisPool.close();
        server.stop();


        assertEquals(0, redisPacketListener.getSubscribedChannels());
    }

    @Test
    @Timeout(10)
    @Disabled
    void stop_success() throws Exception{
        String topic = "test";

        RedisServer server = RedisServer.newRedisServer().start();

        JedisPool jedisPool = new JedisPool(server.getHost(), server.getBindPort());


        RedisPacketListener redisPacketListener = new RedisPacketListener(subscriptionHandler, topic, Logger.getAnonymousLogger());

        Thread runner = new Thread(() -> {
            jedisPool.getResource().subscribe(redisPacketListener, topic);
        });

        runner.start();

        while(!redisPacketListener.isSubscribed()) {
            Thread.sleep(20);
        }

        redisPacketListener.stop();
        jedisPool.close();
        server.stop();

        assertEquals(0, redisPacketListener.getSubscribedChannels());
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