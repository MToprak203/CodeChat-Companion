package com.ai.assistant.enums;

import java.util.Locale;

public enum WebSocketChannelType {
    AI_TOKEN_STREAM,
    CONVERSATION_USER_NOTIFICATION,
    CONVERSATION_GROUP_NOTIFICATION,
    SYSTEM_NOTIFICATION,
    CONVERSATION_MESSAGE,
    PROJECT_SELECTED_FILES;

    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}