package com.wip.workipedia.point.repository;

import com.wip.workipedia.point.domain.PointHistory;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

	List<PointHistory> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);

	Page<PointHistory> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
