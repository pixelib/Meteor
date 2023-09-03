package dev.pixelib.meteor.transport.redis;

import com.github.fppt.jedismock.RedisServer;
import com.github.fppt.jedismock.operations.server.MockExecutor;
import com.github.fppt.jedismock.server.ServiceOptions;
import dev.pixelib.meteor.base.enums.Direction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RedisTransportTest {

    @Test
    @Disabled
    void send_validImplementation() throws IOException {
        String topic = "test";
        String channel = "test_implementation";
        String message = "cool_message";

        List<AssertionFailedError> assertionErrors = new ArrayList<>();

        RedisServer server = RedisServer.newRedisServer()
                .setOptions(ServiceOptions.withInterceptor((state, command, params) -> {
                    try {
                        if ("publish".equals(command)) {
                            // loop over all params and print them
                            assertEquals(channel, params.get(0).toString(), "Channel name is not correct");
                            assertEquals(message, new String(Base64.getDecoder().decode(params.get(1).data())), "Message is not correct");
                        }
                    } catch (AssertionFailedError e) {
                        assertionErrors.add(e);
                    }
                    return MockExecutor.proceed(state, command, params);
                }))
                .start();

        RedisTransport transport = new RedisTransport(server.getHost(), server.getBindPort(), topic);
        transport.send(Direction.IMPLEMENTATION, message.getBytes());

        transport.close();
        server.stop();

        if (!assertionErrors.isEmpty()) {
            throw assertionErrors.get(0);
        }
    }

    @Test
    @Disabled
    void send_validMethodProxy() throws IOException {
        String topic = "test";
        String channel = "test_method_proxy";
        String message = "cool_message";

        List<AssertionFailedError> assertionErrors = new ArrayList<>();

        RedisServer server = RedisServer.newRedisServer()
                .setOptions(ServiceOptions.withInterceptor((state, command, params) -> {
                    try {
                        if ("publish".equals(command)) {
                            // loop over all params and print them
                            assertEquals(channel, params.get(0).toString(), "Channel name is not correct");
                            assertEquals(message, new String(Base64.getDecoder().decode(params.get(1).data())), "Message is not correct");
                        }
                    } catch (AssertionFailedError e) {
                        assertionErrors.add(e);
                    }
                    return MockExecutor.proceed(state, command, params);
                }))
                .start();

        RedisTransport transport = new RedisTransport(server.getHost(), server.getBindPort(), topic);
        transport.send(Direction.METHOD_PROXY, message.getBytes());

        transport.close();
        server.stop();

        if (!assertionErrors.isEmpty()) {
            throw assertionErrors.get(0);
        }
    }

    @Test
    @Disabled
    void subscribe_implementation() throws IOException, InterruptedException {
        String topic = "test";
        String channel = "test_implementation";

        List<AssertionFailedError> assertionErrors = new ArrayList<>();

        RedisServer server = RedisServer.newRedisServer()
                .setOptions(ServiceOptions.withInterceptor((state, command, params) -> {
                    try {
                        if ("subscribe".equals(command)) {
                            assertEquals(channel, params.get(0).toString(), "Channel name is not correct");
                        }
                    } catch (AssertionFailedError e) {
                        assertionErrors.add(e);
                    }
                    return MockExecutor.proceed(state, command, params);
                }))
                .start();

        RedisTransport transport = new RedisTransport(server.getHost(), server.getBindPort(), topic);
        transport.subscribe(Direction.IMPLEMENTATION, packet -> true);

        transport.close();
        server.stop();

        if (!assertionErrors.isEmpty()) {
            throw assertionErrors.get(0);
        }
    }

    @Test
    @Disabled
    void subscribe_methodProxy() throws IOException {
        String topic = "test";
        String channel = "test_method_proxy";

        List<AssertionFailedError> assertionErrors = new ArrayList<>();

        RedisServer server = RedisServer.newRedisServer()
                .setOptions(ServiceOptions.withInterceptor((state, command, params) -> {
                    try {
                        if ("subscribe".equals(command)) {
                            assertEquals(channel, params.get(0).toString(), "Channel name is not correct");
                        }
                    } catch (AssertionFailedError e) {
                        assertionErrors.add(e);
                    }
                    return MockExecutor.proceed(state, command, params);
                }))
                .start();

        RedisTransport transport = new RedisTransport(server.getHost(), server.getBindPort(), topic);
        transport.subscribe(Direction.METHOD_PROXY, packet -> true);

        transport.close();
        server.stop();

        if (!assertionErrors.isEmpty()) {
            throw assertionErrors.get(0);
        }
    }

    @Test
    @Disabled
    void subscribe_secondSubscription() throws IOException {
        String topic = "test";
        String channelProxy = "test_method_proxy";
        String channelImpl = "test_implementation";

        Collection<String> subscribedChannels = new HashSet<>();

        RedisServer server = RedisServer.newRedisServer()
                .setOptions(ServiceOptions.withInterceptor((state, command, params) -> {
                    if ("subscribe".equals(command)) {
                        subscribedChannels.add(params.get(0).toString());
                    }
                    return MockExecutor.proceed(state, command, params);
                }))
                .start();

        RedisTransport transport = new RedisTransport(server.getHost(), server.getBindPort(), topic);
        transport.subscribe(Direction.METHOD_PROXY, packet -> true);
        transport.subscribe(Direction.IMPLEMENTATION, packet -> true);

        transport.close();
        server.stop();

        assertTrue(subscribedChannels.contains(channelProxy));
        assertTrue(subscribedChannels.contains(channelImpl));
    }

    @Test
    void getTopicName_withImplementationDirection() throws IOException {
        String topic = "test";
        String expected = "test_implementation";

        RedisServer server = RedisServer.newRedisServer().start();
        RedisTransport transport = new RedisTransport(server.getHost(), server.getBindPort(), topic);


        String resultTopicName = transport.getTopicName(Direction.IMPLEMENTATION);
        assertEquals(expected, resultTopicName, "Topic name is not correct");


        transport.close();
        server.stop();
    }

    @Test
    @Disabled
    void getTopicName_withMethodProxy() throws IOException {
        String topic = "test";
        String expected = "test_method_proxy";

        RedisServer server = RedisServer.newRedisServer().start();
        RedisTransport transport = new RedisTransport(server.getHost(), server.getBindPort(), topic);


        String resultTopicName = transport.getTopicName(Direction.METHOD_PROXY);
        assertEquals(expected, resultTopicName, "Topic name is not correct");


        transport.close();
        server.stop();
    }



    @Test
    @Disabled
    void getTopic_nameWithNull() throws IOException {
        String topic = "test";

        RedisServer server = RedisServer.newRedisServer().start();
        RedisTransport transport = new RedisTransport(server.getHost(), server.getBindPort(), topic);


        assertThrowsExactly(NullPointerException.class, () -> {
            transport.getTopicName(null);
        }, "Method returned a topic name for a null direction");


        transport.close();
        server.stop();
    }

    @Test
    @Disabled
    void close_success() throws IOException  {
        String topic = "test";

        RedisServer server = RedisServer.newRedisServer().start();

        JedisPool jedisPool = new JedisPool(server.getHost(), server.getBindPort());
        RedisTransport transport = new RedisTransport(jedisPool, topic);

        transport.close();
        server.stop();

        assertThrowsExactly(IllegalStateException.class, () -> {
            transport.send(Direction.IMPLEMENTATION, "test".getBytes());

        }, "Method did not throw an exception when trying to send a message after closing");
        assertThrowsExactly(IllegalStateException.class, () -> {
            transport.subscribe(Direction.IMPLEMENTATION, (data) -> true);
        }, "Method did not throw an exception when trying to send a message after closing");

        assertTrue(jedisPool.isClosed());
    }


    @Test
    @Disabled
    void close_whenAlreadyClosed() throws IOException  {
        String topic = "test";

        RedisServer server = RedisServer.newRedisServer().start();

        JedisPool jedisPool = new JedisPool(server.getHost(), server.getBindPort());
        RedisTransport transport = new RedisTransport(jedisPool, topic);

        transport.close();
        server.stop();

        assertTrue(jedisPool.isClosed());
    }

    @Test
    @Disabled
    void construct_withJedisPool() throws IOException  {
        String topic = "test";

        RedisServer server = RedisServer.newRedisServer().start();

        JedisPool jedisPool = new JedisPool(server.getHost(), server.getBindPort());
        RedisTransport transport = new RedisTransport(jedisPool, topic);

        assertFalse(jedisPool.isClosed());

        transport.close();
        server.stop();

        assertTrue(jedisPool.isClosed());
    }
    @Test
    @Disabled
    void construct_withUrl() throws IOException  {
        String topic = "test";

        RedisServer server = RedisServer.newRedisServer().start();

        RedisTransport transport = new RedisTransport("redis://" + server.getHost() + ":" + server.getBindPort(), topic);
        assertNotNull(transport);

        transport.close();
        server.stop();

    }
}