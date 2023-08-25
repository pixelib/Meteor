package com.meteormsg.base;

public class RpcOptions {

    /**
     * The amount of time in seconds to wait for a response from the server.
     * An InvocationTimedOutException will be thrown if the timeout is exceeded.
     */
    private int timeoutSeconds = 30;

    /**
     * Generated pseudoclass for proxies will be registered with this class loader.
     */
    private ClassLoader classLoader = RpcOptions.class.getClassLoader();

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

}
