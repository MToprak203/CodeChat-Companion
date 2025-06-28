package com.ai.assistant.external.ai.service;

import reactor.core.publisher.Mono;

public interface AiMessageOrchestrator {
    Mono<Void> handleAiResponse(Long conversationId);
}
