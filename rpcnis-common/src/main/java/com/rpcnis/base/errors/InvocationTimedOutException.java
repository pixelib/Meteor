package com.rpcnis.base.errors;

public class InvocationTimedOutException extends RuntimeException {

    /**
     * Name of the method that timed out.
     */
    private final String methodName;


    /**
     * Name of the target that timed out.
     */
    private final String targetName;

    /**
     * Amount of time in seconds that the invocation was allowed to take.
     */
    private final int timeoutSeconds;

    public InvocationTimedOutException(String methodName, String targetName, int timeoutSeconds) {
        super("Invocation of method " + methodName + " on target " + targetName + " timed out after " + timeoutSeconds + " seconds.");
        this.methodName = methodName;
        this.targetName = targetName;
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
    public String getTargetName() {
        return targetName;
    }

    /**
     * @return Amount of time in seconds that the invocation was allowed to take.
     */
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

}
