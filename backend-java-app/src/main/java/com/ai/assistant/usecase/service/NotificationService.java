package com.ai.assistant.usecase.service;

import com.ai.assistant.context.WebSocketContext;
import com.ai.assistant.enums.WebSocketChannelType;
import com.ai.assistant.external.redis.service.RedisParticipantService;
import com.ai.assistant.external.websocket.dispatcher.WebSocketSinkDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final WebSocketSinkDispatcher dispatcher;
    private final RedisParticipantService participantService;

    public Mono<Void> notifyNewMessage(Long conversationId, Long senderId) {
        Flux<Void> users = participantService.getParticipants(conversationId.toString())
                .filter(id -> !id.equals(senderId))
                .flatMap(userId -> {
                    WebSocketContext ctx = WebSocketContext.builder()
                            .userId(userId)
                            .attributes(Map.of("conversationId", conversationId.toString()))
                            .build();
                    return dispatcher.send(
                            WebSocketChannelType.CONVERSATION_USER_NOTIFICATION,
                            ctx,
                            "new-message"
                    );
                });

        return users.then(users.then());
    }

    public Mono<Void> notifyConversation(Long conversationId, String message) {
        Flux<Void> users = participantService.getParticipants(conversationId.toString())
                .flatMap(userId -> {
                    WebSocketContext ctx = WebSocketContext.builder()
                            .userId(userId)
                            .attributes(Map.of("conversationId", conversationId.toString()))
                            .build();
                    return dispatcher.send(
                            WebSocketChannelType.CONVERSATION_USER_NOTIFICATION,
                            ctx,
                            message
                    );
                });

        return users.then();
    }
}
