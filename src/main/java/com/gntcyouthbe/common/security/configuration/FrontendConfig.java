package com.gntcyouthbe.common.security.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FrontendProperties.class)
public class FrontendConfig {
}
