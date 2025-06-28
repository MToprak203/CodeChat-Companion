package com.ai.assistant.persistence.relational.repository;

import com.ai.assistant.persistence.relational.entity.Message;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MessageRepository extends R2dbcRepository<Message, Long>, MessageRepositoryCustom {

    @Query("""
            UPDATE messages
            SET deleted_at = NOW(), deleted_by = :userId
            WHERE conversation_id = :conversationId
              AND deleted_at IS NULL
            """)
    Mono<Void> softDeleteByConversationId(Long conversationId, Long userId);
}
