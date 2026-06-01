package com.wip.workipedia.user.repository;

import com.wip.workipedia.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByEmployeeId(String employeeId);

	boolean existsByEmail(String email);
}
