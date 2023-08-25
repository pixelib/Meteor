package com.meteormsg.base;

import com.meteormsg.base.interfaces.SubscriptionHandler;
import com.meteormsg.base.enums.Direction;

import java.io.Closeable;

public interface RpcTransport extends Closeable {

    /**
     * @param direction the direction of the packet
     * @param bytes the bytes to send
     *              bytes given should already been considered as a packet, and should not be further processed by the transport implementation
     */
    void send(Direction direction, byte[] bytes);

    /**
     * @param target the direction of the packet we want to listen to
     * @param onReceive a function that will be called when a packet is received.
     *                  the function should return a ReadStatus, which will be used to determine if the packet was handled or not.
     *                  if the packet was handled, the transport implementation should stop processing the packet.
     *                  if the packet was not handled, the transport implementation should continue processing the packet.
     *                  the transport implementation should call the onReceive function, regardless of the ReadStatus.
     */
    void subscribe(Direction target, SubscriptionHandler onReceive);

    /**
     * Gracefully shutdown the transport.
     */
    void shutdown();
}
