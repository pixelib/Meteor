package com.rpcnis.core.transport;

import com.rpcnis.base.RpcSerializer;
import com.rpcnis.base.RpcTransport;
import com.rpcnis.base.enums.Direction;
import com.rpcnis.base.enums.ReadStatus;
import com.rpcnis.core.executor.ImplementationWrapper;
import com.rpcnis.core.models.InvocationDescriptor;
import com.rpcnis.core.trackers.IncomingInvocationTracker;

import java.util.Collection;

public class TransportHandler {

    private final RpcSerializer serializer;
    private final RpcTransport transport;
    private IncomingInvocationTracker incomingInvocationTracker;

    public TransportHandler(RpcSerializer serializer, RpcTransport transport, IncomingInvocationTracker incomingInvocationTracker) {
        this.serializer = serializer;
        this.transport = transport;
        this.incomingInvocationTracker = incomingInvocationTracker;

        transport.subscribe(Direction.TO_INVOKER, this::handleInvocationRequest);
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
        Object response;
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

        return ReadStatus.HANDLED;
    }

}
