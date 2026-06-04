package com.wip.workipedia.worki.domain;

public enum QuestionStatus {
    WAITING,      // 답변 대기
    IN_PROGRESS,  // 답변 진행
    ANSWERED,     // 채택 완료
    TICKETED,     // 티켓 전환
    DELETED       // 관리자 삭제 처리
}
