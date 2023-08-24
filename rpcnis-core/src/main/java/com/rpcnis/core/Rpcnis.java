package com.rpcnis.core;

import com.rpcnis.base.RpcOptions;
import com.rpcnis.base.RpcSerializer;
import com.rpcnis.base.RpcTransport;
import com.rpcnis.base.defaults.GsonSerializer;
import com.rpcnis.core.models.InvocationDescriptor;
import com.rpcnis.core.proxy.PendingInvocation;
import com.rpcnis.core.proxy.ProxyInvocHandler;
import com.rpcnis.core.proxy.RpcnisMock;

import java.lang.reflect.Proxy;
import java.util.Locale;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.CompletionException;
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
    public Rpcnis(RpcTransport transport, RpcOptions options, RpcSerializer serializer) {
        this.transport = transport;
        this.options = options;
        this.serializer = serializer;
    }

    /**
     * @param serializer The serializer to use for serializing and deserializing objects.
     * @param transport The transport to use for sending and receiving data.
     */
    public Rpcnis(RpcTransport transport, RpcSerializer serializer) {
        this(transport, new RpcOptions(), serializer);
    }

    /**
     * @param options A preconfigured RpcOptions object.
     * @param transport The transport to use for sending and receiving data.
     */
    public Rpcnis(RpcTransport transport, RpcOptions options) {
        this(transport, options, new GsonSerializer());
    }

    /**
     * Use default GsonSerializer and options.
     * @param transport The transport to use for sending and receiving data.
     */
    public Rpcnis(RpcTransport transport) {
        this(transport, new RpcOptions(), new GsonSerializer());
    }

    public <T> T invoke(InvocationDescriptor invocationDescriptor, Class<T> returnType) throws Throwable {
        // create a pending invocation
        PendingInvocation<T> pendingInvocation = new PendingInvocation<>(this, invocationDescriptor, () -> {
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

    public RpcOptions getOptions() {
        return options;
    }

    public Timer getTimer() {
        return timer;
    }

    public <T> T registerProcedure(Class<T> procedure) {
        return registerProcedure(procedure,procedure.getSimpleName().toLowerCase(Locale.ROOT));
    }

    public <T> T registerProcedure(Class<T> procedure, String name) {
        if (!procedure.isInterface()) {
            throw new IllegalArgumentException("Procedure must be an interface");
        }

        return procedure.cast(Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{procedure, RpcnisMock.class}, new ProxyInvocHandler(this,name)));
    }

    public static boolean isRpc(Object target) {
        return Proxy.isProxyClass(target.getClass());
    }

    public static boolean isRpcnis(Object target) {
        return target instanceof RpcnisMock;
    }
}
