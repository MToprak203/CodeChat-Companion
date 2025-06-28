package com.ai.assistant.external.redis.service.impl;

import com.ai.assistant.external.redis.service.RedisReadyFlagService;
import com.ai.assistant.usecase.resilience.wrapper.ResilienceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.ai.assistant.core.Constants.Resilience.Wrapper.REDIS_RESILIENCE_WRAPPER;

@Slf4j
@Service
public class RedisReadyFlagServiceImpl implements RedisReadyFlagService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ResilienceWrapper resilience;
    private final Integer readyFlagTtlSeconds;

    public RedisReadyFlagServiceImpl(
            ReactiveRedisTemplate<String, String> redisTemplate,
            @Qualifier(REDIS_RESILIENCE_WRAPPER) ResilienceWrapper resilience,
            @Value("${redis.ttl.ready-flag}") Integer readyFlagTtlSeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.resilience = resilience;
        this.readyFlagTtlSeconds = readyFlagTtlSeconds;
    }

    private String redisKey(String channelKey, Long userId) {
        return "ws:" + channelKey + ":ready:" + userId;
    }

    @Override
    public Mono<Void> markReady(String channelKey, Long userId) {
        String key = redisKey(channelKey, userId);
        log.debug("[redis:ready:mark] key={}", key);

        return resilience.wrap(redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(readyFlagTtlSeconds)))
                .then()
                .doOnSuccess(v -> log.debug("[redis:ready:mark:success] key={}", key))
                .doOnError(e -> log.error("[redis:ready:mark:error] key={} error={}", key, e.toString(), e));
    }

    @Override
    public Mono<Boolean> isReady(String channelKey, Long userId) {
        String key = redisKey(channelKey, userId);
        log.debug("[redis:ready:check] key={}", key);

        return resilience.wrap(redisTemplate.hasKey(key))
                .doOnNext(ready -> log.debug("[redis:ready:check:result] key={} ready={}", key, ready))
                .doOnError(e -> log.error("[redis:ready:check:error] key={} error={}", key, e.toString(), e));
    }

    @Override
    public Mono<Boolean> hasAnyReadyUser(String channelKey) {
        String keyPrefix = "ws:" + channelKey + ":ready:";

        return redisTemplate.scan()
                .filter(key -> key.startsWith(keyPrefix))
                .hasElements()
                .doOnNext(found -> log.debug("[redis:ready:hasAnyReadyUser] prefix={} found={}", keyPrefix, found))
                .doOnError(e -> log.error("[redis:ready:hasAnyReadyUser:error] prefix={} error={}", keyPrefix, e.toString(), e));
    }
}

