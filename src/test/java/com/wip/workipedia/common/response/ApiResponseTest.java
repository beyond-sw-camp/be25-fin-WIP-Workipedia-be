package com.wip.workipedia.common.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.wip.workipedia.common.exception.ErrorType;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ApiResponseTest {

	@Test
	void successWrapsDataWithCommonFields() {
		Map<String, Long> data = Map.of("userId", 1L);

		ResponseEntity<ApiResponse<Map<String, Long>>> response =
			ApiResponse.success(HttpStatus.OK, "조회 성공", data);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().code()).isEqualTo(200);
		assertThat(response.getBody().status()).isEqualTo("OK");
		assertThat(response.getBody().message()).isEqualTo("조회 성공");
		assertThat(response.getBody().data()).containsEntry("userId", 1L);
	}

	@Test
	void errorUsesHttpStatusCodeAndNullData() {
		ResponseEntity<ApiResponse<Void>> response = ApiResponse.error(ErrorType.NOT_FOUND);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().code()).isEqualTo(404);
		assertThat(response.getBody().status()).isEqualTo("NOT_FOUND");
		assertThat(response.getBody().message()).isEqualTo("리소스를 찾을 수 없습니다.");
		assertThat(response.getBody().data()).isNull();
	}
}
