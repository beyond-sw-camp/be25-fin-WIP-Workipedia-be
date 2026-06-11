package com.wip.workipedia.search.repository;

import com.wip.workipedia.search.domain.WorkiSearchKeyword;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface WorkiSearchKeywordRepository extends JpaRepository<WorkiSearchKeyword, Long> {

    // 검색어 누적. 같은 keyword가 이미 있으면 search_count만 +1, 없으면 새로 INSERT(=upsert).
    // 동시 검색에도 안전하도록 find-then-save 대신 단일 SQL(ON DUPLICATE KEY UPDATE)로 처리한다.
    @Modifying
    @Query(value = """
            INSERT INTO worki_search_keywords (keyword, search_count, created_at, updated_at)
            VALUES (:keyword, 1, NOW(), NOW())
            ON DUPLICATE KEY UPDATE search_count = search_count + 1, updated_at = NOW()
            """, nativeQuery = true)
    void upsertKeyword(String keyword);

    // 자동완성: keyword가 prefix로 시작(LIKE 'prefix%')하는 검색어를 인기순(검색 많은 순) 최대 10건.
    List<WorkiSearchKeyword> findTop10ByKeywordStartingWithOrderBySearchCountDesc(String prefix);
}
