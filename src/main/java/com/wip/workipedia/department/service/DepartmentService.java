package com.wip.workipedia.department.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.dto.DepartmentRequest;
import com.wip.workipedia.department.dto.DepartmentResponse;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentService {

	private final DepartmentRepository departmentRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public List<DepartmentResponse> findAll() {
		return departmentRepository.findByDeletedAtIsNullOrderByDepartmentIdAsc().stream()
			.map(DepartmentResponse::from)
			.toList();
	}

	@Transactional
	public DepartmentResponse create(DepartmentRequest request) {
		validateDuplicateName(request.departmentName());

		Department department = departmentRepository.save(Department.create(request.departmentName()));

		return DepartmentResponse.from(department);
	}

	@Transactional
	public DepartmentResponse update(Long departmentId, DepartmentRequest request) {
		Department department = getDepartment(departmentId);
		validateDuplicateNameForUpdate(request.departmentName(), departmentId);

		department.update(request.departmentName());

		return DepartmentResponse.from(department);
	}

	@Transactional
	public void delete(Long departmentId) {
		Department department = getDepartment(departmentId);

		if (userRepository.existsByDepartment_DepartmentId(departmentId)) {
			throw new CustomException(ErrorType.DEPARTMENT_IN_USE);
		}

		department.markDeleted();
	}

	private Department getDepartment(Long departmentId) {
		return departmentRepository.findByDepartmentIdAndDeletedAtIsNull(departmentId)
			.orElseThrow(() -> new CustomException(ErrorType.DEPARTMENT_NOT_FOUND));
	}

	private void validateDuplicateName(String departmentName) {
		if (departmentRepository.existsByDepartmentName(departmentName)) {
			throw new CustomException(ErrorType.DEPARTMENT_DUPLICATE_NAME);
		}
	}

	private void validateDuplicateNameForUpdate(String departmentName, Long departmentId) {
		if (departmentRepository.existsByDepartmentNameAndDepartmentIdNot(departmentName, departmentId)) {
			throw new CustomException(ErrorType.DEPARTMENT_DUPLICATE_NAME);
		}
	}
}
