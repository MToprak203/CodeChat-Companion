package com.ai.assistant.persistence.relational.repository;

import com.ai.assistant.persistence.relational.entity.Project;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProjectRepository extends R2dbcRepository<Project, Long> {

    @Query("SELECT * FROM projects WHERE deleted_at IS NULL")
    Flux<Project> findAllActive();

    @Query("SELECT * FROM projects WHERE id = :id AND deleted_at IS NULL")
    Mono<Project> findActiveById(Long id);

    @Query("""
            SELECT p.* FROM projects p
            JOIN project_participants pp ON p.id = pp.project_id
            WHERE p.deleted_at IS NULL
              AND pp.deleted_at IS NULL
              AND pp.user_id = :userId
            ORDER BY p.id
            """)
    Flux<Project> findAllActiveByUserId(Long userId);

    @Query("""
            UPDATE projects
            SET deleted_at = NOW(), deleted_by = :userId
            WHERE id = :id AND deleted_at IS NULL
            """)
    Mono<Void> softDelete(Long id, Long userId);
}
