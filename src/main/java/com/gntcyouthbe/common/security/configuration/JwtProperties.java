package com.gntcyouthbe.common.security.configuration;

import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
@RequiredArgsConstructor
@Getter
public class JwtProperties {

    private final String secret;
    private final String issuer;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public byte[] getSecretBytes() {
        return secret.getBytes(StandardCharsets.UTF_8);
    }
}
