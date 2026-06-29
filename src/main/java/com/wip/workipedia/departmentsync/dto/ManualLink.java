package com.wip.workipedia.departmentsync.dto;

// 자동 매칭이 안 되는(이름 다른 동일 부서) ERP 부서를 관리자가 기존 부서에 직접 연결한다.
// applyRoutingPrompt=true면 해당 ERP duty_desc로 그 부서의 R&R을 설정(덮어쓰기 허용)한다.
public record ManualLink(String externalId, Long departmentId, boolean applyRoutingPrompt) {}
