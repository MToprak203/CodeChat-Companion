package com.ai.assistant.usecase.service;

import com.ai.assistant.core.dbconnection.ReadOnly;
import com.ai.assistant.persistence.relational.entity.ProjectParticipant;
import com.ai.assistant.persistence.relational.repository.ProjectParticipantRepository;
import com.ai.assistant.usecase.resilience.wrapper.ResilienceWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.ai.assistant.core.Constants.Resilience.Wrapper.DB_RESILIENCE_WRAPPER;

@Slf4j
@Service
public class ProjectParticipantService {

    private final ProjectParticipantRepository repository;
    private final ResilienceWrapper resilience;

    public ProjectParticipantService(ProjectParticipantRepository repository,
                                     @Qualifier(DB_RESILIENCE_WRAPPER) ResilienceWrapper resilience) {
        this.repository = repository;
        this.resilience = resilience;
    }

    public Mono<Void> addParticipant(Long projectId, Long userId) {
        return resilience.wrap(repository.existsByProjectIdAndUserId(projectId, userId))
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) return Mono.empty();
                    ProjectParticipant p = ProjectParticipant.builder()
                            .projectId(projectId)
                            .userId(userId)
                            .build();
                    return repository.save(p).then();
                })
                .doOnError(e -> log.error("[projectParticipant:add] Failed to add participant projectId={} userId={}", projectId, userId, e));
    }

    public Mono<Void> removeParticipant(Long projectId, Long userId) {
        return resilience.wrap(repository.deleteByProjectIdAndUserId(projectId, userId))
                .doOnError(e -> log.error("[projectParticipant:remove] Failed to remove participant projectId={} userId={}", projectId, userId, e));
    }

    @ReadOnly
    public Mono<Long> countParticipants(Long projectId) {
        return resilience.wrap(repository.countByProjectId(projectId));
    }
}
