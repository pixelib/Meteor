package com.rpcnis.core.proxy;

import com.rpcnis.base.errors.InvocationTimedOutException;
import com.rpcnis.core.Rpcnis;
import com.rpcnis.core.models.InvocationDescriptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyInvocHandler implements InvocationHandler {

    private final Rpcnis rpcnis;
    private final String targetName;

    public ProxyInvocHandler(Rpcnis rpcnis, String targetName) {
        this.rpcnis = rpcnis;
        this.targetName = targetName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // build invocation descriptor
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++)
            argTypes[i] = args[i].getClass();

        InvocationDescriptor invocationDescriptor = new InvocationDescriptor(targetName, method.getName(), args, argTypes, method.getReturnType());

        // wait for response or timeout
        return rpcnis.invoke(invocationDescriptor, method.getReturnType());
    }

}
