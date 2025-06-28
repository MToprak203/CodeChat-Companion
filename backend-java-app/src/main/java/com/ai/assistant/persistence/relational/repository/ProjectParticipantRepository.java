package com.ai.assistant.persistence.relational.repository;

import com.ai.assistant.persistence.relational.entity.ProjectParticipant;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProjectParticipantRepository extends R2dbcRepository<ProjectParticipant, Long> {

    @Query("""
            UPDATE project_participants
            SET deleted_at = NOW()
            WHERE project_id = :projectId
              AND user_id = :userId
              AND deleted_at IS NULL
            """)
    Mono<Void> deleteByProjectIdAndUserId(Long projectId, Long userId);

    @Query("SELECT COUNT(*) FROM project_participants WHERE project_id = :projectId AND deleted_at IS NULL")
    Mono<Long> countByProjectId(Long projectId);

    @Query("SELECT COUNT(*) > 0 FROM project_participants WHERE project_id = :projectId AND user_id = :userId AND deleted_at IS NULL")
    Mono<Boolean> existsByProjectIdAndUserId(Long projectId, Long userId);
}
