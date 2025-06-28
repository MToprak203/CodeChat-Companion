package com.ai.assistant.dto.response.conversation;

import com.ai.assistant.enums.ConversationType;

public record ConversationResponseDTO(
        Long id,
        String title,
        ConversationType type,
        Long projectId
) {
}
