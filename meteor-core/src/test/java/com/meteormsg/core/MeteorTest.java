package com.meteormsg.core;

import com.meteormsg.base.defaults.LoopbackTransport;
import org.junit.jupiter.api.Test;
import utils.MathFunctions;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

class MeteorTest {

    @Test
    void registerProcedureExpectSuccess() {
        Meteor meteor = new Meteor(new LoopbackTransport());

        MathFunctions mathFunctions = meteor.registerProcedure(MathFunctions.class);

        assertNotNull(mathFunctions);
        assertTrue(Meteor.isRpc(mathFunctions));
        assertTrue(Meteor.isMeteorProxy(mathFunctions));
    }

    @Test
    void registerProcedureNotWithInterfaceExpectToFail() {
        Meteor meteor = new Meteor(new LoopbackTransport());

        assertThrowsExactly(IllegalArgumentException.class, () -> {
            Meteor procedure = meteor.registerProcedure(Meteor.class);
        }, "Procedure was an interface");
    }

    @Test
    void registerProcedureWithNullArgumentExpectToFail() {
        Meteor meteor = new Meteor(new LoopbackTransport());

        assertThrowsExactly(NullPointerException.class, () -> {
            MathFunctions mathFunctions = meteor.registerProcedure(null);
        }, "Argument was not null");
    }


    @Test
    void testRegisterProcedureWithNameExpectSuccess() {
        Meteor meteor = new Meteor(new LoopbackTransport());

        MathFunctions mathFunctions = meteor.registerProcedure(MathFunctions.class, "Cooler-math-functions");

        assertNotNull(mathFunctions);
        assertTrue(Meteor.isRpc(mathFunctions));
        assertTrue(Meteor.isMeteorProxy(mathFunctions));
    }

    @Test
    void isRpcExpectSuccess() {
        Meteor meteor = new Meteor(new LoopbackTransport());

        MathFunctions mathFunctions = meteor.registerProcedure(MathFunctions.class, "Cooler-math-functions");
        boolean isRpc = Meteor.isRpc(mathFunctions);

        assertTrue(isRpc);
    }

    @Test
    void isRpcInvalidTypesExpectFail() {
        boolean objectIsRpc = Meteor.isRpc(new Object());
        assertFalse(objectIsRpc);

        boolean rpcSerializer = Meteor.isRpc("rpcSerializer");
        assertFalse(rpcSerializer);

    }

    @Test
    void isMeteorExpectSuccess() {
        Meteor meteor = new Meteor(new LoopbackTransport());

        MathFunctions mathFunctions = meteor.registerProcedure(MathFunctions.class, "Cooler-math-functions");
        boolean ismeteor = Meteor.isMeteorProxy(mathFunctions);

        assertTrue(ismeteor);
    }

    @Test
    void isMeteorExpectFail() {
        Object rpc = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{MathFunctions.class}, (proxy, method, args) -> null);

        boolean ismeteor = Meteor.isMeteorProxy(rpc);

        assertFalse(ismeteor);
    }

}