package com.wip.workipedia.leaderboard.repository;

public interface LeaderboardMySummaryProjection {

    int getRankNo();

    Long getUserId();

    Integer getGradeId();

    String getGradeName();

    String getGradeImageUrl();

    long getEsgScore();

    long getAnswerCount();

    long getAcceptedAnswerCount();
}
