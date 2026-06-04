package com.wip.workipedia.department.repository;

import com.wip.workipedia.department.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
