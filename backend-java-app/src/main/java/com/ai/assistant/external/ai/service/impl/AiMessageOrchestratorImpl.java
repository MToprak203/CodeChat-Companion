package com.ai.assistant.external.ai.service.impl;

import com.ai.assistant.context.WebSocketContext;
import com.ai.assistant.enums.MessageType;
import com.ai.assistant.enums.RecipientType;
import com.ai.assistant.enums.WebSocketChannelType;
import com.ai.assistant.event.MessageEvent;
import com.ai.assistant.external.ai.service.AiMessageOrchestrator;
import com.ai.assistant.external.ai.service.AiStreamingService;
import com.ai.assistant.external.websocket.dispatcher.WebSocketSinkDispatcher;
import org.springframework.context.annotation.Lazy;
import com.ai.assistant.usecase.resilience.wrapper.ResilienceWrapper;
import com.ai.assistant.usecase.service.MessageService;
import com.ai.assistant.usecase.service.NotificationService;
import com.ai.assistant.usecase.service.SystemNotificationService;
import com.ai.assistant.persistence.relational.repository.ConversationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ai.assistant.core.Constants.AI.AI_FALLBACK_MESSAGE;
import static com.ai.assistant.core.Constants.AI.AI_USER_ID;
import static com.ai.assistant.core.Constants.Resilience.Wrapper.AI_SERVICE_RESILIENCE_WRAPPER;

@Slf4j
@Service
public class AiMessageOrchestratorImpl implements AiMessageOrchestrator {

    private static final String END_TOKEN = "[DONE]";

    private final AiStreamingService aiStreamingService;
    private final WebSocketSinkDispatcher wsDispatcher;
    private final ResilienceWrapper resilience;
    private final MessageService messageService;
    private final NotificationService notificationService;
    private final SystemNotificationService systemNotificationService;
    private final ConversationRepository conversationRepository;

    public AiMessageOrchestratorImpl(AiStreamingService aiStreamingService,
                                     @Lazy WebSocketSinkDispatcher wsDispatcher,
                                     @Qualifier(AI_SERVICE_RESILIENCE_WRAPPER) ResilienceWrapper resilience,
                                     MessageService messageService,
                                     NotificationService notificationService,
                                     SystemNotificationService systemNotificationService,
                                     ConversationRepository conversationRepository) {
        this.aiStreamingService = aiStreamingService;
        this.wsDispatcher = wsDispatcher;
        this.resilience = resilience;
        this.messageService = messageService;
        this.notificationService = notificationService;
        this.systemNotificationService = systemNotificationService;
        this.conversationRepository = conversationRepository;
    }

    @Override
    public Mono<Void> handleAiResponse(Long conversationId) {
        log.debug("[ai:response:start] Starting AI response stream for conversationId={}", conversationId);

        Mono<String> tokenMono = ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .ofType(JwtAuthenticationToken.class)
                .map(auth -> auth.getToken().getTokenValue())
                .defaultIfEmpty("");

        return tokenMono.flatMap(initialToken ->
                conversationRepository.findByIdAndDeletedAtIsNull(conversationId)
                .flatMap(conv -> {
                    WebSocketContext wsContext = new WebSocketContext(AI_USER_ID, Map.of("conversationId", String.valueOf(conversationId)));

                    return wsDispatcher.isReady(WebSocketChannelType.AI_TOKEN_STREAM, wsContext)
                            .flatMap(isReady -> {
                                if (!isReady) {
                                    log.warn("[ai:response:abort] WebSocket not ready for conversationId={}", conversationId);
                                    return Mono.empty();
                                }

                                Flux<String> tokenFlux = resilience.wrap(
                                        aiStreamingService.streamAiResponse(conversationId, conv.getProjectId())
                                );

                                return tokenFlux
                                        .flatMap(token -> wsDispatcher
                                                .sendWithoutReadyCheck(WebSocketChannelType.AI_TOKEN_STREAM, wsContext, token)
                                                .thenReturn(token))
                                        .collectList()
                                        .map(tokens -> tokens.stream()
                                                .filter(t -> !END_TOKEN.equals(t))
                                                .collect(Collectors.joining()))
                                        .flatMap(fullMessage -> {
                                            MessageEvent finalEvent = MessageEvent.builder()
                                                    .messageId(UUID.randomUUID())
                                                    .conversationId(conversationId)
                                                    .senderId(AI_USER_ID)
                                                    .text(fullMessage)
                                                    .type(MessageType.TEXT)
                                                    .recipient(RecipientType.USERS)
                                                    .occurredAt(LocalDateTime.now())
                                                    .build();

                                            return messageService.saveMessage(finalEvent)
                                                    .then(notificationService.notifyNewMessage(finalEvent.conversationId(), finalEvent.senderId()))
                                                    .doOnError(e -> log.error("[ai:response:save] Failed to save AI message", e))
                                                    .onErrorResume(e -> systemNotificationService.notifyUser(finalEvent.senderId(), "Message processing failed")
                                                            .then(Mono.error(e)));
                                        })
                                        .doOnSuccess(v -> log.debug("[ai:response:complete] Final AI message persisted."))
                                        .onErrorResume(ex -> {
                                            log.error("[ai:response:error] Failed to get AI response, using fallback message", ex);

                                            return notificationService.notifyConversation(conversationId, AI_FALLBACK_MESSAGE)
                                                    .then();
                                        })
                                        .doFinally(sig -> {
                                            aiStreamingService
                                                    .stopStreaming(conversationId, initialToken)
                                                    .doOnError(e -> log.warn("[ai:response:stop-error]", e))
                                                    .subscribe();
                                            wsDispatcher
                                                    .sendWithoutReadyCheck(WebSocketChannelType.AI_TOKEN_STREAM, wsContext, END_TOKEN)
                                                    .doOnError(e -> log.warn("[ai:response:end-token] Failed to send", e))
                                                    .subscribe();
                                        });
                            });
                })
                .doOnTerminate(() -> log.debug("[ai:response:end] Finished AI response handling for conversationId={}", conversationId))
        );
    }

}
