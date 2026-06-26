package com.wip.workipedia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch 리포지토리 스캔 범위를 실제 ES 리포지토리가 있는 패키지로 한정한다.
 * (JPA 리포지토리는 Spring Boot 기본 스캔이 담당하므로 영향 없음.)
 *
 * <p>범위를 좁히지 않으면 ES 모듈이 모든 JPA 리포지토리를 후보로 훑으면서
 * "Could not safely identify store assignment" INFO 로그를 대량으로 남긴다.
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.wip.workipedia.search.repository")
public class ElasticsearchRepositoryConfig {
}
