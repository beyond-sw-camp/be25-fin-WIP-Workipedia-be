package com.wip.workipedia.common.domain;

// 마지막 수정 주체를 구분하는 출처. 사용자/관리자/챗봇/시스템 작업의 추적에 사용.
public enum ModifiedSource {
    USER,
    ADMIN,
    CHATBOT,
    SYSTEM
}
