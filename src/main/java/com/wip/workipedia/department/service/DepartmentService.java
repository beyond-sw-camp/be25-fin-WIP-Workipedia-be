package com.wip.workipedia.department.service;

import com.wip.workipedia.department.dto.DepartmentResponse;
import com.wip.workipedia.department.repository.DepartmentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentService {

	private final DepartmentRepository departmentRepository;

	@Transactional(readOnly = true)
	public List<DepartmentResponse> findAll() {
		return departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "departmentId")).stream()
			.map(DepartmentResponse::from)
			.toList();
	}
}
