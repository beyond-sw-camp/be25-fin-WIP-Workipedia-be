package com.wip.workipedia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ai-sync.worker")
public record AiSyncProperties(
    String documentCron,
    String textCron,
    int batchSize,
    int leaseMinutes
) {}
