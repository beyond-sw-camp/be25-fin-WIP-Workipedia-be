package com.wip.workipedia.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.faq.dto.ManualSummaryResponse;
import com.wip.workipedia.faq.dto.PopularWorkiResponse;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * Redis 기반 캐시 설정.
 *
 * <p>FAQ 인기/최근 목록처럼 자주 바뀌지 않는 조회 결과를 30분간 캐싱해 DB 부하를 줄인다.
 *
 * <p>캐시 값은 JSON으로 저장하되, 캐시마다 "저장되는 타입"을 직접 못박는다
 * (예: {@code faq:popularWorki} → {@code List<PopularWorkiResponse>}).
 * 타입을 자동 추론하게 두는 {@code @class} 방식은 {@code Stream.toList()}가 돌려주는
 * 불변 리스트 등에서 역직렬화가 깨지기 쉬워, 타입을 명시하는 편이 견고하다.
 */
@EnableCaching
@Configuration
public class RedisCacheConfig {

    /** 기본 캐시 TTL — 인기/최근 목록은 30분 캐싱한다. */
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    /**
     * 캐시 값 (역)직렬화에 쓰는 매퍼. 직접 {@code new} 하지 않고 Spring이 관리하는
     * 공용 ObjectMapper 빈을 주입받아, 전역 직렬화 설정 변경이 캐시에도 그대로 반영되게 한다.
     */
    private final ObjectMapper objectMapper;

    public RedisCacheConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 캐시 이름별로 "저장 타입"을 못박아 등록한다.
     *
     * <p>{@code @Cacheable}에 쓴 캐시 이름과 여기 이름이 1:1로 맞아야 한다.
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer faqCacheCustomizer() {
        return builder -> builder
                .withCacheConfiguration("faq:popularWorki", listCacheConfig(PopularWorkiResponse.class))
                .withCacheConfiguration("faq:popularManuals", listCacheConfig(ManualSummaryResponse.class))
                .withCacheConfiguration("faq:recentManuals", listCacheConfig(ManualSummaryResponse.class));
    }

    /** {@code List<elementType>} 형태의 값을 저장하는 캐시 설정을 만든다. */
    private RedisCacheConfiguration listCacheConfig(Class<?> elementType) {
        JavaType listType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, elementType);
        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, listType);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }
}
