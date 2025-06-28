package com.ai.assistant.security.context;

import com.ai.assistant.persistence.relational.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepo;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepo.findActiveByUsername(username)
            .map(user -> {
                Stream<SimpleGrantedAuthority> roleAuths = user.getRoles().stream()
                    .map(r -> new SimpleGrantedAuthority(r.toString()));
                Stream<SimpleGrantedAuthority> scopeAuths = user.getScopes().stream()
                    .map(s -> new SimpleGrantedAuthority(s.toString()));
                List<SimpleGrantedAuthority> allAuths = Stream.concat(roleAuths, scopeAuths).collect(Collectors.toList());

                return new SecurityUser(
                    user.getId(),
                    user.getUsername(),
                    user.getPassword(),
                    allAuths,
                    user.isEnabled()
                );
            });
    }
}
