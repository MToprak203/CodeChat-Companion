package com.ai.assistant.dto.response.auth;

public record RegistrationResponseDTO(
        String token,
        Long userId,
        String username
) {
}
