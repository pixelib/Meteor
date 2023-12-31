package dev.pixelib.meteor.base.defaults;

import dev.pixelib.meteor.base.RpcTransport;
import dev.pixelib.meteor.base.enums.Direction;
import dev.pixelib.meteor.base.interfaces.SubscriptionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class LoopbackTransport implements RpcTransport {

    private final Map<Direction, List<SubscriptionHandler>> onReceiveFunctions = new EnumMap<>(Direction.class);

    /**
     * @param bytes the bytes to send
     *              bytes given should already been considered as a packet, and should1not be further processed by the transport implementation
     *              this particular implementation will call all the onReceive functions, and stop if one of them returns HANDLED
     *              no actual sending is done, as this is a loopback transport meant for testing
     */
    @Override
    public void send(Direction direction, byte[] bytes) {
        for (SubscriptionHandler onReceiveFunction : onReceiveFunctions.getOrDefault(direction, new ArrayList<>())) {
            try {
                boolean matched = onReceiveFunction.onPacket(bytes);
                if (matched) break;
            } catch (Exception e) {
                // TODO: Add Logger
                e.printStackTrace();
            }
        }
    }

    /**
     * @param target the direction of the packet we want to listen to
     * @param onReceive a function that will be called when a packet is received.
     *                  the function should return a ReadStatus, which will be used to determine if the packet was handled or not.
     *                  if the packet was handled, the transport implementation should stop processing the packet.
     *                  if the packet was not handled, the transport implementation should continue processing the packet.
     *                  the transport implementation should call the onReceive function, regardless of the ReadStatus.
     */
    @Override
    public void subscribe(Direction target, SubscriptionHandler onReceive) {
        onReceiveFunctions.computeIfAbsent(target, k -> new ArrayList<>()).add(onReceive);
    }

    /**
     * @throws IOException never thrown, as this is a loopback transport meant for testing so this method is unused
     */
    @Override
    public void close() throws IOException {
        // do nothing
    }
}
