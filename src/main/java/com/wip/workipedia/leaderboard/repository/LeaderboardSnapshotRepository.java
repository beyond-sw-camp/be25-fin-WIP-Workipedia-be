package com.wip.workipedia.leaderboard.repository;

import com.wip.workipedia.leaderboard.domain.LeaderboardSnapshot;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LeaderboardSnapshotRepository extends JpaRepository<LeaderboardSnapshot, Long> {

    List<LeaderboardSnapshot> findByRankingPeriodStartAndDeletedAtIsNullOrderByRankNoAsc(LocalDate rankingPeriodStart);

    @Query("""
            SELECT MAX(l.rankingPeriodStart)
              FROM LeaderboardSnapshot l
             WHERE l.deletedAt IS NULL
            """)
    Optional<LocalDate> findLatestRankingPeriodStart();
}
