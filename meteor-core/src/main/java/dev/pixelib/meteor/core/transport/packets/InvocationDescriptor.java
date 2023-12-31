package dev.pixelib.meteor.core.transport.packets;

import dev.pixelib.meteor.base.RpcSerializer;
import dev.pixelib.meteor.core.utils.ArgumentMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;
import java.util.UUID;

public class InvocationDescriptor {

    /**
     * Unique identifier for this invocation, used to match responses to requests.
     */
    private UUID id;

    /**
     * Name of the targeted handler.
     * Can be used to address specific instances of an implementation class.
     */
    private String namespace;

    /**
     * Class that declares the method that should be invoked.
     */
    private Class<?> declaringClass;

    /**
     * Method name that should be invoked (always map against the argTypes due to overloading).
     */
    private String methodName;

    /**
     * Arguments that should be passed to the method, which may contain null values.
     */
    private Object[] args;

    /**
     * Types of the arguments that should be passed to the method.
     */
    private Class<?>[] argTypes;

    /**
     * Return type of the method.
     */
    private Class<?> returnType;

    public InvocationDescriptor(String namespace, Class<?> declaringClass, String methodName, Object[] args, Class<?>[] argTypes, Class<?> returnType) {
        this(UUID.randomUUID(), namespace, declaringClass, methodName, args, argTypes, returnType);
    }

    public InvocationDescriptor(UUID id, String namespace, Class<?> declaringClass, String methodName, Object[] args, Class<?>[] argTypes, Class<?> returnType) {
        this.id = id;
        this.namespace = namespace;
        this.declaringClass = declaringClass;
        this.methodName = methodName;
        this.args = args;
        this.argTypes = argTypes;
        this.returnType = returnType;
    }

    public byte[] toBuffer(RpcSerializer serializer) {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeLong(id.getMostSignificantBits());
        buffer.writeLong(id.getLeastSignificantBits());

        buffer.writeBoolean(namespace != null);
        if (namespace != null) {
            buffer.writeInt(namespace.length());
            buffer.writeCharSequence(namespace, Charset.defaultCharset());
        }

        buffer.writeInt(declaringClass.getName().length());
        buffer.writeCharSequence(declaringClass.getName(), Charset.defaultCharset());

        buffer.writeInt(methodName.length());
        buffer.writeCharSequence(methodName, Charset.defaultCharset());

        buffer.writeInt(args.length);
        for (Object arg : args) {
            buffer.writeBoolean(arg != null);
            if (arg != null) {
                Class<?> argClass = arg.getClass();
                String argClassName = argClass.getName();
                buffer.writeInt(argClassName.length());
                buffer.writeCharSequence(argClassName, Charset.defaultCharset());

                byte[] serialized = serializer.serialize(arg);
                buffer.writeInt(serialized.length);
                buffer.writeBytes(serialized);
            }
        }

        buffer.writeInt(argTypes.length);
        for (Class<?> argType : argTypes) {
            buffer.writeBoolean(argType.isPrimitive());
            buffer.writeInt(argType.getName().length());
            buffer.writeCharSequence(argType.getName(), Charset.defaultCharset());
        }

        buffer.writeBoolean(returnType.isPrimitive());
        buffer.writeInt(returnType.getName().length());
        buffer.writeCharSequence(returnType.getName(), Charset.defaultCharset());
        byte[] byteArray = new byte[buffer.readableBytes()];
        buffer.readBytes(byteArray);
        // release the buffer
        buffer.release();
        return byteArray;
    }

    public static InvocationDescriptor fromBuffer(RpcSerializer customDataSerializer, byte[] raw) throws ClassNotFoundException {
        ByteBuf buffer = Unpooled.wrappedBuffer(raw);
        UUID id = new UUID(buffer.readLong(), buffer.readLong());

        String namespace = null;
        if (buffer.readBoolean()) {
            namespace = buffer.readCharSequence(buffer.readInt(), Charset.defaultCharset()).toString();
        }

        String declaringClassName = buffer.readCharSequence(buffer.readInt(), Charset.defaultCharset()).toString();
        Class<?> declaringClass = Class.forName(declaringClassName);

        String methodName = buffer.readCharSequence(buffer.readInt(), Charset.defaultCharset()).toString();

        Object[] args = new Object[buffer.readInt()];
        for (int i = 0; i < args.length; i++) {
            if (buffer.readBoolean()) {
                String argClassName = buffer.readCharSequence(buffer.readInt(), Charset.defaultCharset()).toString();
                byte[] serialized = new byte[buffer.readInt()];
                buffer.readBytes(serialized);
                args[i] = customDataSerializer.deserialize(serialized, Class.forName(argClassName));
            }
        }

        Class<?>[] argTypes = new Class<?>[buffer.readInt()];
        for (int i = 0; i < argTypes.length; i++) {
            boolean isPrimitive = buffer.readBoolean();
            String argTypeClassName = buffer.readCharSequence(buffer.readInt(), Charset.defaultCharset()).toString();
            if (isPrimitive) {
                argTypes[i] = ArgumentMapper.resolvePrimitive(argTypeClassName);
            } else {
                argTypes[i] = Class.forName(argTypeClassName);
            }
        }

        boolean isReturnPrimitive = buffer.readBoolean();
        String returnTypeName = buffer.readCharSequence(buffer.readInt(), Charset.defaultCharset()).toString();
        Class<?> returnType;
        if (isReturnPrimitive) {
            returnType = ArgumentMapper.resolvePrimitive(returnTypeName);
        } else {
            returnType = Class.forName(returnTypeName);
        }

        // release the buffer
        buffer.release();

        return new InvocationDescriptor(id, namespace, declaringClass, methodName, args, argTypes, returnType);
    }

    public String getNamespace() {
        return namespace;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public Class<?>[] getArgTypes() {
        return argTypes;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public UUID getUniqueInvocationId() {
        return id;
    }

    public Class<?> getDeclaringClass() {
        return declaringClass;
    }
}
