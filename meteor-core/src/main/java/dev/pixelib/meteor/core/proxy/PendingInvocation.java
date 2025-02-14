package dev.pixelib.meteor.core.proxy;

import dev.pixelib.meteor.base.errors.InvocationTimedOutException;
import dev.pixelib.meteor.core.transport.packets.InvocationDescriptor;
import dev.pixelib.meteor.core.utils.ArgumentMapper;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PendingInvocation<T> extends TimerTask {

    /**
     * A pending invocation represents a blocking invocation request which is awaiting a response, acknowledgement or timeout.
     * It is used to block the calling thread until the invocation is complete.
     */

    private final int timeoutSeconds;
    private final CompletableFuture<T> completable;
    private final InvocationDescriptor invocationDescriptor;

    // A callback to be called when the invocation times out. Used to mitigate the risk of memory leaks.
    private final Runnable timeoutCallback;

    private final AtomicBoolean isComplete = new AtomicBoolean(false);
    private final AtomicBoolean isTimedOut = new AtomicBoolean(false);

    public PendingInvocation(int timeoutSeconds, Timer timer, InvocationDescriptor invocationDescriptor, Runnable timeoutCallback) {
        this.invocationDescriptor = invocationDescriptor;
        this.timeoutCallback = timeoutCallback;
        this.timeoutSeconds = timeoutSeconds;
        completable = new CompletableFuture<>();

        // schedule timeout, timeoutSeconds is in seconds, Timer.schedule() takes milliseconds
        timer.schedule(this, TimeUnit.SECONDS.toMillis(timeoutSeconds));
    }

    /**
     * Complete and clean a pending invocation. This method should only be called once.
     * This should be directly invoked from the transport when a response is received and deserialized.
     * @param response The response to complete the invocation with.
     */
    public void complete(Object response) throws IllegalStateException {
        if (isTimedOut.get()) {
            throw new IllegalStateException("Cannot complete invocation after timeout.");
        }

        if (isComplete.get()) {
            throw new IllegalStateException("Cannot complete invocation twice.");
        }

        boolean isVoidOrNullable = invocationDescriptor.getReturnType().equals(Void.TYPE) || !invocationDescriptor.getReturnType().isPrimitive();

        // check instance of response
        if (!isVoidOrNullable && !invocationDescriptor.getReturnType().isInstance(response)) {
            // is the normal return type primitive? then check if its still assignable as a boxed
            if (invocationDescriptor.getReturnType().isPrimitive()) {
                if (!ArgumentMapper.ensureBoxedClass(invocationDescriptor.getReturnType()).isAssignableFrom(response.getClass())) {
                    throw new IllegalStateException("Response is not an instance of the expected return type. " +
                            "Expected: " + invocationDescriptor.getReturnType().getName() + ", " +
                            "Actual: " + response.getClass().getName());
                }
            }
        }

        isComplete.set(true);
        this.completable.complete((T) response);
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

        this.completable.completeExceptionally(
                new InvocationTimedOutException(invocationDescriptor.getMethodName(), invocationDescriptor.getNamespace(), timeoutSeconds)
        );

        // call the timeout callback
        if (timeoutCallback != null) {
            timeoutCallback.run();
        }
    }
}
