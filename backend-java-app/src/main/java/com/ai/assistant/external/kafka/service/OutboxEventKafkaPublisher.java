package com.ai.assistant.external.kafka.service;

import com.ai.assistant.persistence.relational.entity.OutboxEvent;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OutboxEventKafkaPublisher {
    Mono<Void> publish(List<OutboxEvent> events);
}
