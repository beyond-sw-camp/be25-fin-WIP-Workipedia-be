package com.wip.workipedia.aisync.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TextIngestRequest(
    @JsonProperty("source_id") Long sourceId,
    @JsonProperty("source_type") String sourceType,
    @JsonProperty("title") String title,
    @JsonProperty("text") String text
) {}
