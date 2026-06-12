package com.wip.workipedia.user.repository;

import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 사용자를 조회하고 중복을 검사하기 위한 인터페이스입니다.
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmployeeId(String employeeId);

	Optional<User> findByEmployeeIdAndEmail(String employeeId, String email);

	boolean existsByEmployeeId(String employeeId);

	boolean existsByEmail(String email);

	boolean existsByDepartment_DepartmentId(Long departmentId);

	long countByDepartment_DepartmentId(Long departmentId);

	@Query("""
		SELECT u.department.departmentId AS departmentId, COUNT(u) AS memberCount
		FROM User u
		WHERE u.department.departmentId IN :departmentIds
		GROUP BY u.department.departmentId
		""")
	List<DepartmentMemberCountProjection> countMembersByDepartmentIds(@Param("departmentIds") List<Long> departmentIds);

	long countByStatus(UserStatus status);

	long countByStatusAndLastLoginAtGreaterThanEqualAndLastLoginAtLessThan(
		UserStatus status,
		LocalDateTime startAt,
		LocalDateTime endAt
	);

	interface DepartmentMemberCountProjection {
		Long getDepartmentId();

		long getMemberCount();
	}
}
