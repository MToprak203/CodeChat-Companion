package com.ai.assistant.external.websocket.registry;

import com.ai.assistant.enums.WebSocketChannelType;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Set;

public interface WebSocketSessionRegistry {
    void register(WebSocketChannelType type, String key, WebSocketSession session);
    Set<WebSocketSession> getSessions(WebSocketChannelType type, String key);
    void remove(WebSocketChannelType type, String key, WebSocketSession session);
}