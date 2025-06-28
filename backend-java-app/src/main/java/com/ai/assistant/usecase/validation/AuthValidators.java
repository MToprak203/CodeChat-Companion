package com.ai.assistant.usecase.validation;

import com.ai.assistant.core.error.ApplicationException;
import com.ai.assistant.core.error.ErrorCode;
import com.ai.assistant.core.validation.ReactiveValidator;
import com.ai.assistant.dto.request.auth.RegistrationRequestDTO;
import com.ai.assistant.persistence.relational.repository.UserRepository;
import reactor.core.publisher.Mono;

public class AuthValidators {

    public static ReactiveValidator<RegistrationRequestDTO> usernameNotTaken(UserRepository userRepository) {
        return request -> userRepository.existsActiveByUsername(request.username())
                .flatMap(exists -> exists
                        ? Mono.error(new ApplicationException(ErrorCode.USERNAME_ALREADY_EXISTS))
                        : Mono.just(request)
                );
    }

    public static ReactiveValidator<RegistrationRequestDTO> emailNotTaken(UserRepository userRepository) {
        return request -> userRepository.existsActiveByEmail(request.email())
                .flatMap(exists -> exists
                        ? Mono.error(new ApplicationException(ErrorCode.EMAIL_ALREADY_EXISTS))
                        : Mono.just(request)
                );
    }
}