package com.ai.assistant.external.kafka.service.impl;

import com.ai.assistant.core.error.ApplicationException;
import com.ai.assistant.core.error.ErrorCode;
import com.ai.assistant.external.kafka.service.OutboxEventKafkaPublisher;
import com.ai.assistant.persistence.relational.entity.OutboxEvent;
import com.ai.assistant.usecase.resilience.wrapper.ResilienceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.ai.assistant.core.Constants.Resilience.Wrapper.KAFKA_RESILIENCE_WRAPPER;

@Slf4j
@Service
public class OutboxEventKafkaPublisherImpl implements OutboxEventKafkaPublisher {

    private final StreamBridge streamBridge;
    private final ResilienceWrapper resilience;

    public OutboxEventKafkaPublisherImpl(StreamBridge streamBridge,
                                         @Qualifier(KAFKA_RESILIENCE_WRAPPER) ResilienceWrapper resilience) {
        this.streamBridge = streamBridge;
        this.resilience = resilience;
    }

    @Override
    public Mono<Void> publish(List<OutboxEvent> events) {
        return Flux.fromIterable(events)
                .flatMap(event -> resilience.wrap(Mono.fromCallable(() -> {
                            String topic = event.getEventType().getOutBindingName();
                            String payload = event.getPayload();

                            log.debug("[event:publish] Publishing eventId={} type={} topic={} payload={}",
                                    event.getId(), event.getEventType(), topic, payload);

                            boolean success = streamBridge.send(topic, payload);

                            if (!success) {
                                throw new ApplicationException(
                                        ErrorCode.KAFKA_PUBLISH_ERROR,
                                        "Failed to publish event to topic: " + topic
                                );
                            }

                            return true;
                        }))
                        .doOnSuccess(v -> log.info("[event:publish] EventId={} published successfully to topic={}",
                                event.getId(), event.getEventType().getOutBindingName()))
                        .doOnError(e -> log.error("[event:publish] Failed to publish eventId={} type={} to Kafka",
                                event.getId(), event.getEventType(), e)))
                .then();
    }
}
