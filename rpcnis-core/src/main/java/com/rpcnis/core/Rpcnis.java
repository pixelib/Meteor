package com.rpcnis.core;

import com.rpcnis.base.RpcOptions;
import com.rpcnis.base.RpcSerializer;
import com.rpcnis.base.RpcTransport;
import com.rpcnis.base.defaults.GsonSerializer;
import com.rpcnis.core.proxy.ProxyInvocHandler;
import com.rpcnis.core.proxy.RpcnisMock;
import com.rpcnis.core.trackers.IncomingInvocationTracker;
import com.rpcnis.core.trackers.OutgoingInvocationTracker;
import com.rpcnis.core.transport.TransportHandler;

import java.lang.reflect.Proxy;
import java.util.Locale;
import java.util.Timer;

public class Rpcnis {

    private final RpcOptions options;

    // Timer for scheduling timeouts and retries
    private final Timer timer = new Timer();

    private final OutgoingInvocationTracker outgoingInvocationTracker;
    private final IncomingInvocationTracker incomingInvocationTracker;
    private final TransportHandler transportHandler;

    /**
     * @param options    A preconfigured RpcOptions object.
     * @param serializer The serializer to use for serializing and deserializing objects.
     * @param transport  The transport to use for sending and receiving data.
     */
    public Rpcnis(RpcTransport transport, RpcOptions options, RpcSerializer serializer) {
        this.options = options;

        outgoingInvocationTracker = new OutgoingInvocationTracker(transport, serializer, options, timer);
        incomingInvocationTracker = new IncomingInvocationTracker();
        transportHandler = new TransportHandler(serializer, transport, incomingInvocationTracker, outgoingInvocationTracker);
    }

    /**
     * @param serializer The serializer to use for serializing and deserializing objects.
     * @param transport  The transport to use for sending and receiving data.
     */
    public Rpcnis(RpcTransport transport, RpcSerializer serializer) {
        this(transport, new RpcOptions(), serializer);
    }

    /**
     * @param options   A preconfigured RpcOptions object.
     * @param transport The transport to use for sending and receiving data.
     */
    public Rpcnis(RpcTransport transport, RpcOptions options) {
        this(transport, options, new GsonSerializer());
    }

    /**
     * Use default GsonSerializer and options.
     *
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
     *
     * @param procedure The interface to register as a procedure.
     * @param <T>       The type of the interface.
     * @return A proxy object that implements the given interface.
     */
    public <T> T registerProcedure(Class<T> procedure) {
        return registerProcedure(procedure, null);
    }

    /**
     * Register a procedure with a namespace. Invocations will only be mapped on implementations with the same namespace.
     *
     * @param procedure The interface to register as a procedure.
     * @param name      The name of the procedure.
     * @param <T>       The type of the interface.
     * @return A proxy object that implements the given interface.
     */
    public <T> T registerProcedure(Class<T> procedure, String name) {
        if (!procedure.isInterface()) {
            throw new IllegalArgumentException("Procedure must be an interface");
        }

        return procedure.cast(Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{procedure, RpcnisMock.class}, new ProxyInvocHandler(outgoingInvocationTracker, name)));
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
     *
     * @param implementation The object to register as an implementation.
     * @param namespace      The namespace to register the implementation under.
     */
    public void registerImplementation(Object implementation, String namespace) {
        incomingInvocationTracker.registerImplementation(implementation, namespace);
    }

    /**
     * Received remote procedure calls will be dispatched to implementations registered with this method.
     * The implementation will be registered under all interfaces implemented by the object, and must be called without a namespace.
     *
     * @param implementation The object to register as an implementation.
     */
    public void registerImplementation(Object implementation) {
        registerImplementation(implementation, null);
    }

}
