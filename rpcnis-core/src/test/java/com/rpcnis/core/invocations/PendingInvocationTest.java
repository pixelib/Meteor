package com.rpcnis.core.invocations;

import com.rpcnis.base.RpcOptions;
import com.rpcnis.base.errors.InvocationTimedOutException;
import com.rpcnis.core.models.InvocationDescriptor;
import com.rpcnis.core.trackers.OutgoingInvocationTracker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

public class PendingInvocationTest {

    // test thread pool
    private static ThreadPoolExecutor threadPoolExecutor;

    @BeforeAll
    public static void setUp() {
        // create thread pool
        threadPoolExecutor = new ThreadPoolExecutor(5, 5, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    @AfterAll
    public static void tearDown() {
        // shutdown thread pool
        threadPoolExecutor.shutdown();
    }

    @Test
    public void testPendingInvocation() throws Throwable {
        // base instance
        OutgoingInvocationTracker outgoingInvocationTracker = new OutgoingInvocationTracker(new RpcOptions(), new Timer());

        InvocationDescriptor invocationDescriptor = new InvocationDescriptor("namespace", "methodName", new Object[]{}, new Class[]{}, String.class);

        String testString = "test invocation";

        // complete invocation
        threadPoolExecutor.execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            outgoingInvocationTracker.completeInvocation(invocationDescriptor, testString);
        });

        String response = outgoingInvocationTracker.invokeRemoteMethod(invocationDescriptor);
        assertEquals(testString, response);
    }

    @Test
    @Timeout(2) // seconds
    public void testTimeout() {
        RpcOptions options = new RpcOptions();
        options.setTimeoutSeconds(1);
        OutgoingInvocationTracker outgoingInvocationTracker = new OutgoingInvocationTracker(options, new Timer());

        InvocationDescriptor invocationDescriptor = new InvocationDescriptor("namespace", "methodName", new Object[]{}, new Class[]{}, String.class);

        assertThrowsExactly(InvocationTimedOutException.class, () -> {
            outgoingInvocationTracker.invokeRemoteMethod(invocationDescriptor);
        });
    }

}
