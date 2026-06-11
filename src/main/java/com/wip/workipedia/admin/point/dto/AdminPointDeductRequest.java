package com.wip.workipedia.admin.point.dto;

import jakarta.validation.constraints.Min;

// 관리자 포인트 차감 요청값. amount는 실제 차감할 포인트 수량이다.
public record AdminPointDeductRequest(
	@Min(value = 1, message = "차감할 포인트는 1 이상이어야 합니다.")
	int amount
) {
}
