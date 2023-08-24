package com.rpcnis.core.executor;

import com.rpcnis.base.errors.MethodInvocationException;
import com.rpcnis.core.models.InvocationDescriptor;
import com.rpcnis.core.utils.ArgumentTransformer;

import java.lang.reflect.Method;

public class ImplementationWrapper {

    private Object implementation;
    private String namespace;

    public ImplementationWrapper(Object implementation, String namespace) {
        this.implementation = implementation;
        this.namespace = namespace;
    }

    public <T> T invokeOn(InvocationDescriptor invocationDescriptor, Class<T> returnType) throws NoSuchMethodException {
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

}
