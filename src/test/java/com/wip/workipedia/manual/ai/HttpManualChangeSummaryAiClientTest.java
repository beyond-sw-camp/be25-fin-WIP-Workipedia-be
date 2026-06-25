package com.wip.workipedia.manual.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.manual.ai.dto.ManualChangeSummaryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpManualChangeSummaryAiClientTest {

    private MockRestServiceServer server;
    private HttpManualChangeSummaryAiClient client;

    private final ManualChangeSummaryRequest request =
        new ManualChangeSummaryRequest("소개서", "@@ line 1 @@\n- a\n+ b", "PDF_UPLOAD");

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ai.test");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new HttpManualChangeSummaryAiClient(builder.build());
    }

    @Test
    void summarize_returnsSummaryFromResponse() {
        server.expect(requestTo("http://ai.test/api/v1/manual/change-summary"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("{\"summary\":\"소개서 문구가 수정되었습니다.\"}", MediaType.APPLICATION_JSON));

        assertThat(client.summarize(request)).isEqualTo("소개서 문구가 수정되었습니다.");
        server.verify();
    }

    @Test
    void summarize_blankSummary_throws() {
        server.expect(requestTo("http://ai.test/api/v1/manual/change-summary"))
            .andRespond(withSuccess("{\"summary\":\"  \"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.summarize(request)).isInstanceOf(CustomException.class);
    }

    @Test
    void summarize_serverError_throws() {
        server.expect(requestTo("http://ai.test/api/v1/manual/change-summary"))
            .andRespond(withServerError());

        assertThatThrownBy(() -> client.summarize(request)).isInstanceOf(CustomException.class);
    }
}
