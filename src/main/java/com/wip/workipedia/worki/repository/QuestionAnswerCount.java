package com.wip.workipedia.worki.repository;

// 질문별 답변 개수 배치 집계 결과. 목록/검색 카드에서 댓글 수를 N+1 없이 표시하기 위해 사용한다.
public interface QuestionAnswerCount {
    Long getQuestionId();

    long getAnswerCount();
}
