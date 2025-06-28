package com.ai.assistant.external.redis.service.impl;

import com.ai.assistant.external.redis.service.RedisParticipantService;
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
public class RedisParticipantServiceImpl implements RedisParticipantService {

    private final ReactiveRedisTemplate<String, Long> redisTemplate;
    private final ResilienceWrapper resilience;

    public RedisParticipantServiceImpl(
            ReactiveRedisTemplate<String, Long> redisTemplate,
            @Qualifier(REDIS_RESILIENCE_WRAPPER) ResilienceWrapper resilience
    ) {
        this.redisTemplate = redisTemplate;
        this.resilience = resilience;
    }

    private String redisKey(String conversationId) {
        return "ws:conversation:" + conversationId + ":participants";
    }

    @Override
    public Mono<Void> addParticipant(String conversationId, Long userId) {
        String key = redisKey(conversationId);
        log.debug("[redis:participant:add] key={} userId={}", key, userId);
        return resilience.wrap(redisTemplate.opsForSet().add(key, userId))
                .then()
                .doOnSuccess(v -> log.debug("[redis:participant:add:success] key={} userId={}", key, userId))
                .doOnError(e -> log.error("[redis:participant:add:error] key={} error={}", key, e.toString(), e));
    }

    @Override
    public Mono<Void> removeParticipant(String conversationId, Long userId) {
        String key = redisKey(conversationId);
        log.debug("[redis:participant:remove] key={} userId={}", key, userId);
        return resilience.wrap(redisTemplate.opsForSet().remove(key, userId))
                .then()
                .doOnSuccess(v -> log.debug("[redis:participant:remove:success] key={} userId={}", key, userId))
                .doOnError(e -> log.error("[redis:participant:remove:error] key={} error={}", key, e.toString(), e));
    }

    @Override
    public Flux<Long> getParticipants(String conversationId) {
        String key = redisKey(conversationId);
        log.debug("[redis:participant:get] key={}", key);
        return resilience.wrap(redisTemplate.opsForSet().members(key))
                .doOnNext(userId -> log.trace("[redis:participant:get:item] key={} userId={}", key, userId))
                .doOnComplete(() -> log.debug("[redis:participant:get:complete] key={}", key))
                .doOnError(e -> log.error("[redis:participant:get:error] key={} error={}", key, e.toString(), e));
    }

    @Override
    public Mono<Boolean> isParticipant(String conversationId, Long userId) {
        String key = redisKey(conversationId);
        log.debug("[redis:participant:isMember] key={} userId={}", key, userId);
        return resilience.wrap(redisTemplate.opsForSet().isMember(key, userId))
                .doOnNext(isMember -> log.debug("[redis:participant:isMember:result] key={} userId={} isMember={}", key, userId, isMember))
                .doOnError(e -> log.error("[redis:participant:isMember:error] key={} userId={} error={}", key, userId, e.toString(), e));
    }
}
