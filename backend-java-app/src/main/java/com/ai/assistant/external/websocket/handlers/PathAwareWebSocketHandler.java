package com.ai.assistant.external.websocket.handlers;

import org.springframework.web.reactive.socket.WebSocketHandler;

public interface PathAwareWebSocketHandler extends WebSocketHandler {
    String getPathPattern();
}