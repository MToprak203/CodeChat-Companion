package com.ai.assistant.external.websocket.strategy;

import com.ai.assistant.context.WebSocketContext;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WebSocketSinkStrategy<T> {
    Mono<Void> register(WebSocketSession session, WebSocketContext context);
    Mono<Void> deregister(WebSocketSession session, WebSocketContext context);
    Mono<Void> send(WebSocketContext context, T payload);
    Mono<Void> receive(WebSocketSession session, WebSocketContext context, String rawMessage);

    default Flux<T> getBufferedMessage(WebSocketContext context) {
        return Flux.empty();
    }

    Mono<Boolean> isReady(WebSocketContext context);
}