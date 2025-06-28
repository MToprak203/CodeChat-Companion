package com.ai.assistant.security.adapter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ai.assistant.core.Constants.JWT.CLAIM_ROLES;
import static com.ai.assistant.core.Constants.JWT.CLAIM_SCOPES;

public class JwtAuthenticationConverterAdapter
        implements Converter<Jwt, Mono<? extends AbstractAuthenticationToken>> {

  private final JwtAuthenticationConverter delegate;

  public JwtAuthenticationConverterAdapter(JwtAuthenticationConverter delegate) {
    this.delegate = delegate;
  }

  @Override
  public Mono<? extends AbstractAuthenticationToken> convert(Jwt jwt) {
    List<String> roles  = jwt.getClaimAsStringList(CLAIM_ROLES);
    List<String> scopes = jwt.getClaimAsStringList(CLAIM_SCOPES);

    List<SimpleGrantedAuthority> allAuths = Stream.concat(
                    roles.stream(),
                    scopes.stream()
            )
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    AbstractAuthenticationToken token = delegate.convert(jwt);
    return Mono.just(token).map(t -> {
      t.setAuthenticated(true);
      try {
        Field field = t.getClass().getDeclaredField("authorities");
        field.setAccessible(true);
        field.set(t, allAuths);
      } catch (Exception ignored) {}
      return t;
    });
  }
}
