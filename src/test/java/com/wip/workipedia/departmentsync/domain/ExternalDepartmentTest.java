package com.wip.workipedia.departmentsync.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExternalDepartmentTest {

	@Test
	void stage로_생성하면_상태가_NEW이고_원본이_채워진다() {
		ExternalDepartment ext = ExternalDepartment.stage(
			"HANWHA_ERP", "D-0040", "인사문화팀", "D-0001", "채용·평가", "Y", "{}");

		assertThat(ext.getSyncState()).isEqualTo(SyncState.NEW);
		assertThat(ext.getExternalId()).isEqualTo("D-0040");
		assertThat(ext.getUseYn()).isEqualTo("Y");
	}

	@Test
	void markApplied하면_APPLIED와_매핑부서id가_기록된다() {
		ExternalDepartment ext = ExternalDepartment.stage(
			"HANWHA_ERP", "D-0040", "인사문화팀", null, null, "Y", "{}");

		ext.markApplied(42L);

		assertThat(ext.getSyncState()).isEqualTo(SyncState.APPLIED);
		assertThat(ext.getMappedDepartmentId()).isEqualTo(42L);
	}
}
