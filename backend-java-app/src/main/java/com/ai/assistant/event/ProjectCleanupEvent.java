package com.ai.assistant.event;

import com.ai.assistant.enums.OutboxEventType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectCleanupEvent(
        Long projectId,
        Long userId,
        LocalDateTime occurredAt
) implements DomainEvent {

    @Override
    public UUID getAggregateId() {
        return UUID.nameUUIDFromBytes(("project:" + projectId).getBytes());
    }

    @Override
    public OutboxEventType getEventType() {
        return OutboxEventType.PROJECT_CLEANUP;
    }

    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
