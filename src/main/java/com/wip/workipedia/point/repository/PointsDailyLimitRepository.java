package com.wip.workipedia.point.repository;

import com.wip.workipedia.point.domain.PointsDailyLimit;
import com.wip.workipedia.point.domain.PointsDailyLimitId;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PointsDailyLimitRepository extends JpaRepository<PointsDailyLimit, PointsDailyLimitId> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT p
		FROM PointsDailyLimit p
		WHERE p.id.userId = :userId
			AND p.id.pointDate = :pointDate
			AND p.deletedAt IS NULL
		""")
	Optional<PointsDailyLimit> findActiveByUserIdAndPointDateForUpdate(
		@Param("userId") Long userId,
		@Param("pointDate") LocalDate pointDate
	);
}
