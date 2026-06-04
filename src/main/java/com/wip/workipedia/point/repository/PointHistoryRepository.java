package com.wip.workipedia.point.repository;

import com.wip.workipedia.point.domain.PointHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

	List<PointHistory> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);
}
