package com.wip.workipedia.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

/**
 * application-local.yaml의 infra.esg 설정이 실제로 바인딩되는지 검증한다.
 *
 * <p>특히 {@code instance-specs} / {@code downsize-map}처럼 키에 점(.)이 들어가는 Map은
 * 대괄호 표기(예: {@code "[t3.large]"})로 감싸지 않으면 Spring Boot가 중첩 경로로 해석해
 * 바인딩이 비어버린다(런타임 NPE 유발). 이 테스트가 그 회귀를 막는다.
 */
class InfraEsgPropertiesBindingTest {

    @Test
    void bindsInstanceSpecsAndDownsizeMapWithDottedKeys() throws IOException {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        List<PropertySource<?>> sources =
            loader.load("application-local", new ClassPathResource("application-local.yaml"));
        MutablePropertySources mps = new MutablePropertySources();
        sources.forEach(mps::addLast);

        Binder binder = new Binder(ConfigurationPropertySources.from(mps));
        InfraEsgProperties props = binder.bind("infra.esg", InfraEsgProperties.class).get();

        assertThat(props.instanceSpecs()).containsKeys("t3.large", "t3.medium");
        assertThat(props.instanceSpecs().get("t3.large").vCpu()).isEqualTo(2);
        assertThat(props.instanceSpecs().get("t3.large").memoryGb()).isEqualTo(8.0);
        assertThat(props.instanceSpecs().get("t3.medium").memoryGb()).isEqualTo(4.0);
        assertThat(props.downsizeMap()).containsEntry("t3.large", "t3.medium");
        assertThat(props.resources()).isNotEmpty();
    }
}
