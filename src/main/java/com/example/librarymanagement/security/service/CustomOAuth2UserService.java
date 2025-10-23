package com.example.librarymanagement.security.service;

import com.example.librarymanagement.entity.Role;
import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.entity.UserProfile;
import com.example.librarymanagement.enumeration.SocialProvider;
import com.example.librarymanagement.exception.OAuth2AuthenticationProcessingException;
import com.example.librarymanagement.repository.RoleRepository;
import com.example.librarymanagement.repository.UserProfileRepository;
import com.example.librarymanagement.repository.UserRepository;
import com.example.librarymanagement.security.oauth2.CustomOAuth2User;
import com.example.librarymanagement.security.oauth2.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        try {
            return processOAuth2User(oAuth2User, request);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationProcessingException(ex.getMessage(), ex);
        }
    }

    private OAuth2User processOAuth2User(OAuth2User oAuth2User, OAuth2UserRequest request) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.builder()
                .id((String) attributes.get(("sub")))
                .email((String) attributes.get("email"))
                .fullname((String) attributes.get(("name")))
                .picture((String) attributes.get(("picture")))
                .provider("google")
                .build();

        String email = oAuth2UserInfo.getEmail();
        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            user = createNewUser(oAuth2UserInfo, request);
        }
        return CustomOAuth2User.builder()
                .user(user)
                .attributes(attributes)
                .build();
    }

    private User createNewUser(OAuth2UserInfo oAuth2UserInfo, OAuth2UserRequest request) {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("User Role not found"));

        String registrationId = request.getClientRegistration().getRegistrationId();
        SocialProvider provider = SocialProvider.valueOf(registrationId.toUpperCase());

        User user = User.builder()
                .password(null)
                .role(userRole)
                .providerId(oAuth2UserInfo.getId())
                .status(User.UserStatus.ACTIVE)
                .email(oAuth2UserInfo.getEmail())
                .isEmailVerified(true)
                .provider(provider)
                .build();
        userRepository.save(user);

        UserProfile userProfile = UserProfile.builder()
                .user(user)
                .fullName(oAuth2UserInfo.getFullname())
                .avatarUrl(oAuth2UserInfo.getPicture())
                .build();
        userProfileRepository.save(userProfile);

        user.setUserProfile(userProfile);
        log.info("Registered new user via {}: {}", SocialProvider.GOOGLE, user.getEmail());
        return userRepository.save(user);
    }
}
/*
    Local login → UserDetails làm principal
    Google login → OAuth2User làm principal
* */