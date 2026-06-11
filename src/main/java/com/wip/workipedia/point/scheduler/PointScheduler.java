package com.wip.workipedia.point.scheduler;

import com.wip.workipedia.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointScheduler {

    private final PointService pointService;

    // 매년 1월 1일 00:00:00 실행
    @Scheduled(cron = "0 0 0 1 1 *")
    public void resetPointsForNewYear() {
        pointService.resetAllUserPointsForNewYear();
    }
}