package dev.pixelib.meteor.core.trackers;

import dev.pixelib.meteor.core.executor.ImplementationWrapper;
import org.junit.jupiter.api.Test;
import dev.pixelib.meteor.core.utils.MathFunctions;

import static org.junit.jupiter.api.Assertions.*;

class IncomingInvocationTrackerTest {

    @Test
    void testRegisterImplementation_thenSuccess() {
        IncomingInvocationTracker incomingInvocationTracker = new IncomingInvocationTracker();
        TestMathFunctions testMathFunctions = new TestMathFunctions();

        incomingInvocationTracker.registerImplementation(testMathFunctions, "test");

        assertEquals(incomingInvocationTracker.getImplementations().size(), 1);
        assertEquals(incomingInvocationTracker.getImplementations().get(MathFunctions.class).size(), 1);

        boolean matched = false;
        for (ImplementationWrapper implementationWrapper : incomingInvocationTracker.getImplementations().get(MathFunctions.class)) {
            if (implementationWrapper.getImplementation() == testMathFunctions) {
                // inverted, because the namespace is nullable
                if ("test".equals(implementationWrapper.getNamespace())) {
                    if (matched) {
                        fail("Implementation registered twice");
                        return;
                    }
                    matched = true;
                }
            }
        }

        assertTrue(matched, "Implementation not registered");
    }

    @Test
    void testRegisterImplementationArgumentNoInterface_thenFail() {
        IncomingInvocationTracker incomingInvocationTracker = new IncomingInvocationTracker();

        assertThrowsExactly(IllegalArgumentException.class, () -> incomingInvocationTracker.registerImplementation(new Object(), "test"), "Implementation implemented an interface/procedure");
    }

    public static class TestMathFunctions implements MathFunctions {

        @Override
        public int multiply(int x, int times) {
            return x * times;
        }

        @Override
        public int add(int... numbers) {
            int result = 0;
            for (int number : numbers) {
                result += number;
            }
            return result;
        }

        @Override
        public int substract(int from, int... numbers) {
            int result = from;
            for (int number : numbers) {
                result -= number;
            }
            return result;
        }

    }
}
