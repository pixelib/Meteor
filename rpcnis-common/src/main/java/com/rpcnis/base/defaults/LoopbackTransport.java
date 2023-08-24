package com.rpcnis.base.defaults;

import com.rpcnis.base.RpcTransport;
import com.rpcnis.base.enums.ReadStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class LoopbackTransport implements RpcTransport {

    private List<Function<byte[], ReadStatus>> onReceiveFunctions = new ArrayList<>();

    /**
     * @param host the host to connect to (unused)
     * @param port the port to connect to (unused)
     * @throws IOException if the connection failed (unused)
     */
    @Override
    public void connect(String host, int port) throws IOException {
        // unused
    }

    /**
     * @param bytes the bytes to send
     *              bytes given should already been considered as a packet, and should not be further processed by the transport implementation
     *
     *              this particular implementation will call all the onReceive functions, and stop if one of them returns HANDLED
     *              no actual sending is done, as this is a loopback transport meant for testing
     */
    @Override
    public void send(byte[] bytes) {
        for (Function<byte[], ReadStatus> onReceiveFunction : onReceiveFunctions) {
            ReadStatus status = onReceiveFunction.apply(bytes);
            if (status == ReadStatus.HANDLED) {
                break;
            }
        }
    }

    /**
     * @return always true, as this is a loopback transport meant for testing
     */
    @Override
    public boolean isConnected() {
        return true;
    }

    /**
     * @param onReceive a function that will be called when a packet is received.
     *                  the function should return a ReadStatus, which will be used to determine if the packet was handled or not.
     *                  if the packet was handled, the transport implementation should stop processing the packet.
     *                  if the packet was not handled, the transport implementation should continue processing the packet.
     *                  the transport implementation should call the onReceive function, regardless of the ReadStatus.
     */
    @Override
    public void onReceive(Function<byte[], ReadStatus> onReceive) {
        onReceiveFunctions.add(onReceive);
    }

    /**
     * @throws IOException never thrown, as this is a loopback transport meant for testing so this method is unused
     */
    @Override
    public void close() throws IOException {
        // unused
    }
}
