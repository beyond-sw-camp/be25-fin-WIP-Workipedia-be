package com.wip.workipedia.aisync.client;

import com.wip.workipedia.aisync.client.dto.TextIngestRequest;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.directdata.domain.DirectData;
import com.wip.workipedia.directdata.repository.DirectDataRepository;
import com.wip.workipedia.knowledge.domain.KnowledgeData;
import com.wip.workipedia.knowledge.repository.KnowledgeDataRepository;
import com.wip.workipedia.worki.domain.WorkiAnswer;
import com.wip.workipedia.worki.domain.WorkiQuestion;
import com.wip.workipedia.worki.repository.WorkiAnswerRepository;
import com.wip.workipedia.worki.repository.WorkiQuestionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class TextDocumentAiClient {

    private final RestClient restClient;
    private final WorkiQuestionRepository workiQuestionRepository;
    private final WorkiAnswerRepository workiAnswerRepository;
    private final KnowledgeDataRepository knowledgeDataRepository;
    private final DirectDataRepository directDataRepository;

    public TextDocumentAiClient(
        @Qualifier("syncAiRestClient") RestClient restClient,
        WorkiQuestionRepository workiQuestionRepository,
        WorkiAnswerRepository workiAnswerRepository,
        KnowledgeDataRepository knowledgeDataRepository,
        DirectDataRepository directDataRepository
    ) {
        this.restClient = restClient;
        this.workiQuestionRepository = workiQuestionRepository;
        this.workiAnswerRepository = workiAnswerRepository;
        this.knowledgeDataRepository = knowledgeDataRepository;
        this.directDataRepository = directDataRepository;
    }

    public void ingest(AiSyncSourceType sourceType, Long sourceId) {
        TextIngestRequest request = buildRequest(sourceType, sourceId);
        try {
            restClient.post()
                .uri("/api/v1/documents/ingest-text")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.error("텍스트 AI 인덱싱 실패: sourceType={}, sourceId={}, error={}", sourceType, sourceId, e.getMessage());
            throw new CustomException(ErrorType.AI_SYNC_FAILED);
        }
    }

    public void delete(AiSyncSourceType sourceType, Long sourceId) {
        try {
            restClient.delete()
                .uri("/api/v1/documents/{sourceId}?source_type={sourceType}", sourceId, sourceType.name())
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.error("텍스트 AI 삭제 실패: sourceType={}, sourceId={}, error={}", sourceType, sourceId, e.getMessage());
            throw new CustomException(ErrorType.AI_SYNC_FAILED);
        }
    }

    private TextIngestRequest buildRequest(AiSyncSourceType sourceType, Long sourceId) {
        return switch (sourceType) {
            case WORKI -> buildWorkiRequest(sourceId);
            case KNOWLEDGE_DATA -> {
                KnowledgeData k = knowledgeDataRepository
                    .findByKnowledgeDataIdAndDeletedAtIsNull(sourceId)
                    .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND));
                yield new TextIngestRequest(sourceId, sourceType.name(), k.getTitle(), k.getContent());
            }
            case MANUAL_KNOWLEDGE -> {
                DirectData d = directDataRepository
                    .findByDirectDataIdAndDeletedAtIsNull(sourceId)
                    .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND));
                yield new TextIngestRequest(sourceId, sourceType.name(), d.getTitle(), d.getContent());
            }
            default -> throw new CustomException(ErrorType.BAD_REQUEST);
        };
    }

    private TextIngestRequest buildWorkiRequest(Long questionId) {
        WorkiQuestion q = workiQuestionRepository
            .findByQuestionIdAndDeletedAtIsNull(questionId)
            .orElseThrow(() -> new CustomException(ErrorType.WORKI_NOT_FOUND));

        List<WorkiAnswer> answers = workiAnswerRepository
            .findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(questionId);

        // 채택 답변 우선, 없으면 최신 답변 (리스트가 ASC 정렬이므로 마지막이 최신)
        Optional<WorkiAnswer> answerOpt = answers.stream()
            .filter(WorkiAnswer::isAccepted)
            .findFirst()
            .or(() -> answers.isEmpty() ? Optional.empty() : Optional.of(answers.get(answers.size() - 1)));

        String text = answerOpt
            .map(a -> q.getContent() + "\n\n채택 답변: " + a.getContent())
            .orElse(q.getContent());

        return new TextIngestRequest(questionId, "WORKI", q.getTitle(), text);
    }
}
