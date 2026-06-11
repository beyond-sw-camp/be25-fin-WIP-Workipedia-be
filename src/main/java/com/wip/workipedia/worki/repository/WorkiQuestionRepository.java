package com.wip.workipedia.worki.repository;

import com.wip.workipedia.worki.domain.WorkiQuestion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface WorkiQuestionRepository extends JpaRepository<WorkiQuestion, Long> {

    Page<WorkiQuestion> findByDeletedAtIsNull(Pageable pageable);

    // 전체 재색인(reindexAll)용. 삭제되지 않은 질문만 DB에서 걸러 가져온다.
    List<WorkiQuestion> findByDeletedAtIsNull();

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

    // Redis에 모아둔 조회 증가분(amount)을 한 번에 더해 반영한다. 스케줄러의 일괄 flush에서 호출.
    // 질문 1건씩 독립된 트랜잭션으로 커밋한다(@Transactional). flush 루프에서 한 건이 실패해도
    // 앞서 성공한 다른 질문의 반영이 함께 롤백되지 않게 하기 위함이다.
    @Transactional
    @Modifying // 트랜젝션을 체크 하더라.
    @Query("""
            UPDATE WorkiQuestion q
               SET q.viewCount = q.viewCount + :amount
             WHERE q.questionId = :questionId
               AND q.deletedAt IS NULL
            """)
    int increaseViewCount(Long questionId, long amount);
}
