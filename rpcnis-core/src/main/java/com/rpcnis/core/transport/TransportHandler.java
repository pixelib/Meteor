package com.rpcnis.core.transport;

import com.rpcnis.base.RpcSerializer;
import com.rpcnis.base.RpcTransport;
import com.rpcnis.base.enums.Direction;
import com.rpcnis.base.enums.ReadStatus;
import com.rpcnis.core.executor.ImplementationWrapper;
import com.rpcnis.core.trackers.OutgoingInvocationTracker;
import com.rpcnis.core.transport.packets.InvocationDescriptor;
import com.rpcnis.core.trackers.IncomingInvocationTracker;
import com.rpcnis.core.transport.packets.InvocationResponse;

import java.util.Collection;

public class TransportHandler {

    private final RpcSerializer serializer;
    private final RpcTransport transport;
    private final IncomingInvocationTracker incomingInvocationTracker;
    private final OutgoingInvocationTracker outgoingInvocationTracker;

    public TransportHandler(RpcSerializer serializer, RpcTransport transport, IncomingInvocationTracker incomingInvocationTracker, OutgoingInvocationTracker outgoingInvocationTracker) {
        this.serializer = serializer;
        this.transport = transport;
        this.incomingInvocationTracker = incomingInvocationTracker;
        this.outgoingInvocationTracker = outgoingInvocationTracker;

        transport.subscribe(Direction.METHOD_PROXY, this::handleInvocationResponse);
        transport.subscribe(Direction.IMPLEMENTATION, this::handleInvocationRequest);
    }

    private ReadStatus handleInvocationResponse(byte[] bytes) throws ClassNotFoundException {
        InvocationResponse invocationResponse = InvocationResponse.fromBytes(bytes, serializer);
        outgoingInvocationTracker.completeInvocation(invocationResponse);
        return ReadStatus.HANDLED;
    }

    private ReadStatus handleInvocationRequest(byte[] bytes) throws ClassNotFoundException {
        // deserialize the packet
        InvocationDescriptor invocationDescriptor = InvocationDescriptor.fromBuffer(serializer, bytes);

        // get the invocation handler for this packet
        Collection<ImplementationWrapper> implementations = incomingInvocationTracker.getImplementations().get(invocationDescriptor.getDeclaringClass());

        // if there is no invocation handler, return
        if (implementations == null || implementations.isEmpty()) {
            return ReadStatus.UNKNOWN_TARGET;
        }

        // if there is an invocation handler, call it
        Object response = null;
        for (ImplementationWrapper implementation : implementations) {
            try {
                response = implementation.invokeOn(invocationDescriptor, invocationDescriptor.getReturnType());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                // if this happens for all handlers, the origin will eventually get a timeout and just fuck off
                return ReadStatus.UNKNOWN_TARGET;
            }
        }

        // transmit response
        InvocationResponse invocationResponse = new InvocationResponse(invocationDescriptor.getUniqueInvocationId(), response);
        transport.send(Direction.METHOD_PROXY, invocationResponse.toBytes(serializer));

        return ReadStatus.HANDLED;
    }

}
