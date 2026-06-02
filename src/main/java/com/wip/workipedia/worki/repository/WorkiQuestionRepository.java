package com.wip.workipedia.worki.repository;

import com.wip.workipedia.worki.domain.WorkiQuestion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WorkiQuestionRepository extends JpaRepository<WorkiQuestion, Long> {

    Page<WorkiQuestion> findByDeletedAtIsNull(Pageable pageable);

    Optional<WorkiQuestion> findByQuestionIdAndDeletedAtIsNull(Long questionId);

    @Query(value = """
            SELECT q.question_id AS questionId,
                   q.title AS title,
                   q.view_count AS viewCount,
                   q.created_at AS createdAt,
                   COUNT(r.reaction_id) AS likeCount
            FROM worki_questions q
            LEFT JOIN reactions r
              ON r.target_type = 'WORKI_QUESTION'
             AND r.target_id = q.question_id
             AND r.reaction_type = 'LIKE'
            WHERE q.deleted_at IS NULL
            GROUP BY q.question_id, q.title, q.view_count, q.created_at
            ORDER BY likeCount DESC, q.created_at DESC
            LIMIT 10
            """, nativeQuery = true)
    List<PopularWorkiProjection> findTop10PopularByLike();
}
