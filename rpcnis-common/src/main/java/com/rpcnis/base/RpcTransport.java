package com.rpcnis.base;

import com.rpcnis.base.enums.ReadStatus;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Function;

public interface RpcTransport extends Closeable {

    /**
     * @param bytes the bytes to send
     *              bytes given should already been considered as a packet, and should not be further processed by the transport implementation
     */
    void send(byte[] bytes);

    /**
     * @param onReceive a function that will be called when a packet is received.
     *                  the function should return a ReadStatus, which will be used to determine if the packet was handled or not.
     *                  if the packet was handled, the transport implementation should stop processing the packet.
     *                  if the packet was not handled, the transport implementation should continue processing the packet.
     *                  the transport implementation should call the onReceive function, regardless of the ReadStatus.
     */
    void onReceive(Function<byte[], ReadStatus> onReceive);
}
