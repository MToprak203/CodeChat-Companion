package com.ai.assistant.external.websocket.handlers.impl;

import com.ai.assistant.external.websocket.dispatcher.WebSocketSinkDispatcher;
import com.ai.assistant.enums.WebSocketChannelType;
import com.ai.assistant.external.websocket.handlers.AbstractPathAwareWebSocketHandler;
import org.springframework.stereotype.Component;

import static com.ai.assistant.core.Constants.WebSocket.WS_PATH_SYSTEM_NOTIFICATIONS;

@Component
public class SystemNotificationWebSocketHandler extends AbstractPathAwareWebSocketHandler {

    public SystemNotificationWebSocketHandler(WebSocketSinkDispatcher sinkDispatcher) {
        super(sinkDispatcher);
    }

    @Override
    public String getPathPattern() {
        return WS_PATH_SYSTEM_NOTIFICATIONS;
    }

    @Override
    protected WebSocketChannelType channelType() {
        return WebSocketChannelType.SYSTEM_NOTIFICATION;
    }
}
