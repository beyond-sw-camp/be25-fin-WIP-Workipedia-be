package com.wip.workipedia.leaderboard.dto;

import com.wip.workipedia.leaderboard.repository.LeaderboardRankerProjection;
import com.wip.workipedia.leaderboard.repository.LeaderboardMySummaryProjection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record LeaderboardResponse(
    LocalDate rankingPeriodStart,
    LocalDateTime calculatedAt,
    List<LeaderboardRankerResponse> topRankers,
    LeaderboardMySummaryResponse mySummary,
    long totalEsgScore
) {

    // 스냅샷이 아직 생성되지 않은 초기 상태에서는 화면이 빈 값과 0점을 표시하도록 응답한다.
    public static LeaderboardResponse empty() {
        return new LeaderboardResponse(null, null, List.of(), null, 0L);
    }

    public static LeaderboardResponse from(
        LocalDate rankingPeriodStart,
        List<LeaderboardRankerProjection> rankers,
        Optional<LeaderboardMySummaryProjection> mySummary,
        long totalEsgScore
    ) {
        LocalDateTime calculatedAt = rankers.isEmpty() ? null : rankers.get(0).getCalculatedAt();
        return new LeaderboardResponse(
            rankingPeriodStart,
            calculatedAt,
            rankers.stream()
                .map(LeaderboardRankerResponse::from)
                .toList(),
            mySummary.map(LeaderboardMySummaryResponse::from).orElse(null),
            totalEsgScore
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
