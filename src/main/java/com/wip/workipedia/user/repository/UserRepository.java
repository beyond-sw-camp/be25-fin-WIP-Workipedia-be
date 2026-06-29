package com.wip.workipedia.user.repository;

import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 사용자를 조회하고 중복을 검사하기 위한 인터페이스입니다.
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmployeeId(String employeeId);

	Page<User> findByDeletedAtIsNull(Pageable pageable);

	List<User> findByDeletedAtIsNullAndStatus(UserStatus status);

	Optional<User> findByEmployeeIdAndEmail(String employeeId, String email);

	boolean existsByEmployeeId(String employeeId);

	boolean existsByEmail(String email);

	boolean existsByDepartment_DepartmentId(Long departmentId);

	long countByDepartment_DepartmentId(Long departmentId);

	long countByDepartment_DepartmentIdAndDeletedAtIsNullAndStatus(Long departmentId, UserStatus status);

	// 폐지/통폐합되는 부서의 소속 사원을 새 부서로 일괄 이동한다.
	@Modifying(clearAutomatically = true)
	@Query("""
		UPDATE User u
		SET u.department.departmentId = :toDepartmentId
		WHERE u.department.departmentId = :fromDepartmentId
		  AND u.deletedAt IS NULL
		""")
	int reassignDepartment(@Param("fromDepartmentId") Long fromDepartmentId,
		@Param("toDepartmentId") Long toDepartmentId);

	@Query("""
		SELECT u.department.departmentId AS departmentId, COUNT(u) AS memberCount
		FROM User u
		WHERE u.department.departmentId IN :departmentIds
		  AND u.deletedAt IS NULL
		  AND u.status = :status
		GROUP BY u.department.departmentId
		""")
	List<DepartmentMemberCountProjection> countMembersByDepartmentIds(
		@Param("departmentIds") List<Long> departmentIds,
		@Param("status") UserStatus status
	);

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
