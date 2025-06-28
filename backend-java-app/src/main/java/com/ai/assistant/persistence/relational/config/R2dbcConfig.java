package com.ai.assistant.persistence.relational.config;

import com.ai.assistant.persistence.relational.converter.AggregateTypeReadConverter;
import com.ai.assistant.persistence.relational.converter.AggregateTypeWriteConverter;
import com.ai.assistant.persistence.relational.converter.ConversationTypeReadConverter;
import com.ai.assistant.persistence.relational.converter.ConversationTypeWriteConverter;
import com.ai.assistant.persistence.relational.converter.OutboxEventTypeReadConverter;
import com.ai.assistant.persistence.relational.converter.OutboxEventTypeWriteConverter;
import com.ai.assistant.persistence.relational.converter.OutboxStatusReadConverter;
import com.ai.assistant.persistence.relational.converter.OutboxStatusWriteConverter;
import com.ai.assistant.persistence.relational.converter.RoleSetReadConverter;
import com.ai.assistant.persistence.relational.converter.RoleSetWriteConverter;
import com.ai.assistant.persistence.relational.converter.ScopeSetReadConverter;
import com.ai.assistant.persistence.relational.converter.ScopeSetWriteConverter;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableR2dbcAuditing(auditorAwareRef = "auditorProvider")
public class R2dbcConfig {
    /**
     * Tell Spring Data R2DBC how to find the current auditor (user ID).
     */
    @Bean
    public ReactiveAuditorAware<Long> auditorProvider() {
        return () -> ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .flatMap(auth -> {
                    if (auth instanceof JwtAuthenticationToken jwtAuth) {
                        Long userId = jwtAuth.getToken().getClaim("user_id");
                        return Mono.just(userId != null ? userId : 0L);
                    }
                    return Mono.just(0L);
                })
                .defaultIfEmpty(0L);
    }

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(ConnectionFactory cf) {
        R2dbcDialect dialect = DialectResolver.getDialect(cf);

        List<Converter<?, ?>> converters = List.of(
                new RoleSetWriteConverter(),
                new RoleSetReadConverter(),
                new ScopeSetWriteConverter(),
                new ScopeSetReadConverter(),
                new ConversationTypeWriteConverter(),
                new ConversationTypeReadConverter(),
                new OutboxEventTypeWriteConverter(),
                new OutboxEventTypeReadConverter(),
                new AggregateTypeWriteConverter(),
                new AggregateTypeReadConverter(),
                new OutboxStatusReadConverter(),
                new OutboxStatusWriteConverter()
        );

        return R2dbcCustomConversions.of(dialect, converters);
    }
}
