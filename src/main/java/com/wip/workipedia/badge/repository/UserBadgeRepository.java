package com.wip.workipedia.badge.repository;

import com.wip.workipedia.badge.domain.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
}
