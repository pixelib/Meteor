package com.rpcnis.base;

public interface RpcSerializer {

    /**
     * @param obj the object to serialize
     * @return the serialized object as a byte array, possibly untrimmed
     */
    byte[] serialize(Object obj);

    /**
     * @param bytes the bytes to deserialize (untrimmed)
     * @param clazz the class to deserialize to
     * @param <T> the type of the class to deserialize to
     * @return the deserialized object
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);

    /**
     * @param str the string to deserialize (basic utf, not-localized)
     * @param clazz the class to deserialize to
     * @param <T> the type of the class to deserialize to
     * @return the deserialized object
     */
    <T> T deserialize(String str, Class<T> clazz);

}
