package com.wip.workipedia.leaderboard.repository;

import com.wip.workipedia.leaderboard.domain.LeaderboardSnapshot;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface LeaderboardSnapshotRepository extends JpaRepository<LeaderboardSnapshot, Long> {

    // 스냅샷 생성 대상은 TOP 3가 아니라 전체 활성 사용자다. 그래야 TOP 3 밖의 내 순위도 조회할 수 있다.
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
                """,
        nativeQuery = true
    )
    List<LeaderboardCandidateProjection> findSnapshotCandidates();

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
                    AND l.rank_no <= 3
                    AND u.deleted_at IS NULL
                    AND u.status = 'ACTIVE'
                    AND d.deleted_at IS NULL
                    AND eg.deleted_at IS NULL
                ORDER BY l.rank_no ASC
                """,
        nativeQuery = true
    )
    List<LeaderboardRankerProjection> findRankersByRankingPeriodStart(LocalDate rankingPeriodStart);

    @Query(
        value = """
                SELECT
                    l.rank_no AS rankNo,
                    l.user_id AS userId,
                    l.grade_id AS gradeId,
                    eg.grade_name AS gradeName,
                    eg.grade_image_url AS gradeImageUrl,
                    l.esg_score AS esgScore,
                    COUNT(wa.answer_id) AS answerCount,
                    COALESCE(SUM(CASE WHEN wa.accepted = TRUE THEN 1 ELSE 0 END), 0) AS acceptedAnswerCount
                FROM leaderboard_snapshots l
                JOIN users u
                    ON l.user_id = u.user_id
                JOIN esg_grade eg
                    ON l.grade_id = eg.grade_id
                LEFT JOIN worki_answers wa
                    ON wa.author_id = l.user_id
                    AND wa.deleted_at IS NULL
                WHERE l.ranking_period_start = :rankingPeriodStart
                    AND l.user_id = :userId
                    AND l.deleted_at IS NULL
                    AND u.deleted_at IS NULL
                    AND u.status = 'ACTIVE'
                    AND eg.deleted_at IS NULL
                GROUP BY
                    l.rank_no,
                    l.user_id,
                    l.grade_id,
                    eg.grade_name,
                    eg.grade_image_url,
                    l.esg_score
                """,
        nativeQuery = true
    )
    Optional<LeaderboardMySummaryProjection> findMySummaryByRankingPeriodStartAndUserId(
        LocalDate rankingPeriodStart,
        Long userId
    );

    @Query(
        value = """
                SELECT COALESCE(SUM(l.esg_score), 0)
                FROM leaderboard_snapshots l
                WHERE l.ranking_period_start = :rankingPeriodStart
                    AND l.deleted_at IS NULL
                """,
        nativeQuery = true
    )
    long sumEsgScoreByRankingPeriodStart(LocalDate rankingPeriodStart);

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
