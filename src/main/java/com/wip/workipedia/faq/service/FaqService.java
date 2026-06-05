package com.wip.workipedia.faq.service;

import com.wip.workipedia.faq.dto.ManualSummaryResponse;
import com.wip.workipedia.faq.dto.PopularWorkiResponse;
import com.wip.workipedia.manual.domain.ManualStatus;
import com.wip.workipedia.manual.repository.ManualRepository;
import com.wip.workipedia.worki.repository.WorkiQuestionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {

    private final WorkiQuestionRepository workiQuestionRepository;
    private final ManualRepository manualRepository;

    // 캐시 적용. 
    @Cacheable("faq:popularWorki")
    public List<PopularWorkiResponse> getPopularWorki() {
        return workiQuestionRepository.findTop10PopularByLike().stream()
                .map(PopularWorkiResponse::from)
                .toList();
    }

    @Cacheable("faq:popularManuals")
    public List<ManualSummaryResponse> getPopularManuals() {
        return manualRepository.findTop10PopularByCitation().stream()
                .map(ManualSummaryResponse::from)
                .toList();
    }
    
    @Cacheable("faq:recentManuals")
    public List<ManualSummaryResponse> getRecentManuals() {
        return manualRepository
                .findTop10ByDeletedAtIsNullAndStatusOrderByCreatedAtDesc(ManualStatus.PUBLISHED)
                .stream()
                .map(ManualSummaryResponse::from)
                .toList();
    }
}
