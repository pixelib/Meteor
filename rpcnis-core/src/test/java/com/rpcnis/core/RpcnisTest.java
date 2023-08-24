package com.rpcnis.core;

import com.rpcnis.base.defaults.LoopbackTransport;
import com.rpcnis.core.proxy.ProxyInvocHandler;
import com.rpcnis.core.proxy.RpcnisMock;
import org.junit.jupiter.api.Test;
import utils.MathFunctions;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

class RpcnisTest {

    @Test
    void registerProcedureExpectSuccess() {
        Rpcnis rpcNis = new Rpcnis(new LoopbackTransport());

        MathFunctions mathFunctions = rpcNis.registerProcedure(MathFunctions.class);

        assertNotNull(mathFunctions);
        assertTrue(Rpcnis.isRpc(mathFunctions));
        assertTrue(Rpcnis.isRpcnis(mathFunctions));
    }

    @Test
    void registerProcedureNotWithInterfaceExpectToFail() {
        Rpcnis rpcNis = new Rpcnis(new LoopbackTransport());

        assertThrowsExactly(IllegalArgumentException.class, () -> {
            Rpcnis procedure = rpcNis.registerProcedure(Rpcnis.class);
        }, "Procedure was an interface");
    }

    @Test
    void registerProcedureWithNullArgumentExpectToFail() {
        Rpcnis rpcNis = new Rpcnis(new LoopbackTransport());

        assertThrowsExactly(NullPointerException.class, () -> {
            MathFunctions mathFunctions = rpcNis.registerProcedure(null);
        }, "Argument was not null");
    }


    @Test
    void testRegisterProcedureWithNameExpectSuccess() {
        Rpcnis rpcNis = new Rpcnis(new LoopbackTransport());

        MathFunctions mathFunctions = rpcNis.registerProcedure(MathFunctions.class, "Cooler-math-functions");

        assertNotNull(mathFunctions);
        assertTrue(Rpcnis.isRpc(mathFunctions));
        assertTrue(Rpcnis.isRpcnis(mathFunctions));
    }

    @Test
    void isRpcExpectSuccess() {
        Rpcnis rpcNis = new Rpcnis(new LoopbackTransport());

        MathFunctions mathFunctions = rpcNis.registerProcedure(MathFunctions.class, "Cooler-math-functions");
        boolean isRpc = Rpcnis.isRpc(mathFunctions);

        assertTrue(isRpc);
    }

    @Test
    void isRpcInvalidTypesExpectFail() {
        boolean objectIsRpc = Rpcnis.isRpc(new Object());
        assertFalse(objectIsRpc);

        boolean rpcSerializer = Rpcnis.isRpc("rpcSerializer");
        assertFalse(rpcSerializer);

    }

    @Test
    void isRpcnisExpectSuccess() {
        Rpcnis rpcNis = new Rpcnis(new LoopbackTransport());

        MathFunctions mathFunctions = rpcNis.registerProcedure(MathFunctions.class, "Cooler-math-functions");
        boolean isRpcnis = Rpcnis.isRpcnis(mathFunctions);

        assertTrue(isRpcnis);
    }

    @Test
    void isRpcnisExpectFail() {
        Object rpc = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{MathFunctions.class}, (proxy, method, args) -> null);

        boolean isRpcnis = Rpcnis.isRpcnis(rpc);

        assertFalse(isRpcnis);
    }

}