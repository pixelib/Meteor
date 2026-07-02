package dev.pixelib.meteor.transport.redis;

import dev.pixelib.meteor.base.RpcTransport;
import dev.pixelib.meteor.base.enums.Direction;
import dev.pixelib.meteor.base.interfaces.SubscriptionHandler;
import redis.clients.jedis.RedisClient;

import java.io.IOException;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

public class RedisTransport implements RpcTransport {

    private static final Base64.Decoder base64Decoder = Base64.getDecoder();
    private static final Base64.Encoder base64Encoder = Base64.getEncoder();

    private final Logger logger = Logger.getLogger(RedisTransport.class.getSimpleName());

    private final RedisClient redisClient;
    private final String topic;
    private final UUID transportId = UUID.randomUUID();
    private RedisSubscriptionThread redisSubscriptionThread;
    private boolean ignoreSelf = true;
    private boolean closed;

    public RedisTransport(RedisClient redisClient, String topic) {
        this.redisClient = redisClient;
        this.topic = topic;
    }

    public RedisTransport(String url, String topic) {
        this.redisClient = RedisClient.create(url);
        this.topic = topic;
    }

    public RedisTransport(String host, int port, String topic) {
        this.redisClient = RedisClient.create(host, port);
        this.topic = topic;
    }

    public RedisTransport withIgnoreSelf(boolean ignoreSelf) {
        this.ignoreSelf = ignoreSelf;
        return this;
    }

    @Override
    public void send(Direction direction, byte[] bytes) {
        if (closed) {
            throw new IllegalStateException("RedisTransport is closed");
        }
        redisClient.publish(
                getTopicName(direction),
                transportId + base64Encoder.encodeToString(bytes)
        );
    }

    @Override
    public void subscribe(Direction direction, SubscriptionHandler onReceive) {
        if (closed) {
            throw new IllegalStateException("RedisTransport is closed");
        }
        StringMessageBroker wrappedHandler = message -> {
            byte[] bytes = message.getBytes();
            byte[] uuid = new byte[36];
            System.arraycopy(bytes, 0, uuid, 0, 36);

            if (ignoreSelf && transportId.toString().equals(new String(uuid))) {
                return false;
            }

            byte[] data = new byte[bytes.length - 36];
            System.arraycopy(bytes, 36, data, 0, data.length);

            try {
                return onReceive.onPacket(base64Decoder.decode(data));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        if (redisSubscriptionThread == null) {
            redisSubscriptionThread = new RedisSubscriptionThread(wrappedHandler, logger, getTopicName(direction), redisClient);
            redisSubscriptionThread.start().join();
        } else {
            redisSubscriptionThread.subscribe(getTopicName(direction), wrappedHandler);
        }
    }

    public String getTopicName(Direction direction) {
        return topic + "_" + direction.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public void close() throws IOException {
        closed = true;
        if (redisSubscriptionThread != null) {
            redisSubscriptionThread.stop();
        }
        redisClient.close();
    }
}
