package com.wip.workipedia.departmentsync.service;

import com.wip.workipedia.aisync.domain.AiSyncOperation;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.service.AiSyncJobService;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.domain.DepartmentRoutingPrompt;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.department.repository.RoutingPromptRepository;
import com.wip.workipedia.departmentsync.domain.ExternalDepartment;
import com.wip.workipedia.departmentsync.domain.SyncState;
import com.wip.workipedia.departmentsync.dto.ErpDepartmentItem;
import com.wip.workipedia.departmentsync.dto.ManualLink;
import com.wip.workipedia.departmentsync.dto.MergeResolution;
import com.wip.workipedia.departmentsync.dto.SyncApplyRequest;
import com.wip.workipedia.departmentsync.dto.SyncApplyResponse;
import com.wip.workipedia.departmentsync.dto.SyncDiffRow;
import com.wip.workipedia.departmentsync.dto.SyncPreviewRequest;
import com.wip.workipedia.departmentsync.dto.SyncPreviewResponse;
import com.wip.workipedia.departmentsync.repository.ExternalDepartmentRepository;
import com.wip.workipedia.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentSyncService {

	private final ExternalDepartmentRepository externalDepartmentRepository;
	private final DepartmentRepository departmentRepository;
	private final RoutingPromptRepository routingPromptRepository;
	private final AiSyncJobService aiSyncJobService;
	private final UserRepository userRepository;

	// ERP에서 들어온 부서 목록을 external_departments에 적재하고, 현재 운영 부서와 비교해 diff를 계산한다.
	@Transactional
	public SyncPreviewResponse preview(SyncPreviewRequest request) {
		String source = request.sourceSystem();
		List<ExternalDepartment> existing = externalDepartmentRepository.findBySourceSystem(source);
		Set<String> incomingIds = new HashSet<>();

		List<SyncDiffRow> rows = new ArrayList<>();
		int created = 0;
		int renamed = 0;
		int deleted = 0;

		for (ErpDepartmentItem item : request.items()) {
			incomingIds.add(item.externalId());
			ExternalDepartment ext = externalDepartmentRepository
				.findBySourceSystemAndExternalId(source, item.externalId())
				.orElse(null);

			// 원본 적재(신규 stage 또는 갱신)
			if (ext == null) {
				ext = ExternalDepartment.stage(source, item.externalId(),
					item.departmentName(), null, item.dutyDesc(), item.useYn(), null);
				externalDepartmentRepository.save(ext);
			} else {
				ext.refreshFrom(item.departmentName(), null, item.dutyDesc(), item.useYn(), null);
			}

			SyncState state;
			String previousName = null;
			Long mappedDeptId = ext.getMappedDepartmentId();

			if (mappedDeptId != null) {
				// 이미 운영 부서와 연결됨 → 개명/유지 (운영 부서명 기준 비교)
				String currentName = departmentRepository.findByDepartmentIdAndDeletedAtIsNull(mappedDeptId)
					.map(Department::getDepartmentName).orElse(item.departmentName());
				if (!currentName.equals(item.departmentName())) {
					state = SyncState.RENAMED;
					previousName = currentName;
					renamed++;
				} else {
					state = SyncState.MATCHED;
				}
			} else {
				// 미연결 → 같은 이름의 기존 부서가 있으면 흡수(연결), 없으면 신설
				Long byNameId = departmentRepository
					.findByDepartmentNameAndDeletedAtIsNull(item.departmentName())
					.map(Department::getDepartmentId).orElse(null);
				if (byNameId != null) {
					state = SyncState.MATCHED;
					mappedDeptId = byNameId;
				} else {
					state = SyncState.NEW;
					created++;
				}
			}
			ext.assignState(state);
			rows.add(new SyncDiffRow(item.externalId(), item.departmentName(),
				previousName, state, 0, mappedDeptId));
		}

		// 기존 매핑돼 있었는데 이번 목록에 없음 → DELETED
		for (ExternalDepartment ext : existing) {
			if (incomingIds.contains(ext.getExternalId())) {
				continue;
			}
			if (ext.getMappedDepartmentId() == null) {
				continue;
			}
			long memberCount = userRepository.countByDepartment_DepartmentId(ext.getMappedDepartmentId());
			ext.assignState(SyncState.DELETED);
			rows.add(new SyncDiffRow(ext.getExternalId(), ext.getDepartmentName(),
				null, SyncState.DELETED, memberCount, ext.getMappedDepartmentId()));
			deleted++;
		}

		return new SyncPreviewResponse(rows, created, renamed, deleted);
	}

	// 검토 완료된 변경을 운영 departments/users/department_routing_prompts에 반영한다.
	@Transactional
	public SyncApplyResponse apply(SyncApplyRequest request, Long actorUserId) {
		String source = request.sourceSystem();
		int created = 0;
		int updated = 0;
		int deleted = 0;
		int merged = 0;
		int linked = 0;
		long membersReassigned = 0;

		Set<String> mergedFrom = new HashSet<>();
		Set<String> manuallyLinked = new HashSet<>();

		// 0) 수동 연결 먼저 처리 (이름이 달라 자동 매칭 안 되는 동일 부서를 관리자가 직접 연결)
		if (request.manualLinks() != null) {
			for (ManualLink link : request.manualLinks()) {
				ExternalDepartment ext = externalDepartmentRepository
					.findBySourceSystemAndExternalId(source, link.externalId()).orElseThrow();
				Department target = departmentRepository
					.findByDepartmentIdAndDeletedAtIsNull(link.departmentId()).orElseThrow();
				ext.markApplied(target.getDepartmentId());
				manuallyLinked.add(link.externalId());
				if (link.applyRoutingPrompt()) {
					ErpDepartmentItem item = request.items().stream()
						.filter(i -> i.externalId().equals(link.externalId()))
						.findFirst().orElse(null);
					if (item != null) {
						forceRoutingPrompt(target.getDepartmentId(), item.dutyDesc(), actorUserId);
					}
				}
				linked++;
			}
		}

		// 1) 통폐합 처리
		if (request.merges() != null) {
			for (MergeResolution m : request.merges()) {
				ExternalDepartment toExt = externalDepartmentRepository
					.findBySourceSystemAndExternalId(source, m.toExternalId()).orElseThrow();
				Long toDeptId = ensureDepartment(toExt, request.items(), actorUserId);
				for (String fromId : m.fromExternalIds()) {
					mergedFrom.add(fromId);
					ExternalDepartment fromExt = externalDepartmentRepository
						.findBySourceSystemAndExternalId(source, fromId).orElse(null);
					if (fromExt == null || fromExt.getMappedDepartmentId() == null) {
						continue;
					}
					Long fromDeptId = fromExt.getMappedDepartmentId();
					membersReassigned += userRepository.reassignDepartment(fromDeptId, toDeptId);
					departmentRepository.findByDepartmentIdAndDeletedAtIsNull(fromDeptId)
						.ifPresent(Department::markDeleted);
					deleteRoutingPrompt(fromDeptId, actorUserId);
					fromExt.markApplied(toDeptId);
				}
				merged++;
			}
		}

		// 2) 신설/개명/폐지 처리
		for (ErpDepartmentItem item : request.items()) {
			if (mergedFrom.contains(item.externalId()) || manuallyLinked.contains(item.externalId())) {
				continue;
			}
			ExternalDepartment ext = externalDepartmentRepository
				.findBySourceSystemAndExternalId(source, item.externalId()).orElseThrow();

			if ("N".equals(item.useYn())) { // 폐지
				if (ext.getMappedDepartmentId() != null) {
					Long deptId = ext.getMappedDepartmentId();
					if (request.reassignTargetDepartmentId() != null) {
						membersReassigned += userRepository.reassignDepartment(
							deptId, request.reassignTargetDepartmentId());
					}
					departmentRepository.findByDepartmentIdAndDeletedAtIsNull(deptId)
						.ifPresent(Department::markDeleted);
					deleteRoutingPrompt(deptId, actorUserId);
					deleted++;
				}
				continue;
			}

			if (ext.getMappedDepartmentId() == null) { // 미연결 → 이름 흡수 또는 신설
				Department byName = departmentRepository
					.findByDepartmentNameAndDeletedAtIsNull(item.departmentName()).orElse(null);
				Department dept = byName != null
					? byName
					: departmentRepository.save(Department.create(item.departmentName()));
				if (byName == null) {
					created++;
				}
				applyRoutingPrompt(dept.getDepartmentId(), item.dutyDesc(), actorUserId);
				ext.markApplied(dept.getDepartmentId());
			} else { // 개명/유지
				Department dept = departmentRepository
					.findByDepartmentIdAndDeletedAtIsNull(ext.getMappedDepartmentId()).orElseThrow();
				if (!dept.getDepartmentName().equals(item.departmentName())) {
					dept.update(item.departmentName());
					updated++;
				}
				applyRoutingPrompt(dept.getDepartmentId(), item.dutyDesc(), actorUserId);
				ext.markApplied(dept.getDepartmentId());
			}
		}

		return new SyncApplyResponse(created, updated, deleted, merged, linked, membersReassigned);
	}

	// 통폐합 대상 부서를 보장한다(연결됨→그대로 / 같은 이름 있으면 흡수 / 없으면 신설). 매핑된 부서 id 반환.
	private Long ensureDepartment(ExternalDepartment toExt, List<ErpDepartmentItem> items, Long actorUserId) {
		if (toExt.getMappedDepartmentId() != null) {
			return toExt.getMappedDepartmentId();
		}
		Department dept = departmentRepository.findByDepartmentNameAndDeletedAtIsNull(toExt.getDepartmentName())
			.orElseGet(() -> departmentRepository.save(Department.create(toExt.getDepartmentName())));
		items.stream()
			.filter(i -> i.externalId().equals(toExt.getExternalId()))
			.findFirst()
			.ifPresent(i -> applyRoutingPrompt(dept.getDepartmentId(), i.dutyDesc(), actorUserId));
		toExt.markApplied(dept.getDepartmentId());
		return dept.getDepartmentId();
	}

	// R&R 정책: 신설 시에만 ERP duty_desc로 채운다. 이미 R&R이 있으면 수기 튜닝 보호를 위해 덮어쓰지 않는다.
	// (기존 R&R을 건드리지 않으므로 AI 동기화 job도 신설 때만 등록한다.)
	private void applyRoutingPrompt(Long departmentId, String dutyDesc, Long actorUserId) {
		if (dutyDesc == null || dutyDesc.isBlank()) {
			return;
		}
		boolean alreadyHasRoutingPrompt = routingPromptRepository
			.findByDepartment_DepartmentIdAndDeletedAtIsNull(departmentId).isPresent();
		if (alreadyHasRoutingPrompt) {
			return;
		}
		Department dept = departmentRepository.findByDepartmentIdAndDeletedAtIsNull(departmentId).orElseThrow();
		routingPromptRepository.save(DepartmentRoutingPrompt.create(dept, dutyDesc, actorUserId));
		aiSyncJobService.enqueue(AiSyncSourceType.DEPT_RR, departmentId, AiSyncOperation.UPSERT);
	}

	// 수동 연결 시 관리자 의사로 R&R을 명시 설정한다(기존 값 덮어쓰기 허용). 빈 값이면 건드리지 않는다.
	private void forceRoutingPrompt(Long departmentId, String dutyDesc, Long actorUserId) {
		if (dutyDesc == null || dutyDesc.isBlank()) {
			return;
		}
		Department dept = departmentRepository.findByDepartmentIdAndDeletedAtIsNull(departmentId).orElseThrow();
		DepartmentRoutingPrompt existing = routingPromptRepository
			.findByDepartment_DepartmentIdAndDeletedAtIsNull(departmentId).orElse(null);
		if (existing == null) {
			routingPromptRepository.save(DepartmentRoutingPrompt.create(dept, dutyDesc, actorUserId));
		} else {
			existing.update(dutyDesc, actorUserId);
		}
		aiSyncJobService.enqueue(AiSyncSourceType.DEPT_RR, departmentId, AiSyncOperation.UPSERT);
	}

	// 부서 폐지 시 R&R을 비활성화하고 AI 동기화 job(DEPT_RR/DELETE)을 등록한다.
	private void deleteRoutingPrompt(Long departmentId, Long actorUserId) {
		routingPromptRepository.findByDepartment_DepartmentIdAndDeletedAtIsNull(departmentId)
			.ifPresent(rp -> {
				rp.markDeleted(actorUserId);
				aiSyncJobService.enqueue(AiSyncSourceType.DEPT_RR, departmentId, AiSyncOperation.DELETE);
			});
	}
}
