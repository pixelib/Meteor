package com.rpcnis.core.executor;

import com.rpcnis.base.errors.MethodInvocationException;
import com.rpcnis.core.transport.packets.InvocationDescriptor;
import com.rpcnis.core.utils.ArgumentTransformer;

import java.lang.reflect.Method;

public class ImplementationWrapper {

    private final Object implementation;
    private final String namespace;

    public ImplementationWrapper(Object implementation, String namespace) {
        this.implementation = implementation;
        this.namespace = namespace;
    }

    public <R> R invokeOn(InvocationDescriptor invocationDescriptor, Class<R> returnType) throws NoSuchMethodException {
        // Get the method that should be invoked
        String methodName = invocationDescriptor.getMethodName();
        Class<?>[] argTypes = invocationDescriptor.getArgTypes();

        Method method = implementation.getClass().getMethod(methodName, argTypes);
        // make accessible if private
        method.setAccessible(true);

        // sanitize args
        Object[] args = ArgumentTransformer.overflowArguments(method, invocationDescriptor.getArgs());

        // invoke method
        try {
            Object result = method.invoke(implementation, args);
            return returnType.cast(result);
        } catch (Exception e) {
            throw new MethodInvocationException(invocationDescriptor.getMethodName(), namespace, e);
        }
    }

    public Object getImplementation() {
        return implementation;
    }

    public String getNamespace() {
        return namespace;
    }
}
