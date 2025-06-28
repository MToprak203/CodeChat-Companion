package com.ai.assistant.usecase.service;

import com.ai.assistant.context.WebSocketContext;
import com.ai.assistant.enums.WebSocketChannelType;
import com.ai.assistant.external.websocket.dispatcher.WebSocketSinkDispatcher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.ai.assistant.core.Constants.AI.AI_USER_ID;

@Service
public class SystemNotificationService {
    private final WebSocketSinkDispatcher dispatcher;

    public SystemNotificationService(@Lazy WebSocketSinkDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Mono<Void> notifyUser(Long userId, String message) {
        if (AI_USER_ID.equals(userId)) {
            return Mono.empty();
        }

        WebSocketContext ctx = WebSocketContext.builder()
                .userId(userId)
                .attributes(java.util.Collections.emptyMap())
                .build();
        return dispatcher.send(WebSocketChannelType.SYSTEM_NOTIFICATION, ctx, message);
    }
}
