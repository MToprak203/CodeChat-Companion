package com.ai.assistant.security.jwt;

import com.ai.assistant.security.context.SecurityUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static com.ai.assistant.core.Constants.JWT.CLAIM_ROLES;
import static com.ai.assistant.core.Constants.JWT.CLAIM_SCOPES;
import static com.ai.assistant.core.Constants.JWT.CLAIM_USER_ID;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expirationSeconds}")
    private long jwtExpirationSeconds;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }


    public String generateToken(SecurityUser user) {
        Instant now    = Instant.now();
        Instant expiry = now.plusSeconds(jwtExpirationSeconds);

        // split back into separate lists
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .toList();

        List<String> scopes = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("SCOPE_"))
                .map(auth -> auth.substring("SCOPE_".length()))
                .toList();

        return Jwts.builder()
                .setIssuer("codeassistant")
                .setSubject(user.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .claim(CLAIM_USER_ID, user.getUserId())
                .claim(CLAIM_ROLES,  roles)
                .claim(CLAIM_SCOPES, scopes)
                .signWith(signingKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public boolean isExpired(String token) {
        return parseClaims(token)
                .getExpiration()
                .before(new Date());
    }

    public Long getUserId(String token) {
        return parseClaims(token)
                .get(CLAIM_USER_ID, Long.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getAuthorities(String token) {
        return parseClaims(token)
                .get(CLAIM_ROLES, List.class);
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }
}
