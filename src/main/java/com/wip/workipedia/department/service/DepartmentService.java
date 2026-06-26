package com.wip.workipedia.department.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.security.SecurityUtil;
import com.wip.workipedia.department.ai.DepartmentRoutingPromptEditor;
import com.wip.workipedia.admin.department.dto.AdminDepartmentSyncStatus;
import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.domain.AiSyncStatus;
import com.wip.workipedia.aisync.repository.AiSyncJobRepository;
import com.wip.workipedia.aisync.service.AiSyncJobService;
import com.wip.workipedia.department.ai.RoutingPromptEditResult;
import com.wip.workipedia.department.ai.RoutingPromptEditTarget;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.domain.DepartmentRoutingPrompt;
import com.wip.workipedia.admin.department.dto.AdminDepartmentResponse;
import com.wip.workipedia.department.dto.DepartmentRequest;
import com.wip.workipedia.department.dto.DepartmentResponse;
import com.wip.workipedia.department.dto.RoutingPromptEditRequest;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.department.repository.RoutingPromptRepository;
import com.wip.workipedia.user.domain.UserStatus;
import com.wip.workipedia.user.repository.UserRepository.DepartmentMemberCountProjection;
import com.wip.workipedia.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

	private static final String ACTIVE = "Y";
	private static final DateTimeFormatter SYNC_INFO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final DepartmentRepository departmentRepository;
	private final RoutingPromptRepository routingPromptRepository;
	private final DepartmentRoutingPromptEditor departmentRoutingPromptEditor;
	private final AiSyncJobService aiSyncJobService;
	private final AiSyncJobRepository aiSyncJobRepository;
	private final UserRepository userRepository;
	private final TransactionTemplate transactionTemplate;

	@Transactional(readOnly = true)
	public List<DepartmentResponse> findAll() {
		return departmentRepository.findActiveDepartments().stream()
			.map(DepartmentResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<AdminDepartmentResponse> findAllForAdmin() {
		List<Department> departments = departmentRepository.findActiveDepartments();
		Map<Long, DepartmentRoutingPrompt> routingPrompts = findRoutingPromptMap(departments);
		Map<Long, Long> memberCounts = findMemberCountMap(departments);
		Map<Long, AiSyncJob> latestSyncJobs = findLatestDeptRrSyncJobMap(departments);

		return departments.stream()
			.map(department -> {
				Long departmentId = department.getDepartmentId();
				String routingPrompt = getPromptContent(routingPrompts, departmentId);
				AiSyncJob syncJob = latestSyncJobs.get(departmentId);
				return AdminDepartmentResponse.from(
					department,
					routingPrompt,
					memberCounts.getOrDefault(departmentId, 0L),
					resolveSyncStatus(routingPrompt, syncJob),
					resolveSyncInfo(routingPrompt, syncJob)
				);
			})
			.toList();
	}

	@Transactional
	public AdminDepartmentResponse create(DepartmentRequest request) {
		validateDuplicateName(request.departmentName());

		Department department = departmentRepository.save(Department.create(request.departmentName()));

		return AdminDepartmentResponse.from(department, null, 0);
	}

	@Transactional
	public AdminDepartmentResponse update(Long departmentId, DepartmentRequest request) {
		Department department = getDepartment(departmentId);
		validateDuplicateNameForUpdate(request.departmentName(), departmentId);

		department.update(request.departmentName());

		return AdminDepartmentResponse.from(
			department,
			findPromptContent(departmentId),
			userRepository.countByDepartment_DepartmentIdAndDeletedAtIsNullAndStatus(departmentId, UserStatus.ACTIVE)
		);
	}

	public List<AdminDepartmentResponse> editRoutingPrompts(RoutingPromptEditRequest request) {
		List<RoutingPromptEditTarget> targets = transactionTemplate.execute(status -> {
			List<Department> departments = departmentRepository.findActiveDepartments();
			Map<Long, DepartmentRoutingPrompt> routingPrompts = findRoutingPromptMap(departments);
			return departments.stream()
				.map(department -> new RoutingPromptEditTarget(
					department.getDepartmentId(),
					department.getDepartmentName(),
					normalizePromptContent(getPromptContent(routingPrompts, department.getDepartmentId()))
				))
				.toList();
		});

		List<RoutingPromptEditResult> editResults = departmentRoutingPromptEditor.edit(targets, request.instruction());

		if (editResults.isEmpty()) {
			throw new CustomException(ErrorType.BAD_REQUEST, "입력 내용에서 부서명을 찾을 수 없습니다.");
		}

		transactionTemplate.executeWithoutResult(status -> {
			List<Department> departments = departmentRepository.findActiveDepartments();
			Map<Long, Department> departmentMap = departments.stream()
				.collect(Collectors.toMap(Department::getDepartmentId, Function.identity()));

			editResults.forEach(editResult -> {
				Department dept = departmentMap.get(editResult.departmentId());
				if (dept == null) {
					log.warn("AI가 반환한 departmentId가 존재하지 않아 건너뜁니다. departmentId={}", editResult.departmentId());
					return;
				}
				upsertRoutingPrompt(dept, editResult.routingPrompt());
				aiSyncJobService.enqueue(AiSyncSourceType.DEPT_RR, dept.getDepartmentId(), AiSyncOperation.UPSERT);
			});
		});

		return findAllForAdmin();
	}

	@Transactional
	public AdminDepartmentResponse updateRoutingPromptDirect(Long departmentId, String routingPrompt) {
		Department department = getDepartment(departmentId);
		upsertRoutingPrompt(department, routingPrompt);
		aiSyncJobService.enqueue(AiSyncSourceType.DEPT_RR, departmentId, AiSyncOperation.UPSERT);
		long memberCount = userRepository.countByDepartment_DepartmentIdAndDeletedAtIsNullAndStatus(
			departmentId, UserStatus.ACTIVE);
		return AdminDepartmentResponse.from(
			department,
			routingPrompt,
			memberCount,
			AdminDepartmentSyncStatus.PENDING,
			"동기화 대기 중"
		);
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
		aiSyncJobService.enqueue(AiSyncSourceType.DEPT_RR, departmentId, AiSyncOperation.DELETE);
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

	private Map<Long, Long> findMemberCountMap(List<Department> departments) {
		if (departments.isEmpty()) {
			return Map.of();
		}

		List<Long> departmentIds = departments.stream()
			.map(Department::getDepartmentId)
			.toList();

		return userRepository.countMembersByDepartmentIds(departmentIds, UserStatus.ACTIVE)
			.stream()
			.collect(Collectors.toMap(
				DepartmentMemberCountProjection::getDepartmentId,
				DepartmentMemberCountProjection::getMemberCount
			));
	}

	private Map<Long, AiSyncJob> findLatestDeptRrSyncJobMap(List<Department> departments) {
		if (departments.isEmpty()) {
			return Map.of();
		}

		List<Long> departmentIds = departments.stream()
			.map(Department::getDepartmentId)
			.toList();

		return aiSyncJobRepository
			.findLatestJobsBySourceTypeAndSourceIds(AiSyncSourceType.DEPT_RR.name(), departmentIds)
			.stream()
			.collect(Collectors.toMap(AiSyncJob::getSourceId, Function.identity()));
	}

	private String getPromptContent(Map<Long, DepartmentRoutingPrompt> routingPrompts, Long departmentId) {
		DepartmentRoutingPrompt routingPrompt = routingPrompts.get(departmentId);
		return routingPrompt == null ? null : routingPrompt.getPromptContent();
	}

	private AdminDepartmentSyncStatus resolveSyncStatus(String routingPrompt, AiSyncJob syncJob) {
		if (routingPrompt == null || syncJob == null) {
			return AdminDepartmentSyncStatus.EMPTY;
		}
		if (syncJob.getStatus() == AiSyncStatus.SYNCED) {
			return AdminDepartmentSyncStatus.SYNCED;
		}
		if (syncJob.getStatus() == AiSyncStatus.FAILED) {
			return AdminDepartmentSyncStatus.FAILED;
		}
		return AdminDepartmentSyncStatus.PENDING;
	}

	private String resolveSyncInfo(String routingPrompt, AiSyncJob syncJob) {
		if (routingPrompt == null || syncJob == null) {
			return null;
		}
		if (syncJob.getStatus() == AiSyncStatus.SYNCED) {
			return formatSyncTime("마지막 동기화", syncJob.getCompletedAt());
		}
		if (syncJob.getStatus() == AiSyncStatus.FAILED) {
			return syncJob.getLastError();
		}
		return "동기화 대기 중";
	}

	private String formatSyncTime(String label, LocalDateTime syncedAt) {
		if (syncedAt == null) {
			return label + " 시각 없음";
		}
		return label + ": " + syncedAt.format(SYNC_INFO_FORMATTER);
	}

	private String normalizePromptContent(String promptContent) {
		return promptContent == null ? "" : promptContent;
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
