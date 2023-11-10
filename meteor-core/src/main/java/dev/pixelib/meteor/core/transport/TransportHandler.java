package dev.pixelib.meteor.core.transport;

import dev.pixelib.meteor.base.RpcSerializer;
import dev.pixelib.meteor.base.RpcTransport;
import dev.pixelib.meteor.base.enums.Direction;
import dev.pixelib.meteor.core.executor.ImplementationWrapper;
import dev.pixelib.meteor.core.trackers.IncomingInvocationTracker;
import dev.pixelib.meteor.core.trackers.OutgoingInvocationTracker;
import dev.pixelib.meteor.core.transport.packets.InvocationDescriptor;
import dev.pixelib.meteor.core.transport.packets.InvocationResponse;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransportHandler implements Closeable {

    private final Logger logger = Logger.getLogger(TransportHandler.class.getSimpleName());
    private final RpcSerializer serializer;
    private final RpcTransport transport;
    private final IncomingInvocationTracker incomingInvocationTracker;
    private final OutgoingInvocationTracker outgoingInvocationTracker;

    private final ExecutorService executorPool;

    private boolean isClosed = false;

    public TransportHandler(
            RpcSerializer serializer,
            RpcTransport transport,
            IncomingInvocationTracker incomingInvocationTracker,
            OutgoingInvocationTracker outgoingInvocationTracker,
            int threadPoolSize
    ) {
        this.serializer = serializer;
        this.transport = transport;
        this.incomingInvocationTracker = incomingInvocationTracker;
        this.outgoingInvocationTracker = outgoingInvocationTracker;

        this.executorPool = Executors.newFixedThreadPool(threadPoolSize, r -> new Thread(r, "meteor-executor-thread"));

        transport.subscribe(Direction.METHOD_PROXY, this::handleInvocationResponse);
        transport.subscribe(Direction.IMPLEMENTATION, TransportHandler.this::handleInvocationRequest);
    }

    private boolean handleInvocationResponse(byte[] bytes) throws ClassNotFoundException {
        InvocationResponse invocationResponse = InvocationResponse.fromBytes(serializer, bytes);
        outgoingInvocationTracker.completeInvocation(invocationResponse);
        return true;
    }

    private boolean handleInvocationRequest(byte[] bytes) throws ClassNotFoundException {
        if (isClosed) {
            return false;
        }

        // deserialize the packet
        InvocationDescriptor invocationDescriptor = InvocationDescriptor.fromBuffer(serializer, bytes);

        // get the invocation handler for this packet
        Collection<ImplementationWrapper> implementations = incomingInvocationTracker.getImplementations().get(invocationDescriptor.getDeclaringClass());

        // if there is no invocation handler, return
        if (implementations == null || implementations.isEmpty()) {
            return false;
        }

        ImplementationWrapper matchedImplementation = implementations.stream()
                .filter(
                        implementation ->
                                // Either have matching namespaces
                                (invocationDescriptor.getNamespace() != null && invocationDescriptor.getNamespace().equals(implementation.getNamespace()))
                                        ||
                                        // Or they both don't have namespaces
                                        (implementation.getNamespace() == null && invocationDescriptor.getNamespace() == null)
                )
                .findFirst()
                .orElse(null);

        // if there is an invocation handler, call it

        // We do have handlers for this type, just not by the same name
        if (matchedImplementation == null) {
            return false;
        }

        this.executorPool.submit(() -> {
            try {
                // move to separate threading
                Object response = matchedImplementation.invokeOn(invocationDescriptor, invocationDescriptor.getReturnType());
                InvocationResponse invocationResponse = new InvocationResponse(invocationDescriptor.getUniqueInvocationId(), response);
                transport.send(Direction.METHOD_PROXY, invocationResponse.toBytes(serializer));
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "An error occurred while invoking a method", e);
            }
        });

        return true;
    }

    @Override
    public void close() throws IOException {
        if (isClosed) {
            return;
        }
        isClosed = true;
        executorPool.shutdown();
        transport.close();
    }
}
