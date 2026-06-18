package com.wip.workipedia.leaderboard.repository;

import com.wip.workipedia.leaderboard.domain.EsgMetricWeekly;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EsgMetricWeeklyRepository extends JpaRepository<EsgMetricWeekly, Long> {

    @Query(
        value = """
                SELECT
                    COALESCE(SUM(LEAST(daily.cited_answer_count * :minutesPerCitedAnswer, :dailyCapMinutes)), 0)
                        AS savedWorkMinutes,
                    COALESCE(SUM(daily.cited_answer_count), 0)
                        AS citedChatbotAnswerCount
                FROM (
                    SELECT
                        cs.user_id AS user_id,
                        DATE(cm.created_at) AS metric_date,
                        COUNT(*) AS cited_answer_count
                    FROM chatbot_messages cm
                    JOIN chatbot_sessions cs
                        ON cs.session_id = cm.session_id
                    WHERE cm.sender_type = 'ASSISTANT'
                        AND cm.answerable = TRUE
                        AND cm.deleted_at IS NULL
                        AND cm.is_deleted = 'N'
                        AND cs.deleted_at IS NULL
                        AND cs.is_deleted = 'N'
                        AND cm.created_at >= :weekStartAt
                        AND cm.created_at < :weekEndExclusiveAt
                        AND JSON_LENGTH(cm.references_json) > 0
                    GROUP BY cs.user_id, DATE(cm.created_at)
                ) daily
                """,
        nativeQuery = true
    )
    EsgMetricWeeklyCalculationProjection calculateWeeklyMetric(
        LocalDateTime weekStartAt,
        LocalDateTime weekEndExclusiveAt,
        BigDecimal minutesPerCitedAnswer,
        BigDecimal dailyCapMinutes
    );

    boolean existsByMetricWeekStartAndDeletedAtIsNull(LocalDate metricWeekStart);

    Optional<EsgMetricWeekly> findByMetricWeekStartAndDeletedAtIsNull(LocalDate metricWeekStart);

    Optional<EsgMetricWeekly> findTopByDeletedAtIsNullOrderByMetricWeekStartDesc();

    @Query(
        value = """
                SELECT GET_LOCK(:lockName, 0)
                """,
        nativeQuery = true
    )
    Integer getLock(String lockName);

    @Query(
        value = """
                SELECT RELEASE_LOCK(:lockName)
                """,
        nativeQuery = true
    )
    Integer releaseLock(String lockName);
}
