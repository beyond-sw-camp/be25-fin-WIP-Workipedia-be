-- 검색어 자동완성용 테이블. 사용자가 검색한 단어를 누적 집계해 prefix 인기순 추천에 사용한다.
-- keyword에 UNIQUE를 걸어 동일 검색어는 한 행으로 모으고 search_count만 증가시킨다(ON DUPLICATE KEY UPDATE).
CREATE TABLE worki_search_keywords (
    search_keyword_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword           VARCHAR(100) NOT NULL,
    search_count      BIGINT NOT NULL DEFAULT 0,
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_worki_search_keywords_keyword UNIQUE (keyword)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
