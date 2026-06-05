package com.wip.workipedia.user.repository;

import com.wip.workipedia.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmployeeId(String employeeId);

	boolean existsByEmployeeId(String employeeId);

	boolean existsByEmail(String email);
}
