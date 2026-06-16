package com.wip.workipedia.manual.repository;

import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.domain.ManualStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ManualRepository extends JpaRepository<Manual, Long> {

    // 통합검색용. 발행(PUBLISHED)·미삭제 매뉴얼을 제목/본문에서 키워드로 찾는다.
    // 본문(content, LONGTEXT)에 대한 LIKE는 인덱스를 타지 못해 풀스캔이므로, 매뉴얼 규모가 작다는 전제에서만 유효하다.
    // (규모가 커지면 제목검색 축소 / MariaDB FULLTEXT / Elasticsearch 이행을 고려 — ADR-009 참고)
    @Query("""
            SELECT m FROM Manual m
             WHERE m.deletedAt IS NULL
               AND m.status = :status
               AND (m.title LIKE %:keyword% OR m.content LIKE %:keyword%)
            """)
    Page<Manual> searchByKeyword(@Param("keyword") String keyword,
                                 @Param("status") ManualStatus status,
                                 Pageable pageable);

    Page<Manual> findByDeletedAtIsNullAndStatus(ManualStatus status, Pageable pageable);

    Page<Manual> findByDeletedAtIsNull(Pageable pageable);

    long countByDeletedAtIsNull();

    List<Manual> findTop10ByDeletedAtIsNullAndStatusOrderByCreatedAtDesc(ManualStatus status);

    Optional<Manual> findByManualIdAndDeletedAtIsNull(Long manualId);

    Optional<Manual> findByManualIdAndDeletedAtIsNullAndStatus(Long manualId, ManualStatus status);

    boolean existsByTitleAndDeletedAtIsNull(String title);

    boolean existsByTitleAndManualIdNotAndDeletedAtIsNull(String title, Long manualId);

    @Query(value = """
            SELECT m.manual_id AS manualId,
                   m.title AS title,
                   m.department_id AS departmentId,
                   m.created_at AS createdAt,
                   COUNT(mc.citation_id) AS citationCount
            FROM manuals m
            LEFT JOIN manual_citations mc
              ON mc.manual_id = m.manual_id
             AND mc.source_type = 'CHATBOT_MESSAGE'
             AND mc.deleted_at IS NULL
            WHERE m.deleted_at IS NULL
              AND m.status = 'PUBLISHED'
            GROUP BY m.manual_id, m.title, m.department_id, m.created_at
            ORDER BY citationCount DESC, m.created_at DESC
            LIMIT 10
            """, nativeQuery = true)
    List<PopularManualProjection> findTop10PopularByCitation();
}
