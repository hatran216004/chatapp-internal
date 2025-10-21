package com.example.librarymanagement.security.oauth2;

import com.example.librarymanagement.entity.User;
import com.example.librarymanagement.security.util.JwtTokenProvider;
import com.example.librarymanagement.service.inter.RefreshTokenService;
import com.example.librarymanagement.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String targetUrl = determineTargetUrl(request, response, authentication);

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        // Generate JWT tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        refreshTokenService.saveRefreshToken(user, refreshToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);

        log.info("OAuth2 login successful for user: {}", user.getEmail());

        return UriComponentsBuilder.fromUriString("http://localhost:3000/api/v1/users/me")
                .queryParam("token", accessToken)
                .build().toUriString();
    }
}
