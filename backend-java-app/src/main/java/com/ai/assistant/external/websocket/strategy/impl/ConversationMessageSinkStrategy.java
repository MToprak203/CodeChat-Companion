package com.ai.assistant.external.websocket.strategy.impl;


import com.ai.assistant.core.error.ApplicationException;
import com.ai.assistant.core.error.ErrorCode;
import com.ai.assistant.event.MessageEvent;
import com.ai.assistant.external.redis.service.RedisParticipantService;
import com.ai.assistant.context.WebSocketContext;
import com.ai.assistant.external.redis.service.RedisReadyFlagService;
import com.ai.assistant.enums.WebSocketChannelType;
import com.ai.assistant.enums.RecipientType;
import com.ai.assistant.external.websocket.registry.WebSocketSessionRegistry;
import com.ai.assistant.external.websocket.strategy.AbstractWebSocketSinkStrategy;
import com.ai.assistant.external.ai.service.AiMessageOrchestrator;
import com.ai.assistant.usecase.resilience.wrapper.ResilienceWrapper;
import com.ai.assistant.usecase.service.ParticipantService;
import com.ai.assistant.usecase.service.MessageService;
import com.ai.assistant.usecase.service.SystemNotificationService;
import com.ai.assistant.usecase.service.NotificationService;
import org.springframework.context.annotation.Lazy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.ai.assistant.core.Constants.Resilience.Wrapper.WS_RESILIENCE_WRAPPER;

@Slf4j
@Component
public class ConversationMessageSinkStrategy extends AbstractWebSocketSinkStrategy<MessageEvent> {

    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final SystemNotificationService systemNotificationService;
    private final NotificationService notificationService;
    private final AiMessageOrchestrator aiMessageOrchestrator;

    public ConversationMessageSinkStrategy(
            @Qualifier(WS_RESILIENCE_WRAPPER) ResilienceWrapper resilienceWrapper,
            RedisReadyFlagService redisReadyFlagService,
            WebSocketSessionRegistry sessionRegistry,
            ObjectMapper objectMapper,
            RedisParticipantService redisParticipantService,
            ParticipantService participantService,
            MessageService messageService,
            SystemNotificationService systemNotificationService,
            @Lazy NotificationService notificationService,
            @Lazy AiMessageOrchestrator aiMessageOrchestrator) {
        super(resilienceWrapper, redisReadyFlagService, sessionRegistry, redisParticipantService, participantService);
        this.objectMapper = objectMapper;
        this.messageService = messageService;
        this.systemNotificationService = systemNotificationService;
        this.notificationService = notificationService;
        this.aiMessageOrchestrator = aiMessageOrchestrator;
    }

    @Override
    protected WebSocketChannelType channelType() {
        return WebSocketChannelType.CONVERSATION_MESSAGE;
    }

    @Override
    protected Flux<String> encode(Flux<MessageEvent> stream) {
        return stream.handle((dto, sink) -> {
            try {
                sink.next(objectMapper.writeValueAsString(dto));
            } catch (JsonProcessingException e) {
                log.error("[ws:conversation-message:encode:error] Failed to serialize MessageDTO: {}", dto, e);
                sink.error(new ApplicationException(ErrorCode.JSON_PROCESSING_EXCEPTION, e, dto));
            }
        });
    }

    @Override
    protected String extractKey(WebSocketContext context) {
        return "conversation:" + context.getOrThrow("conversationId");
    }


    @Override
    public Mono<Void> register(WebSocketSession session, WebSocketContext context) {
        return super.register(session, context)
                .then(redisParticipantService.addParticipant(
                        context.getOrThrow("conversationId"),
                        context.userId()
                ));
    }

    @Override
    public Mono<Void> deregister(WebSocketSession session, WebSocketContext context) {
        return super.deregister(session, context)
                .then(redisParticipantService.removeParticipant(
                        context.getOrThrow("conversationId"),
                        context.userId()
                ));
    }

    @Override
    protected MessageEvent parseMessage(String raw) {
        try {
            return objectMapper.readValue(raw, MessageEvent.class);
        } catch (JsonProcessingException e) {
            log.warn("[ws:parseMessage:error] Failed to parse MessageEvent. raw={}", raw, e);
            throw new ApplicationException(ErrorCode.JSON_PROCESSING_EXCEPTION, e, raw);
        }
    }

    @Override
    public Mono<Void> receive(WebSocketSession session, WebSocketContext context, String rawMessage) {
        MessageEvent event = parseMessage(rawMessage);

        Mono<Void> errorNotify = systemNotificationService.notifyUser(event.senderId(), "Message processing failed");

        Mono<Void> process = messageService.saveMessage(event)
                .then(notificationService.notifyNewMessage(event.conversationId(), event.senderId()))
                .doOnError(e -> log.error("[ws:conversation-message] Failed to handle message", e))
                .onErrorResume(e -> errorNotify.then(Mono.error(e)));

        return process
                .then(super.receive(session, context, rawMessage))
                .then(Mono.defer(() -> event.recipient() == RecipientType.AI
                        ? aiMessageOrchestrator.handleAiResponse(event.conversationId())
                        : Mono.empty()));
    }
}
