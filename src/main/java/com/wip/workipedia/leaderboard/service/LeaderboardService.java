package com.wip.workipedia.leaderboard.service;

import com.wip.workipedia.leaderboard.dto.LeaderboardResponse;
import com.wip.workipedia.leaderboard.domain.LeaderboardSnapshot;
import com.wip.workipedia.leaderboard.repository.LeaderboardCandidateProjection;
import com.wip.workipedia.leaderboard.repository.LeaderboardRankerProjection;
import com.wip.workipedia.leaderboard.repository.LeaderboardSnapshotRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardService {

    private final LeaderboardSnapshotRepository leaderboardSnapshotRepository;

    public LeaderboardResponse getLeaderboard() {
        return leaderboardSnapshotRepository.findLatestRankingPeriodStart()
            .map(this::getLeaderboardByPeriod)
            .orElseGet(LeaderboardResponse::empty);
    }

    private LeaderboardResponse getLeaderboardByPeriod(LocalDate rankingPeriodStart) {
        List<LeaderboardRankerProjection> rankers =
            leaderboardSnapshotRepository.findRankersByRankingPeriodStart(rankingPeriodStart);
        return LeaderboardResponse.from(rankingPeriodStart, rankers);
    }

    @Transactional
    public void refreshWeeklySnapshot() {
        LocalDate rankingPeriodStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime calculatedAt = LocalDateTime.now();
        List<LeaderboardCandidateProjection> candidates = leaderboardSnapshotRepository.findTop3Candidates();

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
