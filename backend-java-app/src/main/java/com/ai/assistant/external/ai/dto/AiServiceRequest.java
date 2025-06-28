package com.ai.assistant.external.ai.dto;

import lombok.Builder;

import java.util.List;

/** Chat request payload for the Python AI service. */
@Builder
public record AiServiceRequest(
        Long conversationId,
        String prompt,
        List<ChatMessage> chatHistory,
        List<String> projectFiles,
        String correlationId
) {
    /** Single chat message with sender role. */
    public record ChatMessage(String role, String content) {}
}
