package com.wip.workipedia.worki.repository;

import com.wip.workipedia.worki.domain.WorkiAnswer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkiAnswerRepository extends JpaRepository<WorkiAnswer, Long> {

    List<WorkiAnswer> findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long questionId);

    Optional<WorkiAnswer> findByAnswerIdAndDeletedAtIsNull(Long answerId);

    boolean existsByQuestionIdAndAcceptedTrue(Long questionId);
}
