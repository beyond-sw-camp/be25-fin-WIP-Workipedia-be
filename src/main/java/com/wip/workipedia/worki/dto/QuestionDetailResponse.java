package com.wip.workipedia.worki.dto;

import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.worki.domain.QuestionStatus;
import com.wip.workipedia.worki.domain.WorkiQuestion;
import java.time.LocalDateTime;
import java.util.List;

public record QuestionDetailResponse(
        Long questionId,
        Long authorId,
        // 프론트의 "부서 · 닉네임"(authorLabel) 표시를 위해 BE가 내려준다. 작성자가 없으면(탈퇴 등) null.
        String authorNickname,
        String authorDepartmentName,
        String title,
        String content,
        QuestionStatus status,
        Long acceptedAnswerId,
        long viewCount,
        long likeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<AnswerResponse> answers
) {
    // author가 null이면(탈퇴 등) 닉네임/부서명은 null. department는 LAZY라 트랜잭션 안에서 호출해야 한다.
    // 답변 목록은 작성자 batch 조회가 필요해 서비스에서 만들어 넘긴다.
    // likeCount는 reactions에서 COUNT로 집계한 값을 서비스가 넘긴다.
    public static QuestionDetailResponse of(
            WorkiQuestion question,
            User author,
            long likeCount,
            List<AnswerResponse> answers) {
        return new QuestionDetailResponse(
                question.getQuestionId(),
                question.getAuthorId(),
                author != null ? author.getNickname() : null,
                author != null && author.getDepartment() != null
                        ? author.getDepartment().getDepartmentName()
                        : null,
                question.getTitle(),
                question.getContent(),
                question.getStatus(),
                question.getAcceptedAnswerId(),
                question.getViewCount(),
                likeCount,
                question.getCreatedAt(),
                question.getUpdatedAt(),
                answers
        );
    }
}
