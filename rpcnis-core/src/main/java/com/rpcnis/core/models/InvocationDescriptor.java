package com.rpcnis.core.models;

import java.util.UUID;

public class InvocationDescriptor {

    /**
     * Unique identifier for this invocation, used to match responses to requests.
     */
    private final UUID id = UUID.randomUUID();

    /**
     * Name of the targeted handler.
     * Can be used to address specific instances of an implementation class.
     */
    private String namespace;

    /**
     * Method name that should be invoked (always map against the argTypes due to overloading).
     */
    private String methodName;

    /**
     * Arguments that should be passed to the method, which may contain null values.
     */
    private Object[] args;

    /**
     * Types of the arguments that should be passed to the method.
     */
    private Class<?>[] argTypes;


    /**
     * Return type of the method.
     */
    private Class<?> returnType;

    public InvocationDescriptor(String namespace, String methodName, Object[] args, Class<?>[] argTypes, Class<?> returnType) {
        this.namespace = namespace;
        this.methodName = methodName;
        this.args = args;
        this.argTypes = argTypes;
        this.returnType = returnType;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public Class<?>[] getArgTypes() {
        return argTypes;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public UUID getUniqueInvocationId() {
        return id;
    }
}
