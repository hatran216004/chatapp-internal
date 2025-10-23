package com.example.librarymanagement.security.oauth2;

import com.example.librarymanagement.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
@AllArgsConstructor
@Builder
public class CustomOAuth2User implements OAuth2User {
    private User user;
    private Map<String, Object> attributes;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())
        );
    }

    @Override
    public String getName() {
        return user.getEmail();
    }

    public String getEmail() {
        return user.getEmail();
    }
}
