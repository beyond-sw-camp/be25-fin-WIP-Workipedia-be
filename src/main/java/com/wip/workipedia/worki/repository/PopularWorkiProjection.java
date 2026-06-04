package com.wip.workipedia.worki.repository;

import java.time.LocalDateTime;

public interface PopularWorkiProjection {
    Long getQuestionId();
    String getTitle();
    Long getViewCount();
    LocalDateTime getCreatedAt();
    Long getLikeCount();
}
