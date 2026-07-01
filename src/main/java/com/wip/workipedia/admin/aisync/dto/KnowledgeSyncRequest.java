package com.wip.workipedia.admin.aisync.dto;

import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;

import java.util.List;
import java.util.Set;

/**
 * 지식 데이터 수동 동기화(run-now / resync) 요청.
 * sourceTypes가 비면 지식 2종 전체로 간주하며, 허용 외 값이 있으면 BAD_REQUEST.
 */
public record KnowledgeSyncRequest(List<AiSyncSourceType> sourceTypes) {

    private static final Set<AiSyncSourceType> ALLOWED =
        Set.of(AiSyncSourceType.KNOWLEDGE_DATA, AiSyncSourceType.MANUAL_KNOWLEDGE);

    public List<AiSyncSourceType> normalized() {
        if (sourceTypes == null || sourceTypes.isEmpty()) {
            return List.copyOf(ALLOWED);
        }
        if (!ALLOWED.containsAll(sourceTypes)) {
            throw new CustomException(ErrorType.BAD_REQUEST);
        }
        return sourceTypes;
    }
}
