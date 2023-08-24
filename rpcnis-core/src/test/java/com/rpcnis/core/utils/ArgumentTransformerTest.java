package com.rpcnis.core.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ArgumentTransformerTest {

    @Test
    public void testExactMatch() {
        Method method = getMethod("aMethodWithThreeArguments");
        Object[] arguments = new Object[]{1, 2, 3};
        Object[] transformedArguments = ReflectionUtil.overflowArguments(method, arguments);
        assertArrayEquals(arguments, transformedArguments);
    }

    @Test
    public void testOverflow() {
        Method method = getMethod("aMethodWithThreeArguments");
        Object[] arguments = new Object[]{1, 2, 3};
        Object[] transformedArguments = ReflectionUtil.overflowArguments(method, arguments);
        assertArrayEquals(new Object[]{1, 2, 3}, transformedArguments);
    }

    @Test
    public void testOverflowWithArray() {
        Method method = getMethod("aMethodWithOneArrayArgument");
        Object[] arguments = new Object[]{1, 2, 3, 4, 5};
        Object[] transformedArguments = ReflectionUtil.overflowArguments(method, arguments);
        assertArrayEquals(new int[][]{{1, 2, 3, 4, 5}}, transformedArguments);
    }

    @Test
    public void testOverflowWithOptionalArray() {
        Method method = getMethod("aMethodWithAnOptionalArrayArgument");
        Object[] arguments = new Object[]{1, 2, 3, 4, 5};
        Object[] transformedArguments = ReflectionUtil.overflowArguments(method, arguments);
        assertArrayEquals(new Object[]{1, new int[]{2, 3, 4, 5}}, transformedArguments);
    }

    private Method getMethod(String name) {
        for (Method declaredMethod : getClass().getDeclaredMethods()) {
            if (declaredMethod.getName().equals(name))
                return declaredMethod;
        }
        fail("Method not found");
        return null;
    }

    void aMethodWithThreeArguments(int a, int b, int c) {

    }

    void aMethodWithOneArgument(int a) {

    }

    void aMethodWithOneArrayArgument(int[] a) {

    }

    void aMethodWithAnOptionalArrayArgument(int a, int... b) {

    }

}