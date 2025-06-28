package com.ai.assistant.persistence.relational.repository;

import com.ai.assistant.dto.response.user.UserSummaryDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface ParticipantRepositoryCustom {
    Flux<UserSummaryDTO> findUsersByConversationId(Long conversationId);

    Mono<Instant> findLastReadAt(Long conversationId, Long userId);
}
