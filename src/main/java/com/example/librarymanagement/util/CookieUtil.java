package com.example.librarymanagement.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {
    @Value("${jwt.refresh.expiration-time}")
    private long refreshTokenExpirationMs;

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    public void addRefreshTokenCookie(HttpServletResponse res, String token) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, token);

        // cookie properties
        // cookie.setDomain(cookieDomain);
        cookie.setHttpOnly(true); // Không thể access bằng JavaScript (XSS protection)
        cookie.setSecure(true); // Chỉ gửi qua HTTPS (production nên set true)
        cookie.setPath("/"); // Available cho tất cả paths
        cookie.setMaxAge((int) refreshTokenExpirationMs / 1000); // 7 days in seconds

        // SameSite attribute (for CSRF protection)
        // Note: SameSite không support trực tiếp trong javax.servlet.http.Cookie
        // Cần set qua response header
        String cookieHeader = String.format(
                "%s=%s; Path=%s; Max-Age=%d; HttpOnly; %s; SameSite=None",
                cookie.getName(),
                cookie.getValue(),
                cookie.getPath(),
                cookie.getMaxAge(),
                "Secure"
        );
        res.addHeader("Set-Cookie", cookieHeader);
    }

    public Optional<String> getRefreshTokenFromCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();

        if (req.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public void deleteRefreshTokenFromCookie(HttpServletResponse res) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);

        res.addCookie(cookie);
    }
}




















