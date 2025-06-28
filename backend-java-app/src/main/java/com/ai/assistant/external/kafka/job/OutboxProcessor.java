package com.ai.assistant.external.kafka.job;

import com.ai.assistant.external.kafka.service.OutboxEventKafkaPublisher;
import com.ai.assistant.persistence.relational.entity.OutboxEvent;
import com.ai.assistant.usecase.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxProcessor {
    private final OutboxService outboxService;
    private final OutboxEventKafkaPublisher kafkaPublisher;

    @Scheduled(fixedDelay = 2000)
    public void processOutboxEvents() {
        outboxService.findPendingEvents()
                .flatMap(batch ->
                        kafkaPublisher.publish(batch)
                                .doOnSuccess(v -> log.info("[outbox:publish] Successfully published {} events to Kafka", batch.size()))
                                .then(outboxService.markEventsAsPublished(batch))
                                .doOnSuccess(v -> log.info("[outbox:db] Successfully marked {} events as PUBLISHED in DB", batch.size()))
                )
                .onErrorContinue((e, o) -> {
                    if (o instanceof List<?> list && !list.isEmpty() && list.getFirst() instanceof OutboxEvent event) {
                        log.error("[outbox:error] Failed to process batch starting with eventId={}. Retrying later...", event.getId(), e);
                    } else {
                        log.error("[outbox:error] Failed to process unknown batch. Retrying later...", e);
                    }
                })
                .subscribe();
    }
}
