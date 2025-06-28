package com.ai.assistant.usecase.service;

import com.ai.assistant.dto.request.auth.LoginRequestDTO;
import com.ai.assistant.dto.request.auth.RegistrationRequestDTO;
import com.ai.assistant.dto.response.auth.LoginResponseDTO;
import com.ai.assistant.dto.response.auth.RegistrationResponseDTO;
import com.ai.assistant.persistence.relational.entity.User;
import com.ai.assistant.enums.Role;
import com.ai.assistant.enums.Scope;
import com.ai.assistant.persistence.relational.repository.UserRepository;
import com.ai.assistant.security.context.SecurityUser;
import com.ai.assistant.security.jwt.JwtUtils;
import com.ai.assistant.usecase.validation.AuthValidators;
import lombok.RequiredArgsConstructor;
import com.ai.assistant.external.redis.service.RedisOnlineUserService;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ReactiveAuthenticationManager authManager;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final RedisOnlineUserService onlineService;

    /* ---------- LOGIN ---------- */

    public Mono<LoginResponseDTO> validateAndLogin(LoginRequestDTO req) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                req.username(), req.password());

        return authManager.authenticate(authToken)
                .map(auth -> (SecurityUser) auth.getPrincipal())
                .flatMap(user -> onlineService.addOnlineUser(user.getUserId())
                        .thenReturn(buildLoginResponse(user)));
    }

    public Mono<Void> logout(Long userId) {
        return onlineService.removeOnlineUser(userId);
    }

    private LoginResponseDTO buildLoginResponse(SecurityUser user) {
        String token = jwtUtils.generateToken(user);
        return new LoginResponseDTO(token, user.getUserId(), user.getUsername());
    }

    /* ---------- REGISTER ---------- */

    public Mono<RegistrationResponseDTO> register(RegistrationRequestDTO dto) {
        return AuthValidators.usernameNotTaken(userRepository)
                .then(AuthValidators.emailNotTaken(userRepository))
                .validate(dto)
                .flatMap(this::saveUser)
                .map(this::buildRegistrationResponse);
    }

    private Mono<User> saveUser(RegistrationRequestDTO dto) {
        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRoles(Set.of(Role.ROLE_USER));
        user.setScopes(Set.of(Scope.SCOPE_WRITE, Scope.SCOPE_READ));
        return userRepository.save(user);
    }

    private RegistrationResponseDTO buildRegistrationResponse(User u) {
        List<SimpleGrantedAuthority> authorities = Stream.concat(
                u.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.toString())),
                u.getScopes().stream()
                        .map(s -> new SimpleGrantedAuthority(s.toString()))
        ).collect(Collectors.toList());

        SecurityUser secUser = new SecurityUser(
                u.getId(),
                u.getUsername(),
                authorities,
                u.isEnabled()
        );

        String token = jwtUtils.generateToken(secUser);
        return new RegistrationResponseDTO(token, u.getId(), u.getUsername());
    }
}
