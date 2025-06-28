package com.ai.assistant.dto.request.auth;

import com.ai.assistant.core.validation.FieldMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@FieldMatch(first = "password", second = "confirmPassword", message = "Passwords do not match")
public record RegistrationRequestDTO(
        @Size(min = 4, max = 30)
        String username,

        @NotBlank
        @Email
        String email,

        @Size(min = 8, max = 50)
        String password,

        @Size(min = 8, max = 50)
        String confirmPassword
) {
}
