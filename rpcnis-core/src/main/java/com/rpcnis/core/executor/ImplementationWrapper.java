package com.rpcnis.core.executor;

import com.rpcnis.base.errors.MethodInvocationException;
import com.rpcnis.core.transport.packets.InvocationDescriptor;
import com.rpcnis.core.utils.ReflectionUtil;

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

        Method method = findCompatibleDespiteSignature(implementation.getClass().getDeclaredMethods(), methodName, argTypes);

        if (method == null) {
            throw new NoSuchMethodException("No method found with name " + methodName + " and compatible arguments");
        }

        // make accessible if private
        method.setAccessible(true);

        // sanitize args
        Object[] args = ReflectionUtil.overflowArguments(method, invocationDescriptor.getArgs());

        // invoke method
        try {
            return (R) method.invoke(implementation, args);
        } catch (Exception e) {
            throw new MethodInvocationException(invocationDescriptor.getMethodName(), namespace, e);
        }
    }

    private Method findCompatibleDespiteSignature(Method[] methods, String methodName, Class<?>[] argTypes) {
        for (Method method : methods) {
            if (method.getName().equals(methodName) && argTypes.length >= method.getParameterCount()) {
                // check if the methods match, or if the last method is an optional array then if the other types match
                boolean match = true;
                for (int i = 0; i < method.getParameterCount(); i++) {
                    if (i == method.getParameterCount() - 1 && method.getParameterTypes()[i].isArray()) {
                        // if the last parameter is an array, check if the other parameters match
                        Class<?> arrayType = ReflectionUtil.ensureBoxedClass(method.getParameterTypes()[i].getComponentType());
                        Class<?> typeOfLastValue = ReflectionUtil.ensureBoxedClass(argTypes[argTypes.length - 1].isArray() ? argTypes[argTypes.length - 1].getComponentType() : argTypes[argTypes.length - 1]);

                        // are all the other parameters assignable to the array type?
                        if (!arrayType.isAssignableFrom(typeOfLastValue)) {
                            match = false;
                            break;
                        }
                    } else if (!method.getParameterTypes()[i].isAssignableFrom(argTypes[i])) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return method;
                }
            }
        }
        return null;
    }

    public Object getImplementation() {
        return implementation;
    }

    public String getNamespace() {
        return namespace;
    }
}
