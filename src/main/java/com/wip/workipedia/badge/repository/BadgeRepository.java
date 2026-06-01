package com.wip.workipedia.badge.repository;

import com.wip.workipedia.badge.domain.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
}
