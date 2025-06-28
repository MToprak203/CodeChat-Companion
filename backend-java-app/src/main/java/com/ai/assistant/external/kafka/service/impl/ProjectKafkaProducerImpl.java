package com.ai.assistant.external.kafka.service.impl;

import com.ai.assistant.core.error.ApplicationException;
import com.ai.assistant.core.error.ErrorCode;
import com.ai.assistant.event.ProjectUploadedEvent;
import com.ai.assistant.external.kafka.service.ProjectKafkaProducer;
import com.ai.assistant.usecase.resilience.wrapper.ResilienceWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.ai.assistant.core.Constants.Resilience.Wrapper.KAFKA_RESILIENCE_WRAPPER;

@Slf4j
@Service
public class ProjectKafkaProducerImpl implements ProjectKafkaProducer {

    private final StreamBridge streamBridge;
    private final ResilienceWrapper resilience;

    public ProjectKafkaProducerImpl(StreamBridge streamBridge, @Qualifier(KAFKA_RESILIENCE_WRAPPER) ResilienceWrapper resilience) {
        this.streamBridge = streamBridge;
        this.resilience = resilience;
    }

    @Override
    public Mono<Void> publish(ProjectUploadedEvent event) {
        return resilience.wrap(Mono.fromCallable(() -> {
            boolean success = streamBridge.send("projectUploaded-out", event);
            if (!success) {
                throw new ApplicationException(ErrorCode.KAFKA_PUBLISH_ERROR, "projectUploaded-out");
            }
            return true;
        })).then();
    }
}
