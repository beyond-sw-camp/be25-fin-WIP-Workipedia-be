package com.wip.workipedia.worki.repository;

import com.wip.workipedia.worki.domain.WorkiAnswer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkiAnswerRepository extends JpaRepository<WorkiAnswer, Long> {

    List<WorkiAnswer> findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long questionId);

    Optional<WorkiAnswer> findByAnswerIdAndDeletedAtIsNull(Long answerId);

    boolean existsByQuestionIdAndAcceptedTrue(Long questionId);

    // 같은 질문에 같은 사용자가 이미 답변했는지 확인한다(답변 등록 포인트를 질문당 1회만 지급하기 위함).
    boolean existsByQuestionIdAndAuthorIdAndDeletedAtIsNull(Long questionId, Long authorId);
}
