DROP INDEX idx_manual_citations_manual_chunk_id ON manual_citations;
DROP INDEX idx_manual_citations_source ON manual_citations;
DROP TABLE manual_citations;

CREATE TABLE rag_citations (
    citation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cited_by_type VARCHAR(30) NOT NULL,
    cited_by_id BIGINT NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    source_id VARCHAR(100) NOT NULL,
    chunk_index INT NULL,
    page_start INT NULL,
    page_end INT NULL,
    title VARCHAR(255) NULL,
    score DOUBLE NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    modified_source VARCHAR(30) NULL,
    is_deleted CHAR(1) NOT NULL DEFAULT 'N' CHECK (is_deleted IN ('Y', 'N')),
    CONSTRAINT ck_rag_citations_cited_by_type
        CHECK (cited_by_type IN ('CHATBOT_MESSAGE')),
    CONSTRAINT ck_rag_citations_source_type
        CHECK (source_type IN ('MANUAL', 'WORKI', 'KNOWLEDGE_DATA', 'MANUAL_KNOWLEDGE')),
    CONSTRAINT fk_rag_citations_chatbot_message
        FOREIGN KEY (cited_by_id)
        REFERENCES chatbot_messages (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_rag_citations_cited_by
    ON rag_citations (cited_by_type, cited_by_id);

CREATE INDEX idx_rag_citations_source
    ON rag_citations (source_type, source_id);

CREATE INDEX idx_rag_citations_manual_popular
    ON rag_citations (source_type, source_id, deleted_at);
