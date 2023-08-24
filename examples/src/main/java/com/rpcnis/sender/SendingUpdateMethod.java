package com.rpcnis.sender;

import com.rpcnis.base.defaults.LoopbackTransport;
import com.rpcnis.core.Rpcnis;

import java.util.concurrent.CompletableFuture;

public class SendingUpdateMethod {

    public static void main(String[] args) {
        Rpcnis rpcNis = new Rpcnis(new LoopbackTransport());
        MathFunctions mathFunctions = rpcNis.registerProcedures(MathFunctions.class);
        MathFunctions mathFunctions2 = rpcNis.registerProcedures(MathFunctions.class, "Cooler-math-functions");


        int result = mathFunctions.add(1, 2, 3, 4, 5);
    }

    public interface MathFunctions {

        int multiply(int x, int times);
        int add(int... numbers);
        int substract(int from, int... numbers);
        CompletableFuture<Integer> calculateMeaningOfLife();
    }
}
