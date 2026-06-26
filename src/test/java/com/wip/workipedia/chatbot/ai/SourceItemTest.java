package com.wip.workipedia.chatbot.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class SourceItemTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializesFileNameFromAiSnakeCaseField() throws Exception {
        String aiJson = """
            {
              "candidate_id": "MANUAL:1:0",
              "source_type": "MANUAL",
              "source_id": "1",
              "chunk_index": 0,
              "file_name": "file2.pdf",
              "page_start": 1,
              "page_end": 2,
              "title": "소개서",
              "score": 0.9
            }
            """;

        SourceItem source = objectMapper.readValue(aiJson, SourceItem.class);

        assertThat(source.fileName()).isEqualTo("file2.pdf");
        assertThat(source.pageStart()).isEqualTo(1);
        assertThat(source.pageEnd()).isEqualTo(2);
    }

    @Test
    void serializesFileNameIntoReferencesJson() throws Exception {
        SourceItem source = new SourceItem(
                "MANUAL:1:0", "MANUAL", "1", 0, "file2.pdf", 1, 2, "소개서", 0.9, null);

        String json = objectMapper.writeValueAsString(source);

        assertThat(json).contains("\"file_name\":\"file2.pdf\"");
    }
}
