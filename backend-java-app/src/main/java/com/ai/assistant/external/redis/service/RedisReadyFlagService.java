package com.ai.assistant.external.redis.service;

import reactor.core.publisher.Mono;

public interface RedisReadyFlagService {
    Mono<Void> markReady(String channelKey, Long userId);
    Mono<Boolean> isReady(String channelKey, Long userId);
    Mono<Boolean> hasAnyReadyUser(String channelKey);
}