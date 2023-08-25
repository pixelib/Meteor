package com.meteormsg.base.defaults;

import com.google.gson.Gson;
import com.meteormsg.base.RpcSerializer;

public class GsonSerializer implements RpcSerializer {

    /**
     * the Gson instance to use for serialization/deserialization
     * this is a static field so that it is shared between all instances of this class and can be swapped out at runtime
     */
    public static Gson GSON = new Gson();

    /**
     * @param obj the object to serialize
     * @return the serialized object as a byte array
     *
     * this particular implementation uses Gson to serialize the object
     */
    @Override
    public byte[] serialize(Object obj) {
        return GSON.toJson(obj).getBytes();
    }

    /**
     * @param bytes the bytes to deserialize
     * @param clazz the class to deserialize to
     * @param <T> the type of the class to deserialize to
     * @return the deserialized object
     *
     * this particular implementation uses Gson to deserialize the object
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return GSON.fromJson(new String(bytes), clazz);
    }

    /**
     * @param str the string to deserialize
     * @param clazz the class to deserialize to
     * @param <T> the type of the class to deserialize to
     * @return the deserialized object
     */
    @Override
    public <T> T deserialize(String str, Class<T> clazz) {
        return GSON.fromJson(str, clazz);
    }
}
