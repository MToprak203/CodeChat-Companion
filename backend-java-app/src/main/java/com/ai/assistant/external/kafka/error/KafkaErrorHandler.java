package com.ai.assistant.external.kafka.error;

import com.ai.assistant.usecase.service.SystemNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class KafkaErrorHandler {

    private final SystemNotificationService notificationService;

    public KafkaErrorHandler(SystemNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ServiceActivator(inputChannel = "projectUploaded-in-0.errors")
    public Mono<Void> handleKafkaError(Message<?> message) {
        log.error("[KAFKA ERROR] {}", message);

        Long userId = extractUserId(message.getPayload());
        if (userId == null) {
            return Mono.empty();
        }

        return notificationService
                .notifyUser(userId, "Kafka processing error")
                .doOnError(e -> log.error("[KafkaErrorHandler] Failed to notify userId={}", userId, e));
    }

    @ServiceActivator(inputChannel = "conversationCleanup-in-0.errors")
    public Mono<Void> handleConversationCleanupError(Message<?> message) {
        return handleKafkaError(message);
    }

    @ServiceActivator(inputChannel = "projectCleanup-in-0.errors")
    public Mono<Void> handleProjectCleanupError(Message<?> message) {
        return handleKafkaError(message);
    }

    private Long extractUserId(Object payload) {
        if (payload instanceof java.util.List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof com.ai.assistant.event.ConversationCleanupEvent e) {
                return e.userId();
            }
            if (first instanceof com.ai.assistant.event.ProjectCleanupEvent e) {
                return e.userId();
            }
        } else if (payload instanceof com.ai.assistant.event.ConversationCleanupEvent e) {
            return e.userId();
        } else if (payload instanceof com.ai.assistant.event.ProjectCleanupEvent e) {
            return e.userId();
        }
        log.warn("[KafkaErrorHandler] Unable to extract userId from payload type={} ", payload.getClass().getName());
        return null;
    }
}
