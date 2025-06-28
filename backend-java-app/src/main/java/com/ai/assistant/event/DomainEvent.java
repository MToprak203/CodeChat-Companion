package com.ai.assistant.event;

import com.ai.assistant.enums.OutboxEventType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainEvent {
    UUID getAggregateId();
    OutboxEventType getEventType();
    LocalDateTime getOccurredAt();
}
