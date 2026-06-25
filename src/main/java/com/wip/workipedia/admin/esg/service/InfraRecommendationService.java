package com.wip.workipedia.admin.esg.service;

import com.wip.workipedia.config.InfraEsgProperties;
import com.wip.workipedia.admin.esg.domain.OptimizationType;
import com.wip.workipedia.admin.esg.domain.RecommendationStatus;
import com.wip.workipedia.admin.esg.dto.ResourceRecommendationDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class InfraRecommendationService {

    private final InfraEsgProperties properties;
    private final CarbonEstimationService carbonEstimationService;

    public InfraRecommendationService(InfraEsgProperties properties,
                                      CarbonEstimationService carbonEstimationService) {
        this.properties = properties;
        this.carbonEstimationService = carbonEstimationService;
    }

    public ResourceRecommendationDto evaluate(InfraEsgProperties.MonitoredResource resource,
                                              CpuMetrics metrics) {
        String currentType = resource.instanceType();
        BigDecimal currentCarbon =
            carbonEstimationService.estimateGramsPerHour(currentType, metrics.averageCpu());

        InfraEsgProperties.Thresholds t = properties.thresholds();
        String downsizeTarget = properties.downsizeMap().get(currentType);
        boolean underUtilized = metrics.averageCpu() < t.avgCpuPercent()
            && metrics.maxCpu() < t.maxCpuPercent();

        if (underUtilized && downsizeTarget != null) {
            BigDecimal recommendedCarbon =
                carbonEstimationService.estimateGramsPerHour(downsizeTarget, metrics.averageCpu());
            BigDecimal saving = currentCarbon.subtract(recommendedCarbon)
                .setScale(2, RoundingMode.HALF_UP);
            return new ResourceRecommendationDto(
                resource.name(),
                resource.role(),
                OptimizationType.INSTANCE_DOWNSIZE,
                currentType,
                downsizeTarget,
                round1(metrics.averageCpu()),
                round1(metrics.maxCpu()),
                currentCarbon,
                recommendedCarbon,
                saving,
                downsizeTarget + " 변경 검토",
                RecommendationStatus.RECOMMENDED
            );
        }

        return new ResourceRecommendationDto(
            resource.name(),
            resource.role(),
            OptimizationType.KEEP,
            currentType,
            currentType,
            round1(metrics.averageCpu()),
            round1(metrics.maxCpu()),
            currentCarbon,
            currentCarbon,
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
            "현재 구성을 유지합니다.",
            RecommendationStatus.KEEP
        );
    }

    /**
     * Auto Scaling Group 리소스를 평가한다. CPU가 임계값보다 낮고 현재 InService 인스턴스가
     * 2대 이상이면 desired capacity를 한 대 줄이는 ASG_SCALE_IN을 권장한다(최소 1대 유지).
     *
     * @param inServiceCount 현재 InService 인스턴스 수
     */
    public ResourceRecommendationDto evaluateAsg(InfraEsgProperties.MonitoredResource resource,
                                                 CpuMetrics metrics,
                                                 int inServiceCount) {
        String type = resource.instanceType();
        BigDecimal perInstanceCarbon =
            carbonEstimationService.estimateGramsPerHour(type, metrics.averageCpu());
        BigDecimal currentCarbon = perInstanceCarbon.multiply(BigDecimal.valueOf(inServiceCount));

        InfraEsgProperties.Thresholds t = properties.thresholds();
        boolean underUtilized = metrics.averageCpu() < t.avgCpuPercent()
            && metrics.maxCpu() < t.maxCpuPercent();

        if (underUtilized && inServiceCount > 1) {
            int recommendedCount = inServiceCount - 1;
            BigDecimal recommendedCarbon =
                perInstanceCarbon.multiply(BigDecimal.valueOf(recommendedCount));
            BigDecimal saving = currentCarbon.subtract(recommendedCarbon)
                .setScale(2, RoundingMode.HALF_UP);
            return new ResourceRecommendationDto(
                resource.name(),
                resource.role(),
                OptimizationType.ASG_SCALE_IN,
                type + " × " + inServiceCount,
                type + " × " + recommendedCount,
                round1(metrics.averageCpu()),
                round1(metrics.maxCpu()),
                scale2(currentCarbon),
                scale2(recommendedCarbon),
                saving,
                "desired capacity " + inServiceCount + " → " + recommendedCount + " 검토",
                RecommendationStatus.RECOMMENDED
            );
        }

        return new ResourceRecommendationDto(
            resource.name(),
            resource.role(),
            OptimizationType.KEEP,
            type + " × " + inServiceCount,
            type + " × " + inServiceCount,
            round1(metrics.averageCpu()),
            round1(metrics.maxCpu()),
            scale2(currentCarbon),
            scale2(currentCarbon),
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
            "현재 구성을 유지합니다.",
            RecommendationStatus.KEEP
        );
    }

    private double round1(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private BigDecimal scale2(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
