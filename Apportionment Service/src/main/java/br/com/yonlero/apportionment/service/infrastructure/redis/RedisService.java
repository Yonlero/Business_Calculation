package br.com.yonlero.apportionment.service.infrastructure.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class RedisService {
    private final RedisTemplate<String, Serializable> redisTemplate;

    @Autowired
    public RedisService(RedisTemplate<String, Serializable> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void put(String key, Serializable value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public <T extends Serializable> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }
}