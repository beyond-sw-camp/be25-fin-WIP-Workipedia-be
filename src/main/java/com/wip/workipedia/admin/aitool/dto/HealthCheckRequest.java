package com.wip.workipedia.admin.aitool.dto;

import jakarta.validation.constraints.NotBlank;

// 등록 화면에서 아직 저장하지 않은 입력값만으로 연결 체크할 때 쓰는 요청.
// 저장된 Tool 재검증(AiToolResponse 기반)과는 다르게, 이 값들은 DB에 남지 않는다.
public record HealthCheckRequest(
	@NotBlank String toolType,
	String sideEffectType,
	String endpointUrl,
	String httpMethod,
	String authType,
	String credentialRef,
	String datasourceKey,
	Integer timeoutMs
) {
}
