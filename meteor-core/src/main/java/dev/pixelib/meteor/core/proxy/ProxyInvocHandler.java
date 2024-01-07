package dev.pixelib.meteor.core.proxy;

import dev.pixelib.meteor.core.trackers.OutgoingInvocationTracker;
import dev.pixelib.meteor.core.transport.packets.InvocationDescriptor;

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
        // is args null? if so, create an empty array
        if (args == null) {
            args = new Object[0];
        }

        Class<?>[] argTypes = this.buildInvocationDescriptor(args);

        InvocationDescriptor invocationDescriptor = new InvocationDescriptor(namespace, method.getDeclaringClass(), method.getName(), args, argTypes, method.getReturnType());

        // wait for response or timeout
        return localInvocationTracker.invokeRemoteMethod(invocationDescriptor);
    }

    private Class<?>[] buildInvocationDescriptor(Object[] args) {
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++)
            argTypes[i] = args[i].getClass();
        return argTypes;
    }
}
