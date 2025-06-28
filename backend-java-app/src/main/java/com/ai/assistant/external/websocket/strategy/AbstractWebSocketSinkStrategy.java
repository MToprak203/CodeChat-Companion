package com.ai.assistant.external.websocket.strategy;

import com.ai.assistant.external.redis.service.RedisParticipantService;
import com.ai.assistant.external.redis.service.RedisReadyFlagService;
import com.ai.assistant.context.WebSocketContext;
import com.ai.assistant.enums.WebSocketChannelType;
import com.ai.assistant.external.websocket.registry.WebSocketSessionRegistry;
import com.ai.assistant.usecase.resilience.wrapper.ResilienceWrapper;
import com.ai.assistant.usecase.service.ParticipantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

import static com.ai.assistant.core.Constants.Resilience.Wrapper.WS_RESILIENCE_WRAPPER;

@Slf4j
public abstract class AbstractWebSocketSinkStrategy<T> implements WebSocketSinkStrategy<T> {

    private final ResilienceWrapper resilienceWrapper;
    protected final RedisReadyFlagService redisReadyFlagService;
    private final WebSocketSessionRegistry sessionRegistry;
    protected final RedisParticipantService redisParticipantService;
    protected final ParticipantService participantService;

    protected AbstractWebSocketSinkStrategy(
            @Qualifier(WS_RESILIENCE_WRAPPER) ResilienceWrapper resilienceWrapper,
            RedisReadyFlagService redisReadyFlagService,
            WebSocketSessionRegistry sessionRegistry,
            RedisParticipantService redisParticipantService,
            ParticipantService participantService
    ) {
        this.resilienceWrapper = resilienceWrapper;
        this.redisReadyFlagService = redisReadyFlagService;
        this.sessionRegistry = sessionRegistry;
        this.redisParticipantService = redisParticipantService;
        this.participantService = participantService;
    }

    protected abstract WebSocketChannelType channelType();

    protected abstract Flux<String> encode(Flux<T> stream);

    protected abstract String extractKey(WebSocketContext context);

    protected abstract T parseMessage(String rawMessage);

    @Override
    public Mono<Void> register(WebSocketSession session, WebSocketContext context) {
        String key = extractKey(context);
        String channel = channelType().toString();

        log.debug("[ws:{}:register:init] key={} userId={}", channel, key, context.userId());

        sessionRegistry.register(channelType(), key, session);
        log.debug("[ws:{}:register:sessionAdded] key={} sessionId={}", channel, key, session.getId());

        return redisReadyFlagService.markReady(key, context.userId())
                .doOnSuccess(v -> log.debug("[ws:{}:register:ready] key={} userId={}", channel, key, context.userId()))
                .then();
    }

    @Override
    public Mono<Void> deregister(WebSocketSession session, WebSocketContext context) {
        String key = extractKey(context);

        sessionRegistry.remove(channelType(), key, session);
        log.debug("[ws:{}:deregister] key={} userId={} offline", channelType(), key, context.userId());
        return Mono.empty();
    }

    @Override
    public Mono<Void> send(WebSocketContext context, T payload) {
        String key = extractKey(context);
        String channel = channelType().toString();

        return encode(Flux.just(payload))
                .next()
                .flatMap(json -> {
                    log.debug("[ws:{}:send:init] key={} payload={}", channel, key, json);
                    return sendToRegisteredSessions(key, json);
                })
                .doOnSuccess(v -> log.debug("[ws:{}:send:complete] key={} sent successfully", channel, key))
                .doOnError(e -> log.error("[ws:{}:send:error] key={} error={}", channel, key, e.toString(), e));
    }

    @Override
    public Mono<Void> receive(WebSocketSession session, WebSocketContext context, String rawMessage) {
        String key = extractKey(context);
        String channel = channelType().toString();
        Long userId = context.userId();

        log.debug("[ws:{}:receive:message] key={} userId={} raw={}", channel, key, userId, rawMessage);

        String conversationId = context.getOrThrow("conversationId");

        return redisParticipantService.isParticipant(conversationId, userId)
                .flatMap(found -> {
                    Mono<Boolean> allowed;
                    if (Boolean.TRUE.equals(found)) {
                        allowed = Mono.just(true);
                    } else {
                        allowed = participantService.isParticipant(Long.parseLong(conversationId), userId)
                                .flatMap(db -> {
                                    if (Boolean.TRUE.equals(db)) {
                                        return redisParticipantService.addParticipant(conversationId, userId)
                                                .thenReturn(true);
                                    }
                                    return Mono.just(false);
                                });
                    }

                    return allowed.flatMap(isMember -> {
                        if (!isMember) {
                            log.warn("[ws:{}:receive:forbidden] userId={} is not an active participant of conversation={}", channel, userId, key);
                            return session.send(Mono.just(session.textMessage("You are not a participant of this conversation.")))
                                    .then(session.close())
                                    .then(Mono.error(new IllegalAccessException("User is not participant")));
                        }

                        return Mono.just(rawMessage)
                                .map(this::parseMessage)
                                .flatMap(payload -> send(context, payload))
                                .doOnSuccess(v -> log.debug("[ws:{}:receive:processed] key={} message broadcasted", channel, key))
                                .doOnError(e -> log.error("[ws:{}:receive:error] key={} err={}", channel, key, e.toString(), e));
                    });
                });
    }


    @Override
    public Mono<Boolean> isReady(WebSocketContext context) {
        String key = extractKey(context);
        return redisReadyFlagService.isReady(key, context.userId())
                .doOnNext(ready -> log.debug("[ws:{}:isReady] key={} userId={} ready={}", channelType(), key, context.userId(), ready));
    }

    private Mono<Void> sendToRegisteredSessions(String key, String payload) {
        Set<WebSocketSession> sessions = sessionRegistry.getSessions(channelType(), key);
        log.debug("[ws:{}:broadcast:init] key={} sessionCount={}", channelType(), key, sessions.size());

        return Flux.fromIterable(sessions)
                .flatMap(session ->
                        resilienceWrapper.wrap(session.send(Mono.just(session.textMessage(payload))))
                                .doOnSuccess(v -> log.debug("[ws:{}:broadcast:ok] sessionId={} key={}", channelType(), session.getId(), key))
                                .onErrorResume(e -> {
                                    log.warn("[ws:{}:broadcast:fail] sessionId={} key={} err={}", channelType(), session.getId(), key, e.toString());
                                    return Mono.empty();
                                })
                )
                .then();
    }

}
