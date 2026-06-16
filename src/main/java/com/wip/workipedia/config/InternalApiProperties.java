package com.wip.workipedia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("internal")
public record InternalApiProperties(String apiKey) {
}
