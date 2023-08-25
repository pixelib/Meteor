package com.meteormsg.core.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Map;

public class ArgumentMapper {

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

        // is it an empty array? if so, return an empty array
        if (allArguments.length == 0) {
            return output;
        }

        for (int i = 0; i < method.getParameterCount() - 1; i++) {
            output[i] = allArguments[i];
        }

        Class<?> lastParameterType = method.getParameterTypes()[method.getParameterCount() - 1];
        if (lastParameterType.isArray()) {
            Class<?> componentType = lastParameterType.getComponentType();

            int length = allArguments.length - method.getParameterCount() + 1;
            Object array = Array.newInstance(componentType, length);

            for (int i = 0; i < length; i++) {
                Object argument = allArguments[method.getParameterCount() - 1 + i];

                // is argument also an array? of so, then check if the component types match
                if (argument != null && argument.getClass().isArray()) {
                    Class<?> argumentComponentType = argument.getClass().getComponentType();
                    if (componentType.isAssignableFrom(argumentComponentType)) {
                        // loop over argument and push it onto the array
                        for (int j = 0; j < Array.getLength(argument); j++) {
                            // is the array big enough? if not, resize it
                            if (i + j >= length) {
                                Object newArray = Array.newInstance(componentType, i + j + 1);
                                System.arraycopy(array, 0, newArray, 0, Array.getLength(array));
                                array = newArray;
                            }
                            Array.set(array, i + j, Array.get(argument, j));
                        }
                    } else {
                        throw new RuntimeException("Argument type mismatch. " + componentType + " expected, got " + argumentComponentType + " instead for argument " + i + ".");
                    }
                } else {

                    if (componentType.isPrimitive()) {
                        Class<?> wrapperType = PRIMITIVE_TO_BOXED.get(componentType);
                        if (wrapperType.isInstance(argument)) {
                            Array.set(array, i, argument);
                        } else {
                            // ignore nulls, they are fine
                            if (argument != null)
                                throw new RuntimeException("Argument type mismatch. " + wrapperType + " expected, got " + argument.getClass() + " instead for argument " + i + ".");
                        }
                    } else {
                        Array.set(array, i, argument);
                    }
                }
            }

            output[output.length - 1] = array;
        } else {
            output[output.length - 1] = allArguments[allArguments.length - 1];
        }

        if (output.length != method.getParameterCount()) {
            throw new RuntimeException("Overflowing arguments failed! Expected " + method.getParameterCount() + " arguments, got " + output.length + " arguments.");
        }

        return output;
    }


}
