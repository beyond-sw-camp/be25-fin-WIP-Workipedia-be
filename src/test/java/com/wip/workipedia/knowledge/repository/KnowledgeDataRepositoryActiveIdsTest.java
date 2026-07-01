package com.wip.workipedia.knowledge.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * findActiveIds 네이티브 쿼리가 실제 MariaDB 스키마에서 유효하게 실행되는지 검증한다.
 * "삭제 제외" 의미론은 동일 형태의 {@code DirectDataRepositoryActiveIdsTest}에서 데이터로 검증한다.
 * (knowledge_data는 ticket/department/user FK가 있어 이 테스트에서 행을 직접 만들지 않는다)
 */
@SpringBootTest
@Transactional
class KnowledgeDataRepositoryActiveIdsTest {

    @Autowired KnowledgeDataRepository repository;

    @Test
    @DisplayName("findActiveIds는 스키마에서 정상 실행되어 ID 목록을 반환한다")
    void findActiveIds_executesAgainstSchema() {
        List<Long> ids = repository.findActiveIds();

        assertThat(ids).isNotNull();
    }
}
