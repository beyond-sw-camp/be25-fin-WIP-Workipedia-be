package com.wip.workipedia.department.service;

import com.wip.workipedia.department.dto.DepartmentResponse;
import com.wip.workipedia.department.repository.DepartmentRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentService {

	private final DepartmentRepository departmentRepository;

	public DepartmentService(DepartmentRepository departmentRepository) {
		this.departmentRepository = departmentRepository;
	}

	@Transactional(readOnly = true)
	public List<DepartmentResponse> findAll() {
		return departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "departmentName")).stream()
			.map(DepartmentResponse::from)
			.toList();
	}
}
