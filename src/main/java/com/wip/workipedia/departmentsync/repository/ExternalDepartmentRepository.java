package com.wip.workipedia.departmentsync.repository;

import com.wip.workipedia.departmentsync.domain.ExternalDepartment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalDepartmentRepository extends JpaRepository<ExternalDepartment, Long> {
	Optional<ExternalDepartment> findBySourceSystemAndExternalId(String sourceSystem, String externalId);

	List<ExternalDepartment> findBySourceSystem(String sourceSystem);
}
