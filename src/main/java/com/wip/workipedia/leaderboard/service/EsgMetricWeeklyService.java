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
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EsgMetricWeeklyService {

    private static final String SNAPSHOT_LOCK_PREFIX = "esg_metric_weekly:";
    private static final BigDecimal DEFAULT_MINUTES_PER_CITED_ANSWER = new BigDecimal("3");
    private static final BigDecimal DAILY_CAP_MINUTES_PER_USER = new BigDecimal("37.8");
    private static final BigDecimal DEVICE_POWER_KWH_PER_HOUR = new BigDecimal("0.08");
    private static final BigDecimal ELECTRICITY_EMISSION_FACTOR_KG_CO2E_PER_KWH = new BigDecimal("0.478");
    private static final BigDecimal MINUTES_PER_HOUR = new BigDecimal("60");
    private static final int CHATBOT_RESPONSE_SECONDS = 10;
    private static final String MINUTES_PER_CITED_ANSWER_SOURCE_WORKI = "worki_first_answer_average";
    private static final String MINUTES_PER_CITED_ANSWER_SOURCE_FALLBACK = "default_fallback";

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
            if (esgMetricWeeklyRepository.existsByMetricWeekStart(metricWeekStart)) {
                return;
            }

            createWeeklyMetric(metricWeekStart, calculatedAt);
        } finally {
            esgMetricWeeklyRepository.releaseLock(lockName);
        }
    }

    @Transactional
    public EsgMetricWeekly createWeeklyMetric(LocalDate metricWeekStart, LocalDateTime calculatedAt) {
        if (esgMetricWeeklyRepository.existsByMetricWeekStart(metricWeekStart)) {
            throw new IllegalStateException("ESG weekly metric already exists: " + metricWeekStart);
        }

        LocalDate metricWeekEnd = metricWeekStart.plusDays(6);
        LocalDateTime weekStartAt = metricWeekStart.atStartOfDay();
        LocalDateTime weekEndExclusiveAt = metricWeekStart.plusDays(7).atStartOfDay();
        ResolvedMinutesPerCitedAnswer minutesPerCitedAnswer = resolveMinutesPerCitedAnswer();

        EsgMetricWeeklyCalculationProjection calculation = esgMetricWeeklyRepository.calculateWeeklyMetric(
            weekStartAt,
            weekEndExclusiveAt,
            minutesPerCitedAnswer.minutes(),
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
            calculationBasisJson(minutesPerCitedAnswer),
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

    private ResolvedMinutesPerCitedAnswer resolveMinutesPerCitedAnswer() {
        BigDecimal calculatedMinutes =
            esgMetricWeeklyRepository.calculateAverageSavedMinutesPerWorkiAnswer(CHATBOT_RESPONSE_SECONDS);

        if (calculatedMinutes == null || calculatedMinutes.compareTo(BigDecimal.ZERO) <= 0) {
            return new ResolvedMinutesPerCitedAnswer(
                scaleMinutes(DEFAULT_MINUTES_PER_CITED_ANSWER),
                MINUTES_PER_CITED_ANSWER_SOURCE_FALLBACK
            );
        }

        return new ResolvedMinutesPerCitedAnswer(
            scaleMinutes(calculatedMinutes),
            MINUTES_PER_CITED_ANSWER_SOURCE_WORKI
        );
    }

    private String calculationBasisJson(ResolvedMinutesPerCitedAnswer minutesPerCitedAnswer) {
        try {
            Map<String, Object> basis = new LinkedHashMap<>();
            basis.put("minutesPerCitedAnswer", minutesPerCitedAnswer.minutes());
            basis.put("minutesPerCitedAnswerSource", minutesPerCitedAnswer.source());
            basis.put("fallbackMinutesPerCitedAnswer", DEFAULT_MINUTES_PER_CITED_ANSWER);
            basis.put("chatbotResponseSeconds", CHATBOT_RESPONSE_SECONDS);
            basis.put("dailyCapMinutesPerUser", DAILY_CAP_MINUTES_PER_USER);
            basis.put("benchmarkSearchMinutesPerDay", 108);
            basis.put("benchmarkReductionRate", 0.35);
            basis.put("devicePowerKwhPerHour", DEVICE_POWER_KWH_PER_HOUR);
            basis.put("electricityEmissionFactorKgCo2ePerKwh", ELECTRICITY_EMISSION_FACTOR_KG_CO2E_PER_KWH);
            basis.put("emissionFactorUnit", "kgCO2e/kWh");
            basis.put(
                "smartphoneChargeEmissionKgCo2",
                EsgEnvironmentImpactCalculator.smartphoneChargeEmissionKgCo2()
            );
            return objectMapper.writeValueAsString(basis);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize ESG metric calculation basis", e);
        }
    }

    private record ResolvedMinutesPerCitedAnswer(BigDecimal minutes, String source) {
    }
}
