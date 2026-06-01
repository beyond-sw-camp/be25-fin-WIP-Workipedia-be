package com.wip.workipedia.esg.repository;

import com.wip.workipedia.esg.domain.EsgMetricSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EsgMetricSnapshotRepository extends JpaRepository<EsgMetricSnapshot, Long> {
}
