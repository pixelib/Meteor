package dev.pixelib.meteor.core.executor;

import dev.pixelib.meteor.core.transport.packets.InvocationDescriptor;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ImplementationWrapperTest {

    @Test
    void invokeOn_success() throws NoSuchMethodException {

        ImplementationWrapper wrapper = new ImplementationWrapper(new MathFunctionImplementation(), "namespace");
        Class<?>[] argTypes = new Class<?>[] { int.class, int.class };

        InvocationDescriptor descriptor = new InvocationDescriptor("math", MathFunctionImplementation.class, "add", new Object[]{1, 2},argTypes, int.class);
        assertEquals(3, (Integer) wrapper.invokeOn(descriptor));
    }

    @Test
    void invokeOn_withBoxParams() throws NoSuchMethodException {
        ImplementationWrapper wrapper = new ImplementationWrapper(new MathFunctionImplementation(), "namespace");
        Class<?>[] argTypes = new Class<?>[] { Integer.class, Integer.class };

        InvocationDescriptor descriptor = new InvocationDescriptor("math", MathFunctionImplementation.class, "add", new Object[]{1, 2},argTypes, int.class);
        assertEquals(3, (Integer) wrapper.invokeOn(descriptor));
    }

    @Test
    void invokeOn_unknownMethod() {
        ImplementationWrapper wrapper = new ImplementationWrapper(new MathFunctionImplementation(), "namespace");
        Class<?>[] argTypes = new Class<?>[] { Integer.class, Integer.class };

        InvocationDescriptor descriptor = new InvocationDescriptor("math", MathFunctionImplementation.class, "unknown", new Object[]{1, 2},argTypes, int.class);
        NoSuchMethodException noSuchMethodException = assertThrowsExactly(NoSuchMethodException.class, () -> {
            wrapper.invokeOn(descriptor);
        });

        assertEquals("No method found with name unknown and compatible arguments (on " + MathFunctionImplementation.class.getName() + ").", noSuchMethodException.getMessage());
    }
    @Test
    void getImplementation_success() {
        MathFunctionImplementation implementation = new MathFunctionImplementation();

        ImplementationWrapper implementationWrapper = new ImplementationWrapper(implementation, "math");

        assertSame(implementation, implementationWrapper.getImplementation());
    }

    @Test
    void getNamespace_success() {
        ImplementationWrapper implementationWrapper = new ImplementationWrapper(new MathFunctionImplementation(), "math");
        assertEquals("math", implementationWrapper.getNamespace());
    }

    static class MathFunctionImplementation implements MathFunction {
        @Override
        public int add(int a, int b) {
            return a + b;
        }

        @Override
        public int add(int... a) {
            return Arrays.stream(a).sum();
        }

        @Override
        public int add(int a, Integer... b) {
            return a + Arrays.stream(b).mapToInt(Integer::intValue).sum();
        }

        @Override
        public int add(int a, Double... b) {
            return a + Arrays.stream(b).mapToInt(Double::intValue).sum();
        }

        @Override
        public int sub(int a, int b) {
            return  a - b;
        }
    }

    public interface MathFunction {
        int add(int a, int b);
        int add(int... a);
        int add(int a, Integer... b);


        int add(int a, Double... b);

        int sub(int a, int b);
    }
}