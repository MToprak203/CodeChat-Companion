package com.ai.assistant.external.websocket.handlers.impl;

import com.ai.assistant.external.websocket.dispatcher.WebSocketSinkDispatcher;
import com.ai.assistant.enums.WebSocketChannelType;
import com.ai.assistant.external.websocket.handlers.AbstractPathAwareWebSocketHandler;
import org.springframework.stereotype.Component;

import static com.ai.assistant.core.Constants.WebSocket.WS_PATH_CONVERSATION_MESSAGES;

@Component
public class ConversationMessageWebSocketHandler extends AbstractPathAwareWebSocketHandler {

    public ConversationMessageWebSocketHandler(WebSocketSinkDispatcher sinkDispatcher) {
        super(sinkDispatcher);
    }

    @Override
    public String getPathPattern() {
        return WS_PATH_CONVERSATION_MESSAGES;
    }

    @Override
    protected WebSocketChannelType channelType() {
        return WebSocketChannelType.CONVERSATION_MESSAGE;
    }
}