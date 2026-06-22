package com.wip.workipedia.leaderboard.scheduler;

import com.wip.workipedia.leaderboard.service.EsgMetricWeeklyService;
import com.wip.workipedia.leaderboard.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeaderboardScheduler {

    private final LeaderboardService leaderboardService;
    private final EsgMetricWeeklyService esgMetricWeeklyService;

    // 리더보드는 한 주 동안 같은 순위를 보여주기 위해 매주 월요일 00시에만 갱신한다.
    @Scheduled(cron = "0 0 0 * * MON")
    public void refreshWeeklySnapshot() {
        leaderboardService.refreshWeeklySnapshot();
        esgMetricWeeklyService.refreshPreviousWeekMetric();
    }
}
