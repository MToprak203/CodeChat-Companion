package com.ai.assistant.persistence.relational.repository;

import com.ai.assistant.persistence.relational.entity.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MessageRepositoryCustom {
    Flux<Message> findByConversationId(Long conversationId, int offset, int limit);

    Mono<Long> countByConversationId(Long conversationId);

    Flux<Message> findUnreadMessages(Long conversationId, java.time.LocalDateTime since);
}
