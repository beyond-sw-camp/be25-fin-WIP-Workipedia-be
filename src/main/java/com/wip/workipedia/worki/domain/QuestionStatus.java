package com.wip.workipedia.worki.domain;

public enum QuestionStatus {
    WAITING,      // 답변이 없는 상태
    IN_PROGRESS,  // 답변이 있는 상태
    ANSWERED,     // 답변이 채택된 상태
    TICKETED,     // 티켓 전환
    DELETED       // 관리자 삭제 처리
}
