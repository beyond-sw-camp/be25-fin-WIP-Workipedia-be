package com.wip.workipedia.user.repository;

import com.wip.workipedia.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// 사용자를 조회하고 중복을 검사하기 위한 인터페이스입니다.
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmployeeId(String employeeId);

	Optional<User> findByEmployeeIdAndEmail(String employeeId, String email);

	boolean existsByEmployeeId(String employeeId);

	boolean existsByEmail(String email);

	boolean existsByDepartment_DepartmentId(Long departmentId);
}
