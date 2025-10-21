package com.example.librarymanagement.security.oauth2;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OAuth2UserInfo {
    private String id;
    private String email;
    private String fullname;
    private String picture;
    private String provider;
}
