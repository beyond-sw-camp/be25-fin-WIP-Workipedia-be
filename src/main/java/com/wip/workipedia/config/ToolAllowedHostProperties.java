package com.wip.workipedia.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tool")
public record ToolAllowedHostProperties(List<String> allowedHosts) {
}
