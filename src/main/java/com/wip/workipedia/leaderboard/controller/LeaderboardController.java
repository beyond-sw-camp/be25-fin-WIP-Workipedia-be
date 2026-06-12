package com.wip.workipedia.leaderboard.controller;

import com.wip.workipedia.leaderboard.dto.LeaderboardResponse;
import com.wip.workipedia.leaderboard.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    public ResponseEntity<LeaderboardResponse> getLeaderboard(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(leaderboardService.getLeaderboard(userId));
    }
}
