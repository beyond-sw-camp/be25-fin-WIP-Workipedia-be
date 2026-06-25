# 인프라 ESG (CloudWatch 기반) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** CloudWatch 운영 메트릭(EC2 CPU)을 기반으로 인프라 리소스의 효율을 분석해, 다운사이징 권장 리소스를 RECOMMENDED로 표시하고 권장 항목 전체의 CO₂ 절감 추정치를 합산한 관리자용 ESG 대시보드 API를 제공한다.

**Architecture:** `com.wip.workipedia.admin.esg` 신규 패키지. 모니터링 대상 리소스 목록과 탄소 계산 계수·임계값은 설정(`InfraEsgProperties`, `.env`)으로 외부화한다. `CloudWatchMetricService`가 실제 CPU 메트릭을 조회 → `CarbonEstimationService`(순수 계산)가 gCO₂/h 추정 → `InfraRecommendationService`가 임계값으로 권장 여부 판단 → `InfraEsgSummaryService`가 RECOMMENDED 항목만 합산하고 스마트폰 충전 환산까지 조립한다. 응답은 Redis에 60분 TTL로 캐시한다.

**Tech Stack:** Spring Boot, AWS SDK for Java v2 (cloudwatch, ec2), Spring Data Redis 캐시, JUnit5 + Mockito + AssertJ.

## Global Constraints

- 신규 코드는 `com.wip.workipedia.admin.esg` 패키지 하위에 둔다.
- 들여쓰기는 스페이스 4칸. DTO는 Java `record`로 작성한다(기존 `esg` 패키지 컨벤션).
- 엔드포인트는 `GET /api/v1/admin/esg/infra` 단일 경로. 기존 `SecurityConfig`의 `/api/v1/admin/**` → `hasRole("SYSTEM_ADMIN")` 규칙으로 자동 보호되므로 컨트롤러에 별도 권한 애너테이션을 추가하지 않는다.
- AWS SDK 버전은 기존 `software.amazon.awssdk:s3:2.26.0`과 동일한 `2.26.0`을 사용한다.
- AWS 클라이언트는 `S3StorageAdapter`와 동일하게 `StaticCredentialsProvider` + `Region.of(...)` 패턴으로 생성한다.
- 스마트폰 충전 환산은 신규 클래스를 만들지 않고 기존 `com.wip.workipedia.leaderboard.service.EsgEnvironmentImpactCalculator`의 상수(`smartphoneChargeEmissionKgCo2()` = 0.0124kg)를 재사용한다.
- 탄소 배출량은 실측이 아닌 추정치다. 응답의 `measurementType`은 항상 `"ESTIMATED"`.
- 현재 Auto Scaling Group이 없으므로 모든 모니터링 대상은 **독립 EC2**로 취급한다. ASG 권장 로직(`ASG_SCALE_IN`, `ASG_MEMBER`)은 이번 범위에 구현하지 않는다(향후 ASG 도입 + API 스펙 확정 후 별도 태스크).
- 탄소 계산 공식(문서 9장 기준):
  - `averageWatts = minWatts + (avgCpuPercent/100) * (maxWatts - minWatts)`
  - `computeEnergyKwh = averageWatts * vCpu / 1000` (1시간 기준)
  - `memoryEnergyKwh = memoryGb * memoryEnergyKwhPerGbHour`
  - `totalEnergyKwh = (computeEnergyKwh + memoryEnergyKwh) * pue`
  - `gramsCo2PerHour = totalEnergyKwh * emissionFactorKgPerKwh * 1000`
  - 검증값: `minWatts=0.74, maxWatts=3.5, pue=1.135, memoryCoeff=0.000392, emissionFactor=0.478` 일 때 t3.large(vCpu=2, memGb=8) @ avgCpu=10% → **약 2.80 gCO₂/h**, t3.medium(vCpu=2, memGb=4) @ 10% → **약 1.95 gCO₂/h**.

---

## File Structure

**Create:**
- `src/main/java/com/wip/workipedia/config/InfraEsgProperties.java` — 계수·임계값·인스턴스 스펙·다운사이즈 맵·모니터링 리소스 목록 바인딩
- `src/main/java/com/wip/workipedia/config/CloudWatchClientConfig.java` — `CloudWatchClient` 빈
- `src/main/java/com/wip/workipedia/admin/esg/domain/RecommendationStatus.java` — enum
- `src/main/java/com/wip/workipedia/admin/esg/domain/OptimizationType.java` — enum
- `src/main/java/com/wip/workipedia/admin/esg/service/CarbonEstimationService.java` — 순수 탄소 계산
- `src/main/java/com/wip/workipedia/admin/esg/service/InfraRecommendationService.java` — 임계값 기반 권장 판단
- `src/main/java/com/wip/workipedia/admin/esg/service/CloudWatchMetricService.java` — 실제 CPU 메트릭 조회
- `src/main/java/com/wip/workipedia/admin/esg/service/CpuMetrics.java` — 메트릭 값 객체(record)
- `src/main/java/com/wip/workipedia/admin/esg/service/InfraEsgSummaryService.java` — 응답 조립 + 캐시
- `src/main/java/com/wip/workipedia/admin/esg/dto/InfraEsgSummaryResponse.java`
- `src/main/java/com/wip/workipedia/admin/esg/dto/InfraSummaryDto.java`
- `src/main/java/com/wip/workipedia/admin/esg/dto/ResourceRecommendationDto.java`
- `src/main/java/com/wip/workipedia/admin/esg/dto/TotalCarbonComparisonDto.java`
- `src/main/java/com/wip/workipedia/admin/esg/dto/EquivalentDto.java`
- `src/main/java/com/wip/workipedia/admin/esg/dto/CalculationDto.java`
- `src/main/java/com/wip/workipedia/admin/esg/controller/InfraEsgAdminController.java`
- Tests: `CarbonEstimationServiceTest`, `InfraRecommendationServiceTest`, `CloudWatchMetricServiceTest`, `InfraEsgSummaryServiceTest` (모두 `src/test/java/com/wip/workipedia/admin/esg/...`)

**Modify:**
- `build.gradle` — AWS SDK cloudwatch, ec2 의존성 추가
- `src/main/java/com/wip/workipedia/config/RedisCacheConfig.java` — `infra:esgSummary` 캐시(60분) 등록
- `src/main/resources/application.yaml` — `infra.esg.*` 설정 + `${ENV}` 플레이스홀더
- `src/main/resources/application-local.yaml` — 로컬 값
- `.env` — AWS 자격증명/리전/리소스 인스턴스 ID

---

### Task 1: 의존성 + 설정 프로퍼티

**Files:**
- Modify: `build.gradle` (dependencies 블록)
- Create: `src/main/java/com/wip/workipedia/config/InfraEsgProperties.java`
- Modify: `src/main/resources/application.yaml`
- Modify: `src/main/resources/application-local.yaml`
- Modify: `.env`

**Interfaces:**
- Produces:
  - `InfraEsgProperties` record (prefix `infra.esg`):
    - `String region`
    - `Carbon carbon` → `record Carbon(double emissionFactorKgPerKwh, double pue, double memoryEnergyKwhPerGbHour, double minWatts, double maxWatts)`
    - `Thresholds thresholds` → `record Thresholds(double avgCpuPercent, double maxCpuPercent)`
    - `Map<String, InstanceSpec> instanceSpecs` → `record InstanceSpec(int vCpu, double memoryGb)` (키 = 인스턴스 타입명, 예 `t3.large`)
    - `Map<String, String> downsizeMap` (키 = 현재 타입, 값 = 권장 타입)
    - `List<MonitoredResource> resources` → `record MonitoredResource(String name, String instanceId, String role, String instanceType)`

- [ ] **Step 1: build.gradle에 AWS SDK 의존성 추가**

`build.gradle`의 dependencies 블록에서 기존 S3 라인 바로 아래에 추가:

```groovy
	implementation 'software.amazon.awssdk:s3:2.26.0'
	implementation 'software.amazon.awssdk:cloudwatch:2.26.0'
	implementation 'software.amazon.awssdk:ec2:2.26.0'
```

- [ ] **Step 2: 의존성 해석 확인**

Run: `./gradlew dependencies --configuration compileClasspath | grep -i "awssdk:cloudwatch"`
Expected: `software.amazon.awssdk:cloudwatch:2.26.0` 라인이 출력됨

- [ ] **Step 3: InfraEsgProperties 작성**

`src/main/java/com/wip/workipedia/config/InfraEsgProperties.java`:

```java
package com.wip.workipedia.config;

import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "infra.esg")
public record InfraEsgProperties(
    String region,
    Carbon carbon,
    Thresholds thresholds,
    Map<String, InstanceSpec> instanceSpecs,
    Map<String, String> downsizeMap,
    List<MonitoredResource> resources
) {
    public record Carbon(
        double emissionFactorKgPerKwh,
        double pue,
        double memoryEnergyKwhPerGbHour,
        double minWatts,
        double maxWatts
    ) {}

    public record Thresholds(
        double avgCpuPercent,
        double maxCpuPercent
    ) {}

    public record InstanceSpec(
        int vCpu,
        double memoryGb
    ) {}

    public record MonitoredResource(
        String name,
        String instanceId,
        String role,
        String instanceType
    ) {}
}
```

- [ ] **Step 4: application.yaml에 infra.esg 설정 추가**

`src/main/resources/application.yaml` 맨 아래에 추가(자격증명/인스턴스ID는 환경변수 참조):

```yaml
infra:
  esg:
    region: ${AWS_REGION:ap-northeast-2}
    carbon:
      emission-factor-kg-per-kwh: 0.478
      pue: 1.135
      memory-energy-kwh-per-gb-hour: 0.000392
      min-watts: 0.74
      max-watts: 3.5
    thresholds:
      avg-cpu-percent: 20.0
      max-cpu-percent: 50.0
    instance-specs:
      t3.medium:
        v-cpu: 2
        memory-gb: 4
      t3.large:
        v-cpu: 2
        memory-gb: 8
    downsize-map:
      t3.large: t3.medium
    resources:
      - name: workipedia-be
        instance-id: ${INFRA_ESG_BE_INSTANCE_ID:}
        role: Backend
        instance-type: t3.large
      - name: workipedia-ai
        instance-id: ${INFRA_ESG_AI_INSTANCE_ID:}
        role: AI Server
        instance-type: t3.large
      - name: workipedia-qdrant
        instance-id: ${INFRA_ESG_QDRANT_INSTANCE_ID:}
        role: Vector DB
        instance-type: t3.medium

aws:
  credentials:
    access-key: ${AWS_ACCESS_KEY_ID:}
    secret-key: ${AWS_SECRET_ACCESS_KEY:}
```

- [ ] **Step 5: application-local.yaml에 로컬 오버라이드 추가**

`src/main/resources/application-local.yaml` 맨 아래에 추가:

```yaml
infra:
  esg:
    region: ap-northeast-2
```

- [ ] **Step 6: .env에 자격증명/리소스 ID 추가**

`.env`에 추가(값은 실제 환경값으로 채움):

```bash
AWS_REGION=ap-northeast-2
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
INFRA_ESG_BE_INSTANCE_ID=
INFRA_ESG_AI_INSTANCE_ID=
INFRA_ESG_QDRANT_INSTANCE_ID=
```

- [ ] **Step 7: 컴파일 확인**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: 커밋 (`.env`는 절대 커밋하지 않음)**

`.env`는 CLAUDE.md 규칙상 커밋 금지다. `git add`에 `.env`를 포함하지 말 것. (이미 `.gitignore`에 있을 가능성이 높음 — `git status`로 `.env`가 untracked/ignored인지 확인 후 진행.)

```bash
git add build.gradle src/main/java/com/wip/workipedia/config/InfraEsgProperties.java src/main/resources/application.yaml src/main/resources/application-local.yaml
git commit -m "feat: 인프라 ESG AWS SDK 의존성과 설정 프로퍼티 추가"
```

---

### Task 2: enum 도메인

**Files:**
- Create: `src/main/java/com/wip/workipedia/admin/esg/domain/RecommendationStatus.java`
- Create: `src/main/java/com/wip/workipedia/admin/esg/domain/OptimizationType.java`

**Interfaces:**
- Produces:
  - `enum RecommendationStatus { RECOMMENDED, WATCH, KEEP }`
  - `enum OptimizationType { INSTANCE_DOWNSIZE, ASG_SCALE_IN, ASG_MEMBER, KEEP }`

- [ ] **Step 1: RecommendationStatus 작성**

`src/main/java/com/wip/workipedia/admin/esg/domain/RecommendationStatus.java`:

```java
package com.wip.workipedia.admin.esg.domain;

public enum RecommendationStatus {
    RECOMMENDED,
    WATCH,
    KEEP
}
```

- [ ] **Step 2: OptimizationType 작성**

`src/main/java/com/wip/workipedia/admin/esg/domain/OptimizationType.java`:

```java
package com.wip.workipedia.admin.esg.domain;

public enum OptimizationType {
    INSTANCE_DOWNSIZE,
    ASG_SCALE_IN,
    ASG_MEMBER,
    KEEP
}
```

- [ ] **Step 3: 컴파일 확인**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 커밋**

```bash
git add src/main/java/com/wip/workipedia/admin/esg/domain/
git commit -m "feat: 인프라 ESG 권장 상태/최적화 유형 enum 추가"
```

---

### Task 3: 탄소 계산 서비스 (순수 계산, TDD)

**Files:**
- Create: `src/main/java/com/wip/workipedia/admin/esg/service/CarbonEstimationService.java`
- Test: `src/test/java/com/wip/workipedia/admin/esg/service/CarbonEstimationServiceTest.java`

**Interfaces:**
- Consumes: `InfraEsgProperties` (Task 1) — `carbon()`, `instanceSpecs()`
- Produces:
  - `class CarbonEstimationService`
    - 생성자: `CarbonEstimationService(InfraEsgProperties properties)`
    - `BigDecimal estimateGramsPerHour(String instanceType, double avgCpuPercent)` — 소수 둘째 자리 반올림. 알 수 없는 인스턴스 타입이면 `CustomException(ErrorType.INTERNAL_SERVER_ERROR)` 던짐. (`ErrorType`은 `com.wip.workipedia.common.exception` 기존 enum)

- [ ] **Step 1: 실패하는 테스트 작성**

`src/test/java/com/wip/workipedia/admin/esg/service/CarbonEstimationServiceTest.java`:

```java
package com.wip.workipedia.admin.esg.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.config.InfraEsgProperties;
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
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests "com.wip.workipedia.admin.esg.service.CarbonEstimationServiceTest"`
Expected: FAIL (CarbonEstimationService 클래스 없음 → 컴파일 에러)

- [ ] **Step 3: 최소 구현 작성**

`src/main/java/com/wip/workipedia/admin/esg/service/CarbonEstimationService.java`:

```java
package com.wip.workipedia.admin.esg.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.config.InfraEsgProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class CarbonEstimationService {

    private final InfraEsgProperties properties;

    public CarbonEstimationService(InfraEsgProperties properties) {
        this.properties = properties;
    }

    public BigDecimal estimateGramsPerHour(String instanceType, double avgCpuPercent) {
        InfraEsgProperties.InstanceSpec spec = properties.instanceSpecs().get(instanceType);
        if (spec == null) {
            throw new CustomException(ErrorType.INTERNAL_SERVER_ERROR);
        }
        InfraEsgProperties.Carbon c = properties.carbon();

        double cpuFraction = avgCpuPercent / 100.0;
        double averageWatts = c.minWatts() + cpuFraction * (c.maxWatts() - c.minWatts());
        double computeEnergyKwh = averageWatts * spec.vCpu() / 1000.0;
        double memoryEnergyKwh = spec.memoryGb() * c.memoryEnergyKwhPerGbHour();
        double totalEnergyKwh = (computeEnergyKwh + memoryEnergyKwh) * c.pue();
        double gramsPerHour = totalEnergyKwh * c.emissionFactorKgPerKwh() * 1000.0;

        return BigDecimal.valueOf(gramsPerHour).setScale(2, RoundingMode.HALF_UP);
    }
}
```

> 주의: `ErrorType.INTERNAL_SERVER_ERROR` 상수가 실제로 존재하는지 확인하고, 없으면 `ErrorType` enum에서 가장 가까운 서버 오류 상수로 교체한다. 확인: `grep -n "INTERNAL_SERVER_ERROR\|public enum ErrorType" src/main/java/com/wip/workipedia/common/exception/ErrorType.java`

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests "com.wip.workipedia.admin.esg.service.CarbonEstimationServiceTest"`
Expected: PASS (3 tests)

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/wip/workipedia/admin/esg/service/CarbonEstimationService.java src/test/java/com/wip/workipedia/admin/esg/service/CarbonEstimationServiceTest.java
git commit -m "feat: 인프라 ESG CloudWatch CPU 기반 탄소 추정 계산 추가"
```

---

### Task 4: 권장 판단 서비스 (TDD)

**Files:**
- Create: `src/main/java/com/wip/workipedia/admin/esg/service/CpuMetrics.java`
- Create: `src/main/java/com/wip/workipedia/admin/esg/service/InfraRecommendationService.java`
- Create: `src/main/java/com/wip/workipedia/admin/esg/dto/ResourceRecommendationDto.java`
- Test: `src/test/java/com/wip/workipedia/admin/esg/service/InfraRecommendationServiceTest.java`

**Interfaces:**
- Consumes: `InfraEsgProperties` (Task 1), `CarbonEstimationService` (Task 3), `RecommendationStatus`/`OptimizationType` (Task 2)
- Produces:
  - `record CpuMetrics(double averageCpu, double maxCpu)`
  - `record ResourceRecommendationDto(String resourceName, String role, OptimizationType optimizationType, String currentConfiguration, String recommendedConfiguration, double averageCpu, double maxCpu, BigDecimal currentEstimatedCarbonGPerHour, BigDecimal recommendedEstimatedCarbonGPerHour, BigDecimal estimatedCarbonSavingGPerHour, String recommendation, RecommendationStatus status)`
  - `class InfraRecommendationService`
    - 생성자: `InfraRecommendationService(InfraEsgProperties properties, CarbonEstimationService carbonEstimationService)`
    - `ResourceRecommendationDto evaluate(InfraEsgProperties.MonitoredResource resource, CpuMetrics metrics)`

판단 규칙(독립 EC2 기준):
- `averageCpu < thresholds.avgCpuPercent` **AND** `maxCpu < thresholds.maxCpuPercent` **AND** `downsizeMap`에 현재 타입의 권장 타입 존재 → `RECOMMENDED` / `INSTANCE_DOWNSIZE`. recommended = 권장 타입 탄소, saving = current − recommended. `recommendation = "<권장타입> 변경 검토"`.
- 그 외 → `KEEP` / `KEEP`. recommended = current, saving = 0. `recommendation = "현재 구성을 유지합니다."`.

- [ ] **Step 1: 실패하는 테스트 작성**

`src/test/java/com/wip/workipedia/admin/esg/service/InfraRecommendationServiceTest.java`:

```java
package com.wip.workipedia.admin.esg.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wip.workipedia.config.InfraEsgProperties;
import com.wip.workipedia.admin.esg.domain.OptimizationType;
import com.wip.workipedia.admin.esg.domain.RecommendationStatus;
import com.wip.workipedia.admin.esg.dto.ResourceRecommendationDto;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InfraRecommendationServiceTest {

    private InfraRecommendationService service;

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
        service = new InfraRecommendationService(props, new CarbonEstimationService(props));
    }

    @Test
    void lowUsageDownsizableResource_isRecommended() {
        InfraEsgProperties.MonitoredResource be =
            new InfraEsgProperties.MonitoredResource("workipedia-be", "i-be", "Backend", "t3.large");

        ResourceRecommendationDto dto = service.evaluate(be, new CpuMetrics(8.4, 23.1));

        assertThat(dto.status()).isEqualTo(RecommendationStatus.RECOMMENDED);
        assertThat(dto.optimizationType()).isEqualTo(OptimizationType.INSTANCE_DOWNSIZE);
        assertThat(dto.recommendedConfiguration()).isEqualTo("t3.medium");
        assertThat(dto.estimatedCarbonSavingGPerHour().doubleValue()).isGreaterThan(0.0);
    }

    @Test
    void highCpuResource_isKept() {
        InfraEsgProperties.MonitoredResource be =
            new InfraEsgProperties.MonitoredResource("workipedia-be", "i-be", "Backend", "t3.large");

        ResourceRecommendationDto dto = service.evaluate(be, new CpuMetrics(35.0, 70.0));

        assertThat(dto.status()).isEqualTo(RecommendationStatus.KEEP);
        assertThat(dto.estimatedCarbonSavingGPerHour().doubleValue()).isEqualTo(0.0);
    }

    @Test
    void lowUsageButNoDownsizeTarget_isKept() {
        InfraEsgProperties.MonitoredResource qdrant =
            new InfraEsgProperties.MonitoredResource("workipedia-qdrant", "i-q", "Vector DB", "t3.medium");

        ResourceRecommendationDto dto = service.evaluate(qdrant, new CpuMetrics(11.2, 28.6));

        assertThat(dto.status()).isEqualTo(RecommendationStatus.KEEP);
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests "com.wip.workipedia.admin.esg.service.InfraRecommendationServiceTest"`
Expected: FAIL (CpuMetrics / ResourceRecommendationDto / InfraRecommendationService 없음 → 컴파일 에러)

- [ ] **Step 3: CpuMetrics 작성**

`src/main/java/com/wip/workipedia/admin/esg/service/CpuMetrics.java`:

```java
package com.wip.workipedia.admin.esg.service;

public record CpuMetrics(
    double averageCpu,
    double maxCpu
) {
}
```

- [ ] **Step 4: ResourceRecommendationDto 작성**

`src/main/java/com/wip/workipedia/admin/esg/dto/ResourceRecommendationDto.java`:

```java
package com.wip.workipedia.admin.esg.dto;

import com.wip.workipedia.admin.esg.domain.OptimizationType;
import com.wip.workipedia.admin.esg.domain.RecommendationStatus;
import java.math.BigDecimal;

public record ResourceRecommendationDto(
    String resourceName,
    String role,
    OptimizationType optimizationType,
    String currentConfiguration,
    String recommendedConfiguration,
    double averageCpu,
    double maxCpu,
    BigDecimal currentEstimatedCarbonGPerHour,
    BigDecimal recommendedEstimatedCarbonGPerHour,
    BigDecimal estimatedCarbonSavingGPerHour,
    String recommendation,
    RecommendationStatus status
) {
}
```

- [ ] **Step 5: InfraRecommendationService 작성**

`src/main/java/com/wip/workipedia/admin/esg/service/InfraRecommendationService.java`:

```java
package com.wip.workipedia.admin.esg.service;

import com.wip.workipedia.config.InfraEsgProperties;
import com.wip.workipedia.admin.esg.domain.OptimizationType;
import com.wip.workipedia.admin.esg.domain.RecommendationStatus;
import com.wip.workipedia.admin.esg.dto.ResourceRecommendationDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class InfraRecommendationService {

    private final InfraEsgProperties properties;
    private final CarbonEstimationService carbonEstimationService;

    public InfraRecommendationService(InfraEsgProperties properties,
                                      CarbonEstimationService carbonEstimationService) {
        this.properties = properties;
        this.carbonEstimationService = carbonEstimationService;
    }

    public ResourceRecommendationDto evaluate(InfraEsgProperties.MonitoredResource resource,
                                              CpuMetrics metrics) {
        String currentType = resource.instanceType();
        BigDecimal currentCarbon =
            carbonEstimationService.estimateGramsPerHour(currentType, metrics.averageCpu());

        InfraEsgProperties.Thresholds t = properties.thresholds();
        String downsizeTarget = properties.downsizeMap().get(currentType);
        boolean underUtilized = metrics.averageCpu() < t.avgCpuPercent()
            && metrics.maxCpu() < t.maxCpuPercent();

        if (underUtilized && downsizeTarget != null) {
            BigDecimal recommendedCarbon =
                carbonEstimationService.estimateGramsPerHour(downsizeTarget, metrics.averageCpu());
            BigDecimal saving = currentCarbon.subtract(recommendedCarbon)
                .setScale(2, RoundingMode.HALF_UP);
            return new ResourceRecommendationDto(
                resource.name(),
                resource.role(),
                OptimizationType.INSTANCE_DOWNSIZE,
                currentType,
                downsizeTarget,
                round1(metrics.averageCpu()),
                round1(metrics.maxCpu()),
                currentCarbon,
                recommendedCarbon,
                saving,
                downsizeTarget + " 변경 검토",
                RecommendationStatus.RECOMMENDED
            );
        }

        return new ResourceRecommendationDto(
            resource.name(),
            resource.role(),
            OptimizationType.KEEP,
            currentType,
            currentType,
            round1(metrics.averageCpu()),
            round1(metrics.maxCpu()),
            currentCarbon,
            currentCarbon,
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
            "현재 구성을 유지합니다.",
            RecommendationStatus.KEEP
        );
    }

    private double round1(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
```

- [ ] **Step 6: 테스트 통과 확인**

Run: `./gradlew test --tests "com.wip.workipedia.admin.esg.service.InfraRecommendationServiceTest"`
Expected: PASS (3 tests)

- [ ] **Step 7: 커밋**

```bash
git add src/main/java/com/wip/workipedia/admin/esg/service/CpuMetrics.java src/main/java/com/wip/workipedia/admin/esg/dto/ResourceRecommendationDto.java src/main/java/com/wip/workipedia/admin/esg/service/InfraRecommendationService.java src/test/java/com/wip/workipedia/admin/esg/service/InfraRecommendationServiceTest.java
git commit -m "feat: 인프라 ESG 임계값 기반 다운사이징 권장 판단 추가"
```

---

### Task 5: CloudWatch 메트릭 조회 서비스 (TDD, SDK 모킹)

**Files:**
- Create: `src/main/java/com/wip/workipedia/config/CloudWatchClientConfig.java`
- Create: `src/main/java/com/wip/workipedia/admin/esg/service/CloudWatchMetricService.java`
- Test: `src/test/java/com/wip/workipedia/admin/esg/service/CloudWatchMetricServiceTest.java`

**Interfaces:**
- Consumes: `software.amazon.awssdk.services.cloudwatch.CloudWatchClient`, `CpuMetrics` (Task 4)
- Produces:
  - `CloudWatchClientConfig` — `@Bean CloudWatchClient cloudWatchClient(InfraEsgProperties, @Value access/secret)`
  - `class CloudWatchMetricService`
    - 생성자: `CloudWatchMetricService(CloudWatchClient cloudWatchClient)`
    - `CpuMetrics fetchCpu24h(String instanceId)` — 지난 24시간 `AWS/EC2 CPUUtilization`의 Average/Maximum. 데이터포인트가 없으면 `new CpuMetrics(0.0, 0.0)` 반환.

- [ ] **Step 1: 실패하는 테스트 작성**

`src/test/java/com/wip/workipedia/admin/esg/service/CloudWatchMetricServiceTest.java`:

```java
package com.wip.workipedia.admin.esg.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;

@ExtendWith(MockitoExtension.class)
class CloudWatchMetricServiceTest {

    @Mock CloudWatchClient cloudWatchClient;

    @Test
    void fetchCpu24h_returnsAverageAndMax() {
        GetMetricStatisticsResponse response = GetMetricStatisticsResponse.builder()
            .datapoints(
                Datapoint.builder().timestamp(Instant.now()).average(8.4).maximum(23.1).build())
            .build();
        when(cloudWatchClient.getMetricStatistics(any(GetMetricStatisticsRequest.class)))
            .thenReturn(response);

        CloudWatchMetricService service = new CloudWatchMetricService(cloudWatchClient);
        CpuMetrics metrics = service.fetchCpu24h("i-be");

        assertThat(metrics.averageCpu()).isEqualTo(8.4);
        assertThat(metrics.maxCpu()).isEqualTo(23.1);
    }

    @Test
    void fetchCpu24h_noDatapoints_returnsZeros() {
        when(cloudWatchClient.getMetricStatistics(any(GetMetricStatisticsRequest.class)))
            .thenReturn(GetMetricStatisticsResponse.builder().build());

        CloudWatchMetricService service = new CloudWatchMetricService(cloudWatchClient);
        CpuMetrics metrics = service.fetchCpu24h("i-be");

        assertThat(metrics.averageCpu()).isEqualTo(0.0);
        assertThat(metrics.maxCpu()).isEqualTo(0.0);
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests "com.wip.workipedia.admin.esg.service.CloudWatchMetricServiceTest"`
Expected: FAIL (CloudWatchMetricService 없음 → 컴파일 에러)

- [ ] **Step 3: CloudWatchMetricService 작성**

`src/main/java/com/wip/workipedia/admin/esg/service/CloudWatchMetricService.java`:

```java
package com.wip.workipedia.admin.esg.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;

@Service
public class CloudWatchMetricService {

    private static final String NAMESPACE = "AWS/EC2";
    private static final String METRIC_NAME = "CPUUtilization";
    private static final int PERIOD_SECONDS = 86_400;

    private final CloudWatchClient cloudWatchClient;

    public CloudWatchMetricService(CloudWatchClient cloudWatchClient) {
        this.cloudWatchClient = cloudWatchClient;
    }

    public CpuMetrics fetchCpu24h(String instanceId) {
        Instant end = Instant.now();
        Instant start = end.minus(Duration.ofHours(24));

        GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
            .namespace(NAMESPACE)
            .metricName(METRIC_NAME)
            .dimensions(Dimension.builder().name("InstanceId").value(instanceId).build())
            .startTime(start)
            .endTime(end)
            .period(PERIOD_SECONDS)
            .statistics(Statistic.AVERAGE, Statistic.MAXIMUM)
            .build();

        GetMetricStatisticsResponse response = cloudWatchClient.getMetricStatistics(request);
        List<Datapoint> datapoints = response.datapoints();
        if (datapoints.isEmpty()) {
            return new CpuMetrics(0.0, 0.0);
        }

        double avg = datapoints.stream()
            .mapToDouble(dp -> dp.average() == null ? 0.0 : dp.average())
            .average()
            .orElse(0.0);
        double max = datapoints.stream()
            .mapToDouble(dp -> dp.maximum() == null ? 0.0 : dp.maximum())
            .max()
            .orElse(0.0);

        return new CpuMetrics(avg, max);
    }
}
```

- [ ] **Step 4: CloudWatchClientConfig 작성**

`src/main/java/com/wip/workipedia/config/CloudWatchClientConfig.java`:

```java
package com.wip.workipedia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
public class CloudWatchClientConfig {

    @Bean
    public CloudWatchClient cloudWatchClient(
        InfraEsgProperties properties,
        @Value("${aws.credentials.access-key}") String accessKey,
        @Value("${aws.credentials.secret-key}") String secretKey
    ) {
        StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKey, secretKey));
        return CloudWatchClient.builder()
            .credentialsProvider(credentials)
            .region(Region.of(properties.region()))
            .build();
    }
}
```

- [ ] **Step 5: 테스트 통과 확인**

Run: `./gradlew test --tests "com.wip.workipedia.admin.esg.service.CloudWatchMetricServiceTest"`
Expected: PASS (2 tests)

- [ ] **Step 6: 커밋**

```bash
git add src/main/java/com/wip/workipedia/config/CloudWatchClientConfig.java src/main/java/com/wip/workipedia/admin/esg/service/CloudWatchMetricService.java src/test/java/com/wip/workipedia/admin/esg/service/CloudWatchMetricServiceTest.java
git commit -m "feat: 인프라 ESG CloudWatch CPU 메트릭 조회 서비스 추가"
```

---

### Task 6: 응답 DTO들

**Files:**
- Create: `src/main/java/com/wip/workipedia/admin/esg/dto/InfraSummaryDto.java`
- Create: `src/main/java/com/wip/workipedia/admin/esg/dto/TotalCarbonComparisonDto.java`
- Create: `src/main/java/com/wip/workipedia/admin/esg/dto/EquivalentDto.java`
- Create: `src/main/java/com/wip/workipedia/admin/esg/dto/CalculationDto.java`
- Create: `src/main/java/com/wip/workipedia/admin/esg/dto/InfraEsgSummaryResponse.java`

**Interfaces:**
- Consumes: `ResourceRecommendationDto` (Task 4)
- Produces: 아래 5개 record (필드명은 Task 7 조립 로직과 1:1)

- [ ] **Step 1: InfraSummaryDto 작성**

```java
package com.wip.workipedia.admin.esg.dto;

import java.math.BigDecimal;

public record InfraSummaryDto(
    int targetResourceCount,
    int recommendedResourceCount,
    String recommendedAction,
    BigDecimal totalEstimatedCarbonSavingGPerHour
) {
}
```

- [ ] **Step 2: TotalCarbonComparisonDto 작성**

```java
package com.wip.workipedia.admin.esg.dto;

import java.math.BigDecimal;

public record TotalCarbonComparisonDto(
    BigDecimal currentEstimatedCarbonGPerHour,
    BigDecimal recommendedEstimatedCarbonGPerHour,
    BigDecimal estimatedCarbonSavingGPerHour,
    BigDecimal estimatedCarbonSavingGPerDay,
    BigDecimal estimatedCarbonSavingKgPerMonth
) {
}
```

- [ ] **Step 3: EquivalentDto 작성**

```java
package com.wip.workipedia.admin.esg.dto;

import java.math.BigDecimal;

public record EquivalentDto(
    BigDecimal smartphoneChargePerHour,
    BigDecimal smartphoneChargePerDay,
    BigDecimal smartphoneChargePerMonth
) {
}
```

- [ ] **Step 4: CalculationDto 작성**

```java
package com.wip.workipedia.admin.esg.dto;

public record CalculationDto(
    double emissionFactorKgPerKwh,
    double awsPue,
    double memoryEnergyKwhPerGbHour,
    String measurementType,
    String methodology
) {
}
```

- [ ] **Step 5: InfraEsgSummaryResponse 작성**

```java
package com.wip.workipedia.admin.esg.dto;

import java.util.List;

public record InfraEsgSummaryResponse(
    String period,
    InfraSummaryDto summary,
    List<ResourceRecommendationDto> resources,
    TotalCarbonComparisonDto totalCarbonComparison,
    EquivalentDto equivalent,
    CalculationDto calculation
) {
}
```

- [ ] **Step 6: 컴파일 확인**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: 커밋**

```bash
git add src/main/java/com/wip/workipedia/admin/esg/dto/
git commit -m "feat: 인프라 ESG 통합 응답 DTO 추가"
```

---

### Task 7: 합산/조립 서비스 (TDD)

**Files:**
- Create: `src/main/java/com/wip/workipedia/admin/esg/service/InfraEsgSummaryService.java`
- Test: `src/test/java/com/wip/workipedia/admin/esg/service/InfraEsgSummaryServiceTest.java`

**Interfaces:**
- Consumes: `InfraEsgProperties`, `CloudWatchMetricService.fetchCpu24h(String)`, `InfraRecommendationService.evaluate(...)`, `EsgEnvironmentImpactCalculator.smartphoneChargeEmissionKgCo2()`
- Produces:
  - `class InfraEsgSummaryService`
    - 생성자: `(InfraEsgProperties, CloudWatchMetricService, InfraRecommendationService)`
    - `InfraEsgSummaryResponse getSummary()` — 모든 모니터링 리소스 평가 후 RECOMMENDED만 합산.

조립 규칙:
- `resources`: `properties.resources()` 각각에 대해 `fetchCpu24h` → `evaluate`.
- RECOMMENDED 필터 후:
  - `totalCurrent = Σ current`, `totalRecommended = Σ recommended`, `saving = totalCurrent − totalRecommended` (모두 소수 2자리).
  - `savingPerDay = saving × 24`, `savingKgPerMonth = saving × 24 × 30 / 1000` (소수 2자리).
- `summary`: targetResourceCount = 전체, recommendedResourceCount = RECOMMENDED 수, recommendedAction = RECOMMENDED가 1개 이상이면 `"OPTIMIZE"` 아니면 `"KEEP"`, totalEstimatedCarbonSavingGPerHour = saving.
- `equivalent`: `perHour = savingG/1000 ÷ chargeKg`, `perDay = perHour × 24`, `perMonth = perHour × 24 × 30` (모두 소수 2자리). `chargeKg = EsgEnvironmentImpactCalculator.smartphoneChargeEmissionKgCo2()`.
- `calculation`: properties.carbon() 값 + `measurementType="ESTIMATED"`, `methodology="CloudWatch metrics + AWS resource metadata + Cloud Carbon Footprint public coefficients + Korea electricity emission factor"`.
- `period = "LAST_24_HOURS"`.

- [ ] **Step 1: 실패하는 테스트 작성**

`src/test/java/com/wip/workipedia/admin/esg/service/InfraEsgSummaryServiceTest.java`:

```java
package com.wip.workipedia.admin.esg.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.wip.workipedia.config.InfraEsgProperties;
import com.wip.workipedia.admin.esg.dto.InfraEsgSummaryResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InfraEsgSummaryServiceTest {

    @Mock CloudWatchMetricService cloudWatchMetricService;

    private InfraEsgSummaryService service;

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
            List.of(
                new InfraEsgProperties.MonitoredResource("workipedia-be", "i-be", "Backend", "t3.large"),
                new InfraEsgProperties.MonitoredResource("workipedia-qdrant", "i-q", "Vector DB", "t3.medium")
            )
        );
        InfraRecommendationService recommendationService =
            new InfraRecommendationService(props, new CarbonEstimationService(props));
        service = new InfraEsgSummaryService(props, cloudWatchMetricService, recommendationService);
    }

    @Test
    void getSummary_aggregatesOnlyRecommended() {
        when(cloudWatchMetricService.fetchCpu24h(eq("i-be"))).thenReturn(new CpuMetrics(8.4, 23.1));
        when(cloudWatchMetricService.fetchCpu24h(eq("i-q"))).thenReturn(new CpuMetrics(11.2, 28.6));

        InfraEsgSummaryResponse response = service.getSummary();

        assertThat(response.period()).isEqualTo("LAST_24_HOURS");
        assertThat(response.summary().targetResourceCount()).isEqualTo(2);
        assertThat(response.summary().recommendedResourceCount()).isEqualTo(1);
        assertThat(response.summary().recommendedAction()).isEqualTo("OPTIMIZE");
        assertThat(response.totalCarbonComparison().estimatedCarbonSavingGPerHour().doubleValue())
            .isGreaterThan(0.0);
        assertThat(response.calculation().measurementType()).isEqualTo("ESTIMATED");
    }

    @Test
    void getSummary_noRecommended_actionKeep() {
        when(cloudWatchMetricService.fetchCpu24h(eq("i-be"))).thenReturn(new CpuMetrics(35.0, 70.0));
        when(cloudWatchMetricService.fetchCpu24h(eq("i-q"))).thenReturn(new CpuMetrics(11.2, 28.6));

        InfraEsgSummaryResponse response = service.getSummary();

        assertThat(response.summary().recommendedResourceCount()).isEqualTo(0);
        assertThat(response.summary().recommendedAction()).isEqualTo("KEEP");
        assertThat(response.totalCarbonComparison().estimatedCarbonSavingGPerHour().doubleValue())
            .isEqualTo(0.0);
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

Run: `./gradlew test --tests "com.wip.workipedia.admin.esg.service.InfraEsgSummaryServiceTest"`
Expected: FAIL (InfraEsgSummaryService 없음 → 컴파일 에러)

- [ ] **Step 3: InfraEsgSummaryService 작성**

`src/main/java/com/wip/workipedia/admin/esg/service/InfraEsgSummaryService.java`:

```java
package com.wip.workipedia.admin.esg.service;

import com.wip.workipedia.config.InfraEsgProperties;
import com.wip.workipedia.admin.esg.domain.RecommendationStatus;
import com.wip.workipedia.admin.esg.dto.CalculationDto;
import com.wip.workipedia.admin.esg.dto.EquivalentDto;
import com.wip.workipedia.admin.esg.dto.InfraEsgSummaryResponse;
import com.wip.workipedia.admin.esg.dto.InfraSummaryDto;
import com.wip.workipedia.admin.esg.dto.ResourceRecommendationDto;
import com.wip.workipedia.admin.esg.dto.TotalCarbonComparisonDto;
import com.wip.workipedia.leaderboard.service.EsgEnvironmentImpactCalculator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class InfraEsgSummaryService {

    private static final String PERIOD = "LAST_24_HOURS";
    private static final String METHODOLOGY =
        "CloudWatch metrics + AWS resource metadata + Cloud Carbon Footprint public coefficients "
            + "+ Korea electricity emission factor";

    private final InfraEsgProperties properties;
    private final CloudWatchMetricService cloudWatchMetricService;
    private final InfraRecommendationService recommendationService;

    public InfraEsgSummaryService(InfraEsgProperties properties,
                                  CloudWatchMetricService cloudWatchMetricService,
                                  InfraRecommendationService recommendationService) {
        this.properties = properties;
        this.cloudWatchMetricService = cloudWatchMetricService;
        this.recommendationService = recommendationService;
    }

    @Cacheable("infra:esgSummary")
    public InfraEsgSummaryResponse getSummary() {
        List<ResourceRecommendationDto> resources = properties.resources().stream()
            .map(resource -> recommendationService.evaluate(
                resource, cloudWatchMetricService.fetchCpu24h(resource.instanceId())))
            .toList();

        List<ResourceRecommendationDto> recommended = resources.stream()
            .filter(r -> r.status() == RecommendationStatus.RECOMMENDED)
            .toList();

        BigDecimal totalCurrent = sum(recommended, ResourceRecommendationDto::currentEstimatedCarbonGPerHour);
        BigDecimal totalRecommended = sum(recommended, ResourceRecommendationDto::recommendedEstimatedCarbonGPerHour);
        BigDecimal saving = scale2(totalCurrent.subtract(totalRecommended));
        BigDecimal savingPerDay = scale2(saving.multiply(BigDecimal.valueOf(24)));
        BigDecimal savingKgPerMonth = scale2(
            saving.multiply(BigDecimal.valueOf(24 * 30)).divide(BigDecimal.valueOf(1000)));

        String action = recommended.isEmpty() ? "KEEP" : "OPTIMIZE";
        InfraSummaryDto summary = new InfraSummaryDto(
            resources.size(), recommended.size(), action, saving);

        TotalCarbonComparisonDto comparison = new TotalCarbonComparisonDto(
            scale2(totalCurrent), scale2(totalRecommended), saving, savingPerDay, savingKgPerMonth);

        EquivalentDto equivalent = buildEquivalent(saving);

        InfraEsgProperties.Carbon c = properties.carbon();
        CalculationDto calculation = new CalculationDto(
            c.emissionFactorKgPerKwh(), c.pue(), c.memoryEnergyKwhPerGbHour(),
            "ESTIMATED", METHODOLOGY);

        return new InfraEsgSummaryResponse(
            PERIOD, summary, resources, comparison, equivalent, calculation);
    }

    private EquivalentDto buildEquivalent(BigDecimal savingGramsPerHour) {
        BigDecimal chargeKg = EsgEnvironmentImpactCalculator.smartphoneChargeEmissionKgCo2();
        BigDecimal savingKgPerHour = savingGramsPerHour.divide(BigDecimal.valueOf(1000));
        BigDecimal perHour = chargeKg.signum() == 0
            ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
            : savingKgPerHour.divide(chargeKg, 2, RoundingMode.HALF_UP);
        BigDecimal perDay = scale2(perHour.multiply(BigDecimal.valueOf(24)));
        BigDecimal perMonth = scale2(perHour.multiply(BigDecimal.valueOf(24 * 30)));
        return new EquivalentDto(perHour, perDay, perMonth);
    }

    private BigDecimal sum(List<ResourceRecommendationDto> list,
                           java.util.function.Function<ResourceRecommendationDto, BigDecimal> getter) {
        return list.stream().map(getter).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal scale2(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `./gradlew test --tests "com.wip.workipedia.admin.esg.service.InfraEsgSummaryServiceTest"`
Expected: PASS (2 tests)

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/wip/workipedia/admin/esg/service/InfraEsgSummaryService.java src/test/java/com/wip/workipedia/admin/esg/service/InfraEsgSummaryServiceTest.java
git commit -m "feat: 인프라 ESG RECOMMENDED 합산 및 응답 조립 서비스 추가"
```

---

### Task 8: 컨트롤러 + 캐시 등록 + 설정 활성화

**Files:**
- Create: `src/main/java/com/wip/workipedia/admin/esg/controller/InfraEsgAdminController.java`
- Modify: `src/main/java/com/wip/workipedia/config/RedisCacheConfig.java`
- Modify: 메인 애플리케이션 클래스의 `@ConfigurationPropertiesScan` 또는 `@EnableConfigurationProperties` (아래 Step 1에서 확인)

**Interfaces:**
- Consumes: `InfraEsgSummaryService.getSummary()` (Task 7), `InfraEsgProperties` (Task 1)
- Produces: `GET /api/v1/admin/esg/infra` → `InfraEsgSummaryResponse`

- [ ] **Step 1: 프로퍼티 스캔 방식 확인 후 InfraEsgProperties 등록**

Run: `grep -rn "ConfigurationPropertiesScan\|EnableConfigurationProperties" src/main/java/com/wip/workipedia`
- `@ConfigurationPropertiesScan`이 메인 클래스에 있으면 추가 작업 불필요(자동 스캔됨).
- 없고 각 properties가 `@EnableConfigurationProperties(...)`로 개별 등록되는 패턴이면(예: `StorageConfig`), `CloudWatchClientConfig`에 `@EnableConfigurationProperties(InfraEsgProperties.class)`를 추가한다:

```java
@Configuration
@org.springframework.boot.context.properties.EnableConfigurationProperties(InfraEsgProperties.class)
public class CloudWatchClientConfig {
```

- [ ] **Step 2: RedisCacheConfig에 infra:esgSummary 캐시(60분) 등록**

`RedisCacheConfig`에 단일 객체 캐시 설정을 추가한다. 기존 `faqCacheCustomizer` 빈 아래에 새 빈을 추가:

```java
    @Bean
    public org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer infraEsgCacheCustomizer() {
        org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<Object> serializer =
                new org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer<>(
                        objectMapper,
                        objectMapper.getTypeFactory().constructType(
                                com.wip.workipedia.admin.esg.dto.InfraEsgSummaryResponse.class));

        org.springframework.data.redis.cache.RedisCacheConfiguration config =
                org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(java.time.Duration.ofMinutes(60))
                        .disableCachingNullValues()
                        .serializeValuesWith(
                                org.springframework.data.redis.serializer.RedisSerializationContext
                                        .SerializationPair.fromSerializer(serializer));

        return builder -> builder.withCacheConfiguration("infra:esgSummary", config);
    }
```

- [ ] **Step 3: 컨트롤러 작성**

`src/main/java/com/wip/workipedia/admin/esg/controller/InfraEsgAdminController.java`:

```java
package com.wip.workipedia.admin.esg.controller;

import com.wip.workipedia.admin.esg.dto.InfraEsgSummaryResponse;
import com.wip.workipedia.admin.esg.service.InfraEsgSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/esg")
public class InfraEsgAdminController {

    private final InfraEsgSummaryService infraEsgSummaryService;

    public InfraEsgAdminController(InfraEsgSummaryService infraEsgSummaryService) {
        this.infraEsgSummaryService = infraEsgSummaryService;
    }

    @GetMapping("/infra")
    public ResponseEntity<InfraEsgSummaryResponse> getInfraEsgSummary() {
        return ResponseEntity.ok(infraEsgSummaryService.getSummary());
    }
}
```

- [ ] **Step 4: 전체 컴파일 및 테스트**

Run: `./gradlew compileJava && ./gradlew test --tests "com.wip.workipedia.admin.esg.*"`
Expected: BUILD SUCCESSFUL, 모든 admin.esg 테스트 PASS

- [ ] **Step 5: 애플리케이션 컨텍스트 기동 확인 (자격증명 없이 빈 생성만 검증)**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL
(통합 기동 테스트는 AWS 자격증명/네트워크 필요로 제외. 컨텍스트 검증은 로컬 `.env` 채운 뒤 `./gradlew bootRun`으로 수동 확인.)

- [ ] **Step 6: 커밋**

```bash
git add src/main/java/com/wip/workipedia/admin/esg/controller/InfraEsgAdminController.java src/main/java/com/wip/workipedia/config/RedisCacheConfig.java src/main/java/com/wip/workipedia/config/CloudWatchClientConfig.java
git commit -m "feat: 인프라 ESG 관리자 엔드포인트와 60분 캐시 등록"
```

---

### Task 9: 수동 검증 (실데이터 스모크 테스트)

**Files:** 없음 (런타임 검증)

- [ ] **Step 1: .env에 실제 AWS 자격증명/인스턴스 ID 입력 확인**

`.env`의 `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `INFRA_ESG_*_INSTANCE_ID`가 채워졌는지 확인.

- [ ] **Step 2: 로컬 기동**

Run: `./gradlew bootRun --args='--spring.profiles.active=local'`
Expected: 정상 기동, 에러 로그 없음

- [ ] **Step 3: SYSTEM_ADMIN 토큰으로 엔드포인트 호출**

Run (토큰은 실제 발급값으로 치환):
```bash
curl -s -H "Authorization: Bearer <SYSTEM_ADMIN_TOKEN>" \
  http://localhost:8080/api/v1/admin/esg/infra | jq .
```
Expected: `period`, `summary`, `resources[]`, `totalCarbonComparison`, `equivalent`, `calculation` 필드가 포함된 JSON. `resources`의 `averageCpu`가 실제 CloudWatch 값(0이 아님 — 인스턴스가 가동 중일 때).

- [ ] **Step 4: 권한 없는 호출이 거부되는지 확인**

Run: `curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/v1/admin/esg/infra`
Expected: `401` 또는 `403`

- [ ] **Step 5: 캐시 동작 확인**

Step 3을 연속 2회 호출. 두 번째 응답이 즉시 반환되는지(로그에 CloudWatch 호출이 1회만 찍히는지) 확인.

---

## 향후 범위 (이번 플랜 제외)

- **ASG 도입 시**: `AutoScalingDiscoveryService` 추가, `OptimizationType.ASG_SCALE_IN`/`ASG_MEMBER` 분기, `ASG desired capacity 조정` 권장 로직. ASG API 스펙 확정 후 별도 플랜.
- **ALB/RDS 메트릭 반영**: `RequestCount`, `TargetResponseTime`, `HTTPCode_Target_5XX_Count`, `DatabaseConnections` 등을 권장 판단에 추가. 현재는 EC2 CPU 단일 기준.
- **프론트엔드 대시보드**: `Workipedia-fe` 레포에서 `GET /api/v1/admin/esg/infra` 응답을 목업(`workipedia_infra_esg_total_recommendations_mockup.html`) 레이아웃에 매핑.

---

## Self-Review

**Spec coverage:**
- 통합 API 1개 (문서 3장) → Task 8 ✅
- 응답 JSON 구조 (문서 4장) → Task 6 DTO + Task 7 조립 ✅
- RECOMMENDED 합산 로직 (문서 5장) → Task 7 ✅
- 권장 판단 기준 (문서 6장, BE 다운사이징) → Task 4 ✅ / AI ASG·ALB·RDS는 "향후 범위"로 명시 분리 ✅
- CloudWatch 메트릭 (문서 7장 EC2 CPU) → Task 5 ✅
- IAM (문서 8장) → 사용자가 이미 완료(cloudwatch/ec2)
- 탄소 계산 공식 (문서 9장) → Task 3, 검증값 2.80/1.95 테스트 ✅
- 스마트폰 충전 환산 (문서 10장) → Task 7, 기존 calculator 상수 재사용 ✅
- 패키지 구조 (문서 11장) → Discovery/AutoScaling/Aggregator는 ASG 미구성으로 통합·축소(향후 분리)
- admin 보호 → SecurityConfig 기존 규칙으로 자동 적용, Task 9 Step 4 검증 ✅

**Placeholder scan:** 모든 코드 스텝에 실제 코드 포함. "적절한 에러 처리" 류 추상 표현 없음. ✅

**Type consistency:** `CpuMetrics(averageCpu, maxCpu)`, `ResourceRecommendationDto` 필드, `estimateGramsPerHour(String, double)`, `fetchCpu24h(String)`, `evaluate(MonitoredResource, CpuMetrics)`, `getSummary()` 시그니처가 Task 4→5→7→8 전반에서 일치. ✅
