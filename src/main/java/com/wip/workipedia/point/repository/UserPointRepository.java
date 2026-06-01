package com.wip.workipedia.point.repository;

import com.wip.workipedia.point.domain.UserPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPointRepository extends JpaRepository<UserPoint, Long> {
}
