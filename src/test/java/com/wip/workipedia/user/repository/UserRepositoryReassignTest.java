package com.wip.workipedia.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserRepositoryReassignTest {

	@Autowired UserRepository userRepository;
	@Autowired DepartmentRepository departmentRepository;

	@Test
	void reassignDepartment는_소속사원을_새부서로_옮긴다() {
		Department from = departmentRepository.save(Department.create("영업1팀"));
		Department to = departmentRepository.save(Department.create("통합영업팀"));
		userRepository.save(User.signup(from, "E-1", "a@x.com", "pw", "직원1"));
		userRepository.save(User.signup(from, "E-2", "b@x.com", "pw", "직원2"));

		int moved = userRepository.reassignDepartment(from.getDepartmentId(), to.getDepartmentId());

		assertThat(moved).isEqualTo(2);
		assertThat(userRepository.countByDepartment_DepartmentId(to.getDepartmentId())).isEqualTo(2);
		assertThat(userRepository.countByDepartment_DepartmentId(from.getDepartmentId())).isZero();
	}
}
