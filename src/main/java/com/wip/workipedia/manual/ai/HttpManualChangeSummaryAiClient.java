package com.wip.workipedia.manual.ai;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.manual.ai.dto.ManualChangeSummaryAiResponse;
import com.wip.workipedia.manual.ai.dto.ManualChangeSummaryRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// 매뉴얼 변경 diff를 AI 서버에 보내 한 줄 요약을 받는다. AI 호출 책임만 가지며 DB는 접근하지 않는다.
// 요약은 비필수이므로 실패 시 예외를 던져 aisync 워커가 markFailed로 재시도하게 한다(조용한 대체값 금지).
@Slf4j
@Component
public class HttpManualChangeSummaryAiClient implements ManualChangeSummaryAiClient {

    private final RestClient syncAiRestClient;

    public HttpManualChangeSummaryAiClient(@Qualifier("syncAiRestClient") RestClient syncAiRestClient) {
        this.syncAiRestClient = syncAiRestClient;
    }

    @Override
    public String summarize(ManualChangeSummaryRequest request) {
        try {
            ManualChangeSummaryAiResponse response = syncAiRestClient.post()
                .uri("/api/v1/manual/change-summary")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ManualChangeSummaryAiResponse.class);

            if (response == null || response.summary() == null || response.summary().isBlank()) {
                log.error("AI 매뉴얼 요약 응답이 비어 있습니다.");
                throw new CustomException(ErrorType.AI_SYNC_FAILED);
            }
            return response.summary().trim();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 매뉴얼 요약 호출 실패: {}", e.getMessage());
            throw new CustomException(ErrorType.AI_SYNC_FAILED);
        }
    }
}
