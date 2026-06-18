package com.wip.workipedia.leaderboard.repository;

import java.math.BigDecimal;

public interface EsgMetricWeeklyCalculationProjection {

    BigDecimal getSavedWorkMinutes();

    Long getCitedChatbotAnswerCount();
}
