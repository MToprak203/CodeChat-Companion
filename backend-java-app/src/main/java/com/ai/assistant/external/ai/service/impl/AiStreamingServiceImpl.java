package com.ai.assistant.external.ai.service.impl;

import com.ai.assistant.external.ai.config.AiServiceProperties;
import com.ai.assistant.external.ai.dto.AiServiceRequest;
import com.ai.assistant.external.ai.dto.AiServiceRequest.ChatMessage;
import com.ai.assistant.external.ai.service.AiStreamingService;
import com.ai.assistant.usecase.service.MessageService;
import com.ai.assistant.usecase.service.ProjectFileService;
import com.ai.assistant.usecase.service.ProjectSelectedFileService;
import com.ai.assistant.usecase.resilience.wrapper.ResilienceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.util.function.Tuples;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

import static com.ai.assistant.core.Constants.AI.AI_USER_ID;
import com.ai.assistant.enums.MessageType;

import static com.ai.assistant.core.Constants.Correlation.CONTEXT_CORRELATION_KEY;
import static com.ai.assistant.core.Constants.Resilience.Wrapper.WEB_CLIENT_RESILIENCE_WRAPPER;

@Slf4j
@Service
public class AiStreamingServiceImpl implements AiStreamingService {

    private final AiServiceProperties properties;
    private final WebClient webClient;
    private final MessageService messageService;
    private final ProjectFileService projectFileService;
    private final ProjectSelectedFileService selectedFileService;
    private final ResilienceWrapper resilience;
    private final Integer chatHistoryCount;

    public AiStreamingServiceImpl(AiServiceProperties properties,
                                  WebClient webClient,
                                  MessageService messageService,
                                  ProjectFileService projectFileService,
                                  ProjectSelectedFileService selectedFileService,
                                  @Qualifier(WEB_CLIENT_RESILIENCE_WRAPPER) ResilienceWrapper resilience,
                                  @Value("${app.chat.history.count}") Integer chatHistoryCount) {
        this.properties = properties;
        this.webClient = webClient;
        this.messageService = messageService;
        this.projectFileService = projectFileService;
        this.selectedFileService = selectedFileService;
        this.resilience = resilience;
        this.chatHistoryCount = chatHistoryCount;
    }

    @Override
    public Flux<String> streamAiResponse(Long conversationId, Long projectId) {
        log.debug("[ai:stream:start] conversationId={} projectId={}", conversationId, projectId);

        Mono<List<ChatMessage>> historyMono = messageService.fetchMessages(conversationId, 0, chatHistoryCount)
                        .map(event -> {
                            String role;
                            if (event.type() == MessageType.SYSTEM) {
                                role = "system";
                            } else if (AI_USER_ID.equals(event.senderId())) {
                                role = "assistant";
                            } else {
                                role = "user";
                            }
                            return new ChatMessage(role, event.text());
                        })
                        .collectList();

        Mono<List<String>> filesMono = projectId != null
                ? selectedFileService.fetchSelectedFiles(projectId)
                    .flatMap(list -> list.isEmpty()
                            ? projectFileService.readFiles(projectId)
                            : projectFileService.readFiles(projectId, list))
                : Mono.just(List.of());

        Mono<String> corrIdMono = Mono.deferContextual(ctx -> Mono.just(Objects.requireNonNull(ctx.getOrDefault(CONTEXT_CORRELATION_KEY, ""))));
        Mono<String> tokenMono = ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .ofType(JwtAuthenticationToken.class)
                .map(auth -> auth.getToken().getTokenValue())
                .defaultIfEmpty("");

        return Mono.zip(historyMono, filesMono, corrIdMono, tokenMono)
                .doOnNext(t -> log.debug("[ai:stream:prepare] history={} files={}", t.getT1().size(), t.getT2().size()))
                .map(t -> {
                    List<ChatMessage> history = t.getT1();
                    java.util.Collections.reverse(history);
                    AiServiceRequest req;
                    if (history.isEmpty()) {
                        req = AiServiceRequest.builder()
                                .conversationId(conversationId)
                                .prompt("")
                                .chatHistory(List.of())
                                .projectFiles(t.getT2())
                                .correlationId(t.getT3())
                                .build();
                    } else {
                        ChatMessage last = history.removeLast();

                        req = AiServiceRequest.builder()
                                .conversationId(conversationId)
                                .prompt(last.content())
                                .chatHistory(history)
                                .projectFiles(t.getT2())
                                .correlationId(t.getT3())
                                .build();
                    }
                    return Tuples.of(req, t.getT4());
                })
                .flatMapMany(tuple -> resilience.wrap(
                        webClient.post()
                                .uri(properties.getBaseUrl() + "/generate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(h -> {
                                    if (!tuple.getT2().isEmpty()) {
                                        h.setBearerAuth(tuple.getT2());
                                    }
                                })
                                .bodyValue(tuple.getT1())
                                .retrieve()
                                .bodyToFlux(org.springframework.core.io.buffer.DataBuffer.class)
                                .map(dataBuffer -> {
                                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(bytes);
                                    org.springframework.core.io.buffer.DataBufferUtils.release(dataBuffer);
                                    return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                                })
                )
                        .doOnSubscribe(s -> log.debug("[ai:stream:request] POST {}{}", properties.getBaseUrl(), "/generate"))
                        .doOnError(e -> log.error("[ai:stream:error] Failed to reach AI service", e)));
    }

    @Override
    public Mono<Void> stopStreaming(Long conversationId) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .ofType(JwtAuthenticationToken.class)
                .map(auth -> auth.getToken().getTokenValue())
                .defaultIfEmpty("")
                .flatMap(token -> stopStreaming(conversationId, token));
    }

    @Override
    public Mono<Void> stopStreaming(Long conversationId, String jwt) {
        return resilience.wrap(
                        webClient.post()
                                .uri(properties.getBaseUrl() + "/stop/" + conversationId)
                                .headers(h -> {
                                    if (jwt != null && !jwt.isEmpty()) {
                                        h.setBearerAuth(jwt);
                                    }
                                })
                                .retrieve()
                                .bodyToMono(Void.class)
                )

                .doOnSubscribe(s -> log.debug("[ai:stop:request] POST {}/stop/{}", properties.getBaseUrl(), conversationId))
                .doOnError(e -> log.warn("[ai:stop:error] Failed to stop AI service", e))
                .onErrorResume(e -> Mono.empty());
    }
}
