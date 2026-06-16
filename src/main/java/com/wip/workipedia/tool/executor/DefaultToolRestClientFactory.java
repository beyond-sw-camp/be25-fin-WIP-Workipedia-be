package com.wip.workipedia.tool.executor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.Duration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DefaultToolRestClientFactory implements ToolRestClientFactory {

	@Override
	public RestClient create(long timeoutMs) {
		// redirect를 따라가면 SsrfGuard가 검증한 endpointUrl이 아닌 곳으로 우회 호출될 수 있어 비활성화한다.
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
			@Override
			protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
				super.prepareConnection(connection, httpMethod);
				connection.setInstanceFollowRedirects(false);
			}
		};
		requestFactory.setConnectTimeout(Duration.ofMillis(timeoutMs));
		requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));
		return RestClient.builder().requestFactory(requestFactory).build();
	}
}
