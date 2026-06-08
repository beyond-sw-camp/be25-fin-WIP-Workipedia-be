package com.wip.workipedia.worki.event;

/**
 * 워키 질문이 등록/수정/삭제되었음을 알리는 도메인 이벤트.
 * 워키 도메인은 이 이벤트만 발행하고, 검색 색인 같은 후속 처리는 구독하는 쪽(search)이 담당한다.
 * (커밋 후 detached 위험을 피하려고 엔티티가 아니라 questionId만 담는다.)
 */
public record WorkiQuestionChangedEvent(Long questionId) {
}
