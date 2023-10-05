package dev.pixelib.meteor.core.transport.packets;

import dev.pixelib.meteor.base.RpcSerializer;
import dev.pixelib.meteor.base.defaults.GsonSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvocationDescriptorTest {

    private void compareInstances(InvocationDescriptor a, InvocationDescriptor b) {
        assertEquals(a.getNamespace(), b.getNamespace());

        assertEquals(a.getArgs().length, b.getArgs().length);
        for (int i = 0; i < a.getArgs().length; i++) {
            assertEquals(a.getArgs()[i], b.getArgs()[i]);
        }

        assertEquals(a.getArgTypes().length, b.getArgTypes().length);
        for (int i = 0; i < a.getArgTypes().length; i++) {
            assertEquals(a.getArgTypes()[i], b.getArgTypes()[i]);
        }

        assertEquals(a.getReturnType(), b.getReturnType());
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