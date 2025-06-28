package com.ai.assistant.core.correlation;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

import static com.ai.assistant.core.Constants.Correlation.HEADER_CORRELATION_PARAM;
import static com.ai.assistant.core.Constants.Correlation.CONTEXT_CORRELATION_KEY;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdWebFilter implements WebFilter, Ordered {

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest  request  = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 1) Read or generate the ID
        String id = request.getHeaders().getFirst(HEADER_CORRELATION_PARAM);
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }

        // 2) Echo it on the response
        response.getHeaders().set(HEADER_CORRELATION_PARAM, id);

        // 3) Propagate via Reactor Context
        String finalId = id;
        return chain.filter(exchange)
                .contextWrite(ctx -> Context.of(CONTEXT_CORRELATION_KEY, finalId));
    }
}
