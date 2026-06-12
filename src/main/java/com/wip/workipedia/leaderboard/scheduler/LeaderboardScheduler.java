package com.wip.workipedia.leaderboard.scheduler;

import com.wip.workipedia.leaderboard.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeaderboardScheduler {

    private final LeaderboardService leaderboardService;

    // 리더보드는 한 주 동안 같은 순위를 보여주기 위해 매주 월요일 오전 9시에만 갱신한다.
    @Scheduled(cron = "0 0 9 * * MON")
    public void refreshWeeklySnapshot() {
        leaderboardService.refreshWeeklySnapshot();
    }
}
