package com.wip.workipedia.leaderboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.leaderboard.domain.EsgMetricWeekly;
import com.wip.workipedia.leaderboard.repository.EsgMetricWeeklyCalculationProjection;
import com.wip.workipedia.leaderboard.repository.EsgMetricWeeklyRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EsgMetricWeeklyService {

    private static final String SNAPSHOT_LOCK_PREFIX = "esg_metric_weekly:";
    private static final BigDecimal MINUTES_PER_CITED_ANSWER = new BigDecimal("3");
    private static final BigDecimal DAILY_CAP_MINUTES_PER_USER = new BigDecimal("37.8");
    private static final BigDecimal DEVICE_POWER_KWH_PER_HOUR = new BigDecimal("0.08");
    private static final BigDecimal ELECTRICITY_EMISSION_FACTOR_KG_CO2E_PER_KWH = new BigDecimal("0.478");
    private static final BigDecimal MINUTES_PER_HOUR = new BigDecimal("60");

    private final EsgMetricWeeklyRepository esgMetricWeeklyRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void refreshPreviousWeekMetric() {
        LocalDate currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate metricWeekStart = currentWeekStart.minusWeeks(1);
        LocalDateTime calculatedAt = LocalDateTime.now();
        String lockName = SNAPSHOT_LOCK_PREFIX + metricWeekStart;

        if (!isLockAcquired(esgMetricWeeklyRepository.getLock(lockName))) {
            return;
        }

        try {
            if (esgMetricWeeklyRepository.existsByMetricWeekStartAndDeletedAtIsNull(metricWeekStart)) {
                return;
            }

            createWeeklyMetric(metricWeekStart, calculatedAt);
        } finally {
            esgMetricWeeklyRepository.releaseLock(lockName);
        }
    }

    @Transactional
    public EsgMetricWeekly createWeeklyMetric(LocalDate metricWeekStart, LocalDateTime calculatedAt) {
        if (esgMetricWeeklyRepository.existsByMetricWeekStartAndDeletedAtIsNull(metricWeekStart)) {
            throw new IllegalStateException("ESG weekly metric already exists: " + metricWeekStart);
        }

        LocalDate metricWeekEnd = metricWeekStart.plusDays(6);
        LocalDateTime weekStartAt = metricWeekStart.atStartOfDay();
        LocalDateTime weekEndExclusiveAt = metricWeekStart.plusDays(7).atStartOfDay();

        EsgMetricWeeklyCalculationProjection calculation = esgMetricWeeklyRepository.calculateWeeklyMetric(
            weekStartAt,
            weekEndExclusiveAt,
            MINUTES_PER_CITED_ANSWER,
            DAILY_CAP_MINUTES_PER_USER
        );

        BigDecimal savedWorkMinutes = scaleMinutes(nullToZero(calculation.getSavedWorkMinutes()));
        BigDecimal savedWorkHours = scaleHours(savedWorkMinutes.divide(MINUTES_PER_HOUR, 4, RoundingMode.HALF_UP));
        BigDecimal electricitySavedKwh = scaleEnergy(savedWorkHours.multiply(DEVICE_POWER_KWH_PER_HOUR));
        BigDecimal co2SavedKg = scaleEnergy(electricitySavedKwh.multiply(ELECTRICITY_EMISSION_FACTOR_KG_CO2E_PER_KWH));
        long citedChatbotAnswerCount = calculation.getCitedChatbotAnswerCount() == null
            ? 0L
            : calculation.getCitedChatbotAnswerCount();

        return esgMetricWeeklyRepository.save(EsgMetricWeekly.create(
            metricWeekStart,
            metricWeekEnd,
            savedWorkMinutes,
            savedWorkHours,
            electricitySavedKwh,
            co2SavedKg,
            citedChatbotAnswerCount,
            calculationBasisJson(),
            calculatedAt
        ));
    }

    private boolean isLockAcquired(Integer lockResult) {
        return lockResult != null && lockResult == 1;
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal scaleMinutes(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleHours(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleEnergy(BigDecimal value) {
        return value.setScale(3, RoundingMode.HALF_UP);
    }

    private String calculationBasisJson() {
        try {
            return objectMapper.writeValueAsString(Map.of(
                "minutesPerCitedAnswer", MINUTES_PER_CITED_ANSWER,
                "dailyCapMinutesPerUser", DAILY_CAP_MINUTES_PER_USER,
                "benchmarkSearchMinutesPerDay", 108,
                "benchmarkReductionRate", 0.35,
                "devicePowerKwhPerHour", DEVICE_POWER_KWH_PER_HOUR,
                "electricityEmissionFactorKgCo2ePerKwh", ELECTRICITY_EMISSION_FACTOR_KG_CO2E_PER_KWH,
                "emissionFactorUnit", "kgCO2e/kWh"
            ));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize ESG metric calculation basis", e);
        }
    }
}
