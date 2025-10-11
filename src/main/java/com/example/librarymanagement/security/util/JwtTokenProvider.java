package com.example.librarymanagement.security.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {
    @Value("${jwt.access.secret-key}")
    private String accessTokenSecret;

    @Value("${jwt.refresh.secret-key}")
    private String refreshTokenSecret;

    @Value("${jwt.access.expiration-time}")
    @Getter
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh.expiration-time}")
    @Getter
    private long refreshTokenExpirationMs;

    private SecretKey accessKey;
    private SecretKey refreshKey;
    private JwtParser accessParser;
    private JwtParser refreshParser;

    @PostConstruct
    void init() {
        accessKey = Keys.hmacShaKeyFor(decodeKey(accessTokenSecret));
        refreshKey = Keys.hmacShaKeyFor(decodeKey(refreshTokenSecret));

        accessParser = Jwts.parserBuilder().setSigningKey(accessKey).build();
        refreshParser = Jwts.parserBuilder().setSigningKey(refreshKey).build();
    }

    private byte[] decodeKey(String raw) {
        return Base64.getDecoder().decode(raw);
    }

    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setId(UUID.randomUUID().toString())
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setId(UUID.randomUUID().toString())
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private enum TokenKind {ACCESS, REFRESH}

    // ------------ Helpers ------------
    private Claims parseClaims(String token, TokenKind kind) {
        JwtParser parser = (kind == TokenKind.ACCESS) ? accessParser : refreshParser;
        return parser.parseClaimsJws(token).getBody();
    }

    // ------------ Extractors ------------
    public String getEmailFromToken(String token) {
        return parseClaims(token, TokenKind.ACCESS).getSubject();
    }

//    public String getSubjectFromRefreshToken(String token) {
//        return parseClaims(token, TokenKind.REFRESH).getSubject();
//    }

    public String getJtiFromAccessToken(String token) {
        return parseClaims(token, TokenKind.ACCESS).getId();
    }

    public String getJtiFromRefreshToken(String token) {
        return parseClaims(token, TokenKind.REFRESH).getId();
    }

    public Date getExpirationFromAccessToken(String token) {
        return parseClaims(token, TokenKind.ACCESS).getExpiration();
    }

    public Date getExpirationFromRefreshToken(String token) {
        return parseClaims(token, TokenKind.REFRESH).getExpiration();
    }

    // ------------ Validate ------------
    public boolean validateAccessToken(String token) {
        return validate(token, TokenKind.ACCESS);
    }

    public boolean validateRefreshToken(String token) {
        return validate(token, TokenKind.REFRESH);
    }

    private boolean validate(String token, TokenKind kind) {
        try {
            parseClaims(token, kind);
            return true;
        } catch (SecurityException ex) {
            log.warn("{} Invalid JWT signature: {}" + kind + ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("{} Invalid JWT token: {}" + kind + ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.warn("{} Expired JWT token: {}" + kind + ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("{} Unsupported JWT token: {}" + kind + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            // JWT claims string is empty
        }
        return false;
    }
}

/*
* java.lang.Exception
 └── io.jsonwebtoken.JwtException
      ├── ExpiredJwtException           (token hết hạn)
      ├── UnsupportedJwtException       (JWT không được hỗ trợ)
      ├── MalformedJwtException         (JWT bị sai định dạng)
      ├── SignatureException            (chữ ký sai hoặc bị sửa)
      ├── PrematureJwtException         (token chưa đến thời điểm có hiệu lực)
      ├── MissingClaimException         (thiếu claim bắt buộc)
      ├── IncorrectClaimException       (claim sai giá trị)
* */
































































































