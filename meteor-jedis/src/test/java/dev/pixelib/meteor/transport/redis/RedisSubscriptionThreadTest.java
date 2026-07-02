package dev.pixelib.meteor.transport.redis;

import com.github.fppt.jedismock.RedisServer;
import com.github.fppt.jedismock.operations.server.MockExecutor;
import com.github.fppt.jedismock.server.ServiceOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import redis.clients.jedis.RedisClient;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class RedisSubscriptionThreadTest {

    private RedisSubscriptionThread subThread;
    private RedisServer server;
    private RedisClient redisClient;

    @AfterEach
    void tearDown() throws IOException {
        if (subThread != null) {
            subThread.stop();
        }
        if (redisClient != null && !redisClient.getPool().isClosed()) {
            redisClient.close();
        }
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void start_success() throws Exception {
        server = RedisServer.newRedisServer().start();
        redisClient = new RedisClient.Builder().hostAndPort(server.getHost(), server.getBindPort()).build();
        subThread = new RedisSubscriptionThread(packet -> true, Logger.getAnonymousLogger(), "channel", redisClient);

        boolean result = subThread.start().join();

        assertTrue(result);
        assertFalse(redisClient.getPool().isClosed());
    }

    @Test
    @Timeout(10)
    void start_NoConnection() {
        redisClient = new RedisClient.Builder().hostAndPort("127.0.0.5", 2314).build();
        subThread = new RedisSubscriptionThread(packet -> true, Logger.getAnonymousLogger(), "channel", redisClient);

        CompletionException exception = assertThrowsExactly(CompletionException.class, () -> {
            subThread.start().join();
        });

        assertEquals(IllegalStateException.class, exception.getCause().getClass());
        assertEquals("Failed to subscribe within the given timeframe", exception.getCause().getMessage());
    }

    @Test
    @Timeout(10)
    void subscribe_success() throws Exception {
        Collection<String> subscribedChannels = new HashSet<>();

        server = RedisServer.newRedisServer()
                .setOptions(ServiceOptions.withInterceptor((state, command, params) -> {
                    if ("subscribe".equals(command)) {
                        subscribedChannels.add(params.getFirst().toString());
                    }
                    return MockExecutor.proceed(state, command, params);
                }))
                .start();

        redisClient = new RedisClient.Builder().hostAndPort(server.getHost(), server.getBindPort()).build();
        subThread = new RedisSubscriptionThread(packet -> true, Logger.getAnonymousLogger(), "channel", redisClient);

        subThread.start().join();

        assertTrue(subscribedChannels.contains("channel"));
    }

    @Test
    @Timeout(10)
    void stop_idempotent() throws Exception {
        server = RedisServer.newRedisServer().start();
        redisClient = new RedisClient.Builder().hostAndPort(server.getHost(), server.getBindPort()).build();
        subThread = new RedisSubscriptionThread(packet -> true, Logger.getAnonymousLogger(), "channel", redisClient);

        subThread.start().join();
        subThread.stop();
        assertDoesNotThrow(() -> subThread.stop());
    }

    @Test
    @Timeout(10)
    void stop_success() throws Exception {
        server = RedisServer.newRedisServer().start();
        redisClient = new RedisClient.Builder().hostAndPort(server.getHost(), server.getBindPort()).build();
        subThread = new RedisSubscriptionThread(packet -> true, Logger.getAnonymousLogger(), "channel", redisClient);

        subThread.start().join();
        subThread.stop();
        redisClient.close();

        assertTrue(redisClient.getPool().isClosed());
    }
}
