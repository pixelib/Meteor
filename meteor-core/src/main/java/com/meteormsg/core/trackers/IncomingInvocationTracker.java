package com.meteormsg.core.trackers;

import com.meteormsg.core.executor.ImplementationWrapper;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class IncomingInvocationTracker {

    // Map invocation handlers by type; used for dispatching incoming invocations
    // Handlers without a namespace (which is nullable) are also stored here
    private final ConcurrentHashMap<Class<?>, Collection<ImplementationWrapper>> implementations = new ConcurrentHashMap<>();

    public void registerImplementation(Object implementation, String namespace) {
        // get the interfaces implemented by the implementation
        Class<?>[] interfaces = implementation.getClass().getInterfaces();

        // there must be at least one interface
        if (interfaces.length == 0) {
            throw new IllegalArgumentException("Implementation must implement at least one interface/procedure");
        }

        // register this interface as all the implemented interfaces
        ImplementationWrapper implementationWrapper = new ImplementationWrapper(implementation, namespace);

        for (Class<?> anInterface : interfaces) {
            implementations.computeIfAbsent(anInterface, k -> ConcurrentHashMap.newKeySet()).add(implementationWrapper);
        }
    }

    public ConcurrentHashMap<Class<?>, Collection<ImplementationWrapper>> getImplementations() {
        return implementations;
    }
}
