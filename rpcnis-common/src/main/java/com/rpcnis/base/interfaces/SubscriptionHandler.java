package com.rpcnis.base.interfaces;

import com.rpcnis.base.enums.ReadStatus;

public interface SubscriptionHandler {

    ReadStatus onPacket(byte[] packet) throws Exception;

}
