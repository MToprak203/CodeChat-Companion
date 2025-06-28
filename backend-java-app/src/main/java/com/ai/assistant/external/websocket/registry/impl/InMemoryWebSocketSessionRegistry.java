package com.ai.assistant.external.websocket.registry.impl;

import com.ai.assistant.external.websocket.registry.WebSocketSessionRegistry;
import com.ai.assistant.enums.WebSocketChannelType;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class InMemoryWebSocketSessionRegistry implements WebSocketSessionRegistry {

    private final Map<WebSocketChannelType, Map<String, Set<WebSocketSession>>> sessions = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;

    @PostConstruct
    public void initMetrics() {
        meterRegistry.gauge("ws.total.active.sessions", sessions, s ->
                s.values().stream()
                        .flatMap(m -> m.values().stream())
                        .mapToInt(Set::size)
                        .sum());

        for (WebSocketChannelType type : WebSocketChannelType.values()) {
            meterRegistry.gauge("ws.channel.session.count",
                    Tags.of("channel", type.name()),
                    sessions,
                    s -> s.getOrDefault(type, Map.of())
                            .values().stream()
                            .mapToInt(Set::size).sum());
        }
    }

    @Override
    public void register(WebSocketChannelType type, String key, WebSocketSession session) {
        sessions
            .computeIfAbsent(type, t -> new ConcurrentHashMap<>())
            .computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
            .add(session);
    }

    @Override
    public Set<WebSocketSession> getSessions(WebSocketChannelType type, String key) {
        return sessions.getOrDefault(type, Map.of()).getOrDefault(key, Set.of());
    }

    @Override
    public void remove(WebSocketChannelType type, String key, WebSocketSession session) {
        Map<String, Set<WebSocketSession>> channelMap = sessions.get(type);
        if (channelMap != null) {
            Set<WebSocketSession> sessionSet = channelMap.get(key);
            if (sessionSet != null) {
                sessionSet.remove(session);
                if (sessionSet.isEmpty()) {
                    channelMap.remove(key);
                    if (channelMap.isEmpty()) {
                        sessions.remove(type);
                    }
                }
            }
        }
    }
}