package com.wip.workipedia.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AiSyncProperties.class)
public class AiSyncRestClientConfig {

    @Bean
    @Qualifier("syncAiRestClient")
    public RestClient syncAiRestClient(AiProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(props.timeout().syncMs()));
        factory.setReadTimeout(Duration.ofMillis(props.timeout().syncMs()));
        return RestClient.builder()
            .baseUrl(props.baseUrl())
            .requestFactory(factory)
            .build();
    }
}
