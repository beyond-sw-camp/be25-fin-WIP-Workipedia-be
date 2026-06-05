package com.wip.workipedia.department.repository;

import com.wip.workipedia.department.domain.Department;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

	// 삭제되지 않은 부서 한정 오름차순 조회
	List<Department> findByDeletedAtIsNullOrderByDepartmentIdAsc();

	Optional<Department> findByDepartmentIdAndDeletedAtIsNull(Long departmentId);

	boolean existsByDepartmentName(String departmentName);

	boolean existsByDepartmentNameAndDepartmentIdNot(String departmentName, Long departmentId);
}