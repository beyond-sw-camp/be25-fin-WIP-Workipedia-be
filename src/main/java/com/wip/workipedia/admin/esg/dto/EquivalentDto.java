package com.wip.workipedia.admin.esg.dto;

import java.math.BigDecimal;

public record EquivalentDto(
    BigDecimal smartphoneChargePerHour,
    BigDecimal smartphoneChargePerDay,
    BigDecimal smartphoneChargePerMonth,
    // 절감분의 실시간 누적 스마트폰 충전 횟수(가동 시작부터 computedAt까지).
    BigDecimal smartphoneChargeAccum
) {
}
