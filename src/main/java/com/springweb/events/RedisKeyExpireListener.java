package com.springweb.events;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisKeyExpireListener extends KeyExpirationEventMessageListener {

    public RedisKeyExpireListener(RedisMessageListenerContainer redisMessageListenerContainer) {
        super(redisMessageListenerContainer);
    }

    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        String expiredKey = new String(message.getBody());
        String channel = new String(message.getChannel());
        String matchedPattern = pattern != null ? new String(pattern) : "null";

        log.info("Redis key expired � key: [{}], channel: [{}], pattern: [{}]", expiredKey, channel, matchedPattern);

        // Add logic here based on which cache the key belongs to
        if (expiredKey.startsWith("get.pet.types")) {
            log.info("Pet types cache entry expired: {}", expiredKey);
            // e.g. trigger a refresh, emit a Spring event, etc.
        }
    }
}
