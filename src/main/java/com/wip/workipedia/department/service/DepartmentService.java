package com.wip.workipedia.department.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.security.SecurityUtil;
import com.wip.workipedia.department.ai.DepartmentRoutingPromptEditor;
import com.wip.workipedia.department.ai.RoutingPromptEditResult;
import com.wip.workipedia.department.ai.RoutingPromptEditTarget;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.domain.DepartmentRoutingPrompt;
import com.wip.workipedia.department.dto.AdminDepartmentResponse;
import com.wip.workipedia.department.dto.DepartmentRequest;
import com.wip.workipedia.department.dto.DepartmentResponse;
import com.wip.workipedia.department.dto.RoutingPromptEditRequest;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.department.repository.RoutingPromptRepository;
import com.wip.workipedia.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentService {

	private static final String ACTIVE = "Y";

	private final DepartmentRepository departmentRepository;
	private final RoutingPromptRepository routingPromptRepository;
	private final DepartmentRoutingPromptEditor departmentRoutingPromptEditor;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public List<DepartmentResponse> findAll() {
		return departmentRepository.findByDeletedAtIsNullOrderByDepartmentIdAsc().stream()
			.map(DepartmentResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<AdminDepartmentResponse> findAllForAdmin() {
		List<Department> departments = departmentRepository.findByDeletedAtIsNullOrderByDepartmentIdAsc();
		Map<Long, DepartmentRoutingPrompt> routingPrompts = findRoutingPromptMap(departments);

		return departments.stream()
			.map(department -> AdminDepartmentResponse.from(
				department,
				getPromptContent(routingPrompts, department.getDepartmentId())
			))
			.toList();
	}

	@Transactional
	public AdminDepartmentResponse create(DepartmentRequest request) {
		validateDuplicateName(request.departmentName());

		Department department = departmentRepository.save(Department.create(request.departmentName()));

		return AdminDepartmentResponse.from(department, null);
	}

	@Transactional
	public AdminDepartmentResponse update(Long departmentId, DepartmentRequest request) {
		Department department = getDepartment(departmentId);
		validateDuplicateNameForUpdate(request.departmentName(), departmentId);

		department.update(request.departmentName());

		return AdminDepartmentResponse.from(department, findPromptContent(departmentId));
	}

	@Transactional
	public List<AdminDepartmentResponse> editRoutingPrompts(RoutingPromptEditRequest request) {
		List<Department> departments = departmentRepository.findByDeletedAtIsNullOrderByDepartmentIdAsc();
		Map<Long, DepartmentRoutingPrompt> routingPrompts = findRoutingPromptMap(departments);
		List<RoutingPromptEditTarget> targets = departments.stream()
			.map(department -> new RoutingPromptEditTarget(
				department.getDepartmentId(),
				department.getDepartmentName(),
				getPromptContent(routingPrompts, department.getDepartmentId())
			))
			.toList();
		List<RoutingPromptEditResult> editResults = departmentRoutingPromptEditor.edit(targets, request.instruction());

		if (editResults.isEmpty()) {
			throw new CustomException(ErrorType.BAD_REQUEST, "입력 내용에서 부서명을 찾을 수 없습니다.");
		}

		Map<Long, Department> departmentMap = departments.stream()
			.collect(Collectors.toMap(Department::getDepartmentId, Function.identity()));

		editResults.forEach(editResult -> upsertRoutingPrompt(
			departmentMap.get(editResult.departmentId()),
			editResult.routingPrompt()
		));

		return findAllForAdmin();
	}

	@Transactional
	public void delete(Long departmentId) {
		Department department = getDepartment(departmentId);
		Long actorUserId = SecurityUtil.getCurrentUserId();

		if (userRepository.existsByDepartment_DepartmentId(departmentId)) {
			throw new CustomException(ErrorType.DEPARTMENT_IN_USE);
		}

		department.markDeleted();
		routingPromptRepository.findByDepartment_DepartmentIdAndDeletedAtIsNull(departmentId)
			.ifPresent(routingPrompt -> routingPrompt.markDeleted(actorUserId));
	}

	private Map<Long, DepartmentRoutingPrompt> findRoutingPromptMap(List<Department> departments) {
		if (departments.isEmpty()) {
			return Map.of();
		}

		List<Long> departmentIds = departments.stream()
			.map(Department::getDepartmentId)
			.toList();

		return routingPromptRepository
			.findByDepartment_DepartmentIdInAndDeletedAtIsNullAndIsActive(departmentIds, ACTIVE)
			.stream()
			.collect(Collectors.toMap(
				routingPrompt -> routingPrompt.getDepartment().getDepartmentId(),
				Function.identity()
			));
	}

	private String getPromptContent(Map<Long, DepartmentRoutingPrompt> routingPrompts, Long departmentId) {
		DepartmentRoutingPrompt routingPrompt = routingPrompts.get(departmentId);
		return routingPrompt == null ? null : routingPrompt.getPromptContent();
	}

	private String findPromptContent(Long departmentId) {
		return routingPromptRepository.findByDepartment_DepartmentIdAndDeletedAtIsNull(departmentId)
			.map(DepartmentRoutingPrompt::getPromptContent)
			.orElse(null);
	}

	private DepartmentRoutingPrompt upsertRoutingPrompt(Department department, String promptContent) {
		Long actorUserId = SecurityUtil.getCurrentUserId();

		return routingPromptRepository
			.findByDepartment_DepartmentIdAndDeletedAtIsNull(department.getDepartmentId())
			.map(routingPrompt -> {
				routingPrompt.update(promptContent, actorUserId);
				return routingPrompt;
			})
			.orElseGet(() -> routingPromptRepository.save(
				DepartmentRoutingPrompt.create(department, promptContent, actorUserId)
			));
	}

	private Department getDepartment(Long departmentId) {
		return departmentRepository.findByDepartmentIdAndDeletedAtIsNull(departmentId)
			.orElseThrow(() -> new CustomException(ErrorType.DEPARTMENT_NOT_FOUND));
	}

	private void validateDuplicateNameForUpdate(String departmentName, Long departmentId) {
		if (departmentRepository.existsByDepartmentNameAndDepartmentIdNotAndDeletedAtIsNull(departmentName, departmentId)) {
			throw new CustomException(ErrorType.DEPARTMENT_DUPLICATE_NAME);
		}
	}

	private void validateDuplicateName(String departmentName) {
		if (departmentRepository.existsByDepartmentNameAndDeletedAtIsNull(departmentName)) {
			throw new CustomException(ErrorType.DEPARTMENT_DUPLICATE_NAME);
		}
	}
}
