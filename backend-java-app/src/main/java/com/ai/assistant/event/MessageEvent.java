package com.ai.assistant.event;

import com.ai.assistant.enums.MessageType;
import com.ai.assistant.enums.RecipientType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record MessageEvent(
        UUID messageId,
        Long conversationId,
        Long senderId,
        String text,
        MessageType type,
        String replyToMessageId,
        RecipientType recipient,
        @Getter LocalDateTime occurredAt
) {

    public MessageEvent {
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
        if (recipient == null) {
            recipient = RecipientType.USERS;
        }
    }

}
