package com.wip.workipedia.worki.dto;

import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.worki.domain.WorkiAnswer;
import java.time.LocalDateTime;

public record AnswerResponse(
        Long answerId,
        Long questionId,
        Long authorId,
        // 프론트의 "부서 · 닉네임"(authorLabel) 표시를 위해 BE가 내려준다. 작성자가 없으면(탈퇴 등) null.
        String authorNickname,
        String authorDepartmentName,
        String content,
        boolean accepted,
        boolean official,
        LocalDateTime acceptedAt,
        LocalDateTime createdAt
) {
    // author가 null이면(탈퇴 등) 닉네임/부서명은 null로 내려간다. department는 LAZY라 트랜잭션 안에서 호출해야 한다.
    public static AnswerResponse of(WorkiAnswer answer, User author) {
        return new AnswerResponse(
                answer.getAnswerId(),
                answer.getQuestionId(),
                answer.getAuthorId(),
                author != null ? author.getNickname() : null,
                author != null && author.getDepartment() != null
                        ? author.getDepartment().getDepartmentName()
                        : null,
                answer.getContent(),
                answer.isAccepted(),
                answer.isOfficial(),
                answer.getAcceptedAt(),
                answer.getCreatedAt()
        );
    }
}
