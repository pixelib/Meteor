package com.meteormsg.core.executor;

import com.meteormsg.base.errors.MethodInvocationException;
import com.meteormsg.core.transport.packets.InvocationDescriptor;
import com.meteormsg.core.utils.ArgumentMapper;

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

        Method method;
        try {
            method = implementation.getClass().getDeclaredMethod(methodName, argTypes);
        } catch (NoSuchMethodException e) {
            // if the method is not found, try to find a method that is compatible despite signature
            method = findCompatibleDespiteSignature(implementation.getClass().getDeclaredMethods(), methodName, argTypes);

            if (method == null) {
                throw new NoSuchMethodException("No method found with name " + methodName + " and compatible arguments");
            }
        }

        // make accessible if private
        method.setAccessible(true);

        // sanitize args
        Object[] args = ArgumentMapper.overflowArguments(method, invocationDescriptor.getArgs());

        // invoke method
        try {
            return (R) method.invoke(implementation, args);
        } catch (Exception e) {
            throw new MethodInvocationException(invocationDescriptor.getMethodName(), namespace, e);
        }
    }

    /**
     * We don't now yet which method is a best match, because there are multiple ways to arrange the arguments.
     * so this method serves to find the best match for when a normal match cannot be done.
     * this can get pretty computationally expensive, so this should only be done when an initial lookup fails.
     * @param methods   The methods to search through
     * @param methodName The name of the method to find
     * @param argTypes  The argument types of the method to find
     * @return The method if found, null otherwise
     */
    private Method findCompatibleDespiteSignature(Method[] methods, String methodName, Class<?>[] argTypes) {
        for (Method method : methods) {
            if (method.getName().equals(methodName) && argTypes.length >= method.getParameterCount()) {
                // check if the methods match, or if the last method is an optional array then if the other types match
                boolean match = true;
                for (int i = 0; i < method.getParameterCount(); i++) {
                    if (i == method.getParameterCount() - 1 && method.getParameterTypes()[i].isArray()) {
                        // if the last parameter is an array, check if the other parameters match
                        Class<?> arrayType = ArgumentMapper.ensureBoxedClass(method.getParameterTypes()[i].getComponentType());
                        Class<?> typeOfLastValue = ArgumentMapper.ensureBoxedClass(argTypes[argTypes.length - 1].isArray() ? argTypes[argTypes.length - 1].getComponentType() : argTypes[argTypes.length - 1]);

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
