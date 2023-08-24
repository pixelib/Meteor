package com.rpcnis.base.errors;

public class MethodInvocationException extends RuntimeException {

    private final String methodName;
    private final String namespace;
    private final Throwable cause;

    public MethodInvocationException(String methodName, String namespace, Throwable cause) {
        super("Invocation of method " + methodName + " on target " + namespace + " failed.", cause);
        this.methodName = methodName;
        this.namespace = namespace;
        this.cause = cause;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }
}
