package com.wip.workipedia.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ToolAllowedHostProperties.class)
public class ToolSecurityConfig {
}
