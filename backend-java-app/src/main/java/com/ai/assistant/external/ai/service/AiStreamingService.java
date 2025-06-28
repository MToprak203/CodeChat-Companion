package com.ai.assistant.external.ai.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AiStreamingService {
    Flux<String> streamAiResponse(Long conversationId, Long projectId);

    Mono<Void> stopStreaming(Long conversationId);

    /**
     * Stop streaming using the provided JWT. This is useful when the reactive
     * security context might change before the stop call executes.
     */
    Mono<Void> stopStreaming(Long conversationId, String jwt);
}
