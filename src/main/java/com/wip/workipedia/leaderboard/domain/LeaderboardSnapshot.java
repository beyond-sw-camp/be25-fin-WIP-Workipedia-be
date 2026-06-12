package com.wip.workipedia.leaderboard.domain;

import com.wip.workipedia.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "leaderboard_snapshots")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeaderboardSnapshot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leaderboard_snapshot_id")
    private Long leaderboardSnapshotId;

    @Column(name = "ranking_period_start", nullable = false)
    private LocalDate rankingPeriodStart;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "rank_no", nullable = false)
    private int rankNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "grade_id", nullable = false)
    private Integer gradeId;

    @Column(name = "esg_score", nullable = false)
    private long esgScore;

    @Column(name = "is_deleted", nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String isDeleted = "N";

    private LeaderboardSnapshot(
        LocalDate rankingPeriodStart,
        LocalDateTime calculatedAt,
        int rankNo,
        Long userId,
        Integer gradeId,
        long esgScore
    ) {
        this.rankingPeriodStart = rankingPeriodStart;
        this.calculatedAt = calculatedAt;
        this.rankNo = rankNo;
        this.userId = userId;
        this.gradeId = gradeId;
        this.esgScore = esgScore;
    }

    public static LeaderboardSnapshot create(
        LocalDate rankingPeriodStart,
        LocalDateTime calculatedAt,
        int rankNo,
        Long userId,
        Integer gradeId,
        long esgScore
    ) {
        return new LeaderboardSnapshot(rankingPeriodStart, calculatedAt, rankNo, userId, gradeId, esgScore);
    }
}
