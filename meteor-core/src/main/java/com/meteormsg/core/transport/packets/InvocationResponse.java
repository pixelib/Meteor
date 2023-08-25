package com.meteormsg.core.transport.packets;

import com.meteormsg.base.RpcSerializer;
import com.meteormsg.core.utils.ReflectionUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;
import java.util.UUID;

public class InvocationResponse {

    /**
     * Unique identifier for this invocation, used to match responses to requests.
     * References the ID in the InvocationDescriptor.
     */
    private final UUID invocationId;

    /**
     * Result of the invocation.
     */
    private final Object result;

    public InvocationResponse(UUID invocationId, Object result) {
        this.invocationId = invocationId;
        this.result = result;
    }

    public byte[] toBytes(RpcSerializer serializer) {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeLong(invocationId.getMostSignificantBits());
        buffer.writeLong(invocationId.getLeastSignificantBits());

        if (result == null) {
            buffer.writeBoolean(true);
        } else {
            buffer.writeBoolean(false);
            buffer.writeBoolean(result.getClass().isPrimitive());

            String resultType = result.getClass().getName();
            buffer.writeInt(resultType.length());
            buffer.writeCharSequence(resultType, Charset.defaultCharset());

            byte[] resultBytes = serializer.serialize(result);
            buffer.writeInt(resultBytes.length);
            buffer.writeBytes(resultBytes);
        }

        byte[] byteArray = new byte[buffer.readableBytes()];
        buffer.readBytes(byteArray);
        // release the buffer
        buffer.release();
        return byteArray;
    }

    public static InvocationResponse fromBytes(RpcSerializer serializer, byte[] bytes) throws ClassNotFoundException {
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        UUID invocationId = new UUID(buffer.readLong(), buffer.readLong());

        boolean isNull = buffer.readBoolean();
        if (isNull) {
            buffer.release();
            return new InvocationResponse(invocationId, null);
        } else {
            boolean isPrimitive = buffer.readBoolean();

            Class<?> resultClass;
            String responseType = buffer.readCharSequence(buffer.readInt(), Charset.defaultCharset()).toString();
            if (isPrimitive) {
                resultClass = ReflectionUtil.resolvePrimitive(responseType);
            } else {
                resultClass = Class.forName(responseType);
            }

            int resultLength = buffer.readInt();
            byte[] resultBytes = new byte[resultLength];
            buffer.readBytes(resultBytes);
            Object result = serializer.deserialize(resultBytes, resultClass);
            buffer.release();
            return new InvocationResponse(invocationId, result);
        }

    }

    public UUID getInvocationId() {
        return invocationId;
    }

    public Object getResult() {
        return result;
    }

}
