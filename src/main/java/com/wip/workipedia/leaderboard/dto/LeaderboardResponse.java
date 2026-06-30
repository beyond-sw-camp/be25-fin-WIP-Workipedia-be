package com.wip.workipedia.leaderboard.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.leaderboard.domain.EsgMetricWeekly;
import com.wip.workipedia.leaderboard.repository.LeaderboardMySummaryProjection;
import com.wip.workipedia.leaderboard.repository.LeaderboardRankerProjection;
import com.wip.workipedia.leaderboard.service.EsgEnvironmentImpactCalculator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record LeaderboardResponse(
    LocalDate rankingPeriodStart,
    LocalDateTime calculatedAt,
    List<LeaderboardRankerResponse> topRankers,
    LeaderboardMySummaryResponse mySummary,
    long totalEsgScore,
    EnvironmentImpactResponse environmentImpact
) {

    private static final BigDecimal DEFAULT_MINUTES_PER_CITED_ANSWER = BigDecimal.valueOf(3);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static LeaderboardResponse empty() {
        return new LeaderboardResponse(null, null, List.of(), null, 0L, EnvironmentImpactResponse.empty());
    }

    public static LeaderboardResponse from(
        LocalDate rankingPeriodStart,
        LocalDateTime calculatedAt,
        List<LeaderboardRankerProjection> rankers,
        Optional<LeaderboardMySummaryProjection> mySummary,
        long totalEsgScore,
        Optional<EsgMetricWeekly> environmentImpact
    ) {
        return new LeaderboardResponse(
            rankingPeriodStart,
            calculatedAt,
            rankers.stream()
                .map(LeaderboardRankerResponse::from)
                .toList(),
            mySummary.map(LeaderboardMySummaryResponse::from).orElse(null),
            totalEsgScore,
            environmentImpact
                .map(EnvironmentImpactResponse::from)
                .orElseGet(EnvironmentImpactResponse::empty)
        );
    }

    public record EnvironmentImpactResponse(
        BigDecimal savedWorkHours,
        BigDecimal electricitySavedKwh,
        BigDecimal co2SavedKg,
        long smartphoneChargeEquivalentCount,
        BigDecimal minutesPerCitedAnswer
    ) {

        private static EnvironmentImpactResponse empty() {
            return new EnvironmentImpactResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L, BigDecimal.ZERO);
        }

        private static EnvironmentImpactResponse from(EsgMetricWeekly metric) {
            BigDecimal co2SavedKg = metric.getCo2SavedKg();
            return new EnvironmentImpactResponse(
                metric.getSavedWorkHours(),
                metric.getElectricitySavedKwh(),
                co2SavedKg,
                EsgEnvironmentImpactCalculator.toSmartphoneChargeEquivalentCount(co2SavedKg),
                extractMinutesPerCitedAnswer(metric.getCalculationBasisJson())
            );
        }

        private static BigDecimal extractMinutesPerCitedAnswer(String calculationBasisJson) {
            if (calculationBasisJson == null || calculationBasisJson.isBlank()) {
                return DEFAULT_MINUTES_PER_CITED_ANSWER;
            }
            try {
                JsonNode node = OBJECT_MAPPER.readTree(calculationBasisJson).path("minutesPerCitedAnswer");
                if (node.isMissingNode() || node.isNull()) {
                    return DEFAULT_MINUTES_PER_CITED_ANSWER;
                }
                if (node.isNumber()) {
                    return node.decimalValue();
                }
                if (node.isTextual() && !node.asText().isBlank()) {
                    return new BigDecimal(node.asText());
                }
                return DEFAULT_MINUTES_PER_CITED_ANSWER;
            } catch (Exception e) {
                return DEFAULT_MINUTES_PER_CITED_ANSWER;
            }
        }
    }

    public record LeaderboardRankerResponse(
        int rank,
        Long userId,
        String nickname,
        String departmentName,
        Integer gradeId,
        String gradeName,
        String gradeImageUrl,
        long esgScore
    ) {

        private static LeaderboardRankerResponse from(LeaderboardRankerProjection projection) {
            return new LeaderboardRankerResponse(
                projection.getRankNo(),
                projection.getUserId(),
                projection.getNickname(),
                projection.getDepartmentName(),
                projection.getGradeId(),
                projection.getGradeName(),
                projection.getGradeImageUrl(),
                projection.getEsgScore()
            );
        }
    }

    public record LeaderboardMySummaryResponse(
        int rank,
        Long userId,
        String nickname,
        String departmentName,
        Integer gradeId,
        String gradeName,
        String gradeImageUrl,
        long esgScore,
        long answerCount,
        long acceptedAnswerCount
    ) {

        private static LeaderboardMySummaryResponse from(LeaderboardMySummaryProjection projection) {
            return new LeaderboardMySummaryResponse(
                projection.getRankNo(),
                projection.getUserId(),
                projection.getNickname(),
                projection.getDepartmentName(),
                projection.getGradeId(),
                projection.getGradeName(),
                projection.getGradeImageUrl(),
                projection.getEsgScore(),
                projection.getAnswerCount(),
                projection.getAcceptedAnswerCount()
            );
        }
    }
}
