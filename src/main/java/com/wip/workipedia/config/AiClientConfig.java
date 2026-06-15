package com.wip.workipedia.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

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

	private SimpleClientHttpRequestFactory requestFactory(long timeoutMs) {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
		factory.setReadTimeout(Duration.ofMillis(timeoutMs));
		return factory;
	}
}
