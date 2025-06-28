package com.ai.assistant.persistence.relational.repository;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepositoryCustom {
    Mono<Void> markAsPublished(List<UUID> ids);
}