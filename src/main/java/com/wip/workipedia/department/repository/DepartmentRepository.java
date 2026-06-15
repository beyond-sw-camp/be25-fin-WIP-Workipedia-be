package com.wip.workipedia.department.repository;

import com.wip.workipedia.department.domain.Department;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

	// 삭제되지 않은 부서 한정 오름차순 조회
	List<Department> findByDeletedAtIsNullOrderByDepartmentIdAsc();

	@Query("""
		SELECT d
		FROM Department d
		WHERE d.deletedAt IS NULL
		  AND d.isDeleted = 'N'
		ORDER BY d.departmentId ASC
		""")
	List<Department> findActiveDepartments();

	boolean existsByDepartmentNameAndDeletedAtIsNull(String departmentName);

	boolean existsByDepartmentNameAndDepartmentIdNotAndDeletedAtIsNull(String departmentName, Long departmentId);

	boolean existsByDepartmentIdAndDeletedAtIsNull(Long departmentId);

	Optional<Department> findByDepartmentIdAndDeletedAtIsNull(Long departmentId);

	@Query("""
		SELECT d
		FROM Department d
		WHERE d.departmentId = :departmentId
		  AND d.deletedAt IS NULL
		  AND d.isDeleted = 'N'
		""")
	Optional<Department> findActiveDepartmentById(@Param("departmentId") Long departmentId);
}
