package com.wip.workipedia.admin.aisync.dto;

import com.wip.workipedia.aisync.domain.AiSyncSourceType;
import com.wip.workipedia.aisync.domain.AiSyncStatus;
import com.wip.workipedia.common.request.BasePageRequest;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class AiSyncJobListRequest extends BasePageRequest {

    private AiSyncStatus status;
    private AiSyncSourceType sourceType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;

    public AiSyncStatus getStatus() { return status; }
    public void setStatus(AiSyncStatus status) { this.status = status; }
    public AiSyncSourceType getSourceType() { return sourceType; }
    public void setSourceType(AiSyncSourceType sourceType) { this.sourceType = sourceType; }
    public LocalDateTime getFrom() { return from; }
    public void setFrom(LocalDateTime from) { this.from = from; }
    public LocalDateTime getTo() { return to; }
    public void setTo(LocalDateTime to) { this.to = to; }
}
