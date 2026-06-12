package com.wip.workipedia.leaderboard.service;

import com.wip.workipedia.leaderboard.dto.LeaderboardResponse;
import com.wip.workipedia.leaderboard.domain.LeaderboardSnapshot;
import com.wip.workipedia.leaderboard.repository.LeaderboardCandidateProjection;
import com.wip.workipedia.leaderboard.repository.LeaderboardMySummaryProjection;
import com.wip.workipedia.leaderboard.repository.LeaderboardRankerProjection;
import com.wip.workipedia.leaderboard.repository.LeaderboardSnapshotRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardService {

    private final LeaderboardSnapshotRepository leaderboardSnapshotRepository;

    // 최신 주간 스냅샷을 기준으로 TOP 3, 내 요약, 전체 ESG 점수 합계를 한 번에 조회한다.
    public LeaderboardResponse getLeaderboard(Long userId) {
        return leaderboardSnapshotRepository.findLatestRankingPeriodStart()
            .map(rankingPeriodStart -> getLeaderboardByPeriod(rankingPeriodStart, userId))
            .orElseGet(LeaderboardResponse::empty);
    }

    private LeaderboardResponse getLeaderboardByPeriod(LocalDate rankingPeriodStart, Long userId) {
        List<LeaderboardRankerProjection> rankers =
            leaderboardSnapshotRepository.findRankersByRankingPeriodStart(rankingPeriodStart);
        Optional<LeaderboardMySummaryProjection> mySummary =
            leaderboardSnapshotRepository.findMySummaryByRankingPeriodStartAndUserId(rankingPeriodStart, userId);
        long totalEsgScore = leaderboardSnapshotRepository.sumEsgScoreByRankingPeriodStart(rankingPeriodStart);
        return LeaderboardResponse.from(rankingPeriodStart, rankers, mySummary, totalEsgScore);
    }

    // 매주 월요일 09:00 기준 전체 활성 사용자 순위를 스냅샷으로 재생성한다.
    @Transactional
    public void refreshWeeklySnapshot() {
        LocalDate rankingPeriodStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime calculatedAt = LocalDateTime.now();
        List<LeaderboardCandidateProjection> candidates = leaderboardSnapshotRepository.findSnapshotCandidates();

        leaderboardSnapshotRepository.deleteByRankingPeriodStart(rankingPeriodStart);
        leaderboardSnapshotRepository.saveAll(toSnapshots(rankingPeriodStart, calculatedAt, candidates));
    }

    private List<LeaderboardSnapshot> toSnapshots(
        LocalDate rankingPeriodStart,
        LocalDateTime calculatedAt,
        List<LeaderboardCandidateProjection> candidates
    ) {
        int[] rank = {1};
        return candidates.stream()
            .map(candidate -> LeaderboardSnapshot.create(
                rankingPeriodStart,
                calculatedAt,
                rank[0]++,
                candidate.getUserId(),
                candidate.getGradeId(),
                candidate.getEsgScore()
            ))
            .toList();
    }
}
