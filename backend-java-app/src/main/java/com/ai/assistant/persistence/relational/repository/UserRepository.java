package com.ai.assistant.persistence.relational.repository;

import com.ai.assistant.persistence.relational.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {
    @Query("SELECT COUNT(1) > 0 FROM users WHERE username = :username AND deleted_at IS NULL")
    Mono<Boolean> existsActiveByUsername(String username);

    @Query("SELECT COUNT(1) > 0 FROM users WHERE email = :email AND deleted_at IS NULL")
    Mono<Boolean> existsActiveByEmail(String email);

    @Query("SELECT * FROM users WHERE username = :username AND deleted_at IS NULL LIMIT 1")
    Mono<User> findActiveByUsername(String username);
}
