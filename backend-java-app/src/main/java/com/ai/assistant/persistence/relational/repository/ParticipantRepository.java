package com.ai.assistant.persistence.relational.repository;

import com.ai.assistant.persistence.relational.entity.Participant;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ParticipantRepository extends R2dbcRepository<Participant, Long>, ParticipantRepositoryCustom {
    @Query("""
            UPDATE conversation_participants
            SET deleted_at = NOW()
            WHERE conversation_id = :conversationId
              AND user_id = :userId
              AND deleted_at IS NULL
            """)
    Mono<Void> deleteByConversationIdAndUserId(Long conversationId, Long userId);

    @Query("SELECT COUNT(*) > 0 FROM conversation_participants WHERE conversation_id = :conversationId AND user_id = :userId AND deleted_at IS NULL")
    Mono<Boolean> existsByConversationIdAndUserId(Long conversationId, Long userId);

    @Query("SELECT COUNT(*) FROM conversation_participants WHERE conversation_id = :conversationId AND deleted_at IS NULL")
    Mono<Long> countByConversationId(Long conversationId);



    @Query("""
            UPDATE conversation_participants
            SET last_read_at = NOW()
            WHERE conversation_id = :conversationId
              AND user_id = :userId
              AND deleted_at IS NULL
            """)
    Mono<Void> updateLastReadAt(Long conversationId, Long userId);
}
