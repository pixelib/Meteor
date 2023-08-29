package dev.pixelib.meteor.core.utils;

import dev.pixelib.meteor.core.Meteor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentMapperTest {

    @ParameterizedTest
    @MethodSource
    void ensureBoxedClass_success(Class<?> primitive, Class<?> boxed) {
        Class<?> result = ArgumentMapper.ensureBoxedClass(primitive);

        assertEquals(boxed, result);
    }

    static Stream<Arguments> ensureBoxedClass_success() {
        return Stream.of(
                Arguments.of(byte.class, Byte.class),
                Arguments.of(boolean.class, Boolean.class),
                Arguments.of(void.class, Void.class),
                Arguments.of(double.class, Double.class),
                Arguments.of(char.class, Character.class)
        );
    }

    @Test
    void ensureBoxedClass_withNull() {
        assertThrowsExactly(NullPointerException.class, () -> {
            ArgumentMapper.ensureBoxedClass(null);
        });
    }

    @Test
    void ensureBoxedClass_withNonPrimitiveClass() {
        Class<?> result = ArgumentMapper.ensureBoxedClass(Meteor.class);

        assertEquals(Meteor.class, result);
    }


    @ParameterizedTest
    @ValueSource(strings = {"boolean", "byte", "short", "int", "long", "float", "double", "char", "void"})
    void testResolvePrimitive_success(String className) {
        Class<?> result = ArgumentMapper.resolvePrimitive(className);
        assertNotNull(result);
        assertTrue(result.isPrimitive());
    }

    @Test
    void testResolvePrimitive_withFullQualifiedName() {
        Class<?> result = ArgumentMapper.resolvePrimitive("java.lang.String");
        assertNotNull(result);
        assertEquals(String.class, result);
    }

    @Test
    void testResolvePrimitive_withSimpleClassName() {
        Class<?> result = ArgumentMapper.resolvePrimitive("String");
        assertNotNull(result);
        assertEquals(String.class, result);
    }

    @Test
    void testResolvePrimitive_classNotFound() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ArgumentMapper.resolvePrimitive("UnknownClass");
        });
        assertTrue(exception.getMessage().contains("Class not found: java.lang.UnknownClass"));
    }

    @Test
    void testResolvePrimitive_nullValue() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            ArgumentMapper.resolvePrimitive(null);
        });
        assertEquals("className cannot be null", exception.getMessage());
    }


    static class Example {
        private void singleParamMethod(Integer integer) { }
        private void multipleParamsMethod(Integer integer, String str, Double dd) { }
        private void oneArrayMethod(int[] a) { }
        private void optionalArrayMethod(int a, int... b) {}
        private void sampleMethod(String a, Integer... b) {}
    }

    private static Stream<Arguments> provideArgumentsForTest() throws NoSuchMethodException {
        Method singleParamMethod = Example.class.getDeclaredMethod("singleParamMethod", Integer.class);
        Method multiParamsMethod = Example.class.getDeclaredMethod("multipleParamsMethod", Integer.class, String.class, Double.class);
        Method oneArrayMethod = Example.class.getDeclaredMethod("oneArrayMethod", int[].class);
        Method optionalArrayMethod = Example.class.getDeclaredMethod("optionalArrayMethod", int.class, int[].class);

        return Stream.of(
                Arguments.of(singleParamMethod, new Object[]{1}, new Object[]{1}),
                Arguments.of(multiParamsMethod, new Object[]{1, "test", 2.0}, new Object[]{1, "test", 2.0}),
                Arguments.of(singleParamMethod, new Object[]{null}, new Object[]{null}),
                Arguments.of(multiParamsMethod, new Object[]{1, null, 2.0}, new Object[]{1, null, 2.0}),
                Arguments.of(oneArrayMethod, new Object[]{1, 2, 3}, new Object[]{new int[]{1, 2, 3}}),
                Arguments.of(optionalArrayMethod, new Object[]{1, 2, 3, 4}, new Object[]{1, new int[]{2, 3, 4}})

        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTest")
    void testOverflowArguments(Method method, Object[] allArguments, Object[] expected) {
        Object[] output = ArgumentMapper.overflowArguments(method, allArguments);

        assertArrayEquals(expected, output);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testOverflowArguments_WithEmptyOrNullExceptions(Object[] allArguments) {
        assertThrows(NullPointerException.class, () -> ArgumentMapper.overflowArguments(null, allArguments));
    }

    @Test
    void testOverflowArguments() throws NoSuchMethodException {
        Method method = Example.class.getDeclaredMethod("sampleMethod", String.class, Integer[].class);
        final Object[] allArguments = new Object[] { "test", new Integer[] {1, 2, 3}, new Integer[] {4, 5, 6}};

        //Valid path
        Assertions.assertDoesNotThrow(() -> ArgumentMapper.overflowArguments(method, allArguments));

        //Invalid path - Type mismatch
        Object[] allArguments2 = new Object[] { "test", new Boolean[] {true, false}, new Integer[] {4, 5, 6}};
        Assertions.assertThrows(RuntimeException.class,
                () -> ArgumentMapper.overflowArguments(method, allArguments2),
                "Argument type mismatch. java.lang.Integer expected, got java.lang.Boolean instead for argument 0.");
    }
}