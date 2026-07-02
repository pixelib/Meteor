package dev.pixelib.meteor.transport.redis;

import com.github.fppt.jedismock.RedisServer;
import com.github.fppt.jedismock.operations.server.MockExecutor;
import com.github.fppt.jedismock.server.ServiceOptions;
import dev.pixelib.meteor.base.enums.Direction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.RedisClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RedisTransportTest {

    private RedisTransport transport;
    private RedisServer server;

    @AfterEach
    void tearDown() throws IOException {
        if (transport != null) {
            transport.close();
        }
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void send_validImplementation() throws IOException {
        String message = "cool_message";

        List<String> published = new ArrayList<>();

        server = RedisServer.newRedisServer()
                .setOptions(ServiceOptions.withInterceptor((state, command, params) -> {
                    if ("publish".equals(command)) {
                        published.add(new String(params.getLast().data()));
                    }
                    return MockExecutor.proceed(state, command, params);
                }))
                .start();

        transport = new RedisTransport(server.getHost(), server.getBindPort(), "test");
        transport.send(Direction.IMPLEMENTATION, message.getBytes());

        assertEquals(1, published.size());
        String raw = published.getFirst();
        String base64Data = raw.substring(36);
        assertEquals(message, new String(Base64.getDecoder().decode(base64Data)));
    }

    @Test
    void send_validMethodProxy() throws IOException {
        String message = "cool_message";

        List<String> published = new ArrayList<>();

        server = RedisServer.newRedisServer()
                .setOptions(ServiceOptions.withInterceptor((state, command, params) -> {
                    if ("publish".equals(command)) {
                        published.add(new String(params.getLast().data()));
                    }
                    return MockExecutor.proceed(state, command, params);
                }))
                .start();

        transport = new RedisTransport(server.getHost(), server.getBindPort(), "test");
        transport.send(Direction.METHOD_PROXY, message.getBytes());

        assertEquals(1, published.size());
        String raw = published.getFirst();
        String base64Data = raw.substring(36);
        assertEquals(message, new String(Base64.getDecoder().decode(base64Data)));
    }

    @Test
    void subscribe_implementation() throws IOException {
        List<String> subscribedChannels = new ArrayList<>();

        server = RedisServer.newRedisServer()
                .setOptions(ServiceOptions.withInterceptor((state, command, params) -> {
                    if ("subscribe".equals(command)) {
                        subscribedChannels.add(params.getFirst().toString());
                    }
                    return MockExecutor.proceed(state, command, params);
                }))
                .start();

        transport = new RedisTransport(server.getHost(), server.getBindPort(), "test");
        transport.subscribe(Direction.IMPLEMENTATION, packet -> true);

        assertEquals(1, subscribedChannels.size());
        assertEquals("test_implementation", subscribedChannels.getFirst());
    }

    @Test
    void subscribe_methodProxy() throws IOException {
        List<String> subscribedChannels = new ArrayList<>();

        server = RedisServer.newRedisServer()
                .setOptions(ServiceOptions.withInterceptor((state, command, params) -> {
                    if ("subscribe".equals(command)) {
                        subscribedChannels.add(params.getFirst().toString());
                    }
                    return MockExecutor.proceed(state, command, params);
                }))
                .start();

        transport = new RedisTransport(server.getHost(), server.getBindPort(), "test");
        transport.subscribe(Direction.METHOD_PROXY, packet -> true);

        assertEquals(1, subscribedChannels.size());
        assertEquals("test_method_proxy", subscribedChannels.getFirst());
    }

    @Test
    void subscribe_secondSubscription() throws IOException {
        server = RedisServer.newRedisServer().start();
        transport = new RedisTransport(server.getHost(), server.getBindPort(), "test");
        transport.subscribe(Direction.METHOD_PROXY, packet -> true);
        transport.subscribe(Direction.IMPLEMENTATION, packet -> true);
    }

    @Test
    void getTopicName_withImplementationDirection() {
        transport = new RedisTransport("redis://localhost:6379", "test");

        String resultTopicName = transport.getTopicName(Direction.IMPLEMENTATION);
        assertEquals("test_implementation", resultTopicName);
    }

    @Test
    void getTopicName_withMethodProxy() {
        transport = new RedisTransport("redis://localhost:6379", "test");

        String resultTopicName = transport.getTopicName(Direction.METHOD_PROXY);
        assertEquals("test_method_proxy", resultTopicName);
    }

    @Test
    void getTopic_nameWithNull() {
        transport = new RedisTransport("redis://localhost:6379", "test");

        assertThrowsExactly(NullPointerException.class, () -> {
            transport.getTopicName(null);
        });
    }

    @Test
    void close_success() throws IOException {
        String topic = "test";
        transport = new RedisTransport("localhost", 6379, topic);

        transport.close();

        assertThrowsExactly(IllegalStateException.class, () -> transport.send(Direction.IMPLEMENTATION, "test".getBytes()));
        assertThrowsExactly(IllegalStateException.class, () -> transport.subscribe(Direction.IMPLEMENTATION, data -> true));
    }

    @Test
    void close_whenAlreadyClosed() throws IOException {
        server = RedisServer.newRedisServer().start();
        transport = new RedisTransport(server.getHost(), server.getBindPort(), "test");
        transport.subscribe(Direction.IMPLEMENTATION, packet -> true);

        transport.close();
        assertDoesNotThrow(() -> transport.close());
    }

    @Test
    void construct_withRedisClient() {
        RedisClient client = RedisClient.create("localhost", 6379);
        transport = new RedisTransport(client, "test");

        assertNotNull(transport);
    }

    @Test
    void construct_withUrl() {
        transport = new RedisTransport("redis://localhost:6379", "test");

        assertNotNull(transport);
    }
}
