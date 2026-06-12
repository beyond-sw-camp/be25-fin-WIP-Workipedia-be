CREATE TABLE leaderboard_snapshots (
    leaderboard_snapshot_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ranking_period_start DATE NOT NULL,
    calculated_at DATETIME NOT NULL,
    rank_no INT NOT NULL,
    user_id BIGINT NOT NULL,
    grade_id INT NOT NULL,
    esg_score BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    modified_source VARCHAR(30) NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT fk_leaderboard_snapshots_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id),
    CONSTRAINT fk_leaderboard_snapshots_grade
        FOREIGN KEY (grade_id)
        REFERENCES esg_grade (grade_id),
    CONSTRAINT uk_leaderboard_snapshots_period_rank
        UNIQUE (ranking_period_start, rank_no),
    CONSTRAINT uk_leaderboard_snapshots_period_user
        UNIQUE (ranking_period_start, user_id),
    CONSTRAINT ck_leaderboard_snapshots_rank_no
        CHECK (rank_no BETWEEN 1 AND 3),
    CONSTRAINT ck_leaderboard_snapshots_esg_score
        CHECK (esg_score >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_leaderboard_snapshots_period
    ON leaderboard_snapshots (ranking_period_start);

CREATE INDEX idx_leaderboard_snapshots_calculated_at
    ON leaderboard_snapshots (calculated_at);

CREATE INDEX idx_leaderboard_snapshots_user_id
    ON leaderboard_snapshots (user_id);
