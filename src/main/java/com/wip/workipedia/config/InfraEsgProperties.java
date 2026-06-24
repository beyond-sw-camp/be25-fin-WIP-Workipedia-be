package com.wip.workipedia.config;

import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "infra.esg")
public record InfraEsgProperties(
    String region,
    Carbon carbon,
    Thresholds thresholds,
    Map<String, InstanceSpec> instanceSpecs,
    Map<String, String> downsizeMap,
    List<MonitoredResource> resources
) {
    public record Carbon(
        double emissionFactorKgPerKwh,
        double pue,
        double memoryEnergyKwhPerGbHour,
        double minWatts,
        double maxWatts
    ) {}

    public record Thresholds(
        double avgCpuPercent,
        double maxCpuPercent
    ) {}

    public record InstanceSpec(
        int vCpu,
        double memoryGb
    ) {}

    public record MonitoredResource(
        String name,
        String instanceId,
        String role,
        String instanceType
    ) {}
}
