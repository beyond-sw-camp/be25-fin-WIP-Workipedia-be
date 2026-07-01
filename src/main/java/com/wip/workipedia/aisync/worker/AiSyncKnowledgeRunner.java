package com.wip.workipedia.aisync.worker;

import com.wip.workipedia.aisync.domain.AiSyncJob;
import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.service.AiSyncJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 지식 데이터 즉시 실행(run-now)용 비동기 드레인.
 * cron 주기를 기다리지 않고 스코프의 PENDING 잡을 배치 단위로 소진할 때까지 반복 처리한다.
 * <p>
 * {@code @Async} 프록시가 적용되려면 반드시 외부 빈(AdminAiSyncJobService)에서 호출해야 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiSyncKnowledgeRunner {

    // 무한 루프 방어 상한 (batchSize 기준 충분히 큼)
    private static final int MAX_ITERATIONS = 1000;

    private final AiSyncJobService aiSyncJobService;
    private final AiSyncJobProcessor processor;

    @Async
    public void drain(List<AiSyncSourceType> sourceTypes) {
        log.info("[AI-SYNC][RUN-NOW] drain 시작 scope={}", sourceTypes);
        int iterations = 0;
        while (iterations++ < MAX_ITERATIONS) {
            aiSyncJobService.recoverExpiredLeases();
            List<AiSyncJob> jobs = aiSyncJobService.claimPendingKnowledgeJobs(sourceTypes);
            if (jobs.isEmpty()) {
                break;
            }
            processor.processJobs(jobs);
        }
        log.info("[AI-SYNC][RUN-NOW] drain 종료 scope={}, iterations={}", sourceTypes, iterations - 1);
    }
}
