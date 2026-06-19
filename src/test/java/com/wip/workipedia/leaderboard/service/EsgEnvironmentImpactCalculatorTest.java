package com.wip.workipedia.leaderboard.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EsgEnvironmentImpactCalculatorTest {

    @Test
    @DisplayName("exposes EPA smartphone charge emission factor")
    void smartphoneChargeEmissionKgCo2() {
        assertThat(EsgEnvironmentImpactCalculator.smartphoneChargeEmissionKgCo2()).isEqualByComparingTo("0.0124");
    }

    @Test
    @DisplayName("converts CO2 savings to smartphone charge equivalent count")
    void toSmartphoneChargeEquivalentCount() {
        long result = EsgEnvironmentImpactCalculator.toSmartphoneChargeEquivalentCount(new BigDecimal("3.985"));

        assertThat(result).isEqualTo(321L);
    }

    @Test
    @DisplayName("returns zero when CO2 savings is null or not positive")
    void toSmartphoneChargeEquivalentCountReturnsZeroWhenCo2SavedKgIsNotPositive() {
        assertThat(EsgEnvironmentImpactCalculator.toSmartphoneChargeEquivalentCount(null)).isZero();
        assertThat(EsgEnvironmentImpactCalculator.toSmartphoneChargeEquivalentCount(BigDecimal.ZERO)).isZero();
        assertThat(EsgEnvironmentImpactCalculator.toSmartphoneChargeEquivalentCount(new BigDecimal("-1"))).isZero();
    }
}
