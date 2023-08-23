package com.rpcnis.base;

public interface RpcSerializer {

    byte[] serialize(Object obj);

    <T> T deserialize(byte[] bytes, Class<T> clazz);

    <T> T deserialize(String str, Class<T> clazz);

}
