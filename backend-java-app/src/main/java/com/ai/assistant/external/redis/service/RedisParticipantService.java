package com.ai.assistant.external.redis.service;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface RedisParticipantService {
    Mono<Void> addParticipant(String conversationId, Long userId);
    Mono<Void> removeParticipant(String conversationId, Long userId);
    Flux<Long> getParticipants(String conversationId);
    Mono<Boolean> isParticipant(String conversationId, Long userId);
}
