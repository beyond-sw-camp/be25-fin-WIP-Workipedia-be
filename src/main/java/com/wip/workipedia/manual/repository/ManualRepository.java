package com.wip.workipedia.manual.repository;

import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.domain.ManualStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ManualRepository extends JpaRepository<Manual, Long> {

    Page<Manual> findByDeletedAtIsNullAndStatus(ManualStatus status, Pageable pageable);

    Page<Manual> findByDeletedAtIsNull(Pageable pageable);

    List<Manual> findTop10ByDeletedAtIsNullAndStatusOrderByCreatedAtDesc(ManualStatus status);

    Optional<Manual> findByManualIdAndDeletedAtIsNull(Long manualId);

    Optional<Manual> findByManualIdAndDeletedAtIsNullAndStatus(Long manualId, ManualStatus status);

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
