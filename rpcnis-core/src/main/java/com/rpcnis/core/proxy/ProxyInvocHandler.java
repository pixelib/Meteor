package com.rpcnis.core.proxy;

import com.rpcnis.core.transport.packets.InvocationDescriptor;
import com.rpcnis.core.trackers.OutgoingInvocationTracker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyInvocHandler implements InvocationHandler {

    private final OutgoingInvocationTracker localInvocationTracker;
    private final String namespace;

    public ProxyInvocHandler(OutgoingInvocationTracker outgoingInvocationTracker, String namespace) {
        this.localInvocationTracker = outgoingInvocationTracker;
        this.namespace = namespace;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // build invocation descriptor
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++)
            argTypes[i] = args[i].getClass();

        InvocationDescriptor invocationDescriptor = new InvocationDescriptor(namespace, method.getDeclaringClass(), method.getName(), args, argTypes, method.getReturnType());

        // wait for response or timeout
        return localInvocationTracker.invokeRemoteMethod(invocationDescriptor);
    }

}
