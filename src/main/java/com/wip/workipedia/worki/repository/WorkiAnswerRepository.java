package com.wip.workipedia.worki.repository;

import com.wip.workipedia.worki.domain.WorkiAnswer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkiAnswerRepository extends JpaRepository<WorkiAnswer, Long> {

    List<WorkiAnswer> findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long questionId);

    Optional<WorkiAnswer> findByAnswerIdAndDeletedAtIsNull(Long answerId);

    boolean existsByQuestionIdAndAcceptedTrue(Long questionId);

    @Query("""
            SELECT a.questionId AS questionId, COUNT(a) AS answerCount
              FROM WorkiAnswer a
             WHERE a.deletedAt IS NULL
               AND a.questionId IN :questionIds
             GROUP BY a.questionId
            """)
    List<QuestionAnswerCount> countAnswersByQuestionIds(@Param("questionIds") List<Long> questionIds);
}
