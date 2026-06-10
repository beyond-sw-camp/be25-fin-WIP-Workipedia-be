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

    // bool 쿼리로 두 조건을 함께 건다.
    //  - must     : multi_match — 키워드 하나로 title, content 두 필드를 동시 검색(?0 = keyword)
    //  - must_not : 삭제 처리(status=DELETED)된 질문은 검색 결과에서 제외(방어용 필터)
    // 평소엔 인디서가 삭제 글을 ES에서 지워 동기화하지만, 이벤트가 누락돼 잔류하더라도
    // 검색에 노출되지 않도록 쿼리 단에서 한 번 더 막는다.
    @Query("""
            {
              "bool": {
                "must": {
                  "multi_match": {
                    "query": "?0",
                    "fields": ["title", "content"]
                  }
                },
                "must_not": {
                  "term": { "status": "DELETED" }
                }
              }
            }
            """)
    Page<WorkiQuestionDocument> searchByKeyword(String keyword, Pageable pageable);
}
