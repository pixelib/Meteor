package com.meteormsg.transport.redis;

import com.github.fppt.jedismock.RedisServer;
import com.github.fppt.jedismock.operations.server.MockExecutor;
import com.github.fppt.jedismock.server.ServiceOptions;
import com.meteormsg.base.enums.Direction;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RedisTransportTest {

    @Test
    void sendValidImplementation_thenSuccess() throws IOException {
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
    void sendValidMethodProxy_thenSuccess() throws IOException {
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
    void subscribeImplementation_thenSuccess() throws IOException, InterruptedException {
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
    void subscribeMethodProxy_thenSuccess() throws IOException {
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
    void getTopicNameWithImplementationDirection_ThenSuccess() throws IOException {
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
    void getTopicNameWithMethodProxy_ThenSuccess() throws IOException {
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
    void getTopicNameWithNull_ThenFail() throws IOException {
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
    void close_ThenSuccess() throws IOException  {
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
    void closeWhenAlreadyClosed_ThenSuccess() throws IOException  {
        String topic = "test";

        RedisServer server = RedisServer.newRedisServer().start();

        JedisPool jedisPool = new JedisPool(server.getHost(), server.getBindPort());
        RedisTransport transport = new RedisTransport(jedisPool, topic);

        transport.close();
        server.stop();

        assertTrue(jedisPool.isClosed());

    }
}