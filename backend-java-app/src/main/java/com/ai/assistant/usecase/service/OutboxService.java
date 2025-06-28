package com.ai.assistant.usecase.service;

import com.ai.assistant.core.dbconnection.ReadOnly;
import com.ai.assistant.event.DomainEvent;
import com.ai.assistant.persistence.relational.entity.OutboxEvent;
import com.ai.assistant.enums.OutboxStatus;
import com.ai.assistant.persistence.relational.helper.QueryBuilder;
import com.ai.assistant.persistence.relational.helper.SimpleQueryBuilder;
import com.ai.assistant.persistence.relational.repository.OutboxEventRepository;
import com.ai.assistant.usecase.helper.outbox.OutboxEventFactory;
import com.ai.assistant.usecase.resilience.wrapper.ResilienceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.ai.assistant.core.Constants.Outbox.OUTBOX_BATCH_SIZE;
import static com.ai.assistant.core.Constants.Resilience.Wrapper.DB_RESILIENCE_WRAPPER;

@Slf4j
@Service
public class OutboxService {

    private final OutboxEventRepository repository;
    private final OutboxEventFactory eventFactory;
    private final ResilienceWrapper resilience;

    public OutboxService(OutboxEventRepository repository,
                         OutboxEventFactory eventFactory,
                         @Qualifier(DB_RESILIENCE_WRAPPER) ResilienceWrapper resilience) {
        this.repository = repository;
        this.eventFactory = eventFactory;
        this.resilience = resilience;
    }

    public Mono<Void> dispatchEvent(DomainEvent event) {
        OutboxEvent outbox = eventFactory.buildFrom(event);
        log.debug("[outbox:dispatch] Saving event: {}", event);
        return resilience.wrap(repository.save(outbox))
                .doOnSuccess(saved -> log.info("[outbox:dispatch] Event saved. type={}, id={}, aggregateId={}", event.getEventType(), saved.getId(), saved.getAggregateId()))
                .doOnError(e -> log.error("[outbox:dispatch] Failed to save event: {}", event, e))
                .then();
    }

    @ReadOnly
    public Flux<List<OutboxEvent>> findPendingEvents() {
        return resilience.wrap(repository.findPendingEvents().buffer(OUTBOX_BATCH_SIZE));
    }

    public Mono<Void> markEventsAsPublished(List<OutboxEvent> outboxEvents) {
        if (outboxEvents == null || outboxEvents.isEmpty()) {
            return Mono.empty();
        }

        List<UUID> ids = outboxEvents.stream().map(OutboxEvent::getId).toList();

        return resilience.wrap(repository.markAsPublished(ids))
                .doOnSuccess(updatedCount -> log.info("[outbox:markPublished] Marked {} events as PUBLISHED", updatedCount))
                .doOnError(e -> log.error("[outbox:markPublished] Failed to mark events as PUBLISHED. ids={}", ids, e))
                .then();
    }
}
