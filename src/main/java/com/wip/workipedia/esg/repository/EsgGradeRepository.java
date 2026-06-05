package com.wip.workipedia.esg.repository;

import com.wip.workipedia.esg.domain.EsgGrade;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EsgGradeRepository extends JpaRepository<EsgGrade, Integer> {

	Optional<EsgGrade> findByGradeIdAndDeletedAtIsNull(Integer gradeId);
}
