package com.gntcyouthbe.common.security.configuration;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.frontend")
@Getter
public class FrontendProperties {
    private final String url;
    private final String oauth2RedirectPath;

    public FrontendProperties(String url, String oauth2RedirectPath) {
        this.url = url;
        this.oauth2RedirectPath = oauth2RedirectPath;
    }
}
