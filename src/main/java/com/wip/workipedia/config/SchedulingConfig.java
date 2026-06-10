package com.wip.workipedia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Scheduled 활성화. 워키 조회수 일괄 반영처럼 주기적으로 도는 가벼운 작업에 사용한다.
 * (Quartz는 영속·분산 스케줄이 필요한 무거운 잡 전용이라 단순 주기 작업은 @Scheduled로 처리.)
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
