package com.meteormsg.sender;

import com.meteormsg.base.defaults.LoopbackTransport;
import com.meteormsg.core.Meteor;

public class SendingUpdateMethod {

    public static void main(String[] args) throws Exception{
        Meteor meteor = new Meteor(new LoopbackTransport());

        MathAdd mathAdd = meteor.registerProcedure(MathAdd.class);
        MathSubstract mathSubstract = meteor.registerProcedure(MathSubstract.class);

        // register an implementation, invocations will be dispatched to this object.
        // implementations will be registered under all interfaces they implement
        meteor.registerImplementation(new MathFunctionsImpl());

        int subResult = mathSubstract.substract(10, 1, 2, 3, 4, 5);
        System.out.println("10 - 1 - 2 - 3 - 4 - 5 = " + subResult);

        int addResult = mathAdd.add(1, 2, 3, 4, 5);
        System.out.println("1 + 2 + 3 + 4 + 5 = " + addResult);

        meteor.stop();
    }

    public interface MathAdd {
        int add(int... numbers);
    }

    public interface MathSubstract {
        int substract(int from, int... numbers);
    }

    public interface MathMultiply {
        int multiply(int x, int times);
    }

    public static class MathFunctionsImpl implements MathAdd, MathSubstract, MathMultiply {

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
