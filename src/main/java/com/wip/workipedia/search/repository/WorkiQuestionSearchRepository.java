package com.wip.workipedia.search.repository;

import com.wip.workipedia.search.document.WorkiQuestionDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 워키 질문 ES 검색 리포지토리.
 * JpaRepository 대신 ElasticsearchRepository를 상속하면, 저장/삭제는 물론
 * @Query로 ES 검색 쿼리도 선언적으로 쓸 수 있다.
 * 현재는 키워드 검색만 사용. 벡터 검색은 나중에 들어갈 예정.
 */
public interface WorkiQuestionSearchRepository
        extends ElasticsearchRepository<WorkiQuestionDocument, Long> {

    // multi_match: 키워드 하나로 title, content 두 필드를 한 번에 검색한다.
    // ?0 자리에 첫 번째 인자(keyword)가 들어간다.
    @Query("""
            {
              "multi_match": {
                "query": "?0",
                "fields": ["title", "content"]
              }
            }
            """)
    Page<WorkiQuestionDocument> searchByKeyword(String keyword, Pageable pageable);
}
