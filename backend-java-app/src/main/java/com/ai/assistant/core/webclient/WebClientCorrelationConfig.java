package com.ai.assistant.core.webclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static com.ai.assistant.core.Constants.Correlation.CONTEXT_CORRELATION_KEY;
import static com.ai.assistant.core.Constants.Correlation.HEADER_CORRELATION_PARAM;

@Configuration
public class WebClientCorrelationConfig {
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .filter((request, next) ->
                        Mono.deferContextual(ctx -> {
                            String corrId = ctx.getOrDefault(CONTEXT_CORRELATION_KEY, null);
                            ClientRequest filtered = ClientRequest.from(request)
                                    .headers(h -> {
                                        if (corrId != null) {
                                            h.set(HEADER_CORRELATION_PARAM, corrId);
                                        }
                                    })
                                    .build();
                            return next.exchange(filtered);
                        })
                ).build();
    }
}
