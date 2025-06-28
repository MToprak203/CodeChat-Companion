package com.ai.assistant.security.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;


@Getter
@RequiredArgsConstructor
public class SecurityUser implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public SecurityUser(Long userId,
                        String username,
                        Collection<? extends GrantedAuthority> authorities,
                        boolean enabled) {
        this(userId, username, null, authorities, enabled);
    }
}
