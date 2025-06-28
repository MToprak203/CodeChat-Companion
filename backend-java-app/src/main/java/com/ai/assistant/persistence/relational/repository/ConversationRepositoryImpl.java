package com.ai.assistant.persistence.relational.repository;

import com.ai.assistant.enums.ConversationType;
import com.ai.assistant.persistence.relational.entity.Conversation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class ConversationRepositoryImpl implements ConversationRepositoryCustom {
    private final DatabaseClient databaseClient;
    private final R2dbcEntityTemplate template;

    @Override
    public Flux<Conversation> findByUserId(Long userId, long offset, long limit) {
        String sql = """
                SELECT DISTINCT c.* FROM conversations c
                LEFT JOIN conversation_participants p ON c.id = p.conversation_id
                    AND p.user_id = :userId
                    AND p.deleted_at IS NULL
                LEFT JOIN project_participants pp ON c.project_id = pp.project_id
                    AND pp.user_id = :userId
                    AND pp.deleted_at IS NULL
                WHERE c.deleted_at IS NULL
                  AND (p.user_id IS NOT NULL OR pp.user_id IS NOT NULL)
                ORDER BY c.updated_at DESC
                LIMIT :limit OFFSET :offset
                """;
        return databaseClient.sql(sql)
                .bind("userId", userId)
                .bind("limit", limit)
                .bind("offset", offset)
                .map((row, meta) -> {
                    Conversation conv = new Conversation();
                    conv.setId(row.get("id", Long.class));
                    conv.setType(ConversationType.valueOf(row.get("type", String.class)));
                    conv.setTitle(row.get("title", String.class));
                    conv.setProjectId(row.get("project_id", Long.class));
                    conv.setCreatedById(row.get("created_by", Long.class));
                    conv.setCreatedAt(row.get("created_at", Instant.class));
                    conv.setUpdatedById(row.get("updated_by", Long.class));
                    conv.setUpdatedAt(row.get("updated_at", Instant.class));
                    conv.setDeletedById(row.get("deleted_by", Long.class));
                    conv.setDeletedAt(row.get("deleted_at", Instant.class));
                    conv.setVersion(row.get("version", Long.class));
                    return conv;
                })
                .all();
    }

    @Override
    public Mono<Long> countByUserId(Long userId) {
        String sql = """
                SELECT COUNT(DISTINCT c.id) AS cnt FROM conversations c
                LEFT JOIN conversation_participants p ON c.id = p.conversation_id
                    AND p.user_id = :userId
                    AND p.deleted_at IS NULL
                LEFT JOIN project_participants pp ON c.project_id = pp.project_id
                    AND pp.user_id = :userId
                    AND pp.deleted_at IS NULL
                WHERE c.deleted_at IS NULL
                  AND (p.user_id IS NOT NULL OR pp.user_id IS NOT NULL)
                """;
        return databaseClient.sql(sql)
                .bind("userId", userId)
                .map((row, meta) -> row.get("cnt", Long.class))
                .one();
    }

    @Override
    public Flux<Conversation> findNoProjectByUserId(Long userId, long offset, long limit) {
        String sql = """
                SELECT c.* FROM conversations c
                JOIN conversation_participants p ON c.id = p.conversation_id
                WHERE p.user_id = :userId
                  AND c.deleted_at IS NULL
                  AND p.deleted_at IS NULL
                  AND c.project_id IS NULL
                ORDER BY c.updated_at DESC
                LIMIT :limit OFFSET :offset
                """;
        return databaseClient.sql(sql)
                .bind("userId", userId)
                .bind("limit", limit)
                .bind("offset", offset)
                .map((row, meta) -> {
                    Conversation conv = new Conversation();
                    conv.setId(row.get("id", Long.class));
                    conv.setType(ConversationType.valueOf(row.get("type", String.class)));
                    conv.setTitle(row.get("title", String.class));
                    conv.setProjectId(row.get("project_id", Long.class));
                    conv.setCreatedById(row.get("created_by", Long.class));
                    conv.setCreatedAt(row.get("created_at", Instant.class));
                    conv.setUpdatedById(row.get("updated_by", Long.class));
                    conv.setUpdatedAt(row.get("updated_at", Instant.class));
                    conv.setDeletedById(row.get("deleted_by", Long.class));
                    conv.setDeletedAt(row.get("deleted_at", Instant.class));
                    conv.setVersion(row.get("version", Long.class));
                    return conv;
                })
                .all();
    }

    @Override
    public Mono<Long> countNoProjectByUserId(Long userId) {
        String sql = """
                SELECT COUNT(*) AS cnt FROM conversations c
                JOIN conversation_participants p ON c.id = p.conversation_id
                WHERE p.user_id = :userId
                  AND c.deleted_at IS NULL
                  AND p.deleted_at IS NULL
                  AND c.project_id IS NULL
                """;
        return databaseClient.sql(sql)
                .bind("userId", userId)
                .map((row, meta) -> row.get("cnt", Long.class))
                .one();
    }

    @Override
    public Flux<Conversation> findByProjectIdAndUserId(Long projectId, Long userId) {
        String sql = """
                SELECT DISTINCT c.* FROM conversations c
                LEFT JOIN conversation_participants p ON c.id = p.conversation_id
                    AND p.user_id = :userId
                    AND p.deleted_at IS NULL
                LEFT JOIN project_participants pp ON c.project_id = pp.project_id
                    AND pp.user_id = :userId
                    AND pp.deleted_at IS NULL
                WHERE c.project_id = :projectId
                  AND c.deleted_at IS NULL
                  AND (p.user_id IS NOT NULL OR pp.user_id IS NOT NULL)
                ORDER BY c.updated_at DESC
                """;
        return databaseClient.sql(sql)
                .bind("projectId", projectId)
                .bind("userId", userId)
                .map((row, meta) -> {
                    Conversation conv = new Conversation();
                    conv.setId(row.get("id", Long.class));
                    conv.setType(ConversationType.valueOf(row.get("type", String.class)));
                    conv.setTitle(row.get("title", String.class));
                    conv.setProjectId(row.get("project_id", Long.class));
                    conv.setCreatedById(row.get("created_by", Long.class));
                    conv.setCreatedAt(row.get("created_at", Instant.class));
                    conv.setUpdatedById(row.get("updated_by", Long.class));
                    conv.setUpdatedAt(row.get("updated_at", Instant.class));
                    conv.setDeletedById(row.get("deleted_by", Long.class));
                    conv.setDeletedAt(row.get("deleted_at", Instant.class));
                    conv.setVersion(row.get("version", Long.class));
                    return conv;
                })
                .all();
    }

    @Override
    public Mono<Void> updateActivity(Long conversationId, Long userId) {
        String sql = "UPDATE conversations SET updated_at = NOW(), updated_by = :userId WHERE id = :id AND deleted_at IS NULL";
        return databaseClient.sql(sql)
                .bind("userId", userId)
                .bind("id", conversationId)
                .then();
    }
}
