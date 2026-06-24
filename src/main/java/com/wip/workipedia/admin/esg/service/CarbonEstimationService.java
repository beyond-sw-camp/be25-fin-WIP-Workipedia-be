package com.wip.workipedia.admin.esg.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.config.InfraEsgProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class CarbonEstimationService {

    private final InfraEsgProperties properties;

    public CarbonEstimationService(InfraEsgProperties properties) {
        this.properties = properties;
    }

    public BigDecimal estimateGramsPerHour(String instanceType, double avgCpuPercent) {
        InfraEsgProperties.InstanceSpec spec = properties.instanceSpecs().get(instanceType);
        if (spec == null) {
            throw new CustomException(ErrorType.INTERNAL_ERROR);
        }
        InfraEsgProperties.Carbon c = properties.carbon();

        double cpuFraction = avgCpuPercent / 100.0;
        double averageWatts = c.minWatts() + cpuFraction * (c.maxWatts() - c.minWatts());
        double computeEnergyKwh = averageWatts * spec.vCpu() / 1000.0;
        double memoryEnergyKwh = spec.memoryGb() * c.memoryEnergyKwhPerGbHour();
        double totalEnergyKwh = (computeEnergyKwh + memoryEnergyKwh) * c.pue();
        double gramsPerHour = totalEnergyKwh * c.emissionFactorKgPerKwh() * 1000.0;

        return BigDecimal.valueOf(gramsPerHour).setScale(2, RoundingMode.HALF_UP);
    }
}
