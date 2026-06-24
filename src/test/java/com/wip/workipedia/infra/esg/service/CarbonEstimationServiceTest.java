package com.wip.workipedia.infra.esg.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.infra.esg.config.InfraEsgProperties;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

class CarbonEstimationServiceTest {

    private CarbonEstimationService service;

    @BeforeEach
    void setUp() {
        InfraEsgProperties props = new InfraEsgProperties(
            "ap-northeast-2",
            new InfraEsgProperties.Carbon(0.478, 1.135, 0.000392, 0.74, 3.5),
            new InfraEsgProperties.Thresholds(20.0, 50.0),
            Map.of(
                "t3.large", new InfraEsgProperties.InstanceSpec(2, 8),
                "t3.medium", new InfraEsgProperties.InstanceSpec(2, 4)
            ),
            Map.of("t3.large", "t3.medium"),
            List.of()
        );
        service = new CarbonEstimationService(props);
    }

    @Test
    void estimate_t3Large_at10Percent_isAround2_80() {
        BigDecimal grams = service.estimateGramsPerHour("t3.large", 10.0);
        assertThat(grams.doubleValue()).isCloseTo(2.80, org.assertj.core.data.Offset.offset(0.05));
    }

    @Test
    void estimate_t3Medium_at10Percent_isAround1_95() {
        BigDecimal grams = service.estimateGramsPerHour("t3.medium", 10.0);
        assertThat(grams.doubleValue()).isCloseTo(1.95, org.assertj.core.data.Offset.offset(0.05));
    }

    @Test
    void estimate_unknownType_throws() {
        assertThatThrownBy(() -> service.estimateGramsPerHour("t3.unknown", 10.0))
            .isInstanceOf(CustomException.class);
    }
}
