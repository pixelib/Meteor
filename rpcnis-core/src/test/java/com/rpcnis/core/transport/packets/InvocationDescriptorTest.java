package com.rpcnis.core.transport.packets;

import com.rpcnis.base.RpcSerializer;
import com.rpcnis.base.defaults.GsonSerializer;
import com.rpcnis.core.utils.ReflectionUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvocationDescriptorTest {

    private void compareInstances(InvocationDescriptor a, InvocationDescriptor b) {
        assertEquals(a.getNamespace(), b.getNamespace());

        assertEquals(a.getArgs().length, b.getArgs().length);
        for (int i = 0; i < a.getArgs().length; i++) {
            assertEquals(a.getArgs()[i], b.getArgs()[i]);
        }

        assertEquals(a.getArgTypes().length, b.getArgTypes().length);
        for (int i = 0; i < a.getArgTypes().length; i++) {
            assertEquals(ReflectionUtil.ensureBoxedClass(a.getArgTypes()[i]), b.getArgTypes()[i]);
        }

        assertEquals(ReflectionUtil.ensureBoxedClass(a.getReturnType()), b.getReturnType());
    }

    @Test
    public void testSerializationWithNamespace() throws ClassNotFoundException {
        RpcSerializer defaultSerializer = new GsonSerializer();

        InvocationDescriptor original = new InvocationDescriptor(
                "namespace",
                InvocationDescriptorTest.class,
                "methodName",
                new Object[]{1, 2, 3},
                new Class<?>[]{int.class, int.class, int.class},
                int.class
        );

        byte[] serialized = original.toBuffer(defaultSerializer);

        InvocationDescriptor deserialized = InvocationDescriptor.fromBuffer(defaultSerializer, serialized);
        compareInstances(original, deserialized);
    }

    @Test
    public void testSerializationWithoutNamespace() throws ClassNotFoundException {
        RpcSerializer defaultSerializer = new GsonSerializer();

        InvocationDescriptor original = new InvocationDescriptor(
                null,
                InvocationDescriptorTest.class,
                "methodName",
                new Object[]{1, 2, 3},
                new Class<?>[]{int.class, int.class, int.class},
                int.class
        );

        byte[] serialized = original.toBuffer(defaultSerializer);

        InvocationDescriptor deserialized = InvocationDescriptor.fromBuffer(defaultSerializer, serialized);
        compareInstances(original, deserialized);
    }

}