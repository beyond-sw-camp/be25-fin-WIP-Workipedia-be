package com.wip.workipedia.leaderboard.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class EsgEnvironmentImpactCalculator {

    private static final BigDecimal SMARTPHONE_CHARGE_EMISSION_KG_CO2 = new BigDecimal("0.0124");

    private EsgEnvironmentImpactCalculator() {
    }

    public static BigDecimal smartphoneChargeEmissionKgCo2() {
        return SMARTPHONE_CHARGE_EMISSION_KG_CO2;
    }

    public static long toSmartphoneChargeEquivalentCount(BigDecimal co2SavedKg) {
        if (co2SavedKg == null || co2SavedKg.compareTo(BigDecimal.ZERO) <= 0) {
            return 0L;
        }

        return co2SavedKg
            .divide(SMARTPHONE_CHARGE_EMISSION_KG_CO2, 0, RoundingMode.HALF_UP)
            .longValue();
    }
}
