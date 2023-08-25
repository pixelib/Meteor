package com.meteormsg.transport.redis;

import com.github.fppt.jedismock.RedisServer;
import com.github.fppt.jedismock.operations.server.MockExecutor;
import com.github.fppt.jedismock.server.ServiceOptions;
import com.meteormsg.base.enums.Direction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

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

        RedisTransport test = new RedisTransport(server.getHost(), server.getBindPort(), topic);
        test.send(Direction.IMPLEMENTATION, message.getBytes());

        test.close();
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

        RedisTransport test = new RedisTransport(server.getHost(), server.getBindPort(), topic);
        test.send(Direction.METHOD_PROXY, message.getBytes());

        test.close();
        server.stop();

        if (!assertionErrors.isEmpty()) {
            throw assertionErrors.get(0);
        }
    }

    @Test
    @Disabled("not implemented yet")
    void subscribe_thenSuccess() throws IOException, InterruptedException {
        String topic = "test";
        String channel = "test_implementation";

        List<AssertionFailedError> assertionErrors = new ArrayList<>();

        RedisServer server = RedisServer.newRedisServer()
                .setOptions(ServiceOptions.withInterceptor((state, command, params) -> {
                    System.out.println("command: " + command);
                    System.out.println("params: " + params);

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

        RedisTransport test = new RedisTransport(server.getHost(), server.getBindPort(), topic);
        test.subscribe(Direction.IMPLEMENTATION, packet -> true);

        test.close();
        server.stop();

        if (!assertionErrors.isEmpty()) {
            throw assertionErrors.get(0);
        }
    }

    @Test
    void getTopicName() {

    }

    @Test
    void close() {
    }
}