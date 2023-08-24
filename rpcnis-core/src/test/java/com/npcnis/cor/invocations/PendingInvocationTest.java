package com.npcnis.cor.invocations;

import com.rpcnis.base.defaults.LoopbackTransport;
import com.rpcnis.base.errors.InvocationTimedOutException;
import com.rpcnis.core.Rpcnis;
import com.rpcnis.core.models.InvocationDescriptor;
import org.junit.jupiter.api.*;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    public void testPendingInvocation() {
        // base instance
        Rpcnis rpcnis = new Rpcnis(new LoopbackTransport());

        InvocationDescriptor invocationDescriptor = new InvocationDescriptor("targetName", "methodName", new Object[]{}, new Class[]{}, String.class);

        String testString = "test invocation";

        // complete invocation
        threadPoolExecutor.execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            rpcnis.completeInvocation(invocationDescriptor, testString);
        });

        String response = rpcnis.invoke(invocationDescriptor, String.class);

        assert response.equals(testString);
    }

    @Test
    @Timeout(10) // seconds
    public void testTimeout() {
        // base instance
        Rpcnis rpcnis = new Rpcnis(new LoopbackTransport());
        rpcnis.getOptions().setTimeoutSeconds(2); // or else it will take 10 seconds to run

        InvocationDescriptor invocationDescriptor = new InvocationDescriptor("targetName", "methodName", new Object[]{}, new Class[]{}, String.class);

        Assertions.assertThrowsExactly(InvocationTimedOutException.class, () -> {
            rpcnis.invoke(invocationDescriptor, String.class);
        });


    }

}
