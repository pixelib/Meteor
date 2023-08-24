package com.rpcnis.core;

import com.rpcnis.base.RpcSerializer;
import com.rpcnis.base.RpcTransport;
import com.rpcnis.base.defaults.GsonSerializer;
import com.rpcnis.base.defaults.LoopbackTransport;
import com.rpcnis.core.executor.ImplementationWrapper;
import com.rpcnis.core.transport.packets.InvocationDescriptor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class LogicTest {

    @Test
    public void testReflectionLogic() throws ClassNotFoundException, NoSuchMethodException {
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

    public int worstCaseScenarioTest(int a, int... addBeforeMultiplying) {
        int result = 0;
        for (int i : addBeforeMultiplying) {
            result += i;
        }
        return a * result;
    }
}
