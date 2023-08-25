package com.meteormsg.core;

import com.meteormsg.base.RpcSerializer;
import com.meteormsg.base.RpcTransport;
import com.meteormsg.base.defaults.GsonSerializer;
import com.meteormsg.base.defaults.LoopbackTransport;
import com.meteormsg.core.executor.ImplementationWrapper;
import com.meteormsg.core.transport.packets.InvocationDescriptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LogicTest {

    @Test
    public void testSerializedReflectionWithPrimitiveArray() throws ClassNotFoundException, NoSuchMethodException {
        // Confirmation that core logic of barebones reflection invocations over a serialized array still works
        RpcTransport transport = new LoopbackTransport();
        RpcSerializer serializer = new GsonSerializer();

        // this should return 30
        InvocationDescriptor invocationDescriptor = new InvocationDescriptor(
                null,
                LogicTest.class,
                "worstCaseScenarioTest",
                new Object[]{2, 5, 5, 5},
                new Class<?>[]{int.class, int[].class},
                int.class
        );

        byte[] serializedInvocationDescriptor = invocationDescriptor.toBuffer(serializer);

        InvocationDescriptor deserializedInvocationDescriptor = InvocationDescriptor.fromBuffer(serializer, serializedInvocationDescriptor);

        ImplementationWrapper implementationWrapper = new ImplementationWrapper(this, null);

        int result = (int) implementationWrapper.invokeOn(deserializedInvocationDescriptor, deserializedInvocationDescriptor.getReturnType());

        assertEquals(30, result);
    }

    @Test
    public void testSerializedReflectionWithPrimitiveArrayAndNull() throws ClassNotFoundException, NoSuchMethodException {
        // Confirmation that core logic of barebones reflection invocations over a serialized array still works
        RpcTransport transport = new LoopbackTransport();
        RpcSerializer serializer = new GsonSerializer();

        // this should return 30
        InvocationDescriptor invocationDescriptor = new InvocationDescriptor(
                null,
                LogicTest.class,
                "worstCaseScenarioTest",
                new Object[]{2, null, 5, 5},
                new Class<?>[]{int.class, int[].class},
                int.class
        );

        byte[] serializedInvocationDescriptor = invocationDescriptor.toBuffer(serializer);

        InvocationDescriptor deserializedInvocationDescriptor = InvocationDescriptor.fromBuffer(serializer, serializedInvocationDescriptor);

        ImplementationWrapper implementationWrapper = new ImplementationWrapper(this, null);

        int result = (int) implementationWrapper.invokeOn(deserializedInvocationDescriptor, deserializedInvocationDescriptor.getReturnType());

        assertEquals(20, result);
    }

    @Test
    public void testSerializedReflectionWithPrimitiveArrayAndNullAndNull() throws ClassNotFoundException, NoSuchMethodException {
        // Confirmation that core logic of barebones reflection invocations over a serialized array still works
        RpcTransport transport = new LoopbackTransport();
        RpcSerializer serializer = new GsonSerializer();

        // this should return 30
        InvocationDescriptor invocationDescriptor = new InvocationDescriptor(
                null,
                LogicTest.class,
                "concatStrings",
                new Object[]{"hello", null, "world"},
                new Class<?>[]{String[].class},
                String.class
        );

        byte[] serializedInvocationDescriptor = invocationDescriptor.toBuffer(serializer);

        InvocationDescriptor deserializedInvocationDescriptor = InvocationDescriptor.fromBuffer(serializer, serializedInvocationDescriptor);

        ImplementationWrapper implementationWrapper = new ImplementationWrapper(this, null);

        String result = (String) implementationWrapper.invokeOn(deserializedInvocationDescriptor, deserializedInvocationDescriptor.getReturnType());

        assertEquals("hellonullworld", result);
    }


    public String concatStrings(String[] strings) {
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            sb.append(s);
        }
        return sb.toString();
    }

    public int worstCaseScenarioTest(int a, int... addBeforeMultiplying) {
        int result = 0;
        for (int i : addBeforeMultiplying) {
            result += i;
        }
        return a * result;
    }
}
