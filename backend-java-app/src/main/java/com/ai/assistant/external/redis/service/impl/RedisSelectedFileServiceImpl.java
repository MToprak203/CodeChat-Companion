package com.ai.assistant.external.redis.service.impl;

import com.ai.assistant.external.redis.service.RedisSelectedFileService;
import com.ai.assistant.usecase.resilience.wrapper.ResilienceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.ai.assistant.core.Constants.Resilience.Wrapper.REDIS_RESILIENCE_WRAPPER;

@Slf4j
@Service
public class RedisSelectedFileServiceImpl implements RedisSelectedFileService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ResilienceWrapper resilience;

    public RedisSelectedFileServiceImpl(
            ReactiveRedisTemplate<String, String> redisTemplate,
            @Qualifier(REDIS_RESILIENCE_WRAPPER) ResilienceWrapper resilience
    ) {
        this.redisTemplate = redisTemplate;
        this.resilience = resilience;
    }

    private String redisKey(Long projectId) {
        return "project:" + projectId + ":selected-files";
    }

    @Override
    public Mono<Void> setSelectedFiles(Long projectId, List<String> files) {
        String key = redisKey(projectId);
        log.debug("[redis:selected:set] key={} size={}", key, files.size());
        return resilience.wrap(redisTemplate.delete(key))
                .thenMany(Flux.fromIterable(files)
                        .concatMap(f -> redisTemplate.opsForList().rightPush(key, f)))
                .then()
                .doOnSuccess(v -> log.debug("[redis:selected:set:success] key={}", key))
                .doOnError(e -> log.error("[redis:selected:set:error] key={} error={}", key, e.toString(), e));
    }

    @Override
    public Flux<String> getSelectedFiles(Long projectId) {
        String key = redisKey(projectId);
        log.debug("[redis:selected:get] key={}", key);
        return resilience.wrap(redisTemplate.opsForList().range(key, 0, -1))
                .doOnNext(v -> log.trace("[redis:selected:get:item] key={} value={}", key, v))
                .doOnComplete(() -> log.debug("[redis:selected:get:complete] key={}", key))
                .doOnError(e -> log.error("[redis:selected:get:error] key={} error={}", key, e.toString(), e));
    }
}
