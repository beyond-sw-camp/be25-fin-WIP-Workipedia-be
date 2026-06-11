package com.wip.workipedia.worki.scheduler;

import com.wip.workipedia.worki.service.WorkiViewCountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Redis에 쌓인 워키 조회수 증가분을 주기적으로 DB에 일괄 반영한다.
 *
 * <p>요청 경로에서 곧바로 UPDATE를 날리지 않고 여기서 모아 처리하므로, 인기글에 조회가 몰려도
 * DB write 횟수가 (질문 수)로 수렴해 락 경합이 줄어든다.
 *
 * <p>주기는 {@code worki.view-count.flush-interval-ms} 로 조정 가능(기본 60초).
 * 이전 실행이 끝난 뒤부터 간격을 재는 fixedDelay라 작업이 겹치지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkiViewCountScheduler {

    private final WorkiViewCountService viewCountService;

    @Scheduled(fixedDelayString = "${worki.view-count.flush-interval-ms:60000}")
    public void flushViewCounts() {
        try {
            viewCountService.flushToDatabase();
        } catch (Exception e) {
            // 한 번 실패해도 누적분은 Redis에 남아 다음 주기에 다시 시도되므로, 스케줄러를 죽이지 않고 로그만 남긴다.
            log.warn("워키 조회수 DB 반영(flush) 실패. 다음 주기에 재시도한다.", e);
        }
    }
}
