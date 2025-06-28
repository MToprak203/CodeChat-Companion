package com.ai.assistant.persistence.relational.repository;

import com.ai.assistant.dto.response.user.UserSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ParticipantRepositoryImpl implements ParticipantRepositoryCustom {
    private final DatabaseClient databaseClient;

    @Override
    public Flux<UserSummaryDTO> findUsersByConversationId(Long conversationId) {
        String sql = """
                SELECT p.user_id, u.username
                FROM conversation_participants p
                LEFT JOIN users u ON p.user_id = u.id
                WHERE p.conversation_id = :conversationId
                  AND p.deleted_at IS NULL
                  AND u.deleted_at IS NULL
                UNION
                SELECT pp.user_id, u.username
                FROM conversations c
                JOIN project_participants pp ON c.project_id = pp.project_id
                LEFT JOIN users u ON pp.user_id = u.id
                WHERE c.id = :conversationId
                  AND pp.deleted_at IS NULL
                  AND u.deleted_at IS NULL
                """;
        return databaseClient.sql(sql)
                .bind("conversationId", conversationId)
                .map((row, meta) -> new UserSummaryDTO(
                        row.get("user_id", Long.class),
                        row.get("username", String.class),
                        false
                ))
                .all();
    }

    @Override
    public Mono<Instant> findLastReadAt(Long conversationId, Long userId) {
        String sql = """
                SELECT last_read_at
                FROM conversation_participants
                WHERE conversation_id = :conversationId
                  AND user_id = :userId
                  AND deleted_at IS NULL
                """;
        return databaseClient.sql(sql)
                .bind("conversationId", conversationId)
                .bind("userId", userId)
                .map((row, meta) -> Optional.ofNullable(row.get("last_read_at", Instant.class)))
                .one()
                .flatMap(Mono::justOrEmpty);
    }
}
