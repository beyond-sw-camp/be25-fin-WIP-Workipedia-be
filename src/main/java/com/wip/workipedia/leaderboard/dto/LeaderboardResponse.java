package com.wip.workipedia.leaderboard.dto;

import com.wip.workipedia.leaderboard.domain.EsgMetricWeekly;
import com.wip.workipedia.leaderboard.repository.LeaderboardMySummaryProjection;
import com.wip.workipedia.leaderboard.repository.LeaderboardRankerProjection;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
        long smartphoneChargeEquivalentCount
    ) {

        private static final BigDecimal SMARTPHONE_CHARGE_EMISSION_KG_CO2 = new BigDecimal("0.0124");

        private static EnvironmentImpactResponse empty() {
            return new EnvironmentImpactResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L);
        }

        private static EnvironmentImpactResponse from(EsgMetricWeekly metric) {
            BigDecimal co2SavedKg = metric.getCo2SavedKg();
            return new EnvironmentImpactResponse(
                metric.getSavedWorkHours(),
                metric.getElectricitySavedKwh(),
                co2SavedKg,
                calculateSmartphoneChargeEquivalentCount(co2SavedKg)
            );
        }

        private static long calculateSmartphoneChargeEquivalentCount(BigDecimal co2SavedKg) {
            if (co2SavedKg == null || co2SavedKg.compareTo(BigDecimal.ZERO) <= 0) {
                return 0L;
            }

            return co2SavedKg
                .divide(SMARTPHONE_CHARGE_EMISSION_KG_CO2, 0, RoundingMode.HALF_UP)
                .longValue();
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
