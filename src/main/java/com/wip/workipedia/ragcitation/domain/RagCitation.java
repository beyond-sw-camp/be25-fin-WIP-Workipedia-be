package com.wip.workipedia.ragcitation.domain;

import com.wip.workipedia.chatbot.ai.SourceItem;
import com.wip.workipedia.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "rag_citations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagCitation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "citation_id")
    private Long citationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "cited_by_type", nullable = false, length = 30)
    private RagCitationCitedByType citedByType;

    @Column(name = "cited_by_id", nullable = false)
    private Long citedById;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 40)
    private RagCitationSourceType sourceType;

    @Column(name = "source_id", nullable = false, length = 100)
    private String sourceId;

    @Column(name = "chunk_index")
    private Integer chunkIndex;

    @Column(name = "page_start")
    private Integer pageStart;

    @Column(name = "page_end")
    private Integer pageEnd;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "score")
    private Double score;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "CHAR(1)")
    private String isDeleted = "N";

    public static RagCitation fromChatbotMessage(Long messageId, SourceItem source) {
        RagCitation citation = new RagCitation();
        citation.citedByType = RagCitationCitedByType.CHATBOT_MESSAGE;
        citation.citedById = messageId;
        citation.sourceType = RagCitationSourceType.valueOf(source.sourceType());
        citation.sourceId = source.sourceId();
        citation.chunkIndex = source.chunkIndex();
        citation.pageStart = source.pageStart();
        citation.pageEnd = source.pageEnd();
        citation.title = source.title();
        citation.score = source.score();
        return citation;
    }
}
