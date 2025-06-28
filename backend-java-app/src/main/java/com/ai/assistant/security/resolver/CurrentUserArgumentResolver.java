package com.ai.assistant.security.resolver;

import com.ai.assistant.core.error.ApplicationException;
import com.ai.assistant.core.error.ErrorCode;
import com.ai.assistant.security.context.SecurityUser;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static com.ai.assistant.core.Constants.JWT.CLAIM_USER_ID;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(com.ai.assistant.security.annotation.CurrentUser.class);
    }

    @Override
    public Mono<Object> resolveArgument(MethodParameter param,
                                        BindingContext binding,
                                        ServerWebExchange exchange) {

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    SecurityUser user;

                    if (auth instanceof JwtAuthenticationToken jwtAuth) {
                        Jwt jwt = jwtAuth.getToken();
                        Long userId   = jwt.getClaim(CLAIM_USER_ID);
                        String username = jwt.getSubject();

                        List<GrantedAuthority> authorities = auth.getAuthorities().stream()
                                .map(a -> new SimpleGrantedAuthority(a.getAuthority()))
                                .collect(Collectors.toList());

                        user = new SecurityUser(userId, username, authorities, true);

                    } else if (auth.getPrincipal() instanceof SecurityUser su) {
                        user = su;

                    } else {
                        return Mono.error(new ApplicationException(
                                ErrorCode.UNAUTHORIZED, "Unknown principal type"));
                    }

                    return extractValue(user, param);
                });
    }

    private Mono<Object> extractValue(SecurityUser user, MethodParameter parameter) {
        Class<?> type = parameter.getParameterType();

        if (type.equals(SecurityUser.class)) {
            return Mono.just(user);
        } else if (type.equals(Long.class)) {
            return Mono.just(user.getUserId());
        } else {
            return Mono.error(new ApplicationException(
                    ErrorCode.INVALID_CURRENT_USER_TYPE,
                    "@CurrentUser supports only SecurityUser or Long"));
        }
    }
}
