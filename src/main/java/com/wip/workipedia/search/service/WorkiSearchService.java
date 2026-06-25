package com.wip.workipedia.search.service;

import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.search.document.WorkiQuestionDocument;
import com.wip.workipedia.search.dto.WorkiSearchResponse;
import com.wip.workipedia.search.repository.WorkiQuestionSearchRepository;
import com.wip.workipedia.worki.service.WorkiQuestionService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 워키 질문 키워드 검색 서비스.
 * Elasticsearch에서 title/content를 검색한 뒤, 응답 DTO로 변환해 돌려준다.
 */
@Service
@RequiredArgsConstructor
public class WorkiSearchService {

    private final WorkiQuestionSearchRepository searchRepository;
    private final WorkiQuestionService questionService;

    public PageResponse<WorkiSearchResponse> searchQuestions(String keyword, Pageable pageable) {
        Page<WorkiQuestionDocument> page = searchRepository.searchByKeyword(keyword, pageable);
        List<Long> questionIds = page.getContent().stream()
                .map(WorkiQuestionDocument::getQuestionId)
                .toList();
        Map<Long, Long> answerCounts = questionService.loadQuestionAnswerCounts(questionIds);

        return PageResponse.from(page.map(document ->
                WorkiSearchResponse.from(document, answerCounts.getOrDefault(document.getQuestionId(), 0L))));
    }
}
