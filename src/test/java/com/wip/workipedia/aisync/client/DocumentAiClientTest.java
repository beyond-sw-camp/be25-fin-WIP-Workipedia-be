package com.wip.workipedia.aisync.client;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.domain.ManualPage;
import com.wip.workipedia.manual.domain.ManualStatus;
import com.wip.workipedia.manual.repository.ManualPageRepository;
import com.wip.workipedia.manual.repository.ManualRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class DocumentAiClientTest {

    @Mock
    private ManualRepository manualRepository;

    @Mock
    private ManualPageRepository manualPageRepository;

    private MockRestServiceServer server;
    private DocumentAiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ai.test");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new DocumentAiClient(builder.build(), manualRepository, manualPageRepository);
    }

    @Test
    void ingest_withPages_callsIngestPagesEndpoint() {
        Manual manual = manual(100L, "소개서", "전체 본문");
        when(manualRepository.findByManualIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(manual));
        when(manualPageRepository.findByManualManualIdAndDeletedAtIsNullOrderByFileSortOrderAscPageNumberAsc(100L))
            .thenReturn(List.of(
                ManualPage.create(manual, "manuals/100/file1.pdf", "file1.pdf", 0, 1, 1, "f1p1"),
                ManualPage.create(manual, "manuals/100/file2.pdf", "file2.pdf", 1, 1, 2, "f2p1")));

        server.expect(requestTo("http://ai.test/api/v1/documents/ingest-pages"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath("$.source_id").value(100))
            .andExpect(jsonPath("$.source_type").value("MANUAL"))
            .andExpect(jsonPath("$.pages.length()").value(2))
            .andExpect(jsonPath("$.pages[0].file_name").value("file1.pdf"))
            .andExpect(jsonPath("$.pages[0].page_number").value(1))
            .andExpect(jsonPath("$.pages[1].file_name").value("file2.pdf"))
            .andExpect(jsonPath("$.pages[1].global_page_number").value(2))
            .andRespond(withSuccess());

        client.ingest(100L);

        server.verify();
    }

    @Test
    void ingest_withoutPages_fallsBackToIngestText() {
        Manual manual = manual(100L, "소개서", "전체 본문");
        when(manualRepository.findByManualIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(manual));
        when(manualPageRepository.findByManualManualIdAndDeletedAtIsNullOrderByFileSortOrderAscPageNumberAsc(100L))
            .thenReturn(List.of());

        server.expect(requestTo("http://ai.test/api/v1/documents/ingest-text"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath("$.text").value("전체 본문"))
            .andRespond(withSuccess());

        client.ingest(100L);

        server.verify();
    }

    private Manual manual(Long id, String title, String content) {
        Manual manual = Manual.create(null, title, content, ManualStatus.PUBLISHED, null, "1.0", 1L);
        ReflectionTestUtils.setField(manual, "manualId", id);
        return manual;
    }
}
