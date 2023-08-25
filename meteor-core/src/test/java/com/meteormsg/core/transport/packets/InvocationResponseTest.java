package com.meteormsg.core.transport.packets;

import com.meteormsg.base.RpcSerializer;
import com.meteormsg.base.defaults.GsonSerializer;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InvocationResponseTest {

    private RpcSerializer serializer = new GsonSerializer();

    @Test
    void testInvocationResponsePrimitive() throws ClassNotFoundException {
        InvocationResponse invocationResponse = new InvocationResponse(UUID.randomUUID(), 1);
        byte[] bytes = invocationResponse.toBytes(serializer);
        InvocationResponse invocationResponse1 = InvocationResponse.fromBytes(serializer, bytes);
        assertEquals(invocationResponse.getInvocationId(), invocationResponse1.getInvocationId());
        assertEquals(invocationResponse.getResult(), invocationResponse1.getResult());
    }

    @Test
    void testInvocationResponseObject() throws ClassNotFoundException {
        InvocationResponse invocationResponse = new InvocationResponse(UUID.randomUUID(), "test");
        byte[] bytes = invocationResponse.toBytes(serializer);
        InvocationResponse invocationResponse1 = InvocationResponse.fromBytes(serializer, bytes);
        assertEquals(invocationResponse.getInvocationId(), invocationResponse1.getInvocationId());
        assertEquals(invocationResponse.getResult(), invocationResponse1.getResult());
    }

    @Test
    void testInvocationResponseNull() throws ClassNotFoundException {
        InvocationResponse invocationResponse = new InvocationResponse(UUID.randomUUID(), null);
        byte[] bytes = invocationResponse.toBytes(serializer);
        InvocationResponse invocationResponse1 = InvocationResponse.fromBytes(serializer, bytes);
        assertEquals(invocationResponse.getInvocationId(), invocationResponse1.getInvocationId());
        assertEquals(invocationResponse.getResult(), invocationResponse1.getResult());
    }

    @Test
    void testInvocationResponsePrimitiveArray() throws ClassNotFoundException {
        InvocationResponse invocationResponse = new InvocationResponse(UUID.randomUUID(), new int[]{1, 2, 3});
        byte[] bytes = invocationResponse.toBytes(serializer);
        InvocationResponse invocationResponse1 = InvocationResponse.fromBytes(serializer, bytes);
        assertEquals(invocationResponse.getInvocationId(), invocationResponse1.getInvocationId());
        assertArrayEquals((int[]) invocationResponse.getResult(), (int[]) invocationResponse1.getResult());
    }
}