package com.wip.workipedia.search.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 검색어 자동완성용 누적 집계 엔티티.
 * 증가(쓰기)는 native upsert(ON DUPLICATE KEY UPDATE)로 처리하므로 JPA setter/factory는 두지 않고,
 * 자동완성 조회(읽기) 시에만 매핑된다.
 */
@Getter
@Entity
@Table(name = "worki_search_keywords")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkiSearchKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_keyword_id")
    private Long searchKeywordId;

    @Column(name = "keyword", nullable = false, length = 100)
    private String keyword;

    @Column(name = "search_count", nullable = false)
    private long searchCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
