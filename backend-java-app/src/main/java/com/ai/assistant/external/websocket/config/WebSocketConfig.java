package com.ai.assistant.external.websocket.config;

import com.ai.assistant.external.websocket.handlers.PathAwareWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class WebSocketConfig {

    @Bean
    public HandlerMapping webSocketMapping(List<WebSocketHandler> handlers) {
        Map<String, WebSocketHandler> map = new HashMap<>();

        handlers.forEach(handler -> {
            if (handler instanceof PathAwareWebSocketHandler pathAware) {
                map.put(pathAware.getPathPattern(), pathAware);
            }
        });

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(map);
        mapping.setOrder(10);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}