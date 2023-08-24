package com.rpcnis.core;

import com.rpcnis.base.RpcOptions;
import com.rpcnis.base.RpcSerializer;
import com.rpcnis.base.RpcTransport;
import com.rpcnis.base.defaults.GsonSerializer;
import com.rpcnis.base.errors.InvocationTimedOutException;
import com.rpcnis.core.models.InvocationDescriptor;
import com.rpcnis.core.proxy.PendingInvocation;

import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Rpcnis {

    private final RpcOptions options;
    private RpcSerializer serializer;
    private RpcTransport transport;

    // Timer for scheduling timeouts and retries
    private final Timer timer = new Timer();

    // Map of pending invocations, keyed by invocation id
    private ConcurrentHashMap<UUID, PendingInvocation<?>> pendingInvocations = new ConcurrentHashMap<>();

    /**
     * @param options A preconfigured RpcOptions object.
     * @param serializer The serializer to use for serializing and deserializing objects.
     * @param transport The transport to use for sending and receiving data.
     */
    public Rpcnis(RpcOptions options, RpcSerializer serializer, RpcTransport transport) {
        this.options = options;
        this.serializer = serializer;
        this.transport = transport;
    }

    /**
     * @param serializer The serializer to use for serializing and deserializing objects.
     * @param transport The transport to use for sending and receiving data.
     */
    public Rpcnis(RpcSerializer serializer, RpcTransport transport) {
        this(new RpcOptions(), serializer, null);
    }

    /**
     * Use default GsonSerializer and options.
     * @param transport The transport to use for sending and receiving data.
     */
    public Rpcnis(RpcTransport transport) {
        this(new RpcOptions(), new GsonSerializer(), transport);
    }

    public <T> T invoke(InvocationDescriptor invocationDescriptor, Class<T> returnType) throws InvocationTimedOutException {
        // create a pending invocation
        PendingInvocation<T> pendingInvocation = new PendingInvocation<>(this, invocationDescriptor, () -> {
            // remove the pending invocation from the map
            pendingInvocations.remove(invocationDescriptor.getUniqueInvocationId());
        });

        // add the pending invocation to the map
        pendingInvocations.put(invocationDescriptor.getUniqueInvocationId(), pendingInvocation);

        // TODO: transmit

        // wait for response or timeout
        return pendingInvocation.waitForResponse();
    }

    public RpcOptions getOptions() {
        return options;
    }

    public Timer getTimer() {
        return timer;
    }

}
