package com.ai.assistant.event;

import com.ai.assistant.enums.OutboxEventType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversationCleanupEvent(
        Long conversationId,
        Long userId,
        LocalDateTime occurredAt
) implements DomainEvent {

    @Override
    public UUID getAggregateId() {
        return UUID.nameUUIDFromBytes(("conversation:" + conversationId).getBytes());
    }

    @Override
    public OutboxEventType getEventType() {
        return OutboxEventType.CONVERSATION_CLEANUP;
    }

    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
