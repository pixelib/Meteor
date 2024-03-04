package dev.pixelib.meteor.base.interfaces;

@FunctionalInterface
public interface SubscriptionHandler {

    boolean onPacket(byte[] packet) throws Exception;

}
