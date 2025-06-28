package com.ai.assistant.dto.request.conversation;

import jakarta.validation.constraints.NotBlank;

public record ConversationUpdateRequestDTO(@NotBlank String title) {}
