package dev.pixelib.meteor.core.invocations;

import dev.pixelib.meteor.base.RpcOptions;
import dev.pixelib.meteor.base.defaults.GsonSerializer;
import dev.pixelib.meteor.base.defaults.LoopbackTransport;
import dev.pixelib.meteor.base.errors.InvocationTimedOutException;
import dev.pixelib.meteor.core.transport.packets.InvocationDescriptor;
import dev.pixelib.meteor.core.trackers.OutgoingInvocationTracker;
import dev.pixelib.meteor.core.transport.packets.InvocationResponse;
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
        OutgoingInvocationTracker outgoingInvocationTracker = new OutgoingInvocationTracker(new LoopbackTransport(), new GsonSerializer(), new RpcOptions(), new Timer());

        InvocationDescriptor invocationDescriptor = new InvocationDescriptor("namespace", getClass(), "methodName", new Object[]{}, new Class[]{}, String.class);

        String testString = "test invocation";

        // complete invocation
        threadPoolExecutor.execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            outgoingInvocationTracker.completeInvocation(
                    new InvocationResponse(invocationDescriptor.getUniqueInvocationId(), testString)
            );
        });

        String response = outgoingInvocationTracker.invokeRemoteMethod(invocationDescriptor);
        assertEquals(testString, response);
    }

    @Test
    @Timeout(2) // seconds
    public void testTimeout() {
        RpcOptions options = new RpcOptions();
        options.setTimeoutSeconds(1);
        OutgoingInvocationTracker outgoingInvocationTracker = new OutgoingInvocationTracker(new LoopbackTransport(), new GsonSerializer(), options, new Timer());

        InvocationDescriptor invocationDescriptor = new InvocationDescriptor("namespace", getClass(), "methodName", new Object[]{}, new Class[]{}, String.class);

        assertThrowsExactly(InvocationTimedOutException.class, () -> {
            outgoingInvocationTracker.invokeRemoteMethod(invocationDescriptor);
        });
    }

}
