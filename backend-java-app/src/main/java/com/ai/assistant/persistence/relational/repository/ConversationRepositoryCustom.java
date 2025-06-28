package com.ai.assistant.persistence.relational.repository;

import com.ai.assistant.persistence.relational.entity.Conversation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ConversationRepositoryCustom {
    Flux<Conversation> findByUserId(Long userId, long offset, long limit);
    Mono<Long> countByUserId(Long userId);
    Flux<Conversation> findNoProjectByUserId(Long userId, long offset, long limit);
    Mono<Long> countNoProjectByUserId(Long userId);
    Flux<Conversation> findByProjectIdAndUserId(Long projectId, Long userId);
    Mono<Void> updateActivity(Long conversationId, Long userId);
}
