package dev.pixelib.meteor.base.interfaces;

public interface SubscriptionHandler {

    boolean onPacket(byte[] packet) throws Exception;

}
