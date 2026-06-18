package com.springweb.events;

import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class RedisSubscriber implements MessageListener {

    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        System.out.println("Received message: " + new String(message.getBody()));
    }
}
