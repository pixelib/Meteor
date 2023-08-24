package com.rpcnis.core.proxy;

import com.rpcnis.core.Rpcnis;
import com.rpcnis.core.models.InvocationDescriptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyInvocHandler implements InvocationHandler {

    private final Rpcnis rpcnis;
    private final String namespace;

    public ProxyInvocHandler(Rpcnis rpcnis, String namespace) {
        this.rpcnis = rpcnis;
        this.namespace = namespace;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // build invocation descriptor
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++)
            argTypes[i] = args[i].getClass();

        InvocationDescriptor invocationDescriptor = new InvocationDescriptor(namespace, method.getName(), args, argTypes, method.getReturnType());

        // wait for response or timeout
        return rpcnis.invokeRemoteMethod(invocationDescriptor, method.getReturnType());
    }

}
