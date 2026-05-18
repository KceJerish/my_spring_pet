package com.springweb.controller;

import com.springweb.model.PetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cache")
public class CacheController {


    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic topic;

    @PostMapping("/key/{key}")
    public String cachePut(@PathVariable String key, @RequestBody PetResponse value) {
     var val = redisTemplate.opsForValue().setGet(key, value, Duration.ofMinutes(1));
     return val!= null ? val.toString(): null;
    }

    @DeleteMapping("/key/{key}")
    public String removeCache(@PathVariable String key) {
        return String.valueOf(redisTemplate.delete(key));
    }

    @GetMapping("/pub/{pub}")
    public String pubsub(@PathVariable String pub) {
        return String.valueOf(redisTemplate.convertAndSend(topic.getTopic(), pub));
    }
}
