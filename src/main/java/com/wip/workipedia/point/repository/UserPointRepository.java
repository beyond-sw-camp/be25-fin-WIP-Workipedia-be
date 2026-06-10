package com.wip.workipedia.point.repository;

import com.wip.workipedia.point.domain.UserPoint;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPointRepository extends JpaRepository<UserPoint, Long> {

	Optional<UserPoint> findByUserIdAndDeletedAtIsNull(Long userId);

	List<UserPoint> findByDeletedAtIsNull();
}
