package com.ai.assistant.dto.response.user;

public record UserSummaryDTO(
        Long id,
        String username,
        boolean online
) {}
