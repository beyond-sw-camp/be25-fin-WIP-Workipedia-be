package com.wip.workipedia.manual.repository;

import java.time.LocalDateTime;

public interface PopularManualProjection {
    Long getManualId();
    String getTitle();
    Long getDepartmentId();
    LocalDateTime getCreatedAt();
    Long getCitationCount();
}
