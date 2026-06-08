package com.wip.workipedia.esg.repository;

import com.wip.workipedia.esg.domain.EsgGrade;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EsgGradeRepository extends JpaRepository<EsgGrade, Integer> {

	Optional<EsgGrade> findByGradeIdAndDeletedAtIsNull(Integer gradeId);

	// 마이페이지 조회 시 ESG 레벨 진행 구간을 점수 순서대로 조회합니다.
	List<EsgGrade> findByDeletedAtIsNullOrderByMinScoreAsc();
}
