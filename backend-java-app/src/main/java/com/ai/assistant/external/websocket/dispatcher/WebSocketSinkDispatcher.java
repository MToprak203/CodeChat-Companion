package com.ai.assistant.external.websocket.dispatcher;

import com.ai.assistant.context.WebSocketContext;
import com.ai.assistant.enums.WebSocketChannelType;
import com.ai.assistant.external.websocket.strategy.WebSocketSinkStrategy;
import com.ai.assistant.external.websocket.strategy.impl.AiTokenSinkStrategy;
import com.ai.assistant.external.websocket.strategy.impl.ConversationMessageSinkStrategy;
import com.ai.assistant.external.websocket.strategy.impl.ConversationGroupNotificationSinkStrategy;
import com.ai.assistant.external.websocket.strategy.impl.ConversationUserNotificationSinkStrategy;
import com.ai.assistant.external.websocket.strategy.impl.SystemNotificationSinkStrategy;
import com.ai.assistant.external.websocket.strategy.impl.ProjectSelectedFileSinkStrategy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSinkDispatcher {

    @Value("${websocket.ready.interval}")
    private Integer socketReadyIntervalInMillis;

    @Value("${websocket.ready.timeout-duration}")
    private Integer socketReadyTimeoutDurationInSeconds;

    private final AiTokenSinkStrategy aiTokenSink;
    private final ConversationUserNotificationSinkStrategy userNotificationSink;
    private final ConversationGroupNotificationSinkStrategy groupNotificationSink;
    private final ConversationMessageSinkStrategy messageSink;
    private final SystemNotificationSinkStrategy systemNotificationSink;
    private final ProjectSelectedFileSinkStrategy projectSelectedFileSink;

    private final Map<WebSocketChannelType, WebSocketSinkStrategy<?>> strategyMap = new EnumMap<>(WebSocketChannelType.class);

    @PostConstruct
    public void init() {
        strategyMap.put(WebSocketChannelType.AI_TOKEN_STREAM, aiTokenSink);
        strategyMap.put(WebSocketChannelType.CONVERSATION_USER_NOTIFICATION, userNotificationSink);
        strategyMap.put(WebSocketChannelType.CONVERSATION_GROUP_NOTIFICATION, groupNotificationSink);
        strategyMap.put(WebSocketChannelType.CONVERSATION_MESSAGE, messageSink);
        strategyMap.put(WebSocketChannelType.SYSTEM_NOTIFICATION, systemNotificationSink);
        strategyMap.put(WebSocketChannelType.PROJECT_SELECTED_FILES, projectSelectedFileSink);
    }

    public Mono<Void> register(WebSocketChannelType type, WebSocketSession session, WebSocketContext context) {
        @SuppressWarnings("unchecked")
        WebSocketSinkStrategy<Object> strategy = (WebSocketSinkStrategy<Object>) strategyMap.get(type);
        return strategy.register(session, context);
    }

    public Mono<Void> deregister(WebSocketChannelType type, WebSocketSession session, WebSocketContext context) {
        @SuppressWarnings("unchecked")
        WebSocketSinkStrategy<Object> strategy = (WebSocketSinkStrategy<Object>) strategyMap.get(type);
        return strategy.deregister(session, context);
    }

    public Mono<Boolean> isReady(WebSocketChannelType type, WebSocketContext context) {
        @SuppressWarnings("unchecked")
        WebSocketSinkStrategy<Object> strategy = (WebSocketSinkStrategy<Object>) strategyMap.get(type);
        return Flux.interval(Duration.ofMillis(socketReadyIntervalInMillis))
                .flatMap(tick -> strategy.isReady(context))
                .filter(Boolean::booleanValue)
                .next()
                .timeout(Duration.ofSeconds(socketReadyTimeoutDurationInSeconds));
    }

    public <T> Mono<Void> send(WebSocketChannelType type, WebSocketContext context, T payload) {
        @SuppressWarnings("unchecked")
        WebSocketSinkStrategy<T> strategy = (WebSocketSinkStrategy<T>) strategyMap.get(type);

        return strategy.isReady(context)
                .flatMap(ready -> {
                    if (!ready) return Mono.error(new IllegalStateException("WebSocket not ready"));
                    return strategy.send(context, payload);
                })
                .doOnSuccess(v -> log.debug("[ws:send] Sent to {} context={} payload={}", type, context, payload))
                .doOnError(e -> log.warn("[ws:send:skip] WebSocket not ready or error for type={} context={}", type, context, e));
    }

    public <T> Mono<Void> sendWithoutReadyCheck(WebSocketChannelType type, WebSocketContext context, T payload) {
        log.warn("[ws:send:unsafe] Sending without readiness check! Make sure you called isReady() before. type={}, context={}", type, context);

        @SuppressWarnings("unchecked")
        WebSocketSinkStrategy<T> strategy = (WebSocketSinkStrategy<T>) strategyMap.get(type);

        return strategy.send(context, payload)
                .doOnSuccess(v -> log.debug("[ws:send:unsafe] Sent to {} context={} payload={}", type, context, payload))
                .doOnError(e -> log.warn("[ws:send:unsafe:error] Failed to send to type={} context={}", type, context, e));
    }

    public Mono<Void> receive(WebSocketChannelType type, WebSocketSession session, WebSocketContext context, String rawMessage) {
        @SuppressWarnings("unchecked")
        WebSocketSinkStrategy<Object> strategy = (WebSocketSinkStrategy<Object>) strategyMap.get(type);
        return strategy.receive(session, context, rawMessage);
    }

    public <T> Flux<T> getBufferedMessages(WebSocketChannelType type, WebSocketContext context) {
        @SuppressWarnings("unchecked")
        WebSocketSinkStrategy<T> strategy = (WebSocketSinkStrategy<T>) strategyMap.get(type);
        return strategy.getBufferedMessage(context);
    }
}