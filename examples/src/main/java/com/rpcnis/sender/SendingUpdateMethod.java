package com.rpcnis.sender;

import com.rpcnis.base.defaults.LoopbackTransport;
import com.rpcnis.core.Rpcnis;

import java.util.concurrent.CompletableFuture;

public class SendingUpdateMethod {

    public static void main(String[] args) {
        Rpcnis rpcNis = new Rpcnis(new LoopbackTransport());
        MathFunctions mathFunctions = rpcNis.registerProcedure(MathFunctions.class);
        MathFunctions mathFunctions2 = rpcNis.registerProcedure(MathFunctions.class, "Cooler-math-functions");



        int result = mathFunctions.add(1, 2, 3, 4, 5);
    }

    public interface MathFunctions {

        int multiply(int x, int times);
        int add(int... numbers);
        int substract(int from, int... numbers);
    }

    public class MathFunctionsImpl implements MathFunctions {

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
