package com.ai.assistant.persistence.relational.repository;

import com.ai.assistant.persistence.relational.entity.Conversation;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ConversationRepository extends R2dbcRepository<Conversation, Long>, ConversationRepositoryCustom {
    @Query("SELECT * FROM conversations WHERE project_id = :projectId AND deleted_at IS NULL LIMIT 1")
    Mono<Conversation> findByProjectId(Long projectId);

    @Query("SELECT * FROM conversations WHERE project_id = :projectId AND deleted_at IS NULL ORDER BY updated_at DESC")
    reactor.core.publisher.Flux<Conversation> findAllByProjectId(Long projectId);

    @Query("SELECT * FROM conversations WHERE id = :id AND deleted_at IS NULL")
    Mono<Conversation> findByIdAndDeletedAtIsNull(Long id);

    @Query("""
            UPDATE conversations
            SET deleted_at = NOW(), deleted_by = :userId
            WHERE id = :id AND deleted_at IS NULL
            """)
    Mono<Void> softDelete(Long id, Long userId);
}
