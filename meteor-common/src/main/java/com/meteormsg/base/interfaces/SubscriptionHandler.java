package com.meteormsg.base.interfaces;

public interface SubscriptionHandler {

    boolean onPacket(byte[] packet) throws Exception;

}
