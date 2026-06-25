package com.wip.workipedia.admin.esg.dto;

import java.math.BigDecimal;

public record EquivalentDto(
    BigDecimal smartphoneChargePerHour,
    BigDecimal smartphoneChargePerDay,
    BigDecimal smartphoneChargePerMonth
) {
}
