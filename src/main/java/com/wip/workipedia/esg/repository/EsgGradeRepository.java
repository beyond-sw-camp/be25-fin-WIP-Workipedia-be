package com.wip.workipedia.esg.repository;

import com.wip.workipedia.esg.domain.EsgGrade;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EsgGradeRepository extends JpaRepository<EsgGrade, Integer> {

	Optional<EsgGrade> findByGradeIdAndDeletedAtIsNull(Integer gradeId);

	// 마이페이지 조회 시 ESG 등급 진행 구간을 점수 순서대로 조회합니다.
	List<EsgGrade> findByDeletedAtIsNullOrderByMinScoreAsc();

	@Query("""
		SELECT grade
		FROM EsgGrade grade
		WHERE grade.deletedAt IS NULL
		  AND grade.minScore <= :esgScore
		  AND (grade.maxScore IS NULL OR grade.maxScore >= :esgScore)
		""")
	Optional<EsgGrade> findActiveGradeByScore(@Param("esgScore") long esgScore);
}
