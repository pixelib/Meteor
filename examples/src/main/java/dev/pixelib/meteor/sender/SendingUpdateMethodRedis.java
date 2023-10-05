package dev.pixelib.meteor.sender;

import dev.pixelib.meteor.core.Meteor;
import dev.pixelib.meteor.transport.redis.RedisTransport;

public class SendingUpdateMethodRedis {

    public static void main(String[] args) throws Exception{
        Meteor meteor = new Meteor(new RedisTransport("192.168.178.46", 6379, "test"));

        MathAdd mathAdd = meteor.registerProcedure(MathAdd.class);
        MathSubstract mathSubstract = meteor.registerProcedure(MathSubstract.class);
        MathMultiply mathMultiply = meteor.registerProcedure(MathMultiply.class);

        // register an implementation, invocations will be dispatched to this object.
        // implementations will be registered under all interfaces they implement
        meteor.registerImplementation(new MathFunctionsImpl());


        int subResult = mathSubstract.substract(10, 1, 2, 3, 4, 5);
        System.out.println("10 - 1 - 2 - 3 - 4 - 5 = " + subResult);

        int addResult = mathAdd.add(1, 2, 3, 4, 5);
        System.out.println("1 + 2 + 3 + 4 + 5 = " + addResult);

        int multiResult = mathMultiply.multiply(5, 5);
        System.out.println("5 * 5 = " + multiResult);

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
