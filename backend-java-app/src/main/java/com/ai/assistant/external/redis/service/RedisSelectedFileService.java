package com.ai.assistant.external.redis.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RedisSelectedFileService {
    Mono<Void> setSelectedFiles(Long projectId, List<String> files);
    Flux<String> getSelectedFiles(Long projectId);
}
