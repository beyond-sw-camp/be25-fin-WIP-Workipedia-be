package com.wip.workipedia.worki.repository;

import com.wip.workipedia.worki.domain.WorkiQuestion;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkiQuestionRepository extends JpaRepository<WorkiQuestion, Long> {

    Page<WorkiQuestion> findByDeletedAtIsNull(Pageable pageable);

    Optional<WorkiQuestion> findByQuestionIdAndDeletedAtIsNull(Long questionId);
}
