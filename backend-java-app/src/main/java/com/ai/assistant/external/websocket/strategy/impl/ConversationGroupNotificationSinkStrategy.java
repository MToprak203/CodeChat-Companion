package com.ai.assistant.external.websocket.strategy.impl;

import com.ai.assistant.context.WebSocketContext;
import com.ai.assistant.external.redis.service.RedisParticipantService;
import com.ai.assistant.external.redis.service.RedisReadyFlagService;
import com.ai.assistant.enums.WebSocketChannelType;
import com.ai.assistant.external.websocket.registry.WebSocketSessionRegistry;
import com.ai.assistant.external.websocket.strategy.AbstractWebSocketSinkStrategy;
import com.ai.assistant.usecase.resilience.wrapper.ResilienceWrapper;
import com.ai.assistant.usecase.service.ParticipantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static com.ai.assistant.core.Constants.Resilience.Wrapper.WS_RESILIENCE_WRAPPER;

@Slf4j
@Component
public class ConversationGroupNotificationSinkStrategy extends AbstractWebSocketSinkStrategy<String> {

    public ConversationGroupNotificationSinkStrategy(
            @Qualifier(WS_RESILIENCE_WRAPPER) ResilienceWrapper resilienceWrapper,
            RedisReadyFlagService redisReadyFlagService,
            WebSocketSessionRegistry sessionRegistry,
            RedisParticipantService redisParticipantService,
            ParticipantService participantService
    ) {
        super(resilienceWrapper, redisReadyFlagService, sessionRegistry, redisParticipantService, participantService);
    }

    @Override
    protected WebSocketChannelType channelType() {
        return WebSocketChannelType.CONVERSATION_GROUP_NOTIFICATION;
    }

    @Override
    protected Flux<String> encode(Flux<String> stream) {
        return stream;
    }

    @Override
    protected String extractKey(WebSocketContext context) {
        return "conversation-group:" + context.getOrThrow("conversationId");
    }

    @Override
    protected String parseMessage(String rawMessage) {
        return rawMessage;
    }

}
