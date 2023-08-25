package com.meteormsg.base.interfaces;

import com.meteormsg.base.enums.ReadStatus;

public interface SubscriptionHandler {

    ReadStatus onPacket(byte[] packet) throws Exception;

}
