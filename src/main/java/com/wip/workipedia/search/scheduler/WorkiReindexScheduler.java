package com.wip.workipedia.search.scheduler;

import com.wip.workipedia.search.service.WorkiQuestionIndexer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 워키 질문 Elasticsearch 색인을 매일 자정에 DB 기준으로 전체 재색인한다.
 *
 * <p>평소에는 글 등록/수정 이벤트로 색인이 실시간 반영되지만, 색인 실패를 삼키는 구조라
 * 시간이 지나면 DB와 ES가 미세하게 어긋날 수 있다. 자정 전체 재색인으로 매일 한 번 DB 기준으로
 * 완전히 맞춰 정합성을 회복한다.
 *
 * <p>HTTP가 아니라 스프링 스케줄러가 서버 내부에서 직접 호출하므로, 로그인한 사용자(관리자 포함)가
 * 없어도 실행된다. 수동 트리거용 HTTP 엔드포인트(/search/worki/reindex)와는 독립적이다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkiReindexScheduler {

    private final WorkiQuestionIndexer workiQuestionIndexer;

    // 초 분 시 일 월 요일 → 매일 00:00:00. 서버 타임존과 무관하게 한국 자정에 돌도록 zone 명시.
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void reindexAtMidnight() {
        try {
            long count = workiQuestionIndexer.reindexAll();
            log.info("워키 질문 자정 전체 재색인 완료 count={}", count);
        } catch (Exception e) {
            // 한 번 실패해도 다음 자정에 다시 시도되고 실시간 이벤트 색인은 계속 동작하므로,
            // 스케줄러를 죽이지 않고 로그만 남긴다.
            log.warn("워키 질문 자정 전체 재색인 실패. 다음 주기에 재시도한다.", e);
        }
    }
}
