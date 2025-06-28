package com.ai.assistant.presentation.controller;

import com.ai.assistant.core.dto.ApiResponse;
import com.ai.assistant.dto.request.auth.LoginRequestDTO;
import com.ai.assistant.dto.request.auth.RegistrationRequestDTO;
import com.ai.assistant.dto.response.auth.LoginResponseDTO;
import com.ai.assistant.dto.response.auth.RegistrationResponseDTO;
import com.ai.assistant.usecase.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ai.assistant.security.annotation.CurrentUser;
import reactor.core.publisher.Mono;

import static com.ai.assistant.core.Constants.Path.Auth.AUTH_V1;
import static com.ai.assistant.core.Constants.Path.Auth.Method.LOGIN;
import static com.ai.assistant.core.Constants.Path.Auth.Method.REGISTER;
import static com.ai.assistant.core.Constants.Path.Auth.Method.LOGOUT;


@RestController
@RequestMapping(AUTH_V1)
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping(value = LOGIN,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ApiResponse<LoginResponseDTO>>> login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.validateAndLogin(request).map(r -> ResponseEntity.ok(ApiResponse.success(r)));
    }

    @PostMapping(
            value = REGISTER,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<ApiResponse<RegistrationResponseDTO>>> register(@Valid @RequestBody RegistrationRequestDTO request) {
        return authService.register(request).map(r -> new ResponseEntity<>(ApiResponse.success(r), HttpStatus.CREATED));
    }

    @PostMapping(value = LOGOUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ApiResponse<Void>>> logout(@CurrentUser Long userId) {
        return authService.logout(userId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null)));
    }
}
