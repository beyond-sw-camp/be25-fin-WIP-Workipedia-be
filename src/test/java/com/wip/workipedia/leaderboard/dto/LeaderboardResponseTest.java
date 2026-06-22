package com.wip.workipedia.leaderboard.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.wip.workipedia.leaderboard.domain.EsgMetricWeekly;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LeaderboardResponseTest {

    @Test
    @DisplayName("환경 지표 응답에 CO2 절감량 기준 스마트폰 충전 환산 횟수를 포함한다")
    void environmentImpactIncludesSmartphoneChargeEquivalentCount() {
        EsgMetricWeekly metric = EsgMetricWeekly.create(
            LocalDate.of(2026, 6, 22),
            LocalDate.of(2026, 6, 28),
            new BigDecimal("6252.00"),
            new BigDecimal("104.20"),
            new BigDecimal("8.336"),
            new BigDecimal("3.985"),
            2084L,
            "{}",
            LocalDateTime.of(2026, 6, 29, 0, 0)
        );

        LeaderboardResponse response = LeaderboardResponse.from(
            LocalDate.of(2026, 6, 29),
            LocalDateTime.of(2026, 6, 29, 0, 0),
            List.of(),
            Optional.empty(),
            0L,
            Optional.of(metric)
        );

        assertThat(response.environmentImpact().co2SavedKg()).isEqualByComparingTo("3.985");
        assertThat(response.environmentImpact().smartphoneChargeEquivalentCount()).isEqualTo(321L);
    }

    @Test
    @DisplayName("환경 지표 스냅샷이 없으면 스마트폰 충전 환산 횟수는 0으로 응답한다")
    void emptyEnvironmentImpactHasZeroSmartphoneChargeEquivalentCount() {
        LeaderboardResponse response = LeaderboardResponse.empty();

        assertThat(response.environmentImpact().smartphoneChargeEquivalentCount()).isZero();
    }
}
