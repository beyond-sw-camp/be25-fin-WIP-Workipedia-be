package com.wip.workipedia.leaderboard.domain;

import com.wip.workipedia.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "esg_metrics_weekly")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EsgMetricWeekly extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "esg_metric_weekly_id")
    private Long esgMetricWeeklyId;

    @Column(name = "metric_week_start", nullable = false)
    private LocalDate metricWeekStart;

    @Column(name = "metric_week_end", nullable = false)
    private LocalDate metricWeekEnd;

    @Column(name = "saved_work_minutes", nullable = false, precision = 12, scale = 2)
    private BigDecimal savedWorkMinutes;

    @Column(name = "saved_work_hours", nullable = false, precision = 12, scale = 2)
    private BigDecimal savedWorkHours;

    @Column(name = "electricity_saved_kwh", nullable = false, precision = 12, scale = 3)
    private BigDecimal electricitySavedKwh;

    @Column(name = "co2_saved_kg", nullable = false, precision = 12, scale = 3)
    private BigDecimal co2SavedKg;

    @Column(name = "cited_chatbot_answer_count", nullable = false)
    private long citedChatbotAnswerCount;

    @Column(name = "calculation_basis_json", nullable = false, columnDefinition = "JSON")
    private String calculationBasisJson;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "is_deleted", nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String isDeleted = "N";

    private EsgMetricWeekly(
        LocalDate metricWeekStart,
        LocalDate metricWeekEnd,
        BigDecimal savedWorkMinutes,
        BigDecimal savedWorkHours,
        BigDecimal electricitySavedKwh,
        BigDecimal co2SavedKg,
        long citedChatbotAnswerCount,
        String calculationBasisJson,
        LocalDateTime calculatedAt
    ) {
        this.metricWeekStart = metricWeekStart;
        this.metricWeekEnd = metricWeekEnd;
        this.savedWorkMinutes = savedWorkMinutes;
        this.savedWorkHours = savedWorkHours;
        this.electricitySavedKwh = electricitySavedKwh;
        this.co2SavedKg = co2SavedKg;
        this.citedChatbotAnswerCount = citedChatbotAnswerCount;
        this.calculationBasisJson = calculationBasisJson;
        this.calculatedAt = calculatedAt;
    }

    public static EsgMetricWeekly create(
        LocalDate metricWeekStart,
        LocalDate metricWeekEnd,
        BigDecimal savedWorkMinutes,
        BigDecimal savedWorkHours,
        BigDecimal electricitySavedKwh,
        BigDecimal co2SavedKg,
        long citedChatbotAnswerCount,
        String calculationBasisJson,
        LocalDateTime calculatedAt
    ) {
        return new EsgMetricWeekly(
            metricWeekStart,
            metricWeekEnd,
            savedWorkMinutes,
            savedWorkHours,
            electricitySavedKwh,
            co2SavedKg,
            citedChatbotAnswerCount,
            calculationBasisJson,
            calculatedAt
        );
    }
}
