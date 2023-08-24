package com.rpcnis.base;

public class RpcOptions {

    /**
     * The amount of time in seconds to wait for a response from the server.
     * An InvocationTimedOutException will be thrown if the timeout is exceeded.
     */
    private int timeoutSeconds = 30;

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

}
