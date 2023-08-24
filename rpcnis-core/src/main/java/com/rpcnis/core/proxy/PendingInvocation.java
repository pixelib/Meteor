package com.rpcnis.core.proxy;

import com.rpcnis.base.errors.InvocationTimedOutException;
import com.rpcnis.core.Rpcnis;
import com.rpcnis.core.models.InvocationDescriptor;

import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class PendingInvocation<T> extends TimerTask {

    /**
     * A pending invocation represents a blocking invocation request which is awaiting a response, acknowledgement or timeout.
     * It is used to block the calling thread until the invocation is complete.
     */

    private final int timeoutSeconds;
    private final CompletableFuture<T> completable;
    private final InvocationDescriptor invocationDescriptor;

    private AtomicBoolean isComplete = new AtomicBoolean(false);
    private AtomicBoolean isTimedOut = new AtomicBoolean(false);

    public PendingInvocation(Rpcnis rpcnis, InvocationDescriptor invocationDescriptor) {
        this.invocationDescriptor = invocationDescriptor;
        this.timeoutSeconds = rpcnis.getOptions().getTimeoutSeconds();
        completable = new CompletableFuture<>();

        // schedule timeout, timeoutSeconds is in seconds, Timer.schedule() takes milliseconds
        rpcnis.getTimer().schedule(this, timeoutSeconds * 1000L);
    }

    public void complete(T response) {
        if (isTimedOut.get()) {
            throw new IllegalStateException("Cannot complete invocation after timeout.");
        }

        if (isComplete.get()) {
            throw new IllegalStateException("Cannot complete invocation twice.");
        }

        isComplete.set(true);
        this.completable.complete(response);
    }

    public T waitForResponse() throws InvocationTimedOutException {
        // wait for response or timeout
        return this.completable.join();
    }

    /**
     * Inherited from TimerTask.
     * Called when the timeout expires.
     */
    @Override
    public void run() {
        if (isComplete.get()) {
            // the invocation completed before the timeout
            return;
        }

        isTimedOut.set(true);
        this.completable.completeExceptionally(new InvocationTimedOutException(invocationDescriptor.getMethodName(), invocationDescriptor.getTargetName(), timeoutSeconds));
    }
}
