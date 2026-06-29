package com.wip.workipedia.departmentsync;

import static org.assertj.core.api.Assertions.assertThat;

import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.departmentsync.dto.ErpDepartmentItem;
import com.wip.workipedia.departmentsync.dto.MergeResolution;
import com.wip.workipedia.departmentsync.dto.SyncApplyRequest;
import com.wip.workipedia.departmentsync.dto.SyncApplyResponse;
import com.wip.workipedia.departmentsync.dto.SyncPreviewRequest;
import com.wip.workipedia.departmentsync.repository.ExternalDepartmentRepository;
import com.wip.workipedia.departmentsync.service.DepartmentSyncService;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class DepartmentSyncScenarioTest {

	@Autowired DepartmentSyncService syncService;
	@Autowired DepartmentRepository departmentRepository;
	@Autowired ExternalDepartmentRepository externalDepartmentRepository;
	@Autowired UserRepository userRepository;

	@Test
	void 통폐합하면_사원이_통합부서로_이동하고_기존부서는_soft_delete된다() {
		String source = "SCN_ERP";
		// 1차: 영업1/영업2 신설 + 적용
		ErpDepartmentItem i1 = new ErpDepartmentItem("S-1", "영업1팀", "국내영업", "Y");
		ErpDepartmentItem i2 = new ErpDepartmentItem("S-2", "영업2팀", "해외영업", "Y");
		syncService.preview(new SyncPreviewRequest(source, List.of(i1, i2)));
		syncService.apply(new SyncApplyRequest(source, List.of(i1, i2), List.of(), null), 1L);

		Long dept1 = externalDepartmentRepository.findBySourceSystemAndExternalId(source, "S-1")
			.orElseThrow().getMappedDepartmentId();
		Department d1 = departmentRepository.findById(dept1).orElseThrow();
		userRepository.save(User.signup(d1, "U-1", "u1@x.com", "pw", "사원A"));

		// 2차: 통합영업팀(S-3) 신설 + S-1/S-2 통폐합
		ErpDepartmentItem i3 = new ErpDepartmentItem("S-3", "통합영업팀", "국내외 영업", "Y");
		syncService.preview(new SyncPreviewRequest(source, List.of(i3)));
		SyncApplyResponse res = syncService.apply(new SyncApplyRequest(source, List.of(i3),
			List.of(new MergeResolution(List.of("S-1", "S-2"), "S-3", "MERGE")), null), 1L);

		Long mergedDeptId = externalDepartmentRepository.findBySourceSystemAndExternalId(source, "S-3")
			.orElseThrow().getMappedDepartmentId();
		assertThat(res.membersReassigned()).isGreaterThanOrEqualTo(1);
		assertThat(userRepository.countByDepartment_DepartmentId(mergedDeptId)).isEqualTo(1);
		assertThat(departmentRepository.findByDepartmentIdAndDeletedAtIsNull(dept1)).isEmpty();
	}
}
