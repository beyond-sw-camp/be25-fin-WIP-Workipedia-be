package com.wip.workipedia.department.repository;

import com.wip.workipedia.department.domain.DepartmentRoutingPrompt;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRoutingPromptRepository extends JpaRepository<DepartmentRoutingPrompt, Long> {

	List<DepartmentRoutingPrompt> findByDepartment_DepartmentIdInAndDeletedAtIsNullAndIsActive(
		Collection<Long> departmentIds,
		String isActive
	);

	Optional<DepartmentRoutingPrompt> findByDepartment_DepartmentIdAndDeletedAtIsNull(Long departmentId);
}
