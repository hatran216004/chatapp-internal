package com.example.librarymanagement.security.util;

import com.example.librarymanagement.security.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

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

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    public enum TokenKind {ACCESS, REFRESH}

    @PostConstruct
    void init() {
        accessKey = Keys.hmacShaKeyFor(decodeKey(accessTokenSecret));
        refreshKey = Keys.hmacShaKeyFor(decodeKey(refreshTokenSecret));

        accessParser = Jwts.parserBuilder().setSigningKey(accessKey).build();
        refreshParser = Jwts.parserBuilder().setSigningKey(refreshKey).build();
    }

    private byte[] decodeKey(String raw) {
        return Decoders.BASE64.decode(raw);
    }

    public String generateAccessToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);
        return Jwts.builder()
                .setSubject(userPrincipal.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setId(UUID.randomUUID().toString())
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);
        return Jwts.builder()
                .setSubject(userPrincipal.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setId(UUID.randomUUID().toString())
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims extractAllClaims(String token, TokenKind kind) throws JwtException {
        return (kind == TokenKind.ACCESS)
                ? accessParser.parseClaimsJws(token).getBody()
                : refreshParser.parseClaimsJws(token).getBody();
    }

    public <T> T extractClaim(String token, TokenKind kind, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token, kind);
        return resolver.apply(claims);
    }

    public String extractSubject(String token, TokenKind kind) {
        return extractClaim(token, kind, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, TokenKind.ACCESS, Claims::getExpiration);
    }

    public Date extractIssueAt(String token, TokenKind kind) {
        return extractClaim(token, kind, Claims::getIssuedAt);
    }

    public String extractJti(String token, TokenKind kind) {
        return extractClaim(token, kind, Claims::getId);
    }

    public boolean validateAccessToken(String token) {
        return validate(token, TokenKind.ACCESS);
    }

    public boolean validateRefreshToken(String token) {
        return validate(token, TokenKind.REFRESH);
    }

    private boolean validate(String token, TokenKind kind) {
        try {
            extractAllClaims(token, kind);
            return true;
        } catch (SecurityException ex) {
            // Invalid JWT signature
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            // Invalid JWT token
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            // Expired JWT token
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            // Unsupported JWT token
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            // JWT claims string is empty
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        } catch (JwtException ex) {
            // JWT claims string is empty
            logger.error("JWT invalid: {}", ex.getMessage());
        }
        return false;
    }
}

































































































