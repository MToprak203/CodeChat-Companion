package com.ai.assistant.persistence.relational.repository;

import com.ai.assistant.persistence.relational.entity.OutboxEvent;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static com.ai.assistant.core.Constants.Outbox.OUTBOX_BATCH_SIZE;

@Repository
public interface OutboxEventRepository extends R2dbcRepository<OutboxEvent, UUID>, OutboxEventRepositoryCustom {

    @Query("SELECT * FROM outbox_event WHERE status = 'PENDING' ORDER BY created_at ASC LIMIT " + OUTBOX_BATCH_SIZE)
    Flux<OutboxEvent> findPendingEvents();
}
