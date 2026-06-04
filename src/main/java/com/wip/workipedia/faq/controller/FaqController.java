package com.wip.workipedia.faq.controller;

import com.wip.workipedia.faq.dto.ManualSummaryResponse;
import com.wip.workipedia.faq.dto.PopularWorkiResponse;
import com.wip.workipedia.faq.service.FaqService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: 이슬이 시큐리티 통합 후 인증 가드 추가 (현재는 공개).
@RestController
@RequestMapping("/faq")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    @GetMapping("/worki/popular")
    public ResponseEntity<List<PopularWorkiResponse>> popularWorki() {
        return ResponseEntity.ok(faqService.getPopularWorki());
    }

    @GetMapping("/manuals/popular")
    public ResponseEntity<List<ManualSummaryResponse>> popularManuals() {
        return ResponseEntity.ok(faqService.getPopularManuals());
    }

    @GetMapping("/manuals/recent")
    public ResponseEntity<List<ManualSummaryResponse>> recentManuals() {
        return ResponseEntity.ok(faqService.getRecentManuals());
    }
}
