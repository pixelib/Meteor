package com.rpcnis.base.errors;

public class InvocationTimedOutException extends RuntimeException {

    /**
     * Name of the method that timed out.
     */
    private final String methodName;


    /**
     * Name of the target that timed out.
     */
    private final String namespace;

    /**
     * Amount of time in seconds that the invocation was allowed to take.
     */
    private final int timeoutSeconds;

    public InvocationTimedOutException(String methodName, String namespace, int timeoutSeconds) {
        super("Invocation of method " + methodName + " on target " + namespace + " timed out after " + timeoutSeconds + " seconds.");
        this.methodName = methodName;
        this.namespace = namespace;
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * @return Name of the method that timed out.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @return Name of the target that timed out.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @return Amount of time in seconds that the invocation was allowed to take.
     */
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

}
