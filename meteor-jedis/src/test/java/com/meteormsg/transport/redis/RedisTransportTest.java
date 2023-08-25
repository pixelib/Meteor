package com.meteormsg.transport.redis;

import com.github.fppt.jedismock.RedisServer;
import com.github.fppt.jedismock.operations.server.MockExecutor;
import com.github.fppt.jedismock.server.ServiceOptions;
import com.meteormsg.base.enums.Direction;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
class RedisTransportTest {

    @Test
    void sendValidImplementation_thenSuccess() throws IOException {
        String topic = "test";
        String channel = "test_implementation";
        String message = "cool_message";

        RedisServer server = RedisServer.newRedisServer()
                .setOptions(ServiceOptions.withInterceptor((state, command, params) -> {
                    if ("publish".equals(command)) {
                        assertEquals(channel, params.get(0).toString(), "Channel name is not correct");
                        assertEquals(message, params.get(1).toString(), "Message is not correct");
                    }
                    return MockExecutor.proceed(state, command, params);
                }))
                .start();

        RedisTransport test = new RedisTransport(server.getHost(), server.getBindPort(), topic);
        test.send(Direction.IMPLEMENTATION, message.getBytes());

        test.close();
        server.stop();
    }

    @Test
    void sendValidMethodProxy_thenSuccess() throws IOException {
        String topic = "test";
        String channel = "test_method_proxy";
        String message = "cool_message";

        RedisServer server = RedisServer.newRedisServer()
                .setOptions(ServiceOptions.withInterceptor((state, command, params) -> {
                    if ("publish".equals(command)) {
                        assertEquals(channel, params.get(0).toString(), "Channel name is not correct");
                        assertEquals(message, params.get(1).toString(), "Message is not correct");
                    }
                    return MockExecutor.proceed(state, command, params);
                }))
                .start();

        RedisTransport test = new RedisTransport(server.getHost(), server.getBindPort(), topic);
        test.send(Direction.METHOD_PROXY, message.getBytes());

        test.close();
        server.stop();
    }

    @Test
    void subscribe_thenSuccess() throws IOException, InterruptedException {
//        String topic = "test";
//        String channel = "test_implementation";
//
//        RedisServer server = RedisServer.newRedisServer()
//                .setOptions(ServiceOptions.withInterceptor((state, command, params) -> {
//                    System.out.println("command: " + command);
//                    System.out.println("params: " + params);
//
////                    if ("subscribe".equals(command)) {
////                        assertEquals(channel, params.get(0).toString(), "Channel name is not correct");
////                    }
//                    return MockExecutor.proceed(state, command, params);
//                }))
//                .start();
//
//        RedisTransport test = new RedisTransport(server.getHost(), server.getBindPort(), topic);
//        test.subscribe(Direction.IMPLEMENTATION, packet -> true);
//
//        test.close();
//        server.stop();
    }
    @Test
    void getTopicName() {

    }
    @Test
    void close() {
    }
}