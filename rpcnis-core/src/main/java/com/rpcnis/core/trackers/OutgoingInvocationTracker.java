package com.rpcnis.core.trackers;

import com.rpcnis.base.RpcOptions;
import com.rpcnis.core.models.InvocationDescriptor;
import com.rpcnis.core.proxy.PendingInvocation;

import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

public class OutgoingInvocationTracker {

    private final Timer timer;
    private final RpcOptions options;

    // Map of pending invocations, keyed by invocation id
    private final ConcurrentHashMap<UUID, PendingInvocation<?>> pendingInvocations = new ConcurrentHashMap<>();

    public OutgoingInvocationTracker(RpcOptions options, Timer timer) {
        this.options = options;
        this.timer = timer;
    }

    public <T> T invokeRemoteMethod(InvocationDescriptor invocationDescriptor) throws Throwable {
        // create a pending invocation
        PendingInvocation<T> pendingInvocation = new PendingInvocation<>(options.getTimeoutSeconds(), this.timer, invocationDescriptor, () -> {
            // remove the pending invocation from the map
            pendingInvocations.remove(invocationDescriptor.getUniqueInvocationId());
        });

        // add the pending invocation to the map
        pendingInvocations.put(invocationDescriptor.getUniqueInvocationId(), pendingInvocation);

        // TODO: transmit

        // wait for response or timeout
        try {
            return pendingInvocation.waitForResponse();
        } catch (CompletionException e) {
            throw e.getCause();
        }
    }

    public void completeInvocation(InvocationDescriptor invocationDescriptor, Object value) {
        // do we have a pending invocation for this invocation id?
        PendingInvocation<?> pendingInvocation = pendingInvocations.get(invocationDescriptor.getUniqueInvocationId());
        if (pendingInvocation == null) {
            throw new IllegalStateException("No pending invocation found for invocation id " + invocationDescriptor.getUniqueInvocationId());
        }

        pendingInvocation.complete(value);

        // remove the pending invocation from the map
        pendingInvocations.remove(invocationDescriptor.getUniqueInvocationId());
    }

}