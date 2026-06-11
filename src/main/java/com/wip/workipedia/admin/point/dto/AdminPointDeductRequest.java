package com.wip.workipedia.admin.point.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 관리자 포인트 차감 요청값. amount는 차감 수량, reason은 point_history에 남길 차감 사유다.
public record AdminPointDeductRequest(
	@Min(value = 1, message = "차감할 포인트는 1 이상이어야 합니다.")
	int amount,

	@NotBlank(message = "차감 사유를 입력해주세요.")
	@Size(max = 50, message = "차감 사유는 50자 이하여야 합니다.")
	String reason
) {
}
