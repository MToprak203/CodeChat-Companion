package com.ai.assistant.persistence.relational.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class OutboxEventRepositoryCustomImpl implements OutboxEventRepositoryCustom {

    private final R2dbcEntityTemplate template;

    private static final String SQL = """
            UPDATE outbox_event
            SET status = 'PUBLISHED',
            published_at = $1
            WHERE id = ANY($2::uuid[])
            """;

    @Override
    public Mono<Void> markAsPublished(List<UUID> ids) {
        return template.getDatabaseClient()
                .sql(SQL)
                .bind("$1", Instant.now())
                .bind("$2", ids.toArray(new UUID[0]))
                .fetch()
                .rowsUpdated()
                .doOnSuccess(count ->
                        log.info("[outbox:markPublished] Marked {} events as PUBLISHED", count)
                )
                .doOnError(e ->
                        log.error("[outbox:markPublished] Failed to mark events. ids={}", ids, e)
                )
                .then();
    }
}

