package com.wip.workipedia.admin.aisync.dto;

import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnowledgeSyncRequestTest {

    @Test
    @DisplayName("null 이면 지식 2종 전체로 정규화")
    void normalize_null() {
        assertThat(new KnowledgeSyncRequest(null).normalized())
            .containsExactlyInAnyOrder(AiSyncSourceType.KNOWLEDGE_DATA, AiSyncSourceType.MANUAL_KNOWLEDGE);
    }

    @Test
    @DisplayName("허용 외 타입이면 BAD_REQUEST")
    void normalize_invalid() {
        assertThatThrownBy(() -> new KnowledgeSyncRequest(List.of(AiSyncSourceType.WORKI)).normalized())
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
    }

    @Test
    @DisplayName("허용 타입만 있으면 그대로 반환")
    void normalize_valid() {
        assertThat(new KnowledgeSyncRequest(List.of(AiSyncSourceType.KNOWLEDGE_DATA)).normalized())
            .containsExactly(AiSyncSourceType.KNOWLEDGE_DATA);
    }
}
