package com.wip.workipedia.ragcitation.service;

import com.wip.workipedia.chatbot.ai.SourceItem;
import com.wip.workipedia.ragcitation.domain.RagCitation;
import com.wip.workipedia.ragcitation.domain.RagCitationCitedByType;
import com.wip.workipedia.ragcitation.domain.RagCitationSourceType;
import com.wip.workipedia.ragcitation.repository.RagCitationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagCitationService {

    private static final String FAQ_POPULAR_MANUALS_CACHE = "faq:popularManuals";

    private final RagCitationRepository ragCitationRepository;
    private final CacheManager cacheManager;

    public void replaceChatbotMessageCitations(Long messageId, List<SourceItem> sources) {
        ragCitationRepository.deleteByCitedByTypeAndCitedById(RagCitationCitedByType.CHATBOT_MESSAGE, messageId);
        if (sources == null || sources.isEmpty()) {
            return;
        }

        List<RagCitation> citations = sources.stream()
                .filter(this::isPersistable)
                .map(source -> RagCitation.fromChatbotMessage(messageId, source))
                .toList();
        if (citations.isEmpty()) {
            return;
        }

        ragCitationRepository.saveAll(citations);
        if (citations.stream().anyMatch(citation -> citation.getSourceType() == RagCitationSourceType.MANUAL)) {
            evictPopularManualsCache();
        }
    }

    private boolean isPersistable(SourceItem source) {
        if (source == null || isBlank(source.sourceType()) || isBlank(source.sourceId())) {
            return false;
        }
        try {
            RagCitationSourceType.valueOf(source.sourceType());
            return true;
        } catch (IllegalArgumentException e) {
            log.warn("지원하지 않는 RAG sourceType 인용 저장 제외: sourceType={}, sourceId={}",
                    source.sourceType(), source.sourceId());
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void evictPopularManualsCache() {
        Cache cache = cacheManager.getCache(FAQ_POPULAR_MANUALS_CACHE);
        if (cache != null) {
            cache.clear();
        }
    }
}
