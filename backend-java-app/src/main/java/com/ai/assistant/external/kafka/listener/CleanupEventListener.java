package com.ai.assistant.external.kafka.listener;

import com.ai.assistant.dto.response.user.UserSummaryDTO;
import com.ai.assistant.event.ConversationCleanupEvent;
import com.ai.assistant.event.ProjectCleanupEvent;
import com.ai.assistant.persistence.relational.repository.ConversationRepository;
import com.ai.assistant.persistence.relational.repository.ParticipantRepository;
import com.ai.assistant.persistence.relational.repository.ProjectRepository;
import com.ai.assistant.persistence.relational.repository.ProjectParticipantRepository;
import com.ai.assistant.persistence.relational.repository.MessageRepository;
import com.ai.assistant.usecase.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.Consumer;

@Configuration
@Slf4j
public class CleanupEventListener {

    private final ConversationRepository conversationRepository;
    private final ParticipantRepository participantRepository;
    private final ProjectRepository projectRepository;
    private final ProjectParticipantRepository projectParticipantRepository;
    private final ConversationService conversationService;
    private final MessageRepository messageRepository;

    public CleanupEventListener(ConversationRepository conversationRepository,
                                ParticipantRepository participantRepository,
                                ProjectRepository projectRepository,
                                ProjectParticipantRepository projectParticipantRepository,
                                ConversationService conversationService,
                                MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.projectRepository = projectRepository;
        this.projectParticipantRepository = projectParticipantRepository;
        this.conversationService = conversationService;
        this.messageRepository = messageRepository;
    }

    @Bean
    public Consumer<List<ConversationCleanupEvent>> conversationCleanup() {
        return events -> Flux.fromIterable(events)
                .flatMap(event -> participantRepository.findUsersByConversationId(event.conversationId())
                        .map(UserSummaryDTO::id)
                        .collectList()
                        .flatMap(users -> conversationRepository.softDelete(event.conversationId(), event.userId())
                                .then(messageRepository.softDeleteByConversationId(event.conversationId(), event.userId()))
                                .then())
                        .doOnError(e -> log.error("[kafka:cleanup] conversation {} failed", event.conversationId(), e)))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    @Bean
    public Consumer<List<ProjectCleanupEvent>> projectCleanup() {
        return events -> Flux.fromIterable(events)
                .flatMap(event -> conversationRepository.findAllByProjectId(event.projectId())
                        .flatMap(conv -> conversationService.leaveConversation(conv.getId(), event.userId()))
                        .then(projectParticipantRepository.countByProjectId(event.projectId()))
                        .flatMap(count -> {
                            if (count == 0) {
                                return projectRepository.softDelete(event.projectId(), event.userId());
                            }
                            return Mono.empty();
                        })
                        .doOnError(e -> log.error("[kafka:cleanup] project {} failed", event.projectId(), e)))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }
}
