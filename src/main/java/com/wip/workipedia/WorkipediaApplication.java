package com.wip.workipedia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

// Redis는 캐시/RedisTemplate 전용, Reactive Elasticsearch는 미사용(imperative ES만 사용)이라
// 두 리포지토리 자동구성을 끈다. (JPA 리포지토리를 후보로 훑으며 "could not safely identify store"
// INFO 노이즈를 대량으로 남기는 것을 방지. 캐시·imperative ES 검색은 영향 없음)
@SpringBootApplication(exclude = {
    RedisRepositoriesAutoConfiguration.class,
    ReactiveElasticsearchRepositoriesAutoConfiguration.class,
})
@EnableScheduling
public class WorkipediaApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkipediaApplication.class, args);
	}

}
