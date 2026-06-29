package com.wip.workipedia.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiClientConfig {

	@Bean
	@Qualifier("routingAiRestClient")
	public RestClient routingAiRestClient(AiProperties props) {
		return RestClient.builder()
			.baseUrl(props.baseUrl())
			.requestFactory(requestFactory(props.timeout().routingMs()))
			.build();
	}

	@Bean
	@Qualifier("chatbotAiRestClient")
	public RestClient chatbotAiRestClient(AiProperties props) {
		return RestClient.builder()
			.baseUrl(props.baseUrl())
			.requestFactory(requestFactory(props.timeout().chatbotMs()))
			.build();
	}

	// 챗봇 스트리밍(SSE) 중계용 WebClient.
	// 스트리밍은 토큰이 도착할 때마다 흘려보내므로 RestClient(블로킹)가 아닌 WebClient를 사용한다.
	@Bean
	@Qualifier("chatbotAiWebClient")
	public WebClient chatbotAiWebClient(AiProperties props) {
		HttpClient httpClient = HttpClient.create()
			.responseTimeout(Duration.ofMillis(props.timeout().chatbotMs()));
		return WebClient.builder()
			.baseUrl(props.baseUrl())
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.build();
	}

	private SimpleClientHttpRequestFactory requestFactory(long timeoutMs) {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
		factory.setReadTimeout(Duration.ofMillis(timeoutMs));
		return factory;
	}
}
