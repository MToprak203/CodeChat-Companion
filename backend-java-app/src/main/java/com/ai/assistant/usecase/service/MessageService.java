package com.ai.assistant.usecase.service;

import com.ai.assistant.event.MessageEvent;
import com.ai.assistant.mapper.MessageMapper;
import com.ai.assistant.persistence.relational.entity.Message;
import com.ai.assistant.persistence.relational.repository.MessageRepository;
import com.ai.assistant.persistence.relational.repository.ConversationRepository;
import com.ai.assistant.usecase.service.ParticipantService;
import com.ai.assistant.usecase.resilience.wrapper.ResilienceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import static com.ai.assistant.core.Constants.Resilience.Wrapper.DB_RESILIENCE_WRAPPER;

@Slf4j
@Service
public class MessageService {

    private final MessageRepository repository;
    private final ConversationRepository conversationRepository;
    private final ResilienceWrapper resilience;
    private final MessageMapper messageMapper;
    private final ParticipantService participantService;

    public MessageService(MessageRepository repository,
                          ConversationRepository conversationRepository,
                          @Qualifier(DB_RESILIENCE_WRAPPER) ResilienceWrapper resilience,
                          MessageMapper messageMapper,
                          ParticipantService participantService) {
        this.repository = repository;
        this.conversationRepository = conversationRepository;
        this.resilience = resilience;
        this.messageMapper = messageMapper;
        this.participantService = participantService;
    }

    public Mono<Void> saveMessage(MessageEvent messageEvent) {
        Message message = messageMapper.toEntity(messageEvent);

        log.info("[message:save] Saving message to DB");

        return resilience.wrap(repository.save(message))
                .then(conversationRepository.updateActivity(messageEvent.conversationId(), messageEvent.senderId()))
                .then()
                .doOnSuccess(v -> log.info("[message:save] Message saved"))
                .doOnError(e -> log.error("[message:save] Failed to save message to DB", e));
    }

    public Flux<MessageEvent> fetchMessages(Long conversationId, int page, int size) {
        int offset = page * size;

        return resilience.wrap(
                repository.findByConversationId(conversationId, offset, size)
        ).map(messageMapper::toDto);
    }

    public Mono<Long> countMessages(Long conversationId) {
        return resilience.wrap(repository.countByConversationId(conversationId));
    }

    public Flux<MessageEvent> fetchUnreadMessages(Long conversationId, Long userId) {
        return participantService.getLastReadAt(conversationId, userId)
                .map(last -> last != null ? java.time.LocalDateTime.ofInstant(last, java.time.ZoneOffset.UTC) : null)
                .flatMapMany(last -> resilience.wrap(repository.findUnreadMessages(conversationId, last))
                        .map(messageMapper::toDto)
                        .collectList()
                        .flatMapMany(list -> participantService.markRead(conversationId, userId)
                                .thenMany(Flux.fromIterable(list))));
    }
}
