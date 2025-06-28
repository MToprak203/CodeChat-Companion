package com.ai.assistant.usecase.service;

import com.ai.assistant.external.redis.service.RedisSelectedFileService;
import com.ai.assistant.context.WebSocketContext;
import com.ai.assistant.enums.WebSocketChannelType;
import com.ai.assistant.external.websocket.dispatcher.WebSocketSinkDispatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectSelectedFileService {

    private final RedisSelectedFileService redisService;
    private final WebSocketSinkDispatcher dispatcher;
    private final ObjectMapper objectMapper;

    public Mono<Void> saveSelectedFiles(Long userId, Long projectId, List<String> files) {
        return redisService.setSelectedFiles(projectId, files)
                .then(Mono.fromCallable(() -> objectMapper.writeValueAsString(files)))
                .flatMap(json -> dispatcher.send(
                        WebSocketChannelType.PROJECT_SELECTED_FILES,
                        WebSocketContext.builder()
                                .userId(userId)
                                .attributes(Map.of("projectId", projectId.toString()))
                                .build(),
                        json
                ));
    }

    public Mono<List<String>> fetchSelectedFiles(Long projectId) {
        return redisService.getSelectedFiles(projectId).collectList();
    }
}
