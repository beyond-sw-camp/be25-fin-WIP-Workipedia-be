package com.wip.workipedia.leaderboard.repository;

import com.wip.workipedia.leaderboard.domain.LeaderboardSnapshot;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface LeaderboardSnapshotRepository extends JpaRepository<LeaderboardSnapshot, Long> {

    List<LeaderboardSnapshot> findByRankingPeriodStartAndDeletedAtIsNullOrderByRankNoAsc(LocalDate rankingPeriodStart);

    @Query(
        value = """
                SELECT
                    up.user_id AS userId,
                    up.grade_id AS gradeId,
                    up.esg_score AS esgScore
                FROM user_points up
                JOIN users u
                    ON up.user_id = u.user_id
                WHERE up.deleted_at IS NULL
                    AND u.deleted_at IS NULL
                    AND u.status = 'ACTIVE'
                ORDER BY up.esg_score DESC, up.user_id ASC
                LIMIT 3
                """,
        nativeQuery = true
    )
    List<LeaderboardCandidateProjection> findTop3Candidates();

    @Query(
        value = """
                SELECT
                    l.rank_no AS rankNo,
                    l.user_id AS userId,
                    u.nickname AS nickname,
                    d.department_name AS departmentName,
                    l.grade_id AS gradeId,
                    eg.grade_name AS gradeName,
                    eg.grade_image_url AS gradeImageUrl,
                    l.esg_score AS esgScore,
                    l.calculated_at AS calculatedAt
                FROM leaderboard_snapshots l
                JOIN users u
                    ON l.user_id = u.user_id
                JOIN departments d
                    ON u.department_id = d.department_id
                JOIN esg_grade eg
                    ON l.grade_id = eg.grade_id
                WHERE l.ranking_period_start = :rankingPeriodStart
                    AND l.deleted_at IS NULL
                    AND u.deleted_at IS NULL
                    AND u.status = 'ACTIVE'
                    AND d.deleted_at IS NULL
                    AND eg.deleted_at IS NULL
                ORDER BY l.rank_no ASC
                """,
        nativeQuery = true
    )
    List<LeaderboardRankerProjection> findRankersByRankingPeriodStart(LocalDate rankingPeriodStart);

    @Query("""
            SELECT MAX(l.rankingPeriodStart)
              FROM LeaderboardSnapshot l
             WHERE l.deletedAt IS NULL
            """)
    Optional<LocalDate> findLatestRankingPeriodStart();

    @Modifying
    @Query(
        value = """
                DELETE FROM leaderboard_snapshots
                WHERE ranking_period_start = :rankingPeriodStart
                """,
        nativeQuery = true
    )
    void deleteByRankingPeriodStart(LocalDate rankingPeriodStart);
}
