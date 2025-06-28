package com.ai.assistant.usecase.service;

import com.ai.assistant.core.error.ApplicationException;
import com.ai.assistant.core.error.ErrorCode;
import com.ai.assistant.dto.response.conversation.ConversationResponseDTO;
import com.ai.assistant.event.ConversationCleanupEvent;
import com.ai.assistant.core.dbconnection.ReadOnly;
import com.ai.assistant.mapper.ConversationMapper;
import com.ai.assistant.persistence.relational.entity.Conversation;
import com.ai.assistant.persistence.relational.repository.ConversationRepository;
import com.ai.assistant.persistence.relational.repository.MessageRepository;
import com.ai.assistant.usecase.resilience.wrapper.ResilienceWrapper;
import com.ai.assistant.enums.ConversationType;
import com.ai.assistant.usecase.service.SystemNotificationService;
import com.ai.assistant.usecase.service.OutboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.ai.assistant.core.Constants.Resilience.Wrapper.DB_RESILIENCE_WRAPPER;

@Slf4j
@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final ConversationMapper conversationMapper;
    private final ParticipantService participantService;
    private final OutboxService outboxService;
    private final SystemNotificationService systemNotificationService;
    private final TransactionalOperator tx;
    private final ResilienceWrapper resilience;
    private final NotificationService notificationService;
    private final MessageRepository messageRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               ConversationMapper conversationMapper,
                               ParticipantService participantService,
                               SystemNotificationService systemNotificationService,
                               OutboxService outboxService,
                               TransactionalOperator tx,
                               @Qualifier(DB_RESILIENCE_WRAPPER) ResilienceWrapper resilience,
                               NotificationService notificationService,
                               MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.conversationMapper = conversationMapper;
        this.participantService = participantService;
        this.systemNotificationService = systemNotificationService;
        this.outboxService = outboxService;
        this.tx = tx;
        this.resilience = resilience;
        this.notificationService = notificationService;
        this.messageRepository = messageRepository;
    }

    public Mono<ConversationResponseDTO> createConversationWithAI(Long userId) {
        return createConversation(userId, true, null, null);
    }

    public Mono<ConversationResponseDTO> createGroupConversation(Long userId, Set<Long> participantIds) {
        return createConversation(userId, false, participantIds, null);
    }

    public Mono<ConversationResponseDTO> createGroupConversationWithAI(Long userId, Set<Long> participantIds) {
        return createConversation(userId, true, participantIds, null);
    }

    public Mono<ConversationResponseDTO> createDirectConversation(Long userId, Long participantId) {
        return createConversation(userId, false, Set.of(participantId), null);
    }

    public Mono<ConversationResponseDTO> createProjectConversation(Long userId, Long projectId) {
        return createConversation(userId, true, null, projectId);
    }

    @ReadOnly
    public Flux<ConversationResponseDTO> findProjectConversations(Long projectId) {
        return resilience.wrap(conversationRepository.findAllByProjectId(projectId))
                .map(conversationMapper::toResponseDTO);
    }

    @ReadOnly
    public Flux<ConversationResponseDTO> findProjectConversations(Long projectId, Long userId) {
        return resilience.wrap(conversationRepository.findByProjectIdAndUserId(projectId, userId))
                .map(conversationMapper::toResponseDTO);
    }

    @ReadOnly
    public Flux<ConversationResponseDTO> fetchConversations(Long userId, int page, int size) {
        long offset = (long) page * size;
        return resilience.wrap(
                conversationRepository.findByUserId(userId, offset, size)
        ).map(conversationMapper::toResponseDTO);
    }

    @ReadOnly
    public Flux<ConversationResponseDTO> fetchNoProjectConversations(Long userId, int page, int size) {
        long offset = (long) page * size;
        return resilience.wrap(
                conversationRepository.findNoProjectByUserId(userId, offset, size)
        ).map(conversationMapper::toResponseDTO);
    }

    @ReadOnly
    public Mono<Long> countConversations(Long userId) {
        return resilience.wrap(conversationRepository.countByUserId(userId));
    }

    @ReadOnly
    public Mono<Long> countNoProjectConversations(Long userId) {
        return resilience.wrap(conversationRepository.countNoProjectByUserId(userId));
    }

    public Mono<ConversationResponseDTO> updateTitle(Long conversationId, String title) {
        return resilience.wrap(conversationRepository.findById(conversationId))
                .switchIfEmpty(Mono.error(new ApplicationException(
                        ErrorCode.NOT_FOUND,
                        "Conversation not found: " + conversationId
                )))
                .flatMap(conv -> {
                    conv.setTitle(title);
                    return conversationRepository.save(conv);
                })
                .map(conversationMapper::toResponseDTO);
    }

    public Mono<Void> leaveConversation(Long conversationId, Long userId) {
        return participantService.removeParticipant(conversationId, userId)
                .then(participantService.countParticipants(conversationId))
                .flatMap(count -> {
                    if (count == 0) {
                        Long deleter = userId == null ? 0L : userId;
                        return conversationRepository.softDelete(conversationId, deleter)
                                .then(messageRepository.softDeleteByConversationId(conversationId, deleter))
                                .then(outboxService.dispatchEvent(
                                        new ConversationCleanupEvent(conversationId, deleter, LocalDateTime.now())
                                ));
                    }
                    return Mono.empty();
                });
    }

    private Mono<ConversationResponseDTO> createConversation(Long userId, boolean isAI, Set<Long> participantIds, Long projectId) {
        Set<Long> participants = participantIds == null ? java.util.Set.of() : participantIds;

        log.debug("[conversation:create] Starting conversation creation. userId={}, includeAi={}, otherParticipants={}",
                userId, isAI, participants.size());

        Conversation conversation = new Conversation();
        ConversationType type = participants.size() > 1 ? ConversationType.GROUP : ConversationType.PRIVATE;
        conversation.setType(type);
        conversation.setProjectId(projectId);
        return resilience.wrap(conversationRepository.save(conversation))
                .doOnSuccess(conv -> log.debug("[conversation:create] Conversation saved with ID {}", conv.getId()))
                .flatMap(conv -> findConversationById(conv.getId())
                        .flatMap(savedConv -> {
                            log.debug("[conversation:create] Verifying saved conversation: {}", savedConv.getId());
                            Mono<Void> participantSave = participantService.saveParticipants(
                                    savedConv.getId(), userId, participants
                            );

                            // Previously an AI response was automatically triggered
                            // when a conversation included the AI participant.
                            // This behaviour has been removed so the AI only
                            // responds when explicitly messaged by the user.

                            return participantSave.thenReturn(savedConv);
                        }))
                .map(conversationMapper::toResponseDTO)
                .as(tx::transactional)
                .doOnSuccess(conv -> log.debug("[conversation:create] Conversation creation complete. conversationId={}", conv.id()))
                .doOnError(e -> log.error("[conversation:create] Conversation creation failed. userId={}, includeAi={}, error={}",
                        userId, isAI, e.getMessage(), e));
    }

    private Mono<Conversation> findConversationById(Long id) {
        return resilience.wrap(conversationRepository.findById(id))
                .switchIfEmpty(Mono.error(() -> new ApplicationException(
                        ErrorCode.NOT_FOUND,
                        "[conversation:create] Saved conversation not found with ID: " + id)
                ))
                .doOnSubscribe(s -> log.debug("[conversation:lookup] Looking up conversation ID: {}", id));
    }
}

