package com.wip.workipedia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @Async 활성화. 검색 색인 등 후속 처리를 별도 스레드에서 실행하기 위함.
 * 스레드 풀은 Spring Boot 기본 TaskExecutor(applicationTaskExecutor)를 사용한다.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
