package com.rpcnis.core;

import com.rpcnis.base.defaults.LoopbackTransport;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RpcnisLoopbackTest {

    @Test
    public void testLoopbackFunctionality() {
        Rpcnis rpcNis = new Rpcnis(new LoopbackTransport());

        // register a procedure
        ImplementationTracker proxy = rpcNis.registerProcedure(ImplementationTracker.class);

        // register the real implementation
        TrackerImpl impl = new TrackerImpl();
        rpcNis.registerImplementation(impl);

        // call the procedure
        proxy.addString("Hello");
        proxy.addString("World");

        Set<String> strings = proxy.allStrings();
        boolean containsHello = proxy.containsString("Hello");
        boolean containsWorld = proxy.containsString("World");
        int invocationCount = proxy.getInvocationCount();

        assertEquals(2, strings.size());
        assertTrue(containsHello);
        assertTrue(containsWorld);
        assertEquals(5, invocationCount);

    }

    public interface ImplementationTracker {
        int getInvocationCount();
        Set<String> allStrings();
        void addString(String string);
        boolean containsString(String string);
    }

    public class TrackerImpl implements ImplementationTracker {
        private int invocationCount = 0;
        private Set<String> strings = new HashSet<>();

        @Override
        public int getInvocationCount() {
            return invocationCount;
        }

        @Override
        public Set<String> allStrings() {
            invocationCount++;
            return strings;
        }

        @Override
        public void addString(String string) {
            invocationCount++;
            strings.add(string);
        }

        @Override
        public boolean containsString(String string) {
            invocationCount++;
            return strings.contains(string);
        }
    }
}
