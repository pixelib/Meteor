package com.rpcnis.base.enums;

public enum ReadStatus {

    /**
     * These statuses may be returned by Rpcnis at the end of a read transaction,
     * to indicate weather the packet was accepted/handled by a particular handler.
     * this allows transport implementations to stop processing the packet if it was handled by one of the handlers;
     * or to continue processing the packet if it was not handled by any of the handlers.
     * whether the packet was handled or not, the transport implementation should call the onReceive function
     */
    HANDLED,
    UNKNOWN_TARGET,
    OK

}
