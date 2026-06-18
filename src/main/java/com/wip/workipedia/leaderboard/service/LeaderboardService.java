package com.wip.workipedia.leaderboard.service;

import com.wip.workipedia.leaderboard.dto.LeaderboardResponse;
import com.wip.workipedia.leaderboard.domain.LeaderboardSnapshot;
import com.wip.workipedia.leaderboard.repository.LeaderboardCandidateProjection;
import com.wip.workipedia.leaderboard.repository.EsgMetricWeeklyRepository;
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

    private static final String SNAPSHOT_LOCK_PREFIX = "leaderboard_snapshot:";

    private final LeaderboardSnapshotRepository leaderboardSnapshotRepository;
    private final EsgMetricWeeklyRepository esgMetricWeeklyRepository;

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
        LocalDateTime calculatedAt = leaderboardSnapshotRepository
            .findCalculatedAtByRankingPeriodStart(rankingPeriodStart)
            .orElse(null);
        return LeaderboardResponse.from(
            rankingPeriodStart,
            calculatedAt,
            rankers,
            mySummary,
            totalEsgScore,
            esgMetricWeeklyRepository.findTopByDeletedAtIsNullOrderByMetricWeekStartDesc()
        );
    }

    // 매주 월요일 00:00 기준 전체 활성 사용자 순위를 스냅샷으로 재생성한다.
    @Transactional
    public void refreshWeeklySnapshot() {
        LocalDate rankingPeriodStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime calculatedAt = LocalDateTime.now();
        String lockName = SNAPSHOT_LOCK_PREFIX + rankingPeriodStart;

        if (!isLockAcquired(leaderboardSnapshotRepository.getLock(lockName))) {
            return;
        }

        try {
            if (leaderboardSnapshotRepository.existsByRankingPeriodStartAndDeletedAtIsNull(rankingPeriodStart)) {
                return;
            }

            List<LeaderboardCandidateProjection> candidates = leaderboardSnapshotRepository.findSnapshotCandidates();
            leaderboardSnapshotRepository.saveAll(toSnapshots(rankingPeriodStart, calculatedAt, candidates));
        } finally {
            leaderboardSnapshotRepository.releaseLock(lockName);
        }
    }

    private boolean isLockAcquired(Integer lockResult) {
        return lockResult != null && lockResult == 1;
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
                candidate.getNickname(),
                candidate.getDepartmentName(),
                candidate.getGradeId(),
                candidate.getGradeName(),
                candidate.getGradeImageUrl(),
                candidate.getEsgScore()
            ))
            .toList();
    }
}
