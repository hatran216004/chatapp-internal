package com.example.librarymanagement.security.service;

import com.example.librarymanagement.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailsImpl implements UserDetails {
    private Integer id;

    private String username;

    private String email;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())
        );
        return UserDetailsImpl.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .username(user.getEmail())
                .authorities(authorities)
                .build();
    }
}
// authentication.getName() = userDetails.getUsername()