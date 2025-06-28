package com.ai.assistant.event;

import com.ai.assistant.enums.OutboxEventType;
import com.ai.assistant.event.DomainEvent;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectUploadedEvent(Long projectId, String name, String tempDir, LocalDateTime occurredAt)
        implements DomainEvent {

    @Override
    public UUID getAggregateId() {
        return UUID.nameUUIDFromBytes(("project:" + projectId).getBytes());
    }

    @Override
    public OutboxEventType getEventType() {
        return OutboxEventType.PROJECT_UPLOADED;
    }

    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
