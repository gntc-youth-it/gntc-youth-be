package com.gntcyouthbe.common.security.service;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.common.security.configuration.JwtProperties;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties properties;
    private Key key;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(properties.getSecretBytes());
    }

    public String createAccessToken(Long userId,
            String email,
            String name,
            Role role,
            ChurchId church,
            AuthProvider provider) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.getAccessTtlSeconds());

        JwtBuilder builder = Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("name", name)
                .claim("role", role.name());

        if (email != null) {
            builder.claim("email", email);
        }

        if (church != null) {
            builder.claim("church", church.name());
        }

        if (provider != null) {
            builder.claim("provider", provider.name());
        }

        return builder
                .signWith(key)
                .compact();
    }

    public String createAccessToken(UserPrincipal principal) {
        return createAccessToken(
                principal.getUserId(),
                principal.getEmail(),
                principal.getName(),
                principal.getRole(),
                principal.getChurch(),
                principal.getProvider()
        );
    }

    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.getRefreshTtlSeconds());
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("type", "refresh")
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(UserPrincipal principal) {
        return createRefreshToken(principal.getUserId());
    }

    public boolean validate(String token) {
        try {
            parseAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Map<String, Object> getClaims(String token) {
        return parseAllClaims(token).getPayload();
    }

    private Jws<Claims> parseAllClaims(String token) {
        return Jwts.parser()
                .requireIssuer(properties.getIssuer())
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token);
    }

}
