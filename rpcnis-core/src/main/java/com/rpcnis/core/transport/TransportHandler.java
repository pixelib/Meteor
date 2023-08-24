package com.rpcnis.core.transport;

import com.rpcnis.base.RpcSerializer;
import com.rpcnis.base.RpcTransport;
import com.rpcnis.base.enums.ReadStatus;
import com.rpcnis.core.executor.ImplementationWrapper;
import com.rpcnis.core.models.InvocationDescriptor;
import com.rpcnis.core.trackers.IncomingInvocationTracker;
import com.rpcnis.core.transport.packets.InvocationPacket;

import java.util.Collection;

public class TransportHandler {

    private final RpcSerializer serializer;
    private final RpcTransport transport;
    private IncomingInvocationTracker incomingInvocationTracker;

    public TransportHandler(RpcSerializer serializer, RpcTransport transport, IncomingInvocationTracker incomingInvocationTracker) {
        this.serializer = serializer;
        this.transport = transport;
        this.incomingInvocationTracker = incomingInvocationTracker;

        transport.onReceive(this::onReceive);
    }

    private ReadStatus onReceive(byte[] bytes) {
        // deserialize the packet
        InvocationDescriptor invocationDescriptor = serializer.deserialize(bytes, InvocationDescriptor.class);

        // get the invocation handler for this packet
        Collection<ImplementationWrapper> implementations = incomingInvocationTracker.getImplementations().get(packet.getProcedure());

        // if there is no invocation handler, return
        if (implementations == null) {
            return ReadStatus.NOT_HANDLED;
        }

        // if there is an invocation handler, call it
        for (ImplementationWrapper implementation : implementations) {
            implementation.invoke(packet.getArgs());
        }

        return ReadStatus.HANDLED;
    }

}
