package dev.pixelib.meteor.core.executor;

import dev.pixelib.meteor.base.errors.MethodInvocationException;
import dev.pixelib.meteor.core.transport.packets.InvocationDescriptor;
import dev.pixelib.meteor.core.utils.ArgumentMapper;

import java.lang.reflect.Method;

public class ImplementationWrapper {

    private final Object implementation;
    private final String namespace;

    public ImplementationWrapper(Object implementation, String namespace) {
        this.implementation = implementation;
        this.namespace = namespace;
    }

    public <R> R invokeOn(InvocationDescriptor invocationDescriptor, Class<R> returnType /* not unused, see comment below */) throws NoSuchMethodException {
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
                throw new NoSuchMethodException("No method found with name " + methodName + " and compatible arguments (on " + implementation.getClass().getName() + ").");
            }
        }

        // make accessible if private
        method.setAccessible(true);

        // sanitize args
        Object[] args = ArgumentMapper.overflowArguments(method, invocationDescriptor.getArgs());

        // invoke method
        try {
            // Normally, we'd use "returnType.cast" here, but it's technically unsafe.
            // the cast function does an isInstance check, to make sure that the value is at least a descendant of R,
            // However... there are exceptions to this rule, for example, if R is an int and the value is an Integer
            // then they aren't strictly the same type (due to the jvm boxing/unboxing rules), but the cast will still
            // succeed. So we use the unchecked cast here, because we know that the value is assignable to R.
            return (R) method.invoke(implementation, args);
        } catch (Exception e) {
            throw new MethodInvocationException(invocationDescriptor.getMethodName(), namespace, e);
        }
    }

    /**
     * We don't know yet which method is the best match, because there are multiple ways to arrange the arguments.
     * so this method serves to find the best match for when a normal match cannot be done.
     * this can get pretty computationally expensive, so this should only be done when an initial lookup fails.
     * @param methods   The methods to search through
     * @param methodName The name of the method to find
     * @param argTypes  The argument types of the method to find
     * @return The method if found, null otherwise
     */
    private Method findCompatibleDespiteSignature(Method[] methods, String methodName, Class<?>[] argTypes) {
        for (Method method : methods) {
            if (!method.getName().equals(methodName) || argTypes.length < method.getParameterCount()) {
                continue;
            }

            // check if the methods match, or if the last method is an optional array then if the other types match
            if (methodMatchesArguments(method, argTypes)) {
                return method;
            }
        }
        return null;
    }

    private boolean methodMatchesArguments(Method method, Class<?>[] argTypes) {
        for (int i = 0; i < method.getParameterCount(); i++) {
            if (i == method.getParameterCount() - 1 && method.getParameterTypes()[i].isArray()) {
                // if the last parameter is an array, check if the other parameters match
                Class<?> arrayType = ArgumentMapper.ensureBoxedClass(method.getParameterTypes()[i].getComponentType());

                Class<?> argType = argTypes[argTypes.length - 1];
                Class<?> typeOfLastValue = ArgumentMapper.ensureBoxedClass(argType.isArray() ? argType.getComponentType() : argType);

                // are all the other parameters assignable to the array type?
                if (!arrayType.isAssignableFrom(typeOfLastValue)) {
                    return false;
                }
            } else if (!ArgumentMapper.ensureBoxedClass(method.getParameterTypes()[i]).isAssignableFrom(argTypes[i])) {
                return false;
            }
        }
        return true;
    }

    public Object getImplementation() {
        return implementation;
    }

    public String getNamespace() {
        return namespace;
    }
}
