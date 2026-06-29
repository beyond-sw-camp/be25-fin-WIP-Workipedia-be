package com.wip.workipedia.departmentsync.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.repository.AiSyncJobRepository;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.departmentsync.dto.ErpDepartmentItem;
import com.wip.workipedia.departmentsync.dto.SyncApplyRequest;
import com.wip.workipedia.departmentsync.dto.SyncApplyResponse;
import com.wip.workipedia.departmentsync.dto.SyncPreviewRequest;
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

	@Test
	void 신규부서_apply하면_부서가_생기고_DEPT_RR_job이_enqueue된다() {
		String source = "TEST_ERP_APPLY";
		ErpDepartmentItem item = new ErpDepartmentItem("A-001", "신설팀", "신규 업무", "Y");
		syncService.preview(new SyncPreviewRequest(source, List.of(item)));

		SyncApplyResponse res = syncService.apply(
			new SyncApplyRequest(source, List.of(item), List.of(), null), 1L);

		assertThat(res.created()).isEqualTo(1);
		assertThat(departmentRepository.existsByDepartmentNameAndDeletedAtIsNull("신설팀")).isTrue();
		long deptRrJobs = aiSyncJobRepository.findAll().stream()
			.filter(j -> j.getSourceType() == AiSyncSourceType.DEPT_RR).count();
		assertThat(deptRrJobs).isGreaterThanOrEqualTo(1);
	}
}
