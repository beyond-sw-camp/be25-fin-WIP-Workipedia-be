package com.wip.workipedia.search.document;

import com.wip.workipedia.worki.domain.WorkiQuestion;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 워키 질문을 Elasticsearch에서 검색하기 위한 문서.
 * JPA 엔티티(WorkiQuestion)는 DB 저장용, 이 문서는 ES 검색용으로 별도 분리한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "worki_questions")
public class WorkiQuestionDocument {

    // ES 문서의 _id. JPA가 아니라 Spring Data 공용 @Id를 쓴다.
    @Id
    private Long questionId;

    // Text: 형태소/토큰으로 쪼개서 "부분/유사 검색"이 가능한 타입.
    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String content;

    // Keyword: 쪼개지 않고 값 전체로 매칭(필터/정렬용).
    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Long)
    private Long authorId;

    @Field(type = FieldType.Long)
    private long viewCount;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createdAt;

    @Builder
    private WorkiQuestionDocument(Long questionId, String title, String content,
            String status, Long authorId, long viewCount, LocalDateTime createdAt) {
        this.questionId = questionId;
        this.title = title;
        this.content = content;
        this.status = status;
        this.authorId = authorId;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
    }

    /** JPA 엔티티 -> ES 문서 변환. */
    public static WorkiQuestionDocument from(WorkiQuestion question) {
        return WorkiQuestionDocument.builder()
                .questionId(question.getQuestionId())
                .title(question.getTitle())
                .content(question.getContent())
                .status(question.getStatus().name())
                .authorId(question.getAuthorId())
                .viewCount(question.getViewCount())
                .createdAt(question.getCreatedAt())
                .build();
    }
}
