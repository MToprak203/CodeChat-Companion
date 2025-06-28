package com.ai.assistant.context;

import lombok.Builder;

import java.util.Map;

@Builder
public record WebSocketContext(
        Long userId,
        Map<String, String> attributes
) {
    public String getOrThrow(String key) {
        if (!attributes.containsKey(key))
            throw new IllegalStateException("Missing key in context: " + key);
        return attributes.get(key);
    }
}
