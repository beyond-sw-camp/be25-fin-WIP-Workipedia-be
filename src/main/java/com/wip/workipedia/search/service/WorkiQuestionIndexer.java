package com.wip.workipedia.search.service;

import com.wip.workipedia.search.document.WorkiQuestionDocument;
import com.wip.workipedia.search.repository.WorkiQuestionSearchRepository;
import com.wip.workipedia.worki.domain.WorkiQuestion;
import com.wip.workipedia.worki.event.WorkiQuestionChangedEvent;
import com.wip.workipedia.worki.repository.WorkiQuestionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 워키 질문을 Elasticsearch 색인(index)에 반영한다.
 * 색인 실패가 질문 등록/수정 같은 본래 기능을 막지 않도록 예외를 삼키고 로그만 남긴다.
 * (색인이 어긋나면 reindexAll()로 전체 재색인해 복구한다.)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkiQuestionIndexer {

    private final WorkiQuestionSearchRepository searchRepository;
    private final WorkiQuestionRepository questionRepository;

    /**
     * 워키 질문 변경 생성 이벤트를 구독해 ES 색인을 반영한다.
     * - AFTER_COMMIT: DB 커밋이 끝난 뒤에만 실행(롤백되면 색인 안 함).
     * - @Async: 별도 스레드에서 실행해 글쓰기 응답 속도에 영향을 주지 않음.
     * questionId로 DB를 다시 조회해, 살아있으면 색인하고 없으면(삭제됨) 색인에서 제거한다.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleQuestionChanged(WorkiQuestionChangedEvent event) {
        questionRepository.findByQuestionIdAndDeletedAtIsNull(event.questionId())
                .ifPresentOrElse(this::index, () -> delete(event.questionId()));
    }

    /** 질문 1건을 색인(없으면 추가, 있으면 갱신). */
    public void index(WorkiQuestion question) {
        try {
            searchRepository.save(WorkiQuestionDocument.from(question));
        } catch (Exception e) {
            log.warn("워키 질문 색인 실패 questionId={}", question.getQuestionId(), e);
        }
    }

    /** 질문 1건을 색인에서 제거. */
    public void delete(Long questionId) {
        try {
            searchRepository.deleteById(questionId);
        } catch (Exception e) {
            log.warn("워키 질문 색인 삭제 실패 questionId={}", questionId, e);
        }
    }

    /** 삭제되지 않은 전체 질문을 다시 색인(초기 적재/복구용). */
    public long reindexAll() {
        List<WorkiQuestion> questions = questionRepository.findAll().stream()
                .filter(q -> q.getDeletedAt() == null)
                .toList();
        searchRepository.saveAll(questions.stream().map(WorkiQuestionDocument::from).toList());
        log.info("워키 질문 전체 재색인 완료 count={}", questions.size());
        return questions.size();
    }
}
