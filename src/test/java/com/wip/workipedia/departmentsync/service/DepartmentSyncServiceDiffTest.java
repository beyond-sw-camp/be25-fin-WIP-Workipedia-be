package com.wip.workipedia.departmentsync.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.departmentsync.domain.SyncState;
import com.wip.workipedia.departmentsync.dto.ErpDepartmentItem;
import com.wip.workipedia.departmentsync.dto.SyncDiffRow;
import com.wip.workipedia.departmentsync.dto.SyncPreviewRequest;
import com.wip.workipedia.departmentsync.dto.SyncPreviewResponse;
import com.wip.workipedia.departmentsync.repository.ExternalDepartmentRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class DepartmentSyncServiceDiffTest {

	@Autowired DepartmentSyncService syncService;
	@Autowired DepartmentRepository departmentRepository;
	@Autowired ExternalDepartmentRepository externalDepartmentRepository;

	@Test
	void 신규_external_id는_NEW로_분류된다() {
		SyncPreviewRequest req = new SyncPreviewRequest("TEST_ERP",
			List.of(new ErpDepartmentItem("X-001", "신규부서", "업무", "Y")));

		SyncPreviewResponse res = syncService.preview(req);

		SyncDiffRow row = res.rows().stream()
			.filter(r -> r.externalId().equals("X-001")).findFirst().orElseThrow();
		assertThat(row.state()).isEqualTo(SyncState.NEW);
		assertThat(res.created()).isGreaterThanOrEqualTo(1);
		assertThat(externalDepartmentRepository
			.findBySourceSystemAndExternalId("TEST_ERP", "X-001")).isPresent();
	}

	@Test
	void 이미_매핑된_부서의_이름이_바뀌면_RENAMED다() {
		Department saved = departmentRepository.save(Department.create("옛이름"));
		syncService.preview(new SyncPreviewRequest("TEST_ERP",
			List.of(new ErpDepartmentItem("X-100", "옛이름", null, "Y"))));
		externalDepartmentRepository.findBySourceSystemAndExternalId("TEST_ERP", "X-100")
			.orElseThrow().markApplied(saved.getDepartmentId());

		SyncPreviewResponse res = syncService.preview(new SyncPreviewRequest("TEST_ERP",
			List.of(new ErpDepartmentItem("X-100", "새이름", null, "Y"))));

		SyncDiffRow row = res.rows().stream()
			.filter(r -> r.externalId().equals("X-100")).findFirst().orElseThrow();
		assertThat(row.state()).isEqualTo(SyncState.RENAMED);
		assertThat(row.previousName()).isEqualTo("옛이름");
	}
}
