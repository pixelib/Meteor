package com.rpcnis.core.transport;

import com.rpcnis.base.RpcSerializer;
import com.rpcnis.base.RpcTransport;

public class TransportHandler {

    private final RpcSerializer serializer;
    private final RpcTransport transport;

    public TransportHandler(RpcSerializer serializer, RpcTransport transport) {
        this.serializer = serializer;
        this.transport = transport;



    }

}
