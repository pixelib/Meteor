package com.rpcnis.core;

import com.rpcnis.base.RpcOptions;
import com.rpcnis.base.RpcSerializer;
import com.rpcnis.base.RpcTransport;
import com.rpcnis.base.defaults.GsonSerializer;
import com.rpcnis.core.executor.ImplementationWrapper;
import com.rpcnis.core.models.InvocationDescriptor;
import com.rpcnis.core.proxy.PendingInvocation;
import com.rpcnis.core.proxy.ProxyInvocHandler;
import com.rpcnis.core.proxy.RpcnisMock;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Locale;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

public class Rpcnis {

    private final RpcOptions options;
    private final RpcSerializer serializer;
    private final RpcTransport transport;

    // Timer for scheduling timeouts and retries
    private final Timer timer = new Timer();

    // Map of pending invocations, keyed by invocation id
    private final ConcurrentHashMap<UUID, PendingInvocation<?>> pendingInvocations = new ConcurrentHashMap<>();

    // Map invocation handlers by type; used for dispatching incoming invocations
    // Handlers without a namespace (which is nullable) are also stored here
    private final ConcurrentHashMap<Class<?>, Collection<ImplementationWrapper>> invocationHandlers = new ConcurrentHashMap<>();

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

    /**
     * @return Get a mutable reference to the options.
     */
    public RpcOptions getOptions() {
        return options;
    }

    /**
     * Register a procedure without a namespace.
     * @param procedure The interface to register as a procedure.
     * @param <T> The type of the interface.
     * @return A proxy object that implements the given interface.
     */
    public <T> T registerProcedure(Class<T> procedure) {
        return registerProcedure(procedure,procedure.getSimpleName().toLowerCase(Locale.ROOT));
    }

    /**
     * Register a procedure with a namespace. Invocations will only be mapped on implementations with the same namespace.
     * @param procedure The interface to register as a procedure.
     * @param name The name of the procedure.
     * @param <T> The type of the interface.
     * @return A proxy object that implements the given interface.
     */
    public <T> T registerProcedure(Class<T> procedure, String name) {
        if (!procedure.isInterface()) {
            throw new IllegalArgumentException("Procedure must be an interface");
        }

        return procedure.cast(Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{procedure, RpcnisMock.class}, new ProxyInvocHandler(this,name)));
    }

    /**
     * @param target The object to check.
     * @return Whether the given object is a proxy object.
     */
    public static boolean isRpc(Object target) {
        return Proxy.isProxyClass(target.getClass());
    }

    /**
     * @param target The object to check.
     * @return Whether the given object is a proxy object created by Rpcnis.
     */
    public static boolean isRpcnis(Object target) {
        return target instanceof RpcnisMock;
    }

    /**
     * Received remote procedure calls will be dispatched to implementations registered with this method.
     * The implementation will be registered under all interfaces implemented by the object, and under the given namespace.
     * @param implementation The object to register as an implementation.
     * @param namespace The namespace to register the implementation under.
     */
    public void registerImplementation(Object implementation, String namespace) {
        // get the interfaces implemented by the implementation
        Class<?>[] interfaces = implementation.getClass().getInterfaces();

        // there must be at least one interface
        if (interfaces.length == 0) {
            throw new IllegalArgumentException("Implementation must implement at least one interface/procedure");
        }

        // register this interface as all of the implemented interfaces
        ImplementationWrapper implementationWrapper = new ImplementationWrapper(implementation, namespace);

        for (Class<?> anInterface : interfaces) {
            invocationHandlers.computeIfAbsent(anInterface, k -> ConcurrentHashMap.newKeySet()).add(implementationWrapper);
        }
    }

    /**
     * Received remote procedure calls will be dispatched to implementations registered with this method.
     * The implementation will be registered under all interfaces implemented by the object, and must be called without a namespace.
     * @param implementation The object to register as an implementation.
     */
    public void registerImplementation(Object implementation) {
        registerImplementation(implementation, null);
    }

    /* === INTERNAL METHODS === */
    public <T> T invokeRemoteMethod(InvocationDescriptor invocationDescriptor) throws Throwable {
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

    public Timer getTimer() {
        return timer;
    }
}
