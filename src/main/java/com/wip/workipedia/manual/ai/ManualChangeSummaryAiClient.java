package com.wip.workipedia.manual.ai;

import com.wip.workipedia.manual.ai.dto.ManualChangeSummaryRequest;

public interface ManualChangeSummaryAiClient {
    String summarize(ManualChangeSummaryRequest request);
}
