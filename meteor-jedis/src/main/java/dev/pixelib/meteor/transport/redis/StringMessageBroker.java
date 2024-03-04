package dev.pixelib.meteor.transport.redis;

@FunctionalInterface
public interface StringMessageBroker {

    boolean onRedisMessage(String message) throws Exception;

}
