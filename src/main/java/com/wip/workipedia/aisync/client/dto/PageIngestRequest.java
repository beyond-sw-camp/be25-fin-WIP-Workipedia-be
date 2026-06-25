package com.wip.workipedia.aisync.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PageIngestRequest(
    @JsonProperty("source_id") Long sourceId,
    @JsonProperty("source_type") String sourceType,
    @JsonProperty("title") String title,
    @JsonProperty("pages") List<Page> pages
) {

    public record Page(
        @JsonProperty("file_name") String fileName,
        @JsonProperty("file_key") String fileKey,
        @JsonProperty("file_sort_order") int fileSortOrder,
        @JsonProperty("page_number") int pageNumber,
        @JsonProperty("global_page_number") int globalPageNumber,
        @JsonProperty("text") String text
    ) {}
}
