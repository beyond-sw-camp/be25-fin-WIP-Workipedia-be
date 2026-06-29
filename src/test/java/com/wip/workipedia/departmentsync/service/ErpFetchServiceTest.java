package com.wip.workipedia.departmentsync.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.departmentsync.dto.ErpFetchResponse;
import org.junit.jupiter.api.Test;

class ErpFetchServiceTest {

	private final ErpFetchService service = new ErpFetchService(new ObjectMapper(), null);

	@Test
	void data배열_JSON을_columns와_rows로_평탄화한다() {
		String json = """
			{"data":[{"deptCd":"D-1","deptNm":"인사팀","useYn":"Y"}]}
			""";
		ErpFetchResponse res = service.parse(json);

		assertThat(res.columns()).contains("deptCd", "deptNm", "useYn");
		assertThat(res.rows()).hasSize(1);
		assertThat(res.rows().get(0).get("deptNm")).isEqualTo("인사팀");
	}

	@Test
	void 최상위_배열도_처리한다() {
		ErpFetchResponse res = service.parse("[{\"a\":\"1\"}]");
		assertThat(res.rows().get(0).get("a")).isEqualTo("1");
	}
}
