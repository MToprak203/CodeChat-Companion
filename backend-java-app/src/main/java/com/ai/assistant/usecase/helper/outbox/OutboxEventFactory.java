package com.ai.assistant.usecase.helper.outbox;

import com.ai.assistant.core.error.ApplicationException;
import com.ai.assistant.core.error.ErrorCode;
import com.ai.assistant.enums.OutboxStatus;
import com.ai.assistant.event.DomainEvent;
import com.ai.assistant.event.ProjectUploadedEvent;
import com.ai.assistant.event.ConversationCleanupEvent;
import com.ai.assistant.event.ProjectCleanupEvent;
import com.ai.assistant.persistence.relational.entity.OutboxEvent;
import com.ai.assistant.enums.AggregateType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventFactory {

    private final ObjectMapper objectMapper;

    public OutboxEvent buildFrom(DomainEvent event) {
        return OutboxEvent.builder()
                .aggregateId(event.getAggregateId())
                .aggregateType(resolveAggregateType(event))
                .eventType(event.getEventType())
                .status(OutboxStatus.PENDING)
                .payload(toJson(event))
                .build();
    }

    private AggregateType resolveAggregateType(DomainEvent event) {
        if (event instanceof ConversationCleanupEvent) return AggregateType.CONVERSATION;
        if (event instanceof ProjectCleanupEvent) return AggregateType.PROJECT;
        if (event instanceof ProjectUploadedEvent) return AggregateType.PROJECT;
        throw new IllegalArgumentException("Unknown event type: " + event.getClass());
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new ApplicationException(ErrorCode.JSON_PROCESSING_EXCEPTION, "payload: " + payload, e);
        }
    }
}

