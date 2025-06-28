package com.ai.assistant.usecase.service;

import com.ai.assistant.dto.response.user.UserSummaryDTO;
import com.ai.assistant.persistence.relational.entity.Participant;
import com.ai.assistant.persistence.relational.repository.ParticipantRepository;
import com.ai.assistant.persistence.relational.repository.ConversationRepository;
import com.ai.assistant.persistence.relational.repository.ProjectParticipantRepository;
import com.ai.assistant.usecase.resilience.wrapper.ResilienceWrapper;
import com.ai.assistant.external.redis.service.RedisOnlineUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.ai.assistant.core.dbconnection.ReadOnly;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ai.assistant.core.Constants.Resilience.Wrapper.DB_RESILIENCE_WRAPPER;

@Slf4j
@Service
public class ParticipantService {

    private final ParticipantRepository repository;
    private final ResilienceWrapper resilience;
    private final RedisOnlineUserService onlineService;
    private final ConversationRepository conversationRepository;
    private final ProjectParticipantRepository projectParticipantRepository;

    public ParticipantService(ParticipantRepository repository,
                              @Qualifier(DB_RESILIENCE_WRAPPER) ResilienceWrapper resilience,
                              RedisOnlineUserService onlineService,
                              ConversationRepository conversationRepository,
                              ProjectParticipantRepository projectParticipantRepository) {
        this.repository = repository;
        this.resilience = resilience;
        this.onlineService = onlineService;
        this.conversationRepository = conversationRepository;
        this.projectParticipantRepository = projectParticipantRepository;
    }

    public Mono<Void> saveParticipants(Long conversationId,
                                       Long creatorId,
                                       Set<Long> participantIds) {
        return buildParticipants(conversationId, creatorId, participantIds)
                .buffer(100)
                .flatMap(batch -> resilience.wrap(repository.saveAll(batch)))
                .then()
                .doOnError(e -> log.error("[participant:save:error] Failed to save participants", e));
    }

    @ReadOnly
    public Flux<UserSummaryDTO> fetchParticipants(Long conversationId) {
        return resilience.wrap(repository.findUsersByConversationId(conversationId))
                .flatMap(dto -> onlineService.isOnline(dto.id())
                        .map(online -> new UserSummaryDTO(dto.id(), dto.username(), online)))
                .doOnSubscribe(s -> log.debug("[participant:list] Fetching participants for conversation {}", conversationId))
                .doOnError(e -> log.error("[participant:list] Failed to fetch participants for conversation {}", conversationId, e));
    }

    public Mono<Void> addParticipant(Long conversationId, Long userId) {
        return resilience.wrap(repository.existsByConversationIdAndUserId(conversationId, userId))
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) return Mono.empty();
                    Participant p = Participant.builder()
                            .conversationId(conversationId)
                            .userId(userId)
                            .build();
                    return repository.save(p).then();
                })
                .doOnError(e -> log.error("[participant:add] Failed to add participant conversationId={} userId={}", conversationId, userId, e));
    }

    public Mono<Void> removeParticipant(Long conversationId, Long userId) {
        return resilience.wrap(repository.deleteByConversationIdAndUserId(conversationId, userId))
                .doOnError(e -> log.error("[participant:remove] Failed to remove participant conversationId={} userId={}", conversationId, userId, e));
    }

    public Mono<Void> markRead(Long conversationId, Long userId) {
        return resilience.wrap(repository.updateLastReadAt(conversationId, userId))
                .doOnError(e -> log.error("[participant:markRead] Failed to update last read time conversationId={} userId={}", conversationId, userId, e));
    }

    @ReadOnly
    public Mono<java.time.Instant> getLastReadAt(Long conversationId, Long userId) {
        return resilience.wrap(repository.findLastReadAt(conversationId, userId));
    }

    @ReadOnly
    public Mono<Boolean> isParticipant(Long conversationId, Long userId) {
        return resilience.wrap(repository.existsByConversationIdAndUserId(conversationId, userId))
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.just(true);
                    }

                    return conversationRepository.findById(conversationId)
                            .flatMap(conv -> {
                                Long projectId = conv.getProjectId();
                                if (projectId == null) {
                                    return Mono.just(false);
                                }
                                return projectParticipantRepository.existsByProjectIdAndUserId(projectId, userId);
                            });
                });
    }

    @ReadOnly
    public Mono<Long> countParticipants(Long conversationId) {
        return resilience.wrap(repository.countByConversationId(conversationId));
    }

    private Flux<Participant> buildParticipants(Long conversationId,
                                               Long creatorId,
                                               Set<Long> participantIds) {
        Set<Long> userIds = new HashSet<>();

        userIds.add(creatorId);

        if (participantIds != null) {
            userIds.addAll(participantIds);
        }

        Set<Participant> participants = userIds.stream()
                .map(userId -> Participant.builder()
                        .conversationId(conversationId)
                        .userId(userId)
                        .build())
                .collect(Collectors.toSet());

        log.debug("[participant:save:init] Preparing participants. conversationId={}, total={}",
                conversationId, participants.size());

        return Flux.fromIterable(participants);
    }

}
