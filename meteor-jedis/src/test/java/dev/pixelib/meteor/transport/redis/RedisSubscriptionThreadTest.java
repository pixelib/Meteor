package dev.pixelib.meteor.transport.redis;

import com.github.fppt.jedismock.RedisServer;
import com.github.fppt.jedismock.operations.server.MockExecutor;
import com.github.fppt.jedismock.server.ServiceOptions;
import dev.pixelib.meteor.base.interfaces.SubscriptionHandler;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.JedisPool;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.commons.function.Try.success;

class RedisSubscriptionThreadTest {

    @Test
    void start_success() throws Exception{
        RedisServer server = RedisServer.newRedisServer().start();

        JedisPool jedisPool = new JedisPool(server.getHost(), server.getBindPort());
        RedisSubscriptionThread subThread = new RedisSubscriptionThread(packet -> true, Logger.getAnonymousLogger(), "channel", jedisPool);

        boolean result = subThread.start().join();

        assertTrue(result);
        assertFalse(jedisPool.isClosed());


        subThread.stop();
        jedisPool.close();
        server.stop();
        assertTrue(jedisPool.isClosed());
    }

    @Test
    void start_NoConnection() throws Exception{
        JedisPool jedisPool = new JedisPool("127.0.0.5", 2314);
        RedisSubscriptionThread subThread = new RedisSubscriptionThread(packet -> true, Logger.getAnonymousLogger(), "channel", jedisPool);

        CompletionException exception = assertThrowsExactly(CompletionException.class, () -> {
            subThread.start().join();
        });

        assertEquals(IllegalStateException.class, exception.getCause().getClass());
        assertEquals("Failed to subscribe within the given timeframe", exception.getCause().getMessage());
    }

    @Test
    void start_reconnect() throws Exception{
        RedisServer server = RedisServer.newRedisServer().start();

        JedisPool jedisPool = new JedisPool(server.getHost(), server.getBindPort());
        RedisSubscriptionThread subThread = new RedisSubscriptionThread(packet -> true, Logger.getAnonymousLogger(), "channel", jedisPool);

        boolean result = subThread.start().join();

        assertTrue(result);
        assertFalse(jedisPool.isClosed());

        server.stop();

        Thread.sleep(100);
        server.start();

        subThread.stop();
        jedisPool.close();
        assertTrue(jedisPool.isClosed());
    }

    @Test
    void subscribe_success() throws Exception{
        Collection<String> subscribedChannels = new HashSet<>();

        RedisServer server = RedisServer.newRedisServer()
                .setOptions(ServiceOptions.withInterceptor((state, command, params) -> {
                    if ("subscribe".equals(command)) {
                        subscribedChannels.add(params.get(0).toString());
                    }
                    return MockExecutor.proceed(state, command, params);
                }))
                .start();

        JedisPool jedisPool = new JedisPool(server.getHost(), server.getBindPort());
        RedisSubscriptionThread subThread = new RedisSubscriptionThread(packet -> true, Logger.getAnonymousLogger(), "channel", jedisPool);

        subThread.start().join();

        subThread.stop();
        jedisPool.close();
        server.stop();
        assertTrue(jedisPool.isClosed());
        assertTrue(subscribedChannels.contains("channel"));
    }

    @Test
    void stop_success() throws Exception {
        RedisServer server = RedisServer.newRedisServer().start();

        JedisPool jedisPool = new JedisPool(server.getHost(), server.getBindPort());
        RedisSubscriptionThread subThread = new RedisSubscriptionThread(packet -> true, Logger.getAnonymousLogger(), "channel", jedisPool);

        subThread.start().join();

        subThread.stop();
        jedisPool.close();
        server.stop();
        assertTrue(jedisPool.isClosed());
    }
}