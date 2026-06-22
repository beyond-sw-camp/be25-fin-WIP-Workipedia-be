package com.wip.workipedia.search.service;

import com.wip.workipedia.search.document.WorkiQuestionDocument;
import com.wip.workipedia.search.repository.WorkiQuestionSearchRepository;
import com.wip.workipedia.worki.domain.WorkiQuestion;
import com.wip.workipedia.worki.event.WorkiQuestionChangedEvent;
import com.wip.workipedia.worki.repository.WorkiQuestionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 워키 질문을 Elasticsearch 색인(index)에 반영한다.
 * 색인 실패가 질문 등록/수정 같은 본래 기능을 막지 않도록 예외를 삼키고 로그만 남긴다.
 * (색인이 어긋나면 reindexAll()로 전체 재색인해 복구한다.)
 * ToDo: 한국어 검색 기능 상향이 필요함. 나중에 할 예정. (퀄리티 측면이라)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkiQuestionIndexer {

    private final WorkiQuestionSearchRepository searchRepository;
    private final WorkiQuestionRepository questionRepository;
    private final ElasticsearchOperations elasticsearchOperations;

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

    /**
     * 삭제되지 않은 전체 질문을 다시 색인(초기 적재/복구용 + 매핑 변경 반영용).
     * 색인이 어긋났을 때 DB를 기준으로 ES를 완전히 다시 맞추는 게 목적이므로,
     * 인덱스 자체를 drop 후 재생성(create)하고 살아있는 질문만 다시 적재한다.
     * 문서만 지우는(deleteAll) 방식과 달리 인덱스를 새로 만들기 때문에,
     * WorkiQuestionDocument의 매핑 변경(예: analyzer = "nori")도 이 호출로 반영된다.
     * (ES 매핑은 인덱스 생성 시점에 고정되어, 인덱스가 살아있으면 매핑이 바뀌지 않음)
     * 적재 사이 짧은 순간 검색 결과가 비어 보일 수 있으나, 관리자 전용 복구 작업이라 허용한다.
     */
    public long reindexAll() {
        // findAll() 후 메모리 필터링 대신 DB에서 삭제되지 않은 질문만 걸러 가져온다.
        List<WorkiQuestion> questions = questionRepository.findByDeletedAtIsNull();

        // 인덱스를 통째로 지우고 @Document/@Field 기준으로 다시 만들어 최신 매핑을 반영한다.
        IndexOperations indexOps = elasticsearchOperations.indexOps(WorkiQuestionDocument.class);
        indexOps.delete();
        indexOps.createWithMapping();

        searchRepository.saveAll(questions.stream().map(WorkiQuestionDocument::from).toList());
        log.info("워키 질문 전체 재색인 완료 count={}", questions.size());
        return questions.size();
    }
}
