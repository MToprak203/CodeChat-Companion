package com.ai.assistant.external.kafka.service;

import com.ai.assistant.event.ProjectUploadedEvent;
import reactor.core.publisher.Mono;

public interface ProjectKafkaProducer {
    Mono<Void> publish(ProjectUploadedEvent event);
}
