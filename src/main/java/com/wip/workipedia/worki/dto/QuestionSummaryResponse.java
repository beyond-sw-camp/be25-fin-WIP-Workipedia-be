package com.wip.workipedia.worki.dto;

import com.wip.workipedia.worki.domain.QuestionStatus;
import com.wip.workipedia.worki.domain.WorkiQuestion;
import java.time.LocalDateTime;

public record QuestionSummaryResponse(
        Long questionId,
        Long authorId,
        String title,
        String content,
        QuestionStatus status,
        long viewCount,
        long likeCount,
        long answerCount,
        LocalDateTime createdAt
) {
    // likeCount는 reactions에서 배치 집계한 값을 서비스가 넘긴다.
    public static QuestionSummaryResponse of(WorkiQuestion question, long likeCount, long answerCount) {
        return new QuestionSummaryResponse(
                question.getQuestionId(),
                question.getAuthorId(),
                question.getTitle(),
                question.getContent(),
                question.getStatus(),
                question.getViewCount(),
                likeCount,
                answerCount,
                question.getCreatedAt()
        );
    }
}
