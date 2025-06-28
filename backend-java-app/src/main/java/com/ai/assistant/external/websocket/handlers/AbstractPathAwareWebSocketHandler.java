package com.ai.assistant.external.websocket.handlers;

import com.ai.assistant.external.websocket.dispatcher.WebSocketSinkDispatcher;
import com.ai.assistant.context.WebSocketContext;
import com.ai.assistant.enums.WebSocketChannelType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractPathAwareWebSocketHandler implements PathAwareWebSocketHandler {

    private final WebSocketSinkDispatcher sinkDispatcher;

    protected abstract WebSocketChannelType channelType();

    @Override
    public @NonNull Mono<Void> handle(WebSocketSession session) {
        Map<String, String> variables = new UriTemplate(getPathPattern())
                .match(session.getHandshakeInfo().getUri().getPath());

        String userId = variables.get("userId");
        variables.remove("userId");

        long pathUserId = Long.parseLong(userId);

        Mono<Long> authUserIdMono = Mono.from(session.getHandshakeInfo().getPrincipal())
                .map(principal -> {
                    if (principal instanceof JwtAuthenticationToken jwtAuth) {
                        Jwt jwt = jwtAuth.getToken();
                        Object claim = jwt.getClaims().get(com.ai.assistant.core.Constants.JWT.CLAIM_USER_ID);
                        if (claim instanceof Integer i) return i.longValue();
                        if (claim instanceof Long l) return l;
                    } else if (principal instanceof Authentication auth && auth.getPrincipal() instanceof com.ai.assistant.security.context.SecurityUser su) {
                        return su.getUserId();
                    }
                    return null;
                });

        WebSocketContext context = WebSocketContext.builder()
                .userId(pathUserId)
                .attributes(variables)
                .build();

        String variableDump = variables.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        log.debug("[ws:{}:handle:init] {} sessionId={}", channelType(), variableDump, session.getId());

        return authUserIdMono.flatMap(authUserId -> {
            if (authUserId != null && !authUserId.equals(pathUserId)) {
                log.warn("[ws:{}:auth] userId mismatch token={} path={} sessionId={}",
                        channelType(), authUserId, pathUserId, session.getId());
                return session.close(CloseStatus.NOT_ACCEPTABLE.withReason("User ID mismatch"));
            }

            Mono<Void> receiver = session.receive()
                    .flatMap(message -> {
                        if (message.getType() == WebSocketMessage.Type.PING) {
                            log.trace("[ws:{}:ping] Received ping. {}", channelType(), variableDump);
                            return Mono.empty();
                        } else if (message.getType() == WebSocketMessage.Type.TEXT) {
                            String rawMessage = message.getPayloadAsText();
                            log.debug("[ws:{}:text] Received message. {} raw={}", channelType(), variableDump, rawMessage);
                            return sinkDispatcher.receive(channelType(), session, context, rawMessage);
                        }

                        log.debug("[ws:{}:message] Ignored message type={} {}", channelType(), message.getType(), variableDump);
                        return Mono.empty();
                    })
                    .doOnError(e -> log.warn("[ws:{}:receive:error] sessionId={} {} err={}",
                            channelType(), session.getId(), variableDump, e.toString(), e))
                    .then();

            return sinkDispatcher.register(channelType(), session, context)
                    .thenMany(receiver)
                    .doFinally(sig -> sinkDispatcher.deregister(channelType(), session, context)
                            .doOnSuccess(v -> log.info("[ws:{}:handle:cleanup] Deregister complete. {}",
                                    channelType(), variableDump))
                            .doOnError(e -> log.error("[ws:{}:handle:deregister:error] {} err={}",
                                    channelType(), variableDump, e.toString(), e))
                            .subscribe())
                    .then();
        }).onErrorResume(e -> {
            log.warn("[ws:{}:handle:early-exit] Register failed early: {}", channelType(), e.getMessage());
            return Mono.empty();
        });
    }
}
