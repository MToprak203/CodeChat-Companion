package com.ai.assistant.external.redis.service.impl;

import com.ai.assistant.external.redis.service.RedisOnlineUserService;
import com.ai.assistant.usecase.resilience.wrapper.ResilienceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.ai.assistant.core.Constants.Resilience.Wrapper.REDIS_RESILIENCE_WRAPPER;

@Slf4j
@Service
public class RedisOnlineUserServiceImpl implements RedisOnlineUserService {

    private final ReactiveRedisTemplate<String, Long> redisTemplate;
    private final ResilienceWrapper resilience;

    public RedisOnlineUserServiceImpl(
            ReactiveRedisTemplate<String, Long> redisTemplate,
            @Qualifier(REDIS_RESILIENCE_WRAPPER) ResilienceWrapper resilience
    ) {
        this.redisTemplate = redisTemplate;
        this.resilience = resilience;
    }

    private String redisKey() {
        return "users:online";
    }

    @Override
    public Mono<Void> addOnlineUser(Long userId) {
        String key = redisKey();
        log.debug("[redis:online:add] userId={}", userId);
        return resilience.wrap(redisTemplate.opsForSet().add(key, userId))
                .then()
                .doOnError(e -> log.error("[redis:online:add:error] userId={} error={}", userId, e.toString(), e));
    }

    @Override
    public Mono<Void> removeOnlineUser(Long userId) {
        String key = redisKey();
        log.debug("[redis:online:remove] userId={}", userId);
        return resilience.wrap(redisTemplate.opsForSet().remove(key, userId))
                .then()
                .doOnError(e -> log.error("[redis:online:remove:error] userId={} error={}", userId, e.toString(), e));
    }

    @Override
    public Mono<Boolean> isOnline(Long userId) {
        String key = redisKey();
        return resilience.wrap(redisTemplate.opsForSet().isMember(key, userId))
                .doOnError(e -> log.error("[redis:online:isMember:error] userId={} error={}", userId, e.toString(), e));
    }

    @Override
    public Flux<Long> getOnlineUsers() {
        String key = redisKey();
        return resilience.wrap(redisTemplate.opsForSet().members(key))
                .doOnError(e -> log.error("[redis:online:members:error] error={}", e.toString(), e));
    }
}
