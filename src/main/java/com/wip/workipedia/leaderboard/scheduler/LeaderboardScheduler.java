package com.wip.workipedia.leaderboard.scheduler;

import com.wip.workipedia.leaderboard.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeaderboardScheduler {

    private final LeaderboardService leaderboardService;

    @Scheduled(cron = "0 0 9 * * MON", zone = "Asia/Seoul")
    public void refreshWeeklySnapshot() {
        leaderboardService.refreshWeeklySnapshot();
    }
}
