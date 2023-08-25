package com.meteormsg.sender;

import com.meteormsg.base.defaults.LoopbackTransport;
import com.meteormsg.core.Meteor;

public class SendingUpdateMethod {

    public static void main(String[] args) {
        Meteor meteor = new Meteor(new LoopbackTransport());
        MathFunctions mathFunctions = meteor.registerProcedure(MathFunctions.class);
        MathFunctions mathFunctions2 = meteor.registerProcedure(MathFunctions.class, "Cooler-math-functions");

        // register an implementation, invocations will be dispatched to this object.
        // implementations will be registered under all interfaces they implement
        meteor.registerImplementation(new MathFunctionsImpl());

        // you can also register an implementation under a specific namespace
        meteor.registerImplementation(new MathFunctionsImpl(), "Cooler-math-functions");


        int result = mathFunctions.add(1, 2, 3, 4, 5);
        System.out.println("1 + 2 + 3 + 4 + 5 = " + result);

        meteor.stop();
    }

    public interface MathFunctions {
        int multiply(int x, int times);
        int add(int... numbers);
        int substract(int from, int... numbers);
    }

    public static class MathFunctionsImpl implements MathFunctions {

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
