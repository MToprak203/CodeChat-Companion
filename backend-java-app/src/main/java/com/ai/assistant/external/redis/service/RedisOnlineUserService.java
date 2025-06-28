package com.ai.assistant.external.redis.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RedisOnlineUserService {
    Mono<Void> addOnlineUser(Long userId);
    Mono<Void> removeOnlineUser(Long userId);
    Mono<Boolean> isOnline(Long userId);
    Flux<Long> getOnlineUsers();
}
