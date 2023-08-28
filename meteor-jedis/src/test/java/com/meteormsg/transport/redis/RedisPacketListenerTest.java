package com.meteormsg.transport.redis;

import com.meteormsg.base.interfaces.SubscriptionHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.Collection;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisPacketListenerTest {

    @Mock
    SubscriptionHandler subscriptionHandler;

    @Test
    void onMessageWithValidChannel_ThenSuccess() throws Exception {
        String topic = "test";
        String message = "message";
        byte[] expected = Base64.getDecoder().decode(message);

        RedisPacketListener redisPacketListener = new RedisPacketListener(subscriptionHandler, topic, Logger.getAnonymousLogger());

        redisPacketListener.onMessage(topic, message);
        verify(subscriptionHandler, times(1)).onPacket(expected);
        verify(subscriptionHandler).onPacket(argThat(argument -> {
            assertArrayEquals(expected,argument);
            return true;
        }));
    }


    @Test
    void onMessageWithUnKnownChannel_ThenFail() throws Exception {
        String topic = "test";
        String message = "message";

        RedisPacketListener redisPacketListener = new RedisPacketListener(subscriptionHandler, topic, Logger.getAnonymousLogger());

        assertThrows(NullPointerException.class, () -> redisPacketListener.onMessage("fake-test", message));
    }

    @Test
    void subscribe() {
    }

    @Test
    void stop() {
    }

    @Test
    void getCustomSubscribedChannels_ThenSuccess() {
        String topic = "test";

        RedisPacketListener redisPacketListener = new RedisPacketListener(subscriptionHandler, topic, Logger.getAnonymousLogger());

        Collection<String> result = redisPacketListener.getCustomSubscribedChannels();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(topic));
    }
}