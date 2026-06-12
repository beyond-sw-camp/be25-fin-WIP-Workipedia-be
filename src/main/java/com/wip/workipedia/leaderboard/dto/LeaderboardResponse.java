package com.wip.workipedia.leaderboard.dto;

import com.wip.workipedia.leaderboard.repository.LeaderboardRankerProjection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record LeaderboardResponse(
    LocalDate rankingPeriodStart,
    LocalDateTime calculatedAt,
    List<LeaderboardRankerResponse> topRankers
) {

    public static LeaderboardResponse empty() {
        return new LeaderboardResponse(null, null, List.of());
    }

    public static LeaderboardResponse from(LocalDate rankingPeriodStart, List<LeaderboardRankerProjection> rankers) {
        LocalDateTime calculatedAt = rankers.isEmpty() ? null : rankers.get(0).getCalculatedAt();
        return new LeaderboardResponse(
            rankingPeriodStart,
            calculatedAt,
            rankers.stream()
                .map(LeaderboardRankerResponse::from)
                .toList()
        );
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
}
