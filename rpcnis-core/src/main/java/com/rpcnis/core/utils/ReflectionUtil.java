package com.rpcnis.core.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Map;

public class ReflectionUtil {

    /**
     * Map of primitive classes to their boxed counterparts.
     * This is because they aren't strictly the same, so one cannot construct an array of primitives despite
     * the method signature declaring it as such.
     */
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_BOXED = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            double.class, Double.class,
            float.class, Float.class,
            int.class, Integer.class,
            long.class, Long.class,
            short.class, Short.class,
            void.class, Void.class
    );

    public static Class<?> ensureBoxedClass(Class<?> primitiveClass) {
        return PRIMITIVE_TO_BOXED.getOrDefault(primitiveClass, primitiveClass);
    }

    public static Class<?> resolvePrimitive(final String className) {
        switch (className) {
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "char":
                return char.class;
            case "void":
                return void.class;
            default:
                String fqn = className.contains(".") ? className : "java.lang.".concat(className);
                try {
                    return Class.forName(fqn);
                } catch (ClassNotFoundException ex) {
                    throw new IllegalArgumentException("Class not found: " + fqn);
                }
        }
    }

    /**
     * Method arguments cannot be mapped one-to-one to an invocation, because the actual signatures
     * may differ from the declared signatures (this can happen with optional arrays, for example).
     * Luckily, arrays can only be declared as the last argument and there can only be one array,
     * so we can use this to our advantage to simply overflown everything after l-1 into the array, and using that
     * as the final argument.
     */
    public static Object[] overflowArguments(Method method, Object[] allArguments) {
        Object[] output = new Object[method.getParameterCount()];

        // map all arguments except the last one
        if (method.getParameterCount() - 1 >= 0)
            System.arraycopy(allArguments, 0, output, 0, method.getParameterCount() - 1);

        // map the last argument as an array
        Class<?> lastParameterType = method.getParameterTypes()[method.getParameterCount() - 1];
        if (lastParameterType.isArray()) {
            // create an array of the correct type
            int length = allArguments.length - method.getParameterCount() + 1;
            Class<?> componentType = lastParameterType.getComponentType();

            if (componentType.isPrimitive() && componentType == int.class) {
                int[] primitiveArray = new int[length];
                for (int i = 0; i < length; i++) {
                    Object argument = allArguments[method.getParameterCount() - 1 + i];
                    if (argument instanceof Integer) {
                        primitiveArray[i] = ((Integer) argument).intValue();
                    } else {
                        throw new RuntimeException("Argument type mismatch");
                    }
                }
                output[output.length - 1] = primitiveArray;
            } else {
                // Handle non-primitive array case
                Object array = Array.newInstance(componentType, length);

                // fill the array with the remaining arguments
                if (allArguments.length - (method.getParameterCount() - 1) >= 0)
                    System.arraycopy(allArguments, method.getParameterCount() - 1, array, 0, allArguments.length - (method.getParameterCount() - 1));

                // set the array as the last argument
                output[output.length - 1] = array;
            }
        } else {
            // set the last argument as-is
            output[output.length - 1] = allArguments[allArguments.length - 1];
        }

        // sanity check; is the output array the same length as the method's parameter count?
        if (output.length != method.getParameterCount())
            throw new RuntimeException("Overflowing arguments failed! Expected " + method.getParameterCount() + " arguments, got " + output.length + " arguments.");

        return output;
    }



}
