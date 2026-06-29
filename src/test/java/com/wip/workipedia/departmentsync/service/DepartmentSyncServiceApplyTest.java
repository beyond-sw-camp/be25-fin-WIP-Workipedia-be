package com.wip.workipedia.departmentsync.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.repository.AiSyncJobRepository;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.department.repository.RoutingPromptRepository;
import com.wip.workipedia.departmentsync.dto.ErpDepartmentItem;
import com.wip.workipedia.departmentsync.dto.ManualLink;
import com.wip.workipedia.departmentsync.dto.SyncApplyRequest;
import com.wip.workipedia.departmentsync.dto.SyncApplyResponse;
import com.wip.workipedia.departmentsync.dto.SyncPreviewRequest;
import com.wip.workipedia.departmentsync.repository.ExternalDepartmentRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class DepartmentSyncServiceApplyTest {

	@Autowired DepartmentSyncService syncService;
	@Autowired DepartmentRepository departmentRepository;
	@Autowired AiSyncJobRepository aiSyncJobRepository;
	@Autowired RoutingPromptRepository routingPromptRepository;
	@Autowired ExternalDepartmentRepository externalDepartmentRepository;

	@Test
	void 신규부서_apply하면_부서가_생기고_DEPT_RR_job이_enqueue된다() {
		String source = "TEST_ERP_APPLY";
		ErpDepartmentItem item = new ErpDepartmentItem("A-001", "신설팀", "신규 업무", "Y");
		syncService.preview(new SyncPreviewRequest(source, List.of(item)));

		SyncApplyResponse res = syncService.apply(
			new SyncApplyRequest(source, List.of(item), List.of(), List.of(), null), 1L);

		assertThat(res.created()).isEqualTo(1);
		assertThat(departmentRepository.existsByDepartmentNameAndDeletedAtIsNull("신설팀")).isTrue();
		long deptRrJobs = aiSyncJobRepository.findAll().stream()
			.filter(j -> j.getSourceType() == AiSyncSourceType.DEPT_RR).count();
		assertThat(deptRrJobs).isGreaterThanOrEqualTo(1);
	}

	@Test
	void 기존_RR이_있으면_재동기화해도_덮어쓰지_않는다() {
		String source = "TEST_ERP_RR";
		// 1차: 신설 + R&R "초기업무"
		ErpDepartmentItem first = new ErpDepartmentItem("R-1", "RR팀", "초기업무", "Y");
		syncService.preview(new SyncPreviewRequest(source, List.of(first)));
		syncService.apply(new SyncApplyRequest(source, List.of(first), List.of(), List.of(), null), 1L);

		// 2차: 같은 부서, ERP가 duty_desc를 바꿔서 다시 동기화
		ErpDepartmentItem second = new ErpDepartmentItem("R-1", "RR팀", "ERP가 바꾼 설명", "Y");
		syncService.preview(new SyncPreviewRequest(source, List.of(second)));
		syncService.apply(new SyncApplyRequest(source, List.of(second), List.of(), List.of(), null), 1L);

		Long deptId = externalDepartmentRepository.findBySourceSystemAndExternalId(source, "R-1")
			.orElseThrow().getMappedDepartmentId();
		String prompt = routingPromptRepository.findByDepartment_DepartmentIdAndDeletedAtIsNull(deptId)
			.orElseThrow().getPromptContent();
		assertThat(prompt).isEqualTo("초기업무"); // 기존 R&R 보존(덮어쓰지 않음)
	}

	@Test
	void 같은_이름의_기존_부서가_있으면_신설하지_않고_연결한다() {
		String source = "TEST_ERP_ADOPT";
		String name = "흡수검증부서_" + System.nanoTime();
		Department existing = departmentRepository.save(Department.create(name));
		ErpDepartmentItem item = new ErpDepartmentItem("F-1", name, "자금·회계", "Y");
		syncService.preview(new SyncPreviewRequest(source, List.of(item)));

		SyncApplyResponse res = syncService.apply(
			new SyncApplyRequest(source, List.of(item), List.of(), List.of(), null), 1L);

		assertThat(res.created()).isZero(); // 신설 아님
		Long mapped = externalDepartmentRepository.findBySourceSystemAndExternalId(source, "F-1")
			.orElseThrow().getMappedDepartmentId();
		assertThat(mapped).isEqualTo(existing.getDepartmentId()); // 기존 부서에 연결
	}

	@Test
	void 수동연결하면_신설없이_기존부서에_연결되고_RR도_설정된다() {
		String source = "TEST_ERP_MANUAL";
		String existingName = "수동연결기존_" + System.nanoTime();
		Department existing = departmentRepository.save(Department.create(existingName));
		// ERP는 이름이 달라 자동 매칭이 안 되는 동일 부서를 줌
		ErpDepartmentItem item = new ErpDepartmentItem("M-1", "ERP다른이름팀_" + System.nanoTime(), "ERP 담당업무", "Y");
		syncService.preview(new SyncPreviewRequest(source, List.of(item)));

		SyncApplyResponse res = syncService.apply(new SyncApplyRequest(
			source, List.of(item), List.of(),
			List.of(new ManualLink("M-1", existing.getDepartmentId(), true)), null), 1L);

		assertThat(res.created()).isZero();
		assertThat(res.linked()).isEqualTo(1);
		Long mapped = externalDepartmentRepository.findBySourceSystemAndExternalId(source, "M-1")
			.orElseThrow().getMappedDepartmentId();
		assertThat(mapped).isEqualTo(existing.getDepartmentId());
		String prompt = routingPromptRepository
			.findByDepartment_DepartmentIdAndDeletedAtIsNull(existing.getDepartmentId())
			.orElseThrow().getPromptContent();
		assertThat(prompt).isEqualTo("ERP 담당업무"); // applyRoutingPrompt=true → R&R 설정됨
	}
}
