package com.wip.workipedia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ai-sync.worker")
public record AiSyncProperties(
    long fixedDelayMs,
    int batchSize,
    int leaseMinutes
) {}
