package com.wip.workipedia.aisync.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KnowledgeSyncRequest(
    @JsonProperty("source_id") Long sourceId,
    @JsonProperty("source_type") String sourceType,
    @JsonProperty("title") String title,
    @JsonProperty("content") String content,
    @JsonProperty("department_id") Long departmentId,
    @JsonProperty("department_name") String departmentName
) {}
