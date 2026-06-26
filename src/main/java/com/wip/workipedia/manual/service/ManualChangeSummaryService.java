package com.wip.workipedia.manual.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.manual.ai.ManualChangeSummaryAiClient;
import com.wip.workipedia.manual.ai.dto.ManualChangeSummaryRequest;
import com.wip.workipedia.manual.domain.ManualVersion;
import com.wip.workipedia.manual.repository.ManualVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

// aisync 워커가 호출. 버전의 diff/제목/사유로 AI 요약을 받아 change_summary에 저장한다.
// 매뉴얼 저장 트랜잭션과 분리된 자체 트랜잭션이며, AI 예외는 그대로 전파해 워커가 markFailed 처리한다.
@Service
@RequiredArgsConstructor
public class ManualChangeSummaryService {

    private final ManualVersionRepository manualVersionRepository;
    private final ManualChangeSummaryAiClient aiClient;
    private final TransactionTemplate transactionTemplate;

    public void summarize(Long manualVersionId) {
        ManualChangeSummaryRequest request = transactionTemplate.execute(status -> {
            ManualVersion version = manualVersionRepository.findById(manualVersionId)
                .orElseThrow(() -> new CustomException(ErrorType.MANUAL_NOT_FOUND));

            return new ManualChangeSummaryRequest(
                version.getTitle(),
                version.getContentDiff(),
                version.getUpdateReason()
            );
        });

        String summary = aiClient.summarize(request);

        transactionTemplate.executeWithoutResult(status -> {
            ManualVersion version = manualVersionRepository.findById(manualVersionId)
                .orElseThrow(() -> new CustomException(ErrorType.MANUAL_NOT_FOUND));
            version.applyChangeSummary(summary);
        });
    }
}
