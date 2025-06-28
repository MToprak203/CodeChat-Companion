package com.ai.assistant.dto.response.auth;

public record LoginResponseDTO(
        String token,
        Long userId,
        String username
) {
}
