package com.wip.workipedia.leaderboard.repository;

import java.time.LocalDateTime;

public interface LeaderboardRankerProjection {

    int getRankNo();

    Long getUserId();

    String getNickname();

    String getDepartmentName();

    Integer getGradeId();

    String getGradeName();

    String getGradeImageUrl();

    long getEsgScore();

    LocalDateTime getCalculatedAt();
}
