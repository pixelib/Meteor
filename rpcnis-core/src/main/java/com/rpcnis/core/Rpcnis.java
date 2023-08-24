package com.rpcnis.core;

import com.rpcnis.base.RpcOptions;
import com.rpcnis.base.RpcSerializer;
import com.rpcnis.base.RpcTransport;
import com.rpcnis.base.defaults.GsonSerializer;
import com.rpcnis.core.models.InvocationDescriptor;

import java.util.Timer;

public class Rpcnis {

    private final RpcOptions options;
    private RpcSerializer serializer;
    private RpcTransport transport;

    // Timer for scheduling timeouts and retries
    private final Timer timer = new Timer();

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

    public <T> T invoke(InvocationDescriptor invocationDescriptor, Class<T> returnType) {
        // create a completable
    }

    public RpcOptions getOptions() {
        return options;
    }

    public Timer getTimer() {
        return timer;
    }

}
