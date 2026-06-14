package com.wip.workipedia.point.repository;

import com.wip.workipedia.point.domain.PointHistory;
import com.wip.workipedia.point.domain.PointHistoryType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

	List<PointHistory> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);

	Page<PointHistory> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId, Pageable pageable);

	Page<PointHistory> findByUserIdAndTypeNotAndDeletedAtIsNullOrderByCreatedAtDesc(
		Long userId,
		PointHistoryType excludedType,
		Pageable pageable
	);

	Page<PointHistory> findByUserIdAndTypeAndDeletedAtIsNullOrderByCreatedAtDesc(
		Long userId,
		PointHistoryType type,
		Pageable pageable
	);

	boolean existsByUserIdAndReasonTypeAndRelatedTypeAndRelatedIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanAndDeletedAtIsNull(
		Long userId,
		String reasonType,
		String relatedType,
		Long relatedId,
		LocalDateTime startAt,
		LocalDateTime endAt
	);

	boolean existsByUserIdAndReasonTypeAndTypeAndDeletedAtIsNull(
		Long userId,
		String reasonType,
		PointHistoryType type
	);

	boolean existsByReasonTypeAndRelatedTypeAndRelatedIdAndTypeAndDeletedAtIsNull(
		String reasonType,
		String relatedType,
		Long relatedId,
		PointHistoryType type
	);
}
