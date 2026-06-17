# AI Tool 관리 API 및 Tool 실행기 (M2) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** GitHub 이슈 #90의 M2 범위 — HTTP_API Tool + DB_QUERY Tool 등록(관리자 API) + 활성 Tool 목록 조회 + Tool 실행기(AI 서버 → BE 내부 실행 API)를 구현한다.

**Architecture:** 신규 `tool` 도메인(엔티티, HTTP/DB 실행기, 내부 컨트롤러, 감사 로그)과 `admin/aitool` 도메인(관리자 CRUD 컨트롤러)을 추가한다. AI 서버는 `X-Internal-Api-Key` 공유 시크릿 헤더로 인증되는 `/api/v1/internal/ai-tools/**` 엔드포인트를 호출한다. credential은 DB에 `credential_ref`(환경변수 이름)만 저장하고 실제 값은 Spring `Environment`에서 조회한다. DB Query Tool은 AI가 SQL을 생성하지 않고, BE에 등록·승인된 `SELECT` 템플릿에 검증된 파라미터만 바인딩한다. 파라미터 검증은 자체 정의한 단순 JSON 스키마(`properties.{name}.{type,required}`)로 수행한다.

**Tech Stack:** Spring Boot 3.5, Spring Security(OncePerRequestFilter), Spring Data JPA, RestClient, Jackson, JUnit5/Mockito, MariaDB(Flyway 마이그레이션)

---

## 범위 (M2)

- 포함: `ai_tools` 등록(HTTP_API, DB_QUERY)/조회/설정·승인·활성 변경, `GET /internal/ai-tools/active`, `POST /internal/ai-tools/{id}/execute`, `POST /admin/ai-tools/{id}/health-check`(연결 확인), 실행 감사 로그, credential 비노출, 내부 API 인증
- 제외(M4): `POST /admin/ai-tools/{id}/test`(파라미터를 채워 실제 기능을 실행·검증 — health-check와 달리 연결 확인이 아니라 기능 실행 확인), OAUTH2 인증 타입 실제 동작, AI가 임의 SQL을 생성하는 기능

## 실행 시 주의 (CLAUDE.md 준수)

- 각 Task의 "Commit" 스텝은 체크리스트 상의 논리적 경계 표시일 뿐이다. **실제 `git commit`은 실행하지 않고, 변경사항을 staged 상태로 둔 뒤 사용자 검토 후 사용자가 직접 커밋한다.**
- 커밋 메시지가 필요하면 한국어로 제안하되, 사용자가 명시적으로 커밋을 요청할 때만 실행한다.

## 설계 결정 메모

- 내부 경로는 기존 코드베이스 컨벤션(`/api/v1/...`)에 맞춰 `/api/v1/internal/ai-tools/**`로 둔다(이슈 문서의 `/internal/...`에서 prefix만 통일).
- 내부 API 인증은 별도 JWT 발급 없이 고정 헤더 `X-Internal-Api-Key` + 환경변수 시크릿 비교 방식으로 구현한다(사용자 확인 완료).
- `credential_ref`는 환경변수 "이름"만 저장하고 DB에 실제 값을 저장하지 않는다(사용자 확인 완료).
- `response_description`은 이번 M2 화면/등록 API에서 제외한다. 응답 해석 문구가 필요하면 Tool description에 포함하고, 별도 컬럼/입력 필드는 만들지 않는다.
- Tool 실행 감사 로그는 기존 `admin_logs`(관리자 행동 로그)와 분리된 신규 테이블 `tool_execution_logs`에 기록한다. 관리자의 등록/수정 행동 자체는 기존 `AdminLog`에 남긴다.
- M2에서 지원하는 `auth_type`은 `NONE`, `API_KEY`, `BEARER_TOKEN`만이며 `OAUTH2`는 등록 시점에 거부한다. DB_QUERY Tool은 `auth_type=NONE`만 허용하고 DB 접근 권한은 BE의 read-only datasource 설정으로 통제한다.
- `parameters_schema`는 정식 JSON Schema가 아니라 `{"properties": {"name": {"type": "string", "required": true}}}` 형태의 단순 포맷으로 정의하고 자체 Validator로 검사한다.
- (DB_QUERY 안전 정책) AI는 SQL 문자열을 생성·수정하지 않는다. 관리자가 사전 등록한 `query_template`만 실행하며, AI는 `parameters`만 전달한다.
- (DB_QUERY 안전 정책) `query_template`은 단일 `SELECT`만 허용한다. `INSERT/UPDATE/DELETE/MERGE/ALTER/DROP/TRUNCATE/CREATE/CALL/EXEC`, 세미콜론, SQL 주석(`--`, `/* */`)은 거부한다.
- (DB_QUERY 안전 정책) `datasource_key` 신규 입력은 사용하지 않는다. DB Query Tool은 관리자가 등록한 DB Catalog의 `datasourceId`, `tableId`, 허용 컬럼 설정을 기준으로 생성한다.
- (DB_QUERY 안전 정책) `max_result_count`와 `timeout_ms`는 BE 기본값/상한으로 강제하고, 화면에서는 `timeoutMs`를 입력받지 않는다. 결과 row 수는 executor에서 한 번 더 제한한다.
- (코드 리뷰 반영) HTTP_API Tool은 등록·수정·실행 모든 시점에 `SsrfGuard`로 `endpointUrl`을 검사한다. `TOOL_ALLOWED_HOSTS` allowlist에 없는 host는 차단하고(비어 있으면 전체 차단), HTTPS 강제 및 루프백/사설망/링크로컬 주소 차단도 함께 적용한다.
- (코드 리뷰 반영) `internal.api-key`는 base `application.yaml`에서 기본값을 두지 않아 운영 환경에서 env var 누락 시 애플리케이션이 기동 실패하도록 한다. 로컬/테스트 기본값은 `application-local.yaml`/`application-test.yaml`에만 둔다.
- (코드 리뷰 반영) `AiTool`은 V16의 `deleted_at`/`is_deleted`를 엔티티에 매핑하고, 활성 Tool 조회·관리자 목록 조회 모두 `isDeleted = "N"` 조건을 명시해 삭제된 Tool이 노출되지 않게 한다.
- health-check(Task 10)와 test(M4)는 다르다 — health-check는 연결 가능 여부만 확인(parameters/queryTemplate 실행 없음), test는 실제 parameters로 Tool을 실행해 응답 데이터까지 검증한다. health-check는 redirect를 비활성화한 별도 `RestClient`를 사용해 3xx도 실패로 처리하고, 실패 메시지는 일반화해 URL/인증정보/DB 접속정보가 노출되지 않게 한다. DB 마이그레이션과 감사 로그(`AdminLog`)는 이번 범위에서 제외한다.
- (2026-06-17 재확인) `ApprovalStatus`에서 `DRAFT`를 완전히 제거했다. `HTTP_API`/`DB_QUERY` 모두 생성 즉시 **`APPROVED`**로 시작하며, enum은 `APPROVED`/`REJECTED` 두 값만 남는다. SYSTEM_ADMIN이 지원팀·개발팀 권한을 함께 가진 운영자라 "개발자 검증 → SYSTEM_ADMIN 승인"으로 인원을 분리할 필요가 없기 때문이다. `REJECTED`는 SYSTEM_ADMIN이 등록된 Tool을 검토 후 거부했음을 남기는 용도로 유지한다. SYSTEM_ADMIN은 등록 후 활성(`active`) 여부만 별도로 토글한다. DB는 `V16`을 직접 고치지 않고 `V39__remove_draft_from_ai_tools_approval_status.sql`로 기존 `DRAFT` 행을 `APPROVED`로 백필 + 컬럼 기본값/체크 제약을 변경했다.

### FE mock 반영 사항 (2026-06-16)

현재 관리자 화면 mock은 구현 범위를 다음처럼 단순화한다. BE 구현 시 API 계약과 DTO 이름은 이 방향을 우선한다.

- Tool 목록에서는 `승인 상태`, `연결 상태` 컬럼을 노출하지 않는다. 목록 기본 컬럼은 `Tool 이름 / 설명`, `대상`, `Method`, `유형`, `활성`이다.
- HTTP API 등록은 M2에서 `GET` 요청만 지원한다. 화면에서 `Method`는 `GET` 고정값으로 표시한다.
- HTTP API 등록의 요청 파라미터는 실제 실행값이 아니라 Tool 입력 정의다.
  - 필드: `name`, `location(PATH|QUERY|HEADER)`, `type`, `required`, `description`, `exampleValue`
  - `exampleValue`는 연결 체크와 예시 URL 생성에만 사용한다.
  - 실제 실행 시 값은 AI가 사용자 질문에서 추출해 `parameters`로 전달한다.
  - Endpoint URL의 `{serverId}` 같은 PATH placeholder는 화면에서 자동으로 PATH 파라미터 row를 만든다.
  - 같은 파라미터명은 PATH/QUERY/HEADER 위치와 무관하게 중복 등록할 수 없다.
  - 타입 기본값은 `string`이다.
  - 예시 GET 요청 URL은 `exampleValue`를 사용해 PATH placeholder를 치환하고, `exampleValue`가 없는 QUERY 파라미터는 붙이지 않는다.
- HTTP API 등록 화면의 버튼명은 `연결 체크`다. 이것은 health-check 성격이며, M4의 실제 기능 실행 test와 구분한다.
- HTTP API 인증 설정은 `Auth Type`, `인증 Header 이름`, `인증 Key 값`을 한 그룹으로 표시한다. 인증 Key 값은 목록/상세에 평문 노출하지 않는다.
- DB Query 화면은 `카탈로그 등록`과 `Tool 등록` 탭으로 분리한다.
  - `DB Query 등록` 같은 단순 탭 이동용 상단 버튼은 두지 않는다.
  - `DB Query Tool은 허용된 Catalog 기반으로만 생성합니다.` 안내는 `Tool 등록` 탭 안에서만 표시한다.
- DB 연결 등록 모달은 `설명` 필드를 받지 않는다. 필수 흐름은 DB 종류, JDBC URL, username, password 입력 → 연결 테스트 → database 선택, 표시명 입력 → 등록 및 스캔이다.
- DB Catalog 화면은 테이블/컬럼 스캔 결과에서 `사용 여부`만 조정한다.
  - 화면에서는 `반환 가능`, `조건 가능`, `필수 조건`을 노출하지 않는다.
  - 민감 필드는 배지로 표시하고 기본적으로 비활성 처리한다.
- DB Query Tool 등록 화면은 관리자 이해 중심으로 구성한다.
  - `조건 필드`라는 용어 대신 `검색 기준` 또는 `조회 입력값`을 사용한다.
  - 테이블/컬럼은 외부 DB 물리명 그대로 표시한다.
  - `Timeout`과 `Response Description` 입력은 화면에서 제거한다.
  - SQL은 FE가 생성하지 않고, BE Preview 응답을 `개발자 Preview`로 접어 표시한다.

### 구버전 스니펫 보정 규칙

이 문서는 초안 단계의 코드 스니펫을 많이 포함한다. 아래 Task의 일부 테스트/서비스 예시는 예전 필드명(`responseDescription`, `datasourceKey`, 화면 입력용 `timeoutMs`, 다중 HTTP method)을 포함할 수 있다. 실제 구현 시에는 다음 최신 결정사항을 우선한다.

```text
responseDescription 입력/컬럼 추가 없음
HTTP_API 등록 method는 GET 고정
HTTP_API 요청 파라미터는 name/location/type/required/description/exampleValue 구조
credential 값은 평문 저장/응답 금지, credentialRef 또는 암호화 저장 정책 중 프로젝트 최종 정책을 따른다
DB_QUERY는 datasourceKey 직접 입력이 아니라 DB Catalog 기반 datasourceId/tableId/selectColumns/filters 사용
DB_QUERY queryTemplate은 BE가 Catalog 설정으로 생성
timeoutMs는 화면 입력이 아니라 BE 기본값/상한으로 관리
Tool 목록 응답은 승인 상태/연결 상태를 UI 기본 컬럼으로 노출하지 않음
health-check 화면 문구는 연결 체크
```

---

## File Structure

**신규 생성:**
- `src/main/resources/db/migration/V38__create_tool_execution_logs.sql`
- `src/main/java/com/wip/workipedia/tool/domain/ToolType.java`
- `src/main/java/com/wip/workipedia/tool/domain/AuthType.java`
- `src/main/java/com/wip/workipedia/tool/domain/ApprovalStatus.java`
- `src/main/java/com/wip/workipedia/tool/domain/AiTool.java`
- `src/main/java/com/wip/workipedia/tool/domain/ToolExecutionLog.java`
- `src/main/java/com/wip/workipedia/tool/repository/AiToolRepository.java`
- `src/main/java/com/wip/workipedia/tool/repository/ToolExecutionLogRepository.java`
- `src/main/java/com/wip/workipedia/tool/service/ParameterSchemaValidator.java`
- `src/main/java/com/wip/workipedia/tool/service/SqlTemplateValidator.java`
- `src/main/java/com/wip/workipedia/tool/service/ToolExecutionService.java`
- `src/main/java/com/wip/workipedia/tool/exception/ToolExecutionException.java`
- `src/main/java/com/wip/workipedia/tool/executor/ToolRestClientFactory.java`
- `src/main/java/com/wip/workipedia/tool/executor/DefaultToolRestClientFactory.java`
- `src/main/java/com/wip/workipedia/tool/executor/HttpApiToolExecutor.java`
- `src/main/java/com/wip/workipedia/tool/executor/DbQueryToolExecutor.java`
- `src/main/java/com/wip/workipedia/tool/executor/ToolExecutionResult.java`
- `src/main/java/com/wip/workipedia/tool/executor/HealthCheckResult.java`
- `src/main/java/com/wip/workipedia/tool/executor/HealthCheckRestClientFactory.java`
- `src/main/java/com/wip/workipedia/tool/executor/DefaultHealthCheckRestClientFactory.java`
- `src/main/java/com/wip/workipedia/tool/executor/HttpApiHealthChecker.java`
- `src/main/java/com/wip/workipedia/tool/executor/DbQueryHealthChecker.java`
- `src/main/java/com/wip/workipedia/tool/dto/ToolExecuteRequest.java`
- `src/main/java/com/wip/workipedia/tool/dto/ToolExecuteResponse.java`
- `src/main/java/com/wip/workipedia/tool/dto/ActiveAiToolResponse.java`
- `src/main/java/com/wip/workipedia/tool/controller/InternalAiToolController.java`
- `src/main/java/com/wip/workipedia/admin/aitool/dto/AiToolCreateRequest.java`
- `src/main/java/com/wip/workipedia/admin/aitool/dto/AiToolUpdateRequest.java`
- `src/main/java/com/wip/workipedia/admin/aitool/dto/AiToolResponse.java`
- `src/main/java/com/wip/workipedia/admin/aitool/dto/HealthCheckResponse.java`
- `src/main/java/com/wip/workipedia/admin/aitool/service/AdminAiToolService.java`
- `src/main/java/com/wip/workipedia/admin/aitool/controller/AdminAiToolController.java`
- `src/main/java/com/wip/workipedia/config/InternalApiProperties.java`
- `src/main/java/com/wip/workipedia/config/InternalApiConfig.java`
- `src/main/java/com/wip/workipedia/config/ToolDbProperties.java`
- `src/main/java/com/wip/workipedia/config/ToolDbConfig.java`
- `src/main/java/com/wip/workipedia/common/security/InternalApiKeyFilter.java`
- (테스트 파일들은 각 Task에 명시)

**수정:**
- `src/main/java/com/wip/workipedia/common/exception/ErrorType.java` — `ai-tool-*`, `internal-001` 추가
- `src/main/java/com/wip/workipedia/config/SecurityConfig.java` — `/api/v1/internal/**` permitAll + `InternalApiKeyFilter` 등록
- `src/main/resources/application.yaml` — `internal.api-key` 설정 추가 (기본값 없음 — env var 미설정 시 기동 실패)
- `src/main/resources/application-local.yaml` — `internal.api-key` 로컬 기본값 추가
- `src/test/resources/application-test.yaml` — `internal.api-key` 테스트 고정값 추가
- `src/main/resources/application.yaml` — `tool.db.allowed-datasources` 설정 추가

---

## Task 1: DB 마이그레이션 — tool_execution_logs 테이블

**Files:**
- Create: `src/main/resources/db/migration/V38__create_tool_execution_logs.sql`

- [ ] **Step 1: tool_execution_logs 테이블 생성 마이그레이션 작성**

```sql
-- V38__create_tool_execution_logs.sql
CREATE TABLE tool_execution_logs (
    tool_execution_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ai_tool_id BIGINT NOT NULL,
    caller VARCHAR(100) NOT NULL,
    masked_parameters JSON NULL,
    result_count INT NULL,
    duration_ms BIGINT NOT NULL,
    success CHAR(1) NOT NULL,
    error_code VARCHAR(50) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tool_execution_logs_ai_tool
        FOREIGN KEY (ai_tool_id) REFERENCES ai_tools (ai_tool_id),
    CONSTRAINT ck_tool_execution_logs_success
        CHECK (success IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_tool_execution_logs_ai_tool_id
    ON tool_execution_logs (ai_tool_id, created_at);
```

- [ ] **Step 2: 마이그레이션 적용 확인**

Run: `./gradlew flywayMigrate` (로컬 DB 기동 중이어야 함) 또는 애플리케이션 기동 시 자동 적용 확인.
Expected: `flyway_schema_history`에 V38가 success로 기록됨.

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/db/migration/V38__create_tool_execution_logs.sql
git commit -m "feat: tool_execution_logs 테이블 추가"
```

---

## Task 2: ErrorType 추가

**Files:**
- Modify: `src/main/java/com/wip/workipedia/common/exception/ErrorType.java`

- [ ] **Step 1: 기존 `// ai` 그룹 다음에 `// ai tool`, `// internal` 그룹 추가**

`AI_SYNC_FAILED("ai-001", ...)` 라인 바로 다음에 삽입:

```java
	// ai tool
	AI_TOOL_NOT_FOUND("ai-tool-001", "Tool을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	AI_TOOL_INVALID_TYPE("ai-tool-002", "지원하지 않는 Tool 타입입니다.", HttpStatus.BAD_REQUEST),
	AI_TOOL_INVALID_AUTH_TYPE("ai-tool-003", "지원하지 않는 인증 방식입니다.", HttpStatus.BAD_REQUEST),
	AI_TOOL_NOT_EXECUTABLE("ai-tool-004", "비활성 또는 미승인 Tool은 실행할 수 없습니다.", HttpStatus.CONFLICT),
	AI_TOOL_PARAMETER_MISMATCH("ai-tool-005", "등록된 입력 스키마와 일치하지 않는 파라미터입니다.", HttpStatus.BAD_REQUEST),
	AI_TOOL_UNSAFE_ENDPOINT("ai-tool-006", "허용되지 않은 endpoint host입니다.", HttpStatus.BAD_REQUEST),

	// internal
	INTERNAL_API_UNAUTHORIZED("internal-001", "내부 API 인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
```

- [ ] **Step 2: 컴파일 확인**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/wip/workipedia/common/exception/ErrorType.java
git commit -m "feat: AI Tool 관련 ErrorType 추가"
```

---

## Task 3: AiTool / ToolExecutionLog 도메인 모델

**Files:**
- Create: `src/main/java/com/wip/workipedia/tool/domain/ToolType.java`
- Create: `src/main/java/com/wip/workipedia/tool/domain/AuthType.java`
- Create: `src/main/java/com/wip/workipedia/tool/domain/ApprovalStatus.java`
- Create: `src/main/java/com/wip/workipedia/tool/domain/AiTool.java`
- Create: `src/main/java/com/wip/workipedia/tool/domain/ToolExecutionLog.java`
- Create: `src/main/java/com/wip/workipedia/tool/repository/AiToolRepository.java`
- Create: `src/main/java/com/wip/workipedia/tool/repository/ToolExecutionLogRepository.java`
- Test: `src/test/java/com/wip/workipedia/tool/domain/AiToolTest.java`

- [ ] **Step 1: enum 3개 작성**

```java
// src/main/java/com/wip/workipedia/tool/domain/ToolType.java
package com.wip.workipedia.tool.domain;

public enum ToolType {
	HTTP_API,
	DB_QUERY
}
```

```java
// src/main/java/com/wip/workipedia/tool/domain/AuthType.java
package com.wip.workipedia.tool.domain;

public enum AuthType {
	NONE,
	API_KEY,
	BEARER_TOKEN,
	OAUTH2
}
```

```java
// src/main/java/com/wip/workipedia/tool/domain/ApprovalStatus.java
package com.wip.workipedia.tool.domain;

public enum ApprovalStatus {
	DRAFT,
	APPROVED,
	REJECTED
}
```

- [ ] **Step 2: AiTool 엔티티의 핵심 동작에 대한 실패하는 테스트 작성**

```java
// src/test/java/com/wip/workipedia/tool/domain/AiToolTest.java
package com.wip.workipedia.tool.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiToolTest {

	@Test
	void createHttpApiTool_초기상태는_DRAFT_비활성() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "직원 정보를 조회합니다.", "응답 필드: name(이름)",
			"https://hr.example.com/api/employees", "GET",
			"{\"properties\":{\"employeeId\":{\"type\":\"string\",\"required\":true}}}",
			null, AuthType.API_KEY, "TOOL_HR_API_KEY", 5000, 100, 1L
		);

		assertThat(tool.getApprovalStatus()).isEqualTo(ApprovalStatus.DRAFT);
		assertThat(tool.isActive()).isFalse();
		assertThat(tool.isExecutable()).isFalse();
	}

	@Test
	void isExecutable_활성이고_승인된_경우에만_true() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "직원 정보를 조회합니다.", null,
			"https://hr.example.com/api/employees", "GET",
			"{\"properties\":{}}", null, AuthType.NONE, null, 5000, 100, 1L
		);

		tool.changeApprovalStatus(ApprovalStatus.APPROVED, 1L);
		assertThat(tool.isExecutable()).isFalse();

		tool.changeActive(true, 1L);
		assertThat(tool.isExecutable()).isTrue();
	}
}
```

- [ ] **Step 3: 테스트 실행해서 실패 확인**

Run: `./gradlew test --tests "com.wip.workipedia.tool.domain.AiToolTest"`
Expected: FAIL — `AiTool` 클래스가 없어 컴파일 에러

- [ ] **Step 4: AiTool 엔티티 작성**

```java
// src/main/java/com/wip/workipedia/tool/domain/AiTool.java
package com.wip.workipedia.tool.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_tools")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiTool {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ai_tool_id")
	private Long aiToolId;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, length = 1000)
	private String description;

	@Column(length = 1000)
	private String responseDescription;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ToolType toolType;

	@Column(length = 1000)
	private String endpointUrl;

	@Column(length = 10)
	private String httpMethod;

	@Column(length = 100)
	private String datasourceKey;

	@Column(columnDefinition = "LONGTEXT")
	private String queryTemplate;

	@Column(nullable = false, columnDefinition = "JSON")
	private String parametersSchema;

	@Column(columnDefinition = "JSON")
	private String responseSchema;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private AuthType authType;

	@Column(length = 255)
	private String credentialRef;

	@Column(nullable = false)
	private int timeoutMs;

	@Column(nullable = false)
	private int maxResultCount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ApprovalStatus approvalStatus;

	@Column(nullable = false, columnDefinition = "CHAR(1)")
	private String isActive;

	private Long createdBy;
	private Long updatedBy;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	private LocalDateTime deletedAt;

	@Column(nullable = false, columnDefinition = "CHAR(1)")
	private String isDeleted;

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public static AiTool createHttpApiTool(
		String name, String description, String responseDescription,
		String endpointUrl, String httpMethod, String parametersSchema, String responseSchema,
		AuthType authType, String credentialRef, int timeoutMs, int maxResultCount, Long createdBy
	) {
		AiTool tool = new AiTool();
		tool.name = name;
		tool.description = description;
		tool.responseDescription = responseDescription;
		tool.toolType = ToolType.HTTP_API;
		tool.endpointUrl = endpointUrl;
		tool.httpMethod = httpMethod;
		tool.parametersSchema = parametersSchema;
		tool.responseSchema = responseSchema;
		tool.authType = authType;
		tool.credentialRef = credentialRef;
		tool.timeoutMs = timeoutMs;
		tool.maxResultCount = maxResultCount;
		tool.approvalStatus = ApprovalStatus.DRAFT;
		tool.isActive = "N";
		tool.isDeleted = "N";
		tool.createdBy = createdBy;
		tool.updatedBy = createdBy;
		return tool;
	}

	public static AiTool createDbQueryTool(
		String name, String description, String responseDescription,
		String datasourceKey, String queryTemplate, String parametersSchema, String responseSchema,
		int timeoutMs, int maxResultCount, Long createdBy
	) {
		AiTool tool = new AiTool();
		tool.name = name;
		tool.description = description;
		tool.responseDescription = responseDescription;
		tool.toolType = ToolType.DB_QUERY;
		tool.datasourceKey = datasourceKey;
		tool.queryTemplate = queryTemplate;
		tool.parametersSchema = parametersSchema;
		tool.responseSchema = responseSchema;
		tool.authType = AuthType.NONE;
		tool.timeoutMs = timeoutMs;
		tool.maxResultCount = maxResultCount;
		tool.approvalStatus = ApprovalStatus.DRAFT;
		tool.isActive = "N";
		tool.isDeleted = "N";
		tool.createdBy = createdBy;
		tool.updatedBy = createdBy;
		return tool;
	}

	public void updateConfig(
		String description, String responseDescription, String endpointUrl, String httpMethod,
		String datasourceKey, String queryTemplate, String parametersSchema, String responseSchema,
		AuthType authType, String credentialRef, Integer timeoutMs, Integer maxResultCount, Long updatedBy
	) {
		if (description != null) this.description = description;
		if (responseDescription != null) this.responseDescription = responseDescription;
		if (endpointUrl != null) this.endpointUrl = endpointUrl;
		if (httpMethod != null) this.httpMethod = httpMethod;
		if (datasourceKey != null) this.datasourceKey = datasourceKey;
		if (queryTemplate != null) this.queryTemplate = queryTemplate;
		if (parametersSchema != null) this.parametersSchema = parametersSchema;
		if (responseSchema != null) this.responseSchema = responseSchema;
		if (authType != null) this.authType = authType;
		if (credentialRef != null) this.credentialRef = credentialRef;
		if (timeoutMs != null) this.timeoutMs = timeoutMs;
		if (maxResultCount != null) this.maxResultCount = maxResultCount;
		this.updatedBy = updatedBy;
	}

	public void changeApprovalStatus(ApprovalStatus approvalStatus, Long updatedBy) {
		this.approvalStatus = approvalStatus;
		this.updatedBy = updatedBy;
	}

	public void changeActive(boolean active, Long updatedBy) {
		this.isActive = active ? "Y" : "N";
		this.updatedBy = updatedBy;
	}

	public boolean isActive() {
		return "Y".equals(this.isActive);
	}

	public boolean isDeleted() {
		return "Y".equals(this.isDeleted);
	}

	public boolean isExecutable() {
		return isActive() && !isDeleted() && approvalStatus == ApprovalStatus.APPROVED;
	}
}
```

- [ ] **Step 5: 테스트 실행해서 통과 확인**

Run: `./gradlew test --tests "com.wip.workipedia.tool.domain.AiToolTest"`
Expected: PASS (2 tests)

- [ ] **Step 6: ToolExecutionLog 엔티티 작성**

```java
// src/main/java/com/wip/workipedia/tool/domain/ToolExecutionLog.java
package com.wip.workipedia.tool.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tool_execution_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ToolExecutionLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "tool_execution_log_id")
	private Long toolExecutionLogId;

	@Column(name = "ai_tool_id", nullable = false)
	private Long aiToolId;

	@Column(nullable = false, length = 100)
	private String caller;

	@Column(name = "masked_parameters", columnDefinition = "JSON")
	private String maskedParameters;

	private Integer resultCount;

	@Column(nullable = false)
	private long durationMs;

	@Column(nullable = false, columnDefinition = "CHAR(1)")
	private String success;

	@Column(length = 50)
	private String errorCode;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	public static ToolExecutionLog of(
		Long aiToolId, String caller, String maskedParametersJson,
		Integer resultCount, long durationMs, boolean success, String errorCode
	) {
		ToolExecutionLog log = new ToolExecutionLog();
		log.aiToolId = aiToolId;
		log.caller = caller;
		log.maskedParameters = maskedParametersJson;
		log.resultCount = resultCount;
		log.durationMs = durationMs;
		log.success = success ? "Y" : "N";
		log.errorCode = errorCode;
		log.createdAt = LocalDateTime.now();
		return log;
	}
}
```

- [ ] **Step 7: Repository 2개 작성**

```java
// src/main/java/com/wip/workipedia/tool/repository/AiToolRepository.java
package com.wip.workipedia.tool.repository;

import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.domain.ApprovalStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiToolRepository extends JpaRepository<AiTool, Long> {
	List<AiTool> findByIsActiveAndApprovalStatusAndIsDeleted(
		String isActive, ApprovalStatus approvalStatus, String isDeleted
	);

	Optional<AiTool> findByAiToolIdAndIsDeleted(Long aiToolId, String isDeleted);

	Page<AiTool> findByIsDeleted(String isDeleted, Pageable pageable);
}
```

**리뷰 반영(soft delete 일관성):** V16의 `deleted_at`/`is_deleted`를 엔티티가 무시하면 삭제된 Tool이 활성 목록·관리자 목록·**실행 경로**에 노출될 수 있다는 코드 리뷰 지적 반영. `isDeleted` 조건을 모든 조회 메서드(목록 조회 + 단건 실행 조회)에 명시한다. `findByAiToolIdAndIsDeleted`는 Task 6의 `ToolExecutionService.execute()`에서 사용해 삭제된 Tool은 ID를 알아도 실행되지 않게 한다.

```java
// src/main/java/com/wip/workipedia/tool/repository/ToolExecutionLogRepository.java
package com.wip.workipedia.tool.repository;

import com.wip.workipedia.tool.domain.ToolExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToolExecutionLogRepository extends JpaRepository<ToolExecutionLog, Long> {
}
```

- [ ] **Step 8: 전체 컴파일 확인**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/wip/workipedia/tool/domain src/main/java/com/wip/workipedia/tool/repository src/test/java/com/wip/workipedia/tool/domain/AiToolTest.java
git commit -m "feat: AiTool, ToolExecutionLog 도메인 모델 추가"
```

---

## Task 4: ParameterSchemaValidator

**파라미터 스키마 포맷:** `{"properties": {"필드명": {"type": "string|integer|number|boolean", "required": true|false}}}`. 스키마에 없는 키가 들어오거나, required 필드가 빠지거나, 타입이 안 맞으면 무효.

**Files:**
- Create: `src/main/java/com/wip/workipedia/tool/service/ParameterSchemaValidator.java`
- Test: `src/test/java/com/wip/workipedia/tool/service/ParameterSchemaValidatorTest.java`

- [ ] **Step 1: 실패하는 테스트 작성**

```java
// src/test/java/com/wip/workipedia/tool/service/ParameterSchemaValidatorTest.java
package com.wip.workipedia.tool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParameterSchemaValidatorTest {

	private final ParameterSchemaValidator validator = new ParameterSchemaValidator(new ObjectMapper());

	private static final String SCHEMA = """
		{"properties": {
			"employeeId": {"type": "string", "required": true},
			"year": {"type": "integer", "required": false}
		}}
		""";

	@Test
	void validate_필수필드와_타입이_맞으면_유효() {
		var result = validator.validate(SCHEMA, Map.of("employeeId", "E001", "year", 2026));

		assertThat(result.valid()).isTrue();
	}

	@Test
	void validate_옵션필드_생략해도_유효() {
		var result = validator.validate(SCHEMA, Map.of("employeeId", "E001"));

		assertThat(result.valid()).isTrue();
	}

	@Test
	void validate_필수필드_없으면_무효() {
		var result = validator.validate(SCHEMA, Map.of("year", 2026));

		assertThat(result.valid()).isFalse();
		assertThat(result.message()).contains("employeeId");
	}

	@Test
	void validate_타입이_다르면_무효() {
		var result = validator.validate(SCHEMA, Map.of("employeeId", 123));

		assertThat(result.valid()).isFalse();
	}

	@Test
	void validate_스키마에_없는_파라미터는_무효() {
		var result = validator.validate(SCHEMA, Map.of("employeeId", "E001", "extra", "x"));

		assertThat(result.valid()).isFalse();
		assertThat(result.message()).contains("extra");
	}
}
```

- [ ] **Step 2: 테스트 실행해서 실패 확인**

Run: `./gradlew test --tests "com.wip.workipedia.tool.service.ParameterSchemaValidatorTest"`
Expected: FAIL — `ParameterSchemaValidator` 클래스가 없어 컴파일 에러

- [ ] **Step 3: ParameterSchemaValidator 구현**

```java
// src/main/java/com/wip/workipedia/tool/service/ParameterSchemaValidator.java
package com.wip.workipedia.tool.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ParameterSchemaValidator {

	private final ObjectMapper objectMapper;

	public ParameterSchemaValidator(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public ValidationResult validate(String parametersSchemaJson, Map<String, Object> parameters) {
		Map<String, Object> schema;
		try {
			schema = objectMapper.readValue(parametersSchemaJson, new TypeReference<Map<String, Object>>() {});
		} catch (Exception e) {
			return ValidationResult.invalid("Tool 파라미터 스키마가 올바르지 않습니다.");
		}

		Map<String, Object> properties = asMap(schema.get("properties"));
		Map<String, Object> values = parameters == null ? Map.of() : parameters;

		for (String key : values.keySet()) {
			if (!properties.containsKey(key)) {
				return ValidationResult.invalid("허용되지 않은 파라미터입니다: " + key);
			}
		}

		for (Map.Entry<String, Object> entry : properties.entrySet()) {
			String propertyName = entry.getKey();
			Map<String, Object> propertySchema = asMap(entry.getValue());
			boolean required = Boolean.TRUE.equals(propertySchema.get("required"));
			Object value = values.get(propertyName);

			if (value == null) {
				if (required) {
					return ValidationResult.invalid("필수 파라미터가 없습니다: " + propertyName);
				}
				continue;
			}

			String type = (String) propertySchema.get("type");
			if (!matchesType(value, type)) {
				return ValidationResult.invalid("파라미터 타입이 올바르지 않습니다: " + propertyName);
			}
		}

		return ValidationResult.valid();
	}

	private boolean matchesType(Object value, String type) {
		if (type == null) {
			return true;
		}
		return switch (type) {
			case "string" -> value instanceof String;
			case "integer" -> value instanceof Integer || value instanceof Long;
			case "number" -> value instanceof Number;
			case "boolean" -> value instanceof Boolean;
			default -> true;
		};
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> asMap(Object value) {
		return value instanceof Map ? (Map<String, Object>) value : Map.of();
	}

	public record ValidationResult(boolean valid, String message) {
		public static ValidationResult valid() {
			return new ValidationResult(true, null);
		}

		public static ValidationResult invalid(String message) {
			return new ValidationResult(false, message);
		}
	}
}
```

- [ ] **Step 4: 테스트 실행해서 통과 확인**

Run: `./gradlew test --tests "com.wip.workipedia.tool.service.ParameterSchemaValidatorTest"`
Expected: PASS (5 tests)

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/wip/workipedia/tool/service/ParameterSchemaValidator.java src/test/java/com/wip/workipedia/tool/service/ParameterSchemaValidatorTest.java
git commit -m "feat: Tool 파라미터 스키마 Validator 추가"
```

---

## Task 5: HttpApiToolExecutor (실제 외부 API 호출)

**설계:** `ToolRestClientFactory`를 인터페이스로 분리해 테스트에서 `MockRestServiceServer`를 `RestClient.Builder`에 바인딩한 가짜 팩토리를 주입할 수 있게 한다. GET은 쿼리 파라미터, 그 외 메서드는 JSON 바디로 파라미터를 전달한다. 인증 정보는 `Environment`에서 `credentialRef` 이름으로 조회한다. 응답이 리스트면 `maxResultCount`로 자른다.

**리뷰 반영(SSRF 방어 — allowlist 기반):** 관리자만 Tool을 등록하더라도 `endpointUrl`이 내부망/루프백/링크로컬(예: `127.0.0.1`, `169.254.169.254`, `192.168.x.x`) 주소를 가리키면 BE가 내부망 프록시로 악용될 수 있다는 코드 리뷰 지적을 반영한다. "등록 시점에만 검증하고 실행 시점엔 재검증하지 않으면 DNS rebinding으로 우회될 수 있다"는 점도 함께 지적받았는데, 실행 시점에 매번 DNS를 재검증하는 것만으로는 완전히 막을 수 없으므로(검증과 실제 호출 사이에도 race가 가능) **허용된 host만 호출 가능하게 allowlist를 강제**하는 방식으로 범위를 좁힌다. `TOOL_ALLOWED_HOSTS` 환경변수(콤마 구분 host 목록)에 없는 host는 등록 시점·실행 시점 모두 거부한다. allowlist가 비어 있으면 기본적으로 전체 차단(fail-safe)이다. 추가로 HTTPS 강제 및 루프백/사설망/링크로컬 주소 차단도 유지한다(allowlist에 잘못된 사설 IP가 등록되는 실수까지 방어). `SsrfGuard`는 인터페이스로 분리해 `HttpApiToolExecutorTest`에서는 항상 안전하다고 응답하는 스텁을 주입하고, 실제 차단 로직은 `DefaultSsrfGuardTest`에서 검증한다. 동일한 `SsrfGuard`를 Task 9의 `AdminAiToolService`에도 주입해 등록·수정 시점에도 allowlist를 강제한다.

**Files:**
- Create: `src/main/java/com/wip/workipedia/tool/exception/ToolExecutionException.java`
- Create: `src/main/java/com/wip/workipedia/tool/executor/ToolExecutionResult.java`
- Create: `src/main/java/com/wip/workipedia/tool/executor/ToolRestClientFactory.java`
- Create: `src/main/java/com/wip/workipedia/tool/executor/DefaultToolRestClientFactory.java`
- Create: `src/main/java/com/wip/workipedia/tool/executor/SsrfGuard.java`
- Create: `src/main/java/com/wip/workipedia/tool/executor/DefaultSsrfGuard.java`
- Create: `src/main/java/com/wip/workipedia/tool/executor/HttpApiToolExecutor.java`
- Create: `src/main/java/com/wip/workipedia/config/ToolAllowedHostProperties.java`
- Create: `src/main/java/com/wip/workipedia/config/ToolSecurityConfig.java`
- Modify: `src/main/resources/application.yaml`
- Modify: `src/main/resources/application-local.yaml`
- Modify: `src/test/resources/application-test.yaml`
- Test: `src/test/java/com/wip/workipedia/tool/executor/DefaultSsrfGuardTest.java`
- Test: `src/test/java/com/wip/workipedia/tool/executor/HttpApiToolExecutorTest.java`

- [ ] **Step 1: ToolExecutionException, ToolExecutionResult, ToolRestClientFactory 작성**

```java
// src/main/java/com/wip/workipedia/tool/exception/ToolExecutionException.java
package com.wip.workipedia.tool.exception;

public class ToolExecutionException extends RuntimeException {

	private final String errorCode;

	public ToolExecutionException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}
}
```

```java
// src/main/java/com/wip/workipedia/tool/executor/ToolExecutionResult.java
package com.wip.workipedia.tool.executor;

public record ToolExecutionResult(Object data, int resultCount) {
}
```

```java
// src/main/java/com/wip/workipedia/tool/executor/ToolRestClientFactory.java
package com.wip.workipedia.tool.executor;

import org.springframework.web.client.RestClient;

public interface ToolRestClientFactory {
	RestClient create(long timeoutMs);
}
```

```java
// src/main/java/com/wip/workipedia/tool/executor/DefaultToolRestClientFactory.java
package com.wip.workipedia.tool.executor;

import java.time.Duration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DefaultToolRestClientFactory implements ToolRestClientFactory {

	@Override
	public RestClient create(long timeoutMs) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofMillis(timeoutMs));
		requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));
		return RestClient.builder().requestFactory(requestFactory).build();
	}
}
```

- [ ] **Step 2: ToolAllowedHostProperties, ToolSecurityConfig, SsrfGuard 인터페이스 작성**

```java
// src/main/java/com/wip/workipedia/config/ToolAllowedHostProperties.java
package com.wip.workipedia.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tool")
public record ToolAllowedHostProperties(List<String> allowedHosts) {
}
```

```java
// src/main/java/com/wip/workipedia/config/ToolSecurityConfig.java
package com.wip.workipedia.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ToolAllowedHostProperties.class)
public class ToolSecurityConfig {
}
```

```java
// src/main/java/com/wip/workipedia/tool/executor/SsrfGuard.java
package com.wip.workipedia.tool.executor;

public interface SsrfGuard {
	boolean isSafe(String endpointUrl);
}
```

`src/main/resources/application.yaml`의 `ai:` 섹션 다음에 추가 (기본값 없음 → 허용 host를 명시하지 않으면 전체 차단):

```yaml
tool:
  allowed-hosts: ${TOOL_ALLOWED_HOSTS:}
```

`src/main/resources/application-local.yaml` 맨 아래에 추가 (로컬에서 실제 HTTP_API Tool을 등록·실행해보려면 직접 host를 지정해야 함):

```yaml
tool:
  allowed-hosts: ${TOOL_ALLOWED_HOSTS:}
```

`src/test/resources/application-test.yaml` 맨 아래에 추가 (테스트에서 사용하는 고정 host를 명시):

```yaml
tool:
  allowed-hosts: hr.example.com
```

- [ ] **Step 3: 실패하는 테스트 작성 (DefaultSsrfGuard, allowlist 기반)**

```java
// src/test/java/com/wip/workipedia/tool/executor/DefaultSsrfGuardTest.java
package com.wip.workipedia.tool.executor;

import com.wip.workipedia.config.ToolAllowedHostProperties;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultSsrfGuardTest {

	private DefaultSsrfGuard guardWithAllowedHosts(String... hosts) {
		return new DefaultSsrfGuard(new ToolAllowedHostProperties(List.of(hosts)));
	}

	@Test
	void isSafe_allowlist에_있고_HTTPS이면_true() {
		// hr.example.com은 실제 DNS에 존재하지 않아 InetAddress.getByName이 실패한다. example.com은 IANA 예약 도메인으로 실제 resolve된다.
		DefaultSsrfGuard ssrfGuard = guardWithAllowedHosts("example.com");

		assertThat(ssrfGuard.isSafe("https://example.com/api")).isTrue();
	}

	@Test
	void isSafe_allowlist에_없는_host는_차단() {
		DefaultSsrfGuard ssrfGuard = guardWithAllowedHosts("hr.example.com");

		assertThat(ssrfGuard.isSafe("https://other.example.com/api")).isFalse();
	}

	@Test
	void isSafe_allowlist가_비어있으면_전부_차단() {
		DefaultSsrfGuard ssrfGuard = guardWithAllowedHosts();

		assertThat(ssrfGuard.isSafe("https://hr.example.com/api")).isFalse();
	}

	@Test
	void isSafe_HTTP는_allowlist에_있어도_차단() {
		DefaultSsrfGuard ssrfGuard = guardWithAllowedHosts("hr.example.com");

		assertThat(ssrfGuard.isSafe("http://hr.example.com/api")).isFalse();
	}

	@Test
	void isSafe_allowlist에_사설_IP가_등록돼도_차단() {
		DefaultSsrfGuard ssrfGuard = guardWithAllowedHosts("192.168.1.1", "127.0.0.1", "169.254.169.254");

		assertThat(ssrfGuard.isSafe("https://192.168.1.1/api")).isFalse();
		assertThat(ssrfGuard.isSafe("https://127.0.0.1/api")).isFalse();
		assertThat(ssrfGuard.isSafe("https://169.254.169.254/latest/meta-data")).isFalse();
	}
}
```

Run: `./gradlew test --tests "com.wip.workipedia.tool.executor.DefaultSsrfGuardTest"`
Expected: FAIL — `DefaultSsrfGuard` 클래스가 없어 컴파일 에러

- [ ] **Step 4: DefaultSsrfGuard 구현**

```java
// src/main/java/com/wip/workipedia/tool/executor/DefaultSsrfGuard.java
package com.wip.workipedia.tool.executor;

import com.wip.workipedia.config.ToolAllowedHostProperties;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DefaultSsrfGuard implements SsrfGuard {

	private final Set<String> allowedHosts;

	public DefaultSsrfGuard(ToolAllowedHostProperties properties) {
		List<String> configured = properties.allowedHosts();
		this.allowedHosts = (configured == null ? List.<String>of() : configured)
			.stream()
			.filter(host -> host != null && !host.isBlank())
			.map(String::toLowerCase)
			.collect(Collectors.toSet());
	}

	@Override
	public boolean isSafe(String endpointUrl) {
		try {
			URI uri = URI.create(endpointUrl);
			if (!"https".equalsIgnoreCase(uri.getScheme())) {
				return false;
			}

			String host = uri.getHost();
			if (host == null || !allowedHosts.contains(host.toLowerCase())) {
				return false;
			}

			InetAddress address = InetAddress.getByName(host);
			return !(address.isLoopbackAddress()
				|| address.isLinkLocalAddress()
				|| address.isSiteLocalAddress()
				|| address.isAnyLocalAddress()
				|| address.isMulticastAddress());
		} catch (UnknownHostException | IllegalArgumentException e) {
			return false;
		}
	}
}
```

Run: `./gradlew test --tests "com.wip.workipedia.tool.executor.DefaultSsrfGuardTest"`
Expected: PASS (5 tests)

- [ ] **Step 5: 실패하는 테스트 작성 (MockRestServiceServer로 외부 API를 가짜로 둠, SsrfGuard는 스텁으로 우회)**

```java
// src/test/java/com/wip/workipedia/tool/executor/HttpApiToolExecutorTest.java
package com.wip.workipedia.tool.executor;

import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.domain.AuthType;
import com.wip.workipedia.tool.exception.ToolExecutionException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpApiToolExecutorTest {

	private final RestClient.Builder builder = RestClient.builder();
	private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
	private final ToolRestClientFactory restClientFactory = timeoutMs -> builder.build();
	private final Environment environment = mock(Environment.class);
	private final SsrfGuard ssrfGuard = endpointUrl -> true;
	private final HttpApiToolExecutor executor = new HttpApiToolExecutor(restClientFactory, environment, ssrfGuard);

	@Test
	void execute_GET_요청은_쿼리파라미터로_전달하고_응답을_그대로_반환() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "직원 정보를 조회합니다.", null,
			"https://hr.example.com/api/employees", "GET",
			"{\"properties\":{\"employeeId\":{\"type\":\"string\",\"required\":true}}}",
			null, AuthType.NONE, null, 5000, 100, 1L
		);

		server.expect(MockRestRequestMatchers.requestTo("https://hr.example.com/api/employees?employeeId=E001"))
			.andRespond(MockRestResponseCreators.withSuccess(
				"{\"name\":\"홍길동\"}", MediaType.APPLICATION_JSON
			));

		ToolExecutionResult result = executor.execute(tool, Map.of("employeeId", "E001"));

		assertThat(result.resultCount()).isEqualTo(1);
		server.verify();
	}

	@Test
	void execute_리스트응답은_maxResultCount로_잘림() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원목록조회", "직원 목록을 조회합니다.", null,
			"https://hr.example.com/api/employees/list", "GET",
			"{\"properties\":{}}", null, AuthType.NONE, null, 5000, 1, 1L
		);

		server.expect(MockRestRequestMatchers.requestTo("https://hr.example.com/api/employees/list"))
			.andRespond(MockRestResponseCreators.withSuccess(
				"[{\"name\":\"a\"},{\"name\":\"b\"}]", MediaType.APPLICATION_JSON
			));

		ToolExecutionResult result = executor.execute(tool, Map.of());

		assertThat(result.resultCount()).isEqualTo(1);
	}

	@Test
	void execute_API_KEY_인증은_헤더에_credential을_담아_전달() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "직원 정보를 조회합니다.", null,
			"https://hr.example.com/api/employees", "GET",
			"{\"properties\":{}}", null, AuthType.API_KEY, "TOOL_HR_API_KEY", 5000, 100, 1L
		);
		when(environment.getProperty("TOOL_HR_API_KEY")).thenReturn("secret-key");

		server.expect(MockRestRequestMatchers.requestTo("https://hr.example.com/api/employees"))
			.andExpect(MockRestRequestMatchers.header("X-API-Key", "secret-key"))
			.andRespond(MockRestResponseCreators.withSuccess("{}", MediaType.APPLICATION_JSON));

		executor.execute(tool, Map.of());
	}

	@Test
	void execute_credential이_없으면_ToolExecutionException() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "직원 정보를 조회합니다.", null,
			"https://hr.example.com/api/employees", "GET",
			"{\"properties\":{}}", null, AuthType.API_KEY, "TOOL_HR_API_KEY", 5000, 100, 1L
		);
		when(environment.getProperty("TOOL_HR_API_KEY")).thenReturn(null);

		assertThatThrownBy(() -> executor.execute(tool, Map.of()))
			.isInstanceOf(ToolExecutionException.class)
			.satisfies(e -> assertThat(((ToolExecutionException) e).getErrorCode()).isEqualTo("CREDENTIAL_NOT_CONFIGURED"));
	}

	@Test
	void execute_외부API_오류시_ToolExecutionException() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "직원 정보를 조회합니다.", null,
			"https://hr.example.com/api/employees", "GET",
			"{\"properties\":{}}", null, AuthType.NONE, null, 5000, 100, 1L
		);

		server.expect(MockRestRequestMatchers.requestTo("https://hr.example.com/api/employees"))
			.andRespond(MockRestResponseCreators.withServerError());

		assertThatThrownBy(() -> executor.execute(tool, Map.of()))
			.isInstanceOf(ToolExecutionException.class)
			.satisfies(e -> assertThat(((ToolExecutionException) e).getErrorCode()).isEqualTo("EXTERNAL_API_ERROR"));
	}

	@Test
	void execute_안전하지않은_endpoint면_ToolExecutionException() {
		SsrfGuard unsafeGuard = endpointUrl -> false;
		HttpApiToolExecutor unsafeExecutor = new HttpApiToolExecutor(restClientFactory, environment, unsafeGuard);
		AiTool tool = AiTool.createHttpApiTool(
			"내부망조회", "설명", null,
			"https://192.168.1.1/api", "GET",
			"{\"properties\":{}}", null, AuthType.NONE, null, 5000, 100, 1L
		);

		assertThatThrownBy(() -> unsafeExecutor.execute(tool, Map.of()))
			.isInstanceOf(ToolExecutionException.class)
			.satisfies(e -> assertThat(((ToolExecutionException) e).getErrorCode()).isEqualTo("UNSAFE_ENDPOINT"));
	}
}
```

- [ ] **Step 6: 테스트 실행해서 실패 확인**

Run: `./gradlew test --tests "com.wip.workipedia.tool.executor.HttpApiToolExecutorTest"`
Expected: FAIL — `HttpApiToolExecutor` 클래스가 없어 컴파일 에러

- [ ] **Step 7: HttpApiToolExecutor 구현**

```java
// src/main/java/com/wip/workipedia/tool/executor/HttpApiToolExecutor.java
package com.wip.workipedia.tool.executor;

import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.domain.AuthType;
import com.wip.workipedia.tool.exception.ToolExecutionException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class HttpApiToolExecutor {

	private final ToolRestClientFactory restClientFactory;
	private final Environment environment;
	private final SsrfGuard ssrfGuard;

	public ToolExecutionResult execute(AiTool tool, Map<String, Object> parameters) {
		if (!ssrfGuard.isSafe(tool.getEndpointUrl())) {
			throw new ToolExecutionException("UNSAFE_ENDPOINT", "내부망/루프백 주소로는 Tool을 실행할 수 없습니다.");
		}

		RestClient client = restClientFactory.create(tool.getTimeoutMs());
		HttpMethod method = HttpMethod.valueOf(tool.getHttpMethod());

		try {
			Object body;
			if (method == HttpMethod.GET) {
				body = client.get()
					.uri(buildGetUri(tool.getEndpointUrl(), parameters))
					.headers(headers -> applyAuth(headers, tool))
					.retrieve()
					.body(Object.class);
			} else {
				body = client.method(method)
					.uri(URI.create(tool.getEndpointUrl()))
					.headers(headers -> applyAuth(headers, tool))
					.body(parameters)
					.retrieve()
					.body(Object.class);
			}
			return buildResult(tool, body);
		} catch (RestClientException e) {
			throw new ToolExecutionException("EXTERNAL_API_ERROR", "외부 API 호출에 실패했습니다: " + e.getMessage());
		}
	}

	private URI buildGetUri(String endpointUrl, Map<String, Object> parameters) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(endpointUrl);
		parameters.forEach(builder::queryParam);
		return builder.build().encode().toUri();
	}

	private void applyAuth(HttpHeaders headers, AiTool tool) {
		AuthType authType = tool.getAuthType();
		if (authType == AuthType.NONE) {
			return;
		}

		String credential = environment.getProperty(tool.getCredentialRef());
		if (credential == null || credential.isBlank()) {
			throw new ToolExecutionException("CREDENTIAL_NOT_CONFIGURED", "Tool credential이 설정되지 않았습니다.");
		}

		switch (authType) {
			case API_KEY -> headers.set("X-API-Key", credential);
			case BEARER_TOKEN -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + credential);
			default -> throw new ToolExecutionException("AUTH_TYPE_NOT_SUPPORTED", "지원하지 않는 인증 방식입니다.");
		}
	}

	private ToolExecutionResult buildResult(AiTool tool, Object body) {
		if (body instanceof List<?> list) {
			List<?> truncated = list.size() > tool.getMaxResultCount()
				? list.subList(0, tool.getMaxResultCount())
				: list;
			return new ToolExecutionResult(truncated, truncated.size());
		}
		return new ToolExecutionResult(body, body != null ? 1 : 0);
	}
}
```

- [ ] **Step 8: 테스트 실행해서 통과 확인**

Run: `./gradlew test --tests "com.wip.workipedia.tool.executor.HttpApiToolExecutorTest"`
Expected: PASS (6 tests)

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/wip/workipedia/tool/exception src/main/java/com/wip/workipedia/tool/executor src/main/java/com/wip/workipedia/config/ToolAllowedHostProperties.java src/main/java/com/wip/workipedia/config/ToolSecurityConfig.java src/main/resources/application.yaml src/main/resources/application-local.yaml src/test/resources/application-test.yaml src/test/java/com/wip/workipedia/tool/executor/HttpApiToolExecutorTest.java src/test/java/com/wip/workipedia/tool/executor/DefaultSsrfGuardTest.java
git commit -m "feat: HTTP_API Tool 실행기 추가 (SSRF allowlist 방어 포함)"
```

---

## Task 5B: DB_QUERY Tool SQL Validator + 실행기

**설계:** DB_QUERY Tool은 AI가 SQL을 생성하지 않는다. BE에 등록된 `query_template`만 실행하고, AI는 `parameters`만 전달한다. 실행 datasource는 `tool.db.datasources`에 설정된 항목 중 `tool.db.allowed-datasources`에 명시된 키만 허용한다(allowlist에 없으면 빈 Map이라 조회 자체가 안 됨). SQL은 단일 `SELECT` 템플릿만 허용하고, 결과는 `maxResultCount`로 제한한다. read-only 계정/replica 연결은 `NamedParameterJdbcTemplate`을 datasourceKey별로 별도 `HikariDataSource`에 바인딩해 구성한다(JPA의 기본 datasource와는 분리된 별도 커넥션 풀). JSqlParser 같은 정식 SQL parser 도입은 M2 범위 밖으로 두고, 문자열 기반 검증 + read-only datasource + timeout + maxResultCount로 1차 방어한다.

**Files:**
- Create: `src/main/java/com/wip/workipedia/config/ToolDbProperties.java`
- Create: `src/main/java/com/wip/workipedia/config/ToolDbConfig.java`
- Create: `src/main/java/com/wip/workipedia/tool/service/SqlTemplateValidator.java`
- Create: `src/main/java/com/wip/workipedia/tool/executor/DbQueryToolExecutor.java`
- Modify: `src/main/resources/application.yaml`
- Test: `src/test/java/com/wip/workipedia/tool/service/SqlTemplateValidatorTest.java`
- Test: `src/test/java/com/wip/workipedia/tool/executor/DbQueryToolExecutorTest.java`

- [ ] **Step 1: ToolDbProperties, ToolDbConfig 작성 + application.yaml 설정 추가**

```java
// src/main/java/com/wip/workipedia/config/ToolDbProperties.java
package com.wip.workipedia.config;

import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tool.db")
public record ToolDbProperties(List<String> allowedDatasources, Map<String, DatasourceConfig> datasources) {

	public record DatasourceConfig(String url, String username, String password) {
	}
}
```

```java
// src/main/java/com/wip/workipedia/config/ToolDbConfig.java
package com.wip.workipedia.config;

import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
@EnableConfigurationProperties(ToolDbProperties.class)
public class ToolDbConfig {

	@Bean
	public Map<String, NamedParameterJdbcTemplate> toolJdbcTemplates(ToolDbProperties properties) {
		Set<String> allowed = properties.allowedDatasources() == null
			? Set.of()
			: new HashSet<>(properties.allowedDatasources());

		Map<String, NamedParameterJdbcTemplate> templates = new HashMap<>();
		if (properties.datasources() == null) {
			return templates;
		}

		properties.datasources().forEach((key, config) -> {
			if (!allowed.contains(key)) {
				return;
			}
			HikariDataSource dataSource = new HikariDataSource();
			dataSource.setJdbcUrl(config.url());
			dataSource.setUsername(config.username());
			dataSource.setPassword(config.password());
			dataSource.setMaximumPoolSize(2);
			dataSource.setReadOnly(true);
			templates.put(key, new NamedParameterJdbcTemplate(dataSource));
		});

		return templates;
	}
}
```

`src/main/resources/application.yaml`의 `tool.allowed-hosts` 다음에 추가 (allowlist가 비어 있으면 `datasources`에 항목이 있어도 차단 — fail-safe):

```yaml
tool:
  db:
    allowed-datasources: ${TOOL_DB_ALLOWED_DATASOURCES:}
    datasources:
      workipediaReadonly:
        url: ${TOOL_DB_READONLY_URL:}
        username: ${TOOL_DB_READONLY_USERNAME:}
        password: ${TOOL_DB_READONLY_PASSWORD:}
```

`application-local.yaml`/`application-test.yaml`에는 `tool.db.datasources`를 추가하지 않는다 — 빈 환경변수로 `HikariDataSource`를 즉시 생성하면 로컬/테스트 컨텍스트 기동 시 연결 시도가 발생할 수 있으므로, 기본값 없는 빈 `datasources` 맵(= 빈 `Map<String, NamedParameterJdbcTemplate>` 빈)으로 둔다. `DbQueryToolExecutorTest`는 Spring 컨텍스트 없이 `NamedParameterJdbcTemplate`을 직접 모킹해 검증하므로 실제 DB 연결이 필요 없다.

- [ ] **Step 2: SqlTemplateValidator에 대한 실패하는 테스트 작성**

```java
// src/test/java/com/wip/workipedia/tool/service/SqlTemplateValidatorTest.java
package com.wip.workipedia.tool.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SqlTemplateValidatorTest {

	private final SqlTemplateValidator validator = new SqlTemplateValidator();

	@Test
	void validate_SELECT와_LIMIT가_있으면_유효() {
		var result = validator.validate(
			"SELECT name, remaining_days FROM employee_vacations WHERE employee_id = :employeeId LIMIT 1"
		);

		assertThat(result.valid()).isTrue();
	}

	@Test
	void validate_named_parameter_사용가능() {
		var result = validator.validate("SELECT name FROM employee_vacations WHERE employee_id = :employeeId LIMIT 10");

		assertThat(result.valid()).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"INSERT INTO employee_vacations VALUES (1) LIMIT 1",
		"UPDATE employee_vacations SET remaining_days = 1 LIMIT 1",
		"DELETE FROM employee_vacations LIMIT 1",
		"DROP TABLE employee_vacations",
		"ALTER TABLE employee_vacations ADD COLUMN x INT",
		"TRUNCATE TABLE employee_vacations",
		"CREATE TABLE x (id INT)",
		"MERGE INTO employee_vacations USING dual ON (1=1)",
		"CALL some_procedure()",
		"EXEC some_procedure"
	})
	void validate_금지된_키워드나_SELECT가_아닌_문장은_거부(String sql) {
		assertThat(validator.validate(sql).valid()).isFalse();
	}

	@Test
	void validate_세미콜론_포함시_거부() {
		var result = validator.validate(
			"SELECT name FROM employee_vacations LIMIT 1; DROP TABLE employee_vacations"
		);

		assertThat(result.valid()).isFalse();
	}

	@Test
	void validate_라인주석_포함시_거부() {
		var result = validator.validate("SELECT name FROM employee_vacations LIMIT 1 -- comment");

		assertThat(result.valid()).isFalse();
	}

	@Test
	void validate_블록주석_포함시_거부() {
		var result = validator.validate("SELECT name /* comment */ FROM employee_vacations LIMIT 1");

		assertThat(result.valid()).isFalse();
	}

	@Test
	void validate_LIMIT_없으면_거부() {
		var result = validator.validate("SELECT name FROM employee_vacations WHERE employee_id = :employeeId");

		assertThat(result.valid()).isFalse();
	}
}
```

- [ ] **Step 3: 테스트 실행해서 실패 확인**

Run: `./gradlew test --tests "com.wip.workipedia.tool.service.SqlTemplateValidatorTest"`
Expected: FAIL — `SqlTemplateValidator` 클래스가 없어 컴파일 에러

- [ ] **Step 4: SqlTemplateValidator 구현**

```java
// src/main/java/com/wip/workipedia/tool/service/SqlTemplateValidator.java
package com.wip.workipedia.tool.service;

import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SqlTemplateValidator {

	private static final Set<String> FORBIDDEN_KEYWORDS = Set.of(
		"INSERT", "UPDATE", "DELETE", "MERGE", "ALTER", "DROP", "TRUNCATE", "CREATE", "CALL", "EXEC", "EXECUTE"
	);

	public ValidationResult validate(String queryTemplate) {
		if (queryTemplate == null || queryTemplate.isBlank()) {
			return ValidationResult.invalid("queryTemplate이 비어 있습니다.");
		}

		String trimmed = queryTemplate.trim();
		String upper = trimmed.toUpperCase();

		if (!upper.startsWith("SELECT")) {
			return ValidationResult.invalid("SELECT 쿼리만 허용됩니다.");
		}
		if (trimmed.contains(";")) {
			return ValidationResult.invalid("세미콜론(;)은 허용되지 않습니다.");
		}
		if (trimmed.contains("--") || trimmed.contains("/*") || trimmed.contains("*/")) {
			return ValidationResult.invalid("SQL 주석은 허용되지 않습니다.");
		}
		for (String keyword : FORBIDDEN_KEYWORDS) {
			if (Pattern.compile("\\b" + keyword + "\\b").matcher(upper).find()) {
				return ValidationResult.invalid("허용되지 않은 키워드입니다: " + keyword);
			}
		}
		if (!upper.contains("LIMIT")) {
			return ValidationResult.invalid("LIMIT 절이 필요합니다.");
		}

		return ValidationResult.ok();
	}

	public record ValidationResult(boolean valid, String message) {
		// 레코드 컴포넌트 valid의 자동 접근자 valid()와 이름이 충돌해 static 팩토리 메서드명은 ok()로 둔다.
		public static ValidationResult ok() {
			return new ValidationResult(true, null);
		}

		public static ValidationResult invalid(String message) {
			return new ValidationResult(false, message);
		}
	}
}
```

- [ ] **Step 5: 테스트 실행해서 통과 확인**

Run: `./gradlew test --tests "com.wip.workipedia.tool.service.SqlTemplateValidatorTest"`
Expected: PASS (15 tests — `@ParameterizedTest` 10건 포함)

- [ ] **Step 6: DbQueryToolExecutor에 대한 실패하는 테스트 작성**

```java
// src/test/java/com/wip/workipedia/tool/executor/DbQueryToolExecutorTest.java
package com.wip.workipedia.tool.executor;

import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.exception.ToolExecutionException;
import com.wip.workipedia.tool.service.SqlTemplateValidator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DbQueryToolExecutorTest {

	@Mock NamedParameterJdbcTemplate jdbcTemplate;

	private final SqlTemplateValidator sqlTemplateValidator = new SqlTemplateValidator();
	private DbQueryToolExecutor executor;

	@BeforeEach
	void setUp() {
		executor = new DbQueryToolExecutor(Map.of("workipediaReadonly", jdbcTemplate), sqlTemplateValidator);
	}

	private AiTool dbQueryTool(String datasourceKey, String queryTemplate, int maxResultCount) {
		return AiTool.createDbQueryTool(
			"휴가잔여일조회", "직원 휴가 잔여일을 조회합니다.", null,
			datasourceKey, queryTemplate, "{\"properties\":{}}", null, 3000, maxResultCount, 1L
		);
	}

	@Test
	void execute_허용된_datasource면_NamedParameterJdbcTemplate으로_실행() {
		AiTool tool = dbQueryTool(
			"workipediaReadonly",
			"SELECT name FROM employee_vacations WHERE employee_id = :employeeId LIMIT 1",
			10
		);
		given(jdbcTemplate.queryForList(tool.getQueryTemplate(), Map.of("employeeId", "E001")))
			.willReturn(List.of(Map.of("name", "홍길동")));

		ToolExecutionResult result = executor.execute(tool, Map.of("employeeId", "E001"));

		assertThat(result.resultCount()).isEqualTo(1);
	}

	@Test
	void execute_allowlist에_없는_datasourceKey면_ToolExecutionException() {
		AiTool tool = dbQueryTool("unknownDatasource", "SELECT name FROM employee_vacations LIMIT 1", 10);

		assertThatThrownBy(() -> executor.execute(tool, Map.of()))
			.isInstanceOf(ToolExecutionException.class)
			.satisfies(e -> assertThat(((ToolExecutionException) e).getErrorCode()).isEqualTo("DATASOURCE_NOT_ALLOWED"));
	}

	@Test
	void execute_SELECT가_아닌_queryTemplate이면_ToolExecutionException() {
		AiTool tool = dbQueryTool("workipediaReadonly", "DELETE FROM employee_vacations LIMIT 1", 10);

		assertThatThrownBy(() -> executor.execute(tool, Map.of()))
			.isInstanceOf(ToolExecutionException.class)
			.satisfies(e -> assertThat(((ToolExecutionException) e).getErrorCode()).isEqualTo("INVALID_QUERY_TEMPLATE"));
	}

	@Test
	void execute_결과가_maxResultCount보다_많으면_잘림() {
		AiTool tool = dbQueryTool("workipediaReadonly", "SELECT name FROM employee_vacations LIMIT 10", 1);
		given(jdbcTemplate.queryForList(tool.getQueryTemplate(), Map.of()))
			.willReturn(List.of(Map.of("name", "a"), Map.of("name", "b")));

		ToolExecutionResult result = executor.execute(tool, Map.of());

		assertThat(result.resultCount()).isEqualTo(1);
	}

	@Test
	void execute_DataAccessException_발생시_ToolExecutionException() {
		AiTool tool = dbQueryTool("workipediaReadonly", "SELECT name FROM employee_vacations LIMIT 10", 10);
		given(jdbcTemplate.queryForList(tool.getQueryTemplate(), Map.of()))
			.willThrow(new QueryTimeoutException("timeout"));

		assertThatThrownBy(() -> executor.execute(tool, Map.of()))
			.isInstanceOf(ToolExecutionException.class)
			.satisfies(e -> assertThat(((ToolExecutionException) e).getErrorCode()).isEqualTo("DB_QUERY_ERROR"));
	}
}
```

- [ ] **Step 7: 테스트 실행해서 실패 확인**

Run: `./gradlew test --tests "com.wip.workipedia.tool.executor.DbQueryToolExecutorTest"`
Expected: FAIL — `DbQueryToolExecutor` 클래스가 없어 컴파일 에러

- [ ] **Step 8: DbQueryToolExecutor 구현**

```java
// src/main/java/com/wip/workipedia/tool/executor/DbQueryToolExecutor.java
package com.wip.workipedia.tool.executor;

import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.exception.ToolExecutionException;
import com.wip.workipedia.tool.service.SqlTemplateValidator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DbQueryToolExecutor {

	private final Map<String, NamedParameterJdbcTemplate> toolJdbcTemplates;
	private final SqlTemplateValidator sqlTemplateValidator;

	public ToolExecutionResult execute(AiTool tool, Map<String, Object> parameters) {
		NamedParameterJdbcTemplate jdbcTemplate = toolJdbcTemplates.get(tool.getDatasourceKey());
		if (jdbcTemplate == null) {
			throw new ToolExecutionException(
				"DATASOURCE_NOT_ALLOWED", "허용되지 않은 datasource입니다: " + tool.getDatasourceKey()
			);
		}

		SqlTemplateValidator.ValidationResult validation = sqlTemplateValidator.validate(tool.getQueryTemplate());
		if (!validation.valid()) {
			throw new ToolExecutionException("INVALID_QUERY_TEMPLATE", validation.message());
		}

		try {
			List<Map<String, Object>> rows = jdbcTemplate.queryForList(tool.getQueryTemplate(), parameters);
			List<Map<String, Object>> truncated = rows.size() > tool.getMaxResultCount()
				? rows.subList(0, tool.getMaxResultCount())
				: rows;
			return new ToolExecutionResult(truncated, truncated.size());
		} catch (DataAccessException e) {
			throw new ToolExecutionException("DB_QUERY_ERROR", "DB 쿼리 실행에 실패했습니다: " + e.getMessage());
		}
	}
}
```

`DB_QUERY Tool은 authType=NONE만 허용`하고 `credentialRef`는 사용하지 않는다 — DB 접근 권한은 `datasourceKey`가 가리키는 read-only `HikariDataSource`(Step 1)로만 통제하고, `queryTemplate`은 `AdminAiToolService`(Task 9)에서 관리자가 등록한 값만 저장·실행하며 AI는 `parameters`만 전달한다(Task 6에서 분기).

- [ ] **Step 9: 테스트 실행해서 통과 확인**

Run: `./gradlew test --tests "com.wip.workipedia.tool.executor.DbQueryToolExecutorTest"`
Expected: PASS (5 tests)

- [ ] **Step 10: Commit**

```bash
git add src/main/java/com/wip/workipedia/config/ToolDbProperties.java src/main/java/com/wip/workipedia/config/ToolDbConfig.java src/main/java/com/wip/workipedia/tool/service/SqlTemplateValidator.java src/main/java/com/wip/workipedia/tool/executor/DbQueryToolExecutor.java src/main/resources/application.yaml src/test/java/com/wip/workipedia/tool/service/SqlTemplateValidatorTest.java src/test/java/com/wip/workipedia/tool/executor/DbQueryToolExecutorTest.java
git commit -m "feat: DB_QUERY Tool SQL 검증 및 실행기 추가"
```

---

## Task 6: ToolExecutionService + DTO

**흐름:** Tool 조회(`isDeleted = "N"` 조건 포함) → 실행 가능 여부 확인(비활성/미승인이면 거부) → 파라미터 스키마 검증(실패하면 로그 남기고 예외) → `toolType`에 따라 HTTP_API/DB_QUERY 실행기 분기 → 성공/실패 모두 `tool_execution_logs`에 감사 로그 기록(파라미터는 키만 남기고 값은 마스킹) → 실행기 실패는 HTTP 200 + `{"data": null, "errorCode": ...}`로 응답.

**Files:**
- Create: `src/main/java/com/wip/workipedia/tool/dto/ToolExecuteRequest.java`
- Create: `src/main/java/com/wip/workipedia/tool/dto/ToolExecuteResponse.java`
- Create: `src/main/java/com/wip/workipedia/tool/dto/ActiveAiToolResponse.java`
- Create: `src/main/java/com/wip/workipedia/tool/service/ToolExecutionService.java`
- Test: `src/test/java/com/wip/workipedia/tool/service/ToolExecutionServiceTest.java`

- [ ] **Step 1: DTO 3개 작성**

```java
// src/main/java/com/wip/workipedia/tool/dto/ToolExecuteRequest.java
package com.wip.workipedia.tool.dto;

import java.util.Map;

public record ToolExecuteRequest(Map<String, Object> parameters) {
}
```

```java
// src/main/java/com/wip/workipedia/tool/dto/ToolExecuteResponse.java
package com.wip.workipedia.tool.dto;

public record ToolExecuteResponse(Object data, String errorCode, String errorMessage) {

	public static ToolExecuteResponse success(Object data) {
		return new ToolExecuteResponse(data, null, null);
	}

	public static ToolExecuteResponse failure(String errorCode, String errorMessage) {
		return new ToolExecuteResponse(null, errorCode, errorMessage);
	}
}
```

```java
// src/main/java/com/wip/workipedia/tool/dto/ActiveAiToolResponse.java
package com.wip.workipedia.tool.dto;

import com.wip.workipedia.tool.domain.AiTool;

public record ActiveAiToolResponse(
	Long aiToolId,
	String toolType,
	String name,
	String description,
	String responseDescription,
	String parametersSchema
) {
	public static ActiveAiToolResponse from(AiTool tool) {
		return new ActiveAiToolResponse(
			tool.getAiToolId(),
			tool.getToolType().name(),
			tool.getName(),
			tool.getDescription(),
			tool.getResponseDescription(),
			tool.getParametersSchema()
		);
	}
}
```

- [ ] **Step 2: 실패하는 테스트 작성**

```java
// src/test/java/com/wip/workipedia/tool/service/ToolExecutionServiceTest.java
package com.wip.workipedia.tool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.domain.ApprovalStatus;
import com.wip.workipedia.tool.domain.AuthType;
import com.wip.workipedia.tool.dto.ToolExecuteResponse;
import com.wip.workipedia.tool.exception.ToolExecutionException;
import com.wip.workipedia.tool.executor.DbQueryToolExecutor;
import com.wip.workipedia.tool.executor.HttpApiToolExecutor;
import com.wip.workipedia.tool.executor.ToolExecutionResult;
import com.wip.workipedia.tool.repository.AiToolRepository;
import com.wip.workipedia.tool.repository.ToolExecutionLogRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ToolExecutionServiceTest {

	private static final String SCHEMA = "{\"properties\":{\"employeeId\":{\"type\":\"string\",\"required\":true}}}";

	@Mock AiToolRepository aiToolRepository;
	@Mock ToolExecutionLogRepository toolExecutionLogRepository;
	@Mock HttpApiToolExecutor httpApiToolExecutor;
	@Mock DbQueryToolExecutor dbQueryToolExecutor;
	@Spy ParameterSchemaValidator parameterSchemaValidator = new ParameterSchemaValidator(new ObjectMapper());
	@Spy ObjectMapper objectMapper = new ObjectMapper();

	@InjectMocks ToolExecutionService toolExecutionService;

	private AiTool executableTool() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "직원 정보를 조회합니다.", null,
			"https://hr.example.com/api/employees", "GET", SCHEMA, null,
			AuthType.NONE, null, 5000, 100, 1L
		);
		tool.changeApprovalStatus(ApprovalStatus.APPROVED, 1L);
		tool.changeActive(true, 1L);
		return tool;
	}

	@Test
	void execute_정상_실행시_성공_응답과_감사로그_기록() {
		AiTool tool = executableTool();
		given(aiToolRepository.findByAiToolIdAndIsDeleted(1L, "N")).willReturn(Optional.of(tool));
		given(httpApiToolExecutor.execute(tool, Map.of("employeeId", "E001")))
			.willReturn(new ToolExecutionResult(Map.of("name", "홍길동"), 1));

		ToolExecuteResponse response = toolExecutionService.execute("ai-server", 1L, Map.of("employeeId", "E001"));

		assertThat(response.errorCode()).isNull();
		assertThat(response.data()).isNotNull();
		verify(toolExecutionLogRepository).save(any());
	}

	@Test
	void execute_DB_QUERY이면_DbQueryToolExecutor를_호출() {
		AiTool tool = AiTool.createDbQueryTool(
			"휴가잔여일조회", "직원 휴가 잔여일을 조회합니다.", null,
			"workipediaReadonly",
			"SELECT name, remaining_days FROM employee_vacations WHERE employee_id = :employeeId LIMIT 1",
			SCHEMA, null, 3000, 10, 1L
		);
		tool.changeApprovalStatus(ApprovalStatus.APPROVED, 1L);
		tool.changeActive(true, 1L);
		given(aiToolRepository.findByAiToolIdAndIsDeleted(2L, "N")).willReturn(Optional.of(tool));
		given(dbQueryToolExecutor.execute(tool, Map.of("employeeId", "E001")))
			.willReturn(new ToolExecutionResult(java.util.List.of(Map.of("remainingDays", 3)), 1));

		ToolExecuteResponse response = toolExecutionService.execute("ai-server", 2L, Map.of("employeeId", "E001"));

		assertThat(response.errorCode()).isNull();
		verify(dbQueryToolExecutor).execute(tool, Map.of("employeeId", "E001"));
		verify(toolExecutionLogRepository).save(any());
	}

	@Test
	void execute_비활성_Tool은_AI_TOOL_NOT_EXECUTABLE_예외() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "직원 정보를 조회합니다.", null,
			"https://hr.example.com/api/employees", "GET", SCHEMA, null,
			AuthType.NONE, null, 5000, 100, 1L
		);
		given(aiToolRepository.findByAiToolIdAndIsDeleted(1L, "N")).willReturn(Optional.of(tool));

		assertThatThrownBy(() -> toolExecutionService.execute("ai-server", 1L, Map.of("employeeId", "E001")))
			.isInstanceOf(CustomException.class);
		verify(toolExecutionLogRepository).save(any());
	}

	@Test
	void execute_스키마에_안맞는_파라미터는_AI_TOOL_PARAMETER_MISMATCH_예외() {
		AiTool tool = executableTool();
		given(aiToolRepository.findByAiToolIdAndIsDeleted(1L, "N")).willReturn(Optional.of(tool));

		assertThatThrownBy(() -> toolExecutionService.execute("ai-server", 1L, Map.of()))
			.isInstanceOf(CustomException.class);
		verify(toolExecutionLogRepository).save(any());
	}

	@Test
	void execute_외부API_실패시_data_null인_실패_응답을_200으로_반환() {
		AiTool tool = executableTool();
		given(aiToolRepository.findByAiToolIdAndIsDeleted(1L, "N")).willReturn(Optional.of(tool));
		given(httpApiToolExecutor.execute(tool, Map.of("employeeId", "E001")))
			.willThrow(new ToolExecutionException("EXTERNAL_API_ERROR", "외부 API 호출에 실패했습니다."));

		ToolExecuteResponse response = toolExecutionService.execute("ai-server", 1L, Map.of("employeeId", "E001"));

		assertThat(response.data()).isNull();
		assertThat(response.errorCode()).isEqualTo("EXTERNAL_API_ERROR");
		verify(toolExecutionLogRepository).save(any());
	}

	@Test
	void execute_삭제된_Tool은_AI_TOOL_NOT_FOUND_예외() {
		given(aiToolRepository.findByAiToolIdAndIsDeleted(1L, "N")).willReturn(Optional.empty());

		assertThatThrownBy(() -> toolExecutionService.execute("ai-server", 1L, Map.of("employeeId", "E001")))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void findActiveTools_활성화되고_승인된_Tool만_반환() {
		given(aiToolRepository.findByIsActiveAndApprovalStatusAndIsDeleted("Y", ApprovalStatus.APPROVED, "N"))
			.willReturn(java.util.List.of(executableTool()));

		var result = toolExecutionService.findActiveTools();

		assertThat(result).hasSize(1);
	}
}
```

- [ ] **Step 3: 테스트 실행해서 실패 확인**

Run: `./gradlew test --tests "com.wip.workipedia.tool.service.ToolExecutionServiceTest"`
Expected: FAIL — `ToolExecutionService` 클래스가 없어 컴파일 에러

- [ ] **Step 4: ToolExecutionService 구현**

```java
// src/main/java/com/wip/workipedia/tool/service/ToolExecutionService.java
package com.wip.workipedia.tool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.domain.ApprovalStatus;
import com.wip.workipedia.tool.domain.ToolExecutionLog;
import com.wip.workipedia.tool.domain.ToolType;
import com.wip.workipedia.tool.dto.ActiveAiToolResponse;
import com.wip.workipedia.tool.dto.ToolExecuteResponse;
import com.wip.workipedia.tool.exception.ToolExecutionException;
import com.wip.workipedia.tool.executor.DbQueryToolExecutor;
import com.wip.workipedia.tool.executor.HttpApiToolExecutor;
import com.wip.workipedia.tool.executor.ToolExecutionResult;
import com.wip.workipedia.tool.repository.AiToolRepository;
import com.wip.workipedia.tool.repository.ToolExecutionLogRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ToolExecutionService {

	private final AiToolRepository aiToolRepository;
	private final ToolExecutionLogRepository toolExecutionLogRepository;
	private final HttpApiToolExecutor httpApiToolExecutor;
	private final DbQueryToolExecutor dbQueryToolExecutor;
	private final ParameterSchemaValidator parameterSchemaValidator;
	private final ObjectMapper objectMapper;

	@Transactional(readOnly = true)
	public List<ActiveAiToolResponse> findActiveTools() {
		return aiToolRepository.findByIsActiveAndApprovalStatusAndIsDeleted("Y", ApprovalStatus.APPROVED, "N")
			.stream()
			.map(ActiveAiToolResponse::from)
			.toList();
	}

	@Transactional
	public ToolExecuteResponse execute(String caller, Long aiToolId, Map<String, Object> parameters) {
		AiTool tool = aiToolRepository.findByAiToolIdAndIsDeleted(aiToolId, "N")
			.orElseThrow(() -> new CustomException(ErrorType.AI_TOOL_NOT_FOUND));

		long startedAt = System.currentTimeMillis();
		String maskedParametersJson = maskParametersAsJson(parameters);

		if (!tool.isExecutable()) {
			recordLog(tool, caller, maskedParametersJson, null, elapsed(startedAt), false, "AI_TOOL_NOT_EXECUTABLE");
			throw new CustomException(ErrorType.AI_TOOL_NOT_EXECUTABLE);
		}

		ParameterSchemaValidator.ValidationResult validation =
			parameterSchemaValidator.validate(tool.getParametersSchema(), parameters);
		if (!validation.valid()) {
			recordLog(tool, caller, maskedParametersJson, null, elapsed(startedAt), false, "AI_TOOL_PARAMETER_MISMATCH");
			throw new CustomException(ErrorType.AI_TOOL_PARAMETER_MISMATCH, validation.message());
		}

		try {
			ToolExecutionResult result = executeByType(tool, parameters);
			recordLog(tool, caller, maskedParametersJson, result.resultCount(), elapsed(startedAt), true, null);
			return ToolExecuteResponse.success(result.data());
		} catch (ToolExecutionException e) {
			recordLog(tool, caller, maskedParametersJson, null, elapsed(startedAt), false, e.getErrorCode());
			return ToolExecuteResponse.failure(e.getErrorCode(), e.getMessage());
		}
	}

	private ToolExecutionResult executeByType(AiTool tool, Map<String, Object> parameters) {
		if (tool.getToolType() == ToolType.HTTP_API) {
			return httpApiToolExecutor.execute(tool, parameters);
		}
		if (tool.getToolType() == ToolType.DB_QUERY) {
			return dbQueryToolExecutor.execute(tool, parameters);
		}
		throw new CustomException(ErrorType.AI_TOOL_INVALID_TYPE);
	}

	private void recordLog(
		AiTool tool, String caller, String maskedParametersJson,
		Integer resultCount, long durationMs, boolean success, String errorCode
	) {
		toolExecutionLogRepository.save(ToolExecutionLog.of(
			tool.getAiToolId(), caller, maskedParametersJson, resultCount, durationMs, success, errorCode
		));
	}

	private long elapsed(long startedAt) {
		return System.currentTimeMillis() - startedAt;
	}

	private String maskParametersAsJson(Map<String, Object> parameters) {
		if (parameters == null || parameters.isEmpty()) {
			return "{}";
		}
		Map<String, Object> masked = new LinkedHashMap<>();
		parameters.keySet().forEach(key -> masked.put(key, "***"));
		try {
			return objectMapper.writeValueAsString(masked);
		} catch (Exception e) {
			return "{}";
		}
	}
}
```

- [ ] **Step 5: 테스트 실행해서 통과 확인**

Run: `./gradlew test --tests "com.wip.workipedia.tool.service.ToolExecutionServiceTest"`
Expected: PASS (6 tests)

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/wip/workipedia/tool/dto src/main/java/com/wip/workipedia/tool/service/ToolExecutionService.java src/test/java/com/wip/workipedia/tool/service/ToolExecutionServiceTest.java
git commit -m "feat: Tool 실행 서비스 및 감사 로그 기록 추가"
```

---

## Task 7: 내부 API 인증 (X-Internal-Api-Key)

**리뷰 반영(기본값 위험):** base `application.yaml`에 `INTERNAL_API_KEY` 기본값(`local-dev-internal-key`)을 두면 운영 환경에서 env var를 빠뜨려도 고정 키로 내부 API가 열린다는 코드 리뷰 지적을 반영한다. base `application.yaml`은 기본값 없이 `${INTERNAL_API_KEY}`만 두어 env var 미설정 시 애플리케이션 기동 자체가 실패하게 하고, 로컬/테스트 전용 기본값은 `application-local.yaml`/`application-test.yaml`에만 둔다.

**Files:**
- Create: `src/main/java/com/wip/workipedia/config/InternalApiProperties.java`
- Create: `src/main/java/com/wip/workipedia/config/InternalApiConfig.java`
- Create: `src/main/java/com/wip/workipedia/common/security/InternalApiKeyFilter.java`
- Modify: `src/main/java/com/wip/workipedia/config/SecurityConfig.java`
- Modify: `src/main/resources/application.yaml`
- Modify: `src/main/resources/application-local.yaml`
- Modify: `src/test/resources/application-test.yaml`
- Test: `src/test/java/com/wip/workipedia/common/security/InternalApiKeyFilterTest.java`

- [ ] **Step 1: InternalApiProperties, InternalApiConfig 작성**

```java
// src/main/java/com/wip/workipedia/config/InternalApiProperties.java
package com.wip.workipedia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("internal")
public record InternalApiProperties(String apiKey) {
}
```

```java
// src/main/java/com/wip/workipedia/config/InternalApiConfig.java
package com.wip.workipedia.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(InternalApiProperties.class)
public class InternalApiConfig {
}
```

- [ ] **Step 2: application.yaml / application-local.yaml / application-test.yaml에 internal.api-key 추가**

`src/main/resources/application.yaml`의 `ai:` 섹션 바로 다음에 추가 (기본값을 두지 않아 env var 미설정 시 기동 실패):

```yaml
internal:
  api-key: ${INTERNAL_API_KEY}
```

`src/main/resources/application-local.yaml` 맨 아래에 추가 (로컬 개발 전용 기본값):

```yaml
internal:
  api-key: ${INTERNAL_API_KEY:local-dev-internal-key}
```

`src/test/resources/application-test.yaml` 맨 아래에 추가 (테스트 전용 고정값):

```yaml
internal:
  api-key: test-internal-api-key
```

- [ ] **Step 3: 실패하는 테스트 작성 (필터를 Spring 컨텍스트 없이 직접 검증)**

```java
// src/test/java/com/wip/workipedia/common/security/InternalApiKeyFilterTest.java
package com.wip.workipedia.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.config.InternalApiProperties;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class InternalApiKeyFilterTest {

	private final InternalApiProperties properties = new InternalApiProperties("correct-key");
	private final InternalApiKeyFilter filter = new InternalApiKeyFilter(properties, new ObjectMapper());

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void doFilterInternal_internal경로가_아니면_그대로_통과() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/ai-tools");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilterInternal(request, response, chain);

		verify(chain).doFilter(request, response);
	}

	@Test
	void doFilterInternal_올바른_키면_통과하고_인증정보_설정() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/internal/ai-tools/active");
		request.addHeader("X-Internal-Api-Key", "correct-key");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilterInternal(request, response, chain);

		verify(chain).doFilter(request, response);
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
	}

	@Test
	void doFilterInternal_키가_틀리면_401이고_체인_호출안함() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/internal/ai-tools/active");
		request.addHeader("X-Internal-Api-Key", "wrong-key");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilterInternal(request, response, chain);

		assertThat(response.getStatus()).isEqualTo(401);
		verifyNoInteractions(chain);
	}
}
```

- [ ] **Step 4: 테스트 실행해서 실패 확인**

Run: `./gradlew test --tests "com.wip.workipedia.common.security.InternalApiKeyFilterTest"`
Expected: FAIL — `InternalApiKeyFilter` 클래스가 없어 컴파일 에러

- [ ] **Step 5: InternalApiKeyFilter 구현**

```java
// src/main/java/com/wip/workipedia/common/security/InternalApiKeyFilter.java
package com.wip.workipedia.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.config.InternalApiProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class InternalApiKeyFilter extends OncePerRequestFilter {

	private static final String INTERNAL_PATH_PREFIX = "/api/v1/internal/";
	private static final String API_KEY_HEADER = "X-Internal-Api-Key";

	private final InternalApiProperties internalApiProperties;
	private final ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		if (!request.getRequestURI().startsWith(INTERNAL_PATH_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		String apiKey = request.getHeader(API_KEY_HEADER);
		if (apiKey == null || !apiKey.equals(internalApiProperties.apiKey())) {
			writeUnauthorized(response);
			return;
		}

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			"ai-server",
			null,
			List.of(new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE"))
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		filterChain.doFilter(request, response);
	}

	private void writeUnauthorized(HttpServletResponse response) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write(objectMapper.writeValueAsString(
			new ErrorBody(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED", "내부 API 인증에 실패했습니다.", null)
		));
	}

	private record ErrorBody(int code, String status, String message, Object data) {
	}
}
```

- [ ] **Step 6: 테스트 실행해서 통과 확인**

Run: `./gradlew test --tests "com.wip.workipedia.common.security.InternalApiKeyFilterTest"`
Expected: PASS (3 tests)

- [ ] **Step 7: SecurityConfig에 필터와 경로 등록**

`SecurityConfig.java`에 필드 추가 (`private final JwtFilter jwtFilter;` 다음 줄):

```java
	private final InternalApiKeyFilter internalApiKeyFilter;
```

`authorizeHttpRequests` 블록에서 `.requestMatchers("/api/v1/faq/**").permitAll()` 다음 줄에 추가:

```java
						.requestMatchers("/api/v1/internal/**").permitAll()
```

`.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)` 다음 줄에 추가:

```java
				.addFilterBefore(internalApiKeyFilter, JwtFilter.class)
```

- [ ] **Step 8: 전체 컴파일 및 기존 보안 테스트 확인**

Run: `./gradlew compileJava test --tests "com.wip.workipedia.admin.flashchat.controller.AdminFlashChatControllerTest"`
Expected: BUILD SUCCESSFUL — 기존 보안 관련 테스트에 영향 없음

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/wip/workipedia/config/InternalApiProperties.java src/main/java/com/wip/workipedia/config/InternalApiConfig.java src/main/java/com/wip/workipedia/common/security/InternalApiKeyFilter.java src/main/java/com/wip/workipedia/config/SecurityConfig.java src/main/resources/application.yaml src/test/java/com/wip/workipedia/common/security/InternalApiKeyFilterTest.java
git commit -m "feat: 내부 API 인증 필터(X-Internal-Api-Key) 추가"
```

---

## Task 8: InternalAiToolController

**Files:**
- Create: `src/main/java/com/wip/workipedia/tool/controller/InternalAiToolController.java`
- Test: `src/test/java/com/wip/workipedia/tool/controller/InternalAiToolControllerTest.java`

- [ ] **Step 1: 실패하는 테스트 작성 (`@WebMvcTest`, 보안 필터 제외 — 기존 AdminFlashChatControllerTest 패턴과 동일)**

```java
// src/test/java/com/wip/workipedia/tool/controller/InternalAiToolControllerTest.java
package com.wip.workipedia.tool.controller;

import com.wip.workipedia.common.security.InternalApiKeyFilter;
import com.wip.workipedia.common.security.JwtFilter;
import com.wip.workipedia.common.security.JwtProvider;
import com.wip.workipedia.tool.dto.ActiveAiToolResponse;
import com.wip.workipedia.tool.dto.ToolExecuteResponse;
import com.wip.workipedia.tool.service.ToolExecutionService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
	value = InternalAiToolController.class,
	excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
	excludeFilters = @ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE,
		classes = {JwtFilter.class, JwtProvider.class, InternalApiKeyFilter.class}
	)
)
class InternalAiToolControllerTest {

	@Autowired MockMvc mockMvc;
	@MockitoBean ToolExecutionService toolExecutionService;

	@Test
	void getActiveTools_활성_Tool_목록_반환() throws Exception {
		given(toolExecutionService.findActiveTools())
			.willReturn(List.of(new ActiveAiToolResponse(1L, "HTTP_API", "직원정보조회", "설명", "응답설명", "{}")));

		mockMvc.perform(get("/api/v1/internal/ai-tools/active"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].name").value("직원정보조회"));
	}

	@Test
	void execute_파라미터를_전달해서_실행_결과_반환() throws Exception {
		given(toolExecutionService.execute(eq("ai-server"), eq(1L), eq(Map.of("employeeId", "E001"))))
			.willReturn(ToolExecuteResponse.success(Map.of("name", "홍길동")));

		mockMvc.perform(post("/api/v1/internal/ai-tools/1/execute")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"parameters": {"employeeId": "E001"}}
						"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.errorCode").doesNotExist());

		verify(toolExecutionService).execute("ai-server", 1L, Map.of("employeeId", "E001"));
	}
}
```

- [ ] **Step 2: 테스트 실행해서 실패 확인**

Run: `./gradlew test --tests "com.wip.workipedia.tool.controller.InternalAiToolControllerTest"`
Expected: FAIL — `InternalAiToolController` 클래스가 없어 컴파일 에러

- [ ] **Step 3: InternalAiToolController 구현**

```java
// src/main/java/com/wip/workipedia/tool/controller/InternalAiToolController.java
package com.wip.workipedia.tool.controller;

import com.wip.workipedia.tool.dto.ActiveAiToolResponse;
import com.wip.workipedia.tool.dto.ToolExecuteRequest;
import com.wip.workipedia.tool.dto.ToolExecuteResponse;
import com.wip.workipedia.tool.service.ToolExecutionService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/ai-tools")
@RequiredArgsConstructor
public class InternalAiToolController {

	private static final String CALLER = "ai-server";

	private final ToolExecutionService toolExecutionService;

	@GetMapping("/active")
	public ResponseEntity<List<ActiveAiToolResponse>> getActiveTools() {
		return ResponseEntity.ok(toolExecutionService.findActiveTools());
	}

	@PostMapping("/{aiToolId}/execute")
	public ResponseEntity<ToolExecuteResponse> execute(
		@PathVariable Long aiToolId,
		@RequestBody ToolExecuteRequest request
	) {
		Map<String, Object> parameters = request.parameters() != null ? request.parameters() : Map.of();
		return ResponseEntity.ok(toolExecutionService.execute(CALLER, aiToolId, parameters));
	}
}
```

- [ ] **Step 4: 테스트 실행해서 통과 확인**

Run: `./gradlew test --tests "com.wip.workipedia.tool.controller.InternalAiToolControllerTest"`
Expected: PASS (2 tests)

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/wip/workipedia/tool/controller/InternalAiToolController.java src/test/java/com/wip/workipedia/tool/controller/InternalAiToolControllerTest.java
git commit -m "feat: 내부 Tool 실행/활성목록 API 추가"
```

---

## Task 9: AdminAiToolController/Service (등록·조회·설정 변경)

**범위:** M2에서는 `toolType=HTTP_API`와 `toolType=DB_QUERY`를 등록 허용한다. HTTP_API는 `GET` 고정이며, `endpointUrl`, 요청 파라미터 정의, 인증 설정을 저장한다. DB_QUERY는 `authType=NONE`이며, 신규 `datasourceKey/queryTemplate` 직접 입력 대신 DB Catalog 기반의 `datasourceId`, `tableId`, select/filter 설정으로 BE가 queryTemplate을 생성한다. `responseDescription`, `timeoutMs`는 화면 입력에서 제외하고 BE 기본값을 사용한다.

**리뷰 반영(SSRF allowlist를 등록 시점에도 강제):** Task 5에서 만든 `SsrfGuard`를 재사용해 `endpointUrl`이 `TOOL_ALLOWED_HOSTS`에 없는 host를 가리키면 등록(`create`)·수정(`update`, `endpointUrl` 변경 시) 시점에 즉시 거부한다. 실행 시점(Task 6) 검증과 동일한 정책을 등록 시점에도 적용해, allowlist에 없는 host는 애초에 저장조차 되지 않게 한다.

**Files:**
- Create: `src/main/java/com/wip/workipedia/admin/aitool/dto/AiToolCreateRequest.java`
- Create: `src/main/java/com/wip/workipedia/admin/aitool/dto/AiToolUpdateRequest.java`
- Create: `src/main/java/com/wip/workipedia/admin/aitool/dto/AiToolResponse.java`
- Create: `src/main/java/com/wip/workipedia/admin/aitool/service/AdminAiToolService.java`
- Create: `src/main/java/com/wip/workipedia/admin/aitool/controller/AdminAiToolController.java`
- Test: `src/test/java/com/wip/workipedia/admin/aitool/service/AdminAiToolServiceTest.java`
- Test: `src/test/java/com/wip/workipedia/admin/aitool/controller/AdminAiToolControllerTest.java`

- [ ] **Step 1: DTO 3개 작성**

```java
// src/main/java/com/wip/workipedia/admin/aitool/dto/AiToolCreateRequest.java
package com.wip.workipedia.admin.aitool.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiToolCreateRequest(
	@NotBlank @Size(max = 100) String name,
	@NotBlank @Size(max = 1000) String description,
	@NotBlank String toolType,
	@Size(max = 1000) String endpointUrl,
	String httpMethod,
	List<ToolParameterRequest> parameters,
	Long datasourceId,
	Long tableId,
	List<String> selectColumns,
	List<QueryFilterRequest> filters,
	Integer limit,
	@NotBlank String parametersSchema,
	String responseSchema,
	@NotBlank String authType,
	String authHeaderName,
	String credentialRef,
	@Min(1) @Max(1000) int maxResultCount
) {
}
```

```java
public record ToolParameterRequest(
	@NotBlank String name,
	@NotBlank String location, // PATH, QUERY, HEADER
	@NotBlank String type,     // string, number, boolean
	boolean required,
	@Size(max = 500) String description,
	String exampleValue
) {
}
```

```java
public record QueryFilterRequest(
	@NotBlank String columnName,
	@NotBlank String operator,
	@NotBlank String parameterName,
	@NotBlank String parameterType,
	boolean required
) {
}
```

```java
// src/main/java/com/wip/workipedia/admin/aitool/dto/AiToolUpdateRequest.java
package com.wip.workipedia.admin.aitool.dto;

public record AiToolUpdateRequest(
	String description,
	String endpointUrl,
	String httpMethod,
	List<ToolParameterRequest> parameters,
	Long datasourceId,
	Long tableId,
	List<String> selectColumns,
	List<QueryFilterRequest> filters,
	Integer limit,
	String parametersSchema,
	String responseSchema,
	String authType,
	String authHeaderName,
	String credentialRef,
	Integer maxResultCount,
	String approvalStatus,
	Boolean active
) {
}
```

```java
// src/main/java/com/wip/workipedia/admin/aitool/dto/AiToolResponse.java
package com.wip.workipedia.admin.aitool.dto;

import com.wip.workipedia.tool.domain.AiTool;
import java.time.LocalDateTime;

public record AiToolResponse(
	Long aiToolId,
	String name,
	String description,
	String toolType,
	String endpointUrl,
	String httpMethod,
	Long datasourceId,
	Long tableId,
	String queryTemplate,
	String authType,
	int maxResultCount,
	boolean active,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static AiToolResponse from(AiTool tool) {
		return new AiToolResponse(
			tool.getAiToolId(),
			tool.getName(),
			tool.getDescription(),
			tool.getToolType().name(),
			tool.getEndpointUrl(),
			tool.getHttpMethod(),
			tool.getDatasourceId(),
			tool.getTableId(),
			tool.getQueryTemplate(),
			tool.getAuthType().name(),
			tool.getMaxResultCount(),
			tool.isActive(),
			tool.getCreatedAt(),
			tool.getUpdatedAt()
		);
	}
}
```

- [ ] **Step 2: AdminAiToolService에 대한 실패하는 테스트 작성**

```java
// src/test/java/com/wip/workipedia/admin/aitool/service/AdminAiToolServiceTest.java
package com.wip.workipedia.admin.aitool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.admin.aitool.dto.AiToolCreateRequest;
import com.wip.workipedia.admin.aitool.dto.AiToolResponse;
import com.wip.workipedia.admin.aitool.dto.AiToolUpdateRequest;
import com.wip.workipedia.admin.domain.AdminLog;
import com.wip.workipedia.admin.repository.AdminLogRepository;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.domain.ApprovalStatus;
import com.wip.workipedia.tool.domain.AuthType;
import com.wip.workipedia.tool.executor.SsrfGuard;
import com.wip.workipedia.tool.repository.AiToolRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminAiToolServiceTest {

	@Mock AiToolRepository aiToolRepository;
	@Mock AdminLogRepository adminLogRepository;
	@Spy ObjectMapper objectMapper = new ObjectMapper();

	private boolean ssrfSafe = true;
	private final SsrfGuard ssrfGuard = endpointUrl -> ssrfSafe;

	private AdminAiToolService adminAiToolService;

	@BeforeEach
	void setUp() {
		adminAiToolService = new AdminAiToolService(aiToolRepository, adminLogRepository, objectMapper, ssrfGuard);
	}

	private AiToolCreateRequest createRequest() {
		return new AiToolCreateRequest(
			"직원정보조회", "직원 정보를 조회합니다.", "응답 필드: name(이름)",
			"HTTP_API", "https://hr.example.com/api/employees", "GET",
			null, null,
			"{\"properties\":{\"employeeId\":{\"type\":\"string\",\"required\":true}}}",
			null, "API_KEY", "TOOL_HR_API_KEY", 5000, 100
		);
	}

	@Test
	void create_HTTP_API_등록_성공시_AdminLog_기록() {
		AiToolCreateRequest request = createRequest();

		AiToolResponse response = adminAiToolService.create(1L, request);

		assertThat(response.name()).isEqualTo("직원정보조회");
		assertThat(response.approvalStatus()).isEqualTo("DRAFT");
		assertThat(response.active()).isFalse();
		verify(aiToolRepository).save(any(AiTool.class));
		verify(adminLogRepository).save(any(AdminLog.class));
	}

	@Test
	void create_DB_QUERY_등록_성공시_AdminLog_기록() {
		AiToolCreateRequest request = new AiToolCreateRequest(
			"휴가잔여일조회", "설명", "응답설명", "DB_QUERY", null, null,
			"workipediaReadonly",
			"SELECT name, remaining_days FROM employee_vacations WHERE employee_id = :employeeId LIMIT 1",
			"{\"properties\":{}}", null, "NONE", null, 5000, 100
		);

		AiToolResponse response = adminAiToolService.create(1L, request);

		assertThat(response.toolType()).isEqualTo("DB_QUERY");
		assertThat(response.approvalStatus()).isEqualTo("DRAFT");
		verify(aiToolRepository).save(any(AiTool.class));
		verify(adminLogRepository).save(any(AdminLog.class));
	}

	@Test
	void create_DB_QUERY인데_datasourceKey가_없으면_거부() {
		AiToolCreateRequest request = new AiToolCreateRequest(
			"휴가잔여일조회", "설명", "응답설명", "DB_QUERY", null, null,
			null, "SELECT name FROM employee_vacations LIMIT 1",
			"{\"properties\":{}}", null, "NONE", null, 5000, 100
		);

		assertThatThrownBy(() -> adminAiToolService.create(1L, request))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void create_DB_QUERY인데_queryTemplate이_없으면_거부() {
		AiToolCreateRequest request = new AiToolCreateRequest(
			"휴가잔여일조회", "설명", "응답설명", "DB_QUERY", null, null,
			"workipediaReadonly", null,
			"{\"properties\":{}}", null, "NONE", null, 5000, 100
		);

		assertThatThrownBy(() -> adminAiToolService.create(1L, request))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void create_authType_API_KEY인데_credentialRef_없으면_거부() {
		AiToolCreateRequest request = new AiToolCreateRequest(
			"직원정보조회", "설명", "응답설명", "HTTP_API", "https://hr.example.com", "GET",
			null, null,
			"{\"properties\":{}}", null, "API_KEY", null, 5000, 100
		);

		assertThatThrownBy(() -> adminAiToolService.create(1L, request))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void create_OAUTH2_인증타입은_M2범위에서_거부() {
		AiToolCreateRequest request = new AiToolCreateRequest(
			"직원정보조회", "설명", "응답설명", "HTTP_API", "https://hr.example.com", "GET",
			null, null,
			"{\"properties\":{}}", null, "OAUTH2", "ref", 5000, 100
		);

		assertThatThrownBy(() -> adminAiToolService.create(1L, request))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void create_allowlist에_없는_endpoint_host는_AI_TOOL_UNSAFE_ENDPOINT_예외() {
		ssrfSafe = false;
		AiToolCreateRequest request = createRequest();

		assertThatThrownBy(() -> adminAiToolService.create(1L, request))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void update_active를_true로_변경() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "설명", null, "https://hr.example.com", "GET",
			"{\"properties\":{}}", null, AuthType.NONE, null, 5000, 100, 1L
		);
		given(aiToolRepository.findById(1L)).willReturn(Optional.of(tool));

		AiToolResponse response = adminAiToolService.update(
			1L, 1L,
			new AiToolUpdateRequest(null, null, null, null, null, null, null, null, null, null, null, null, "APPROVED", true)
		);

		assertThat(response.approvalStatus()).isEqualTo("APPROVED");
		assertThat(response.active()).isTrue();
	}

	@Test
	void update_존재하지않는_Tool은_AI_TOOL_NOT_FOUND() {
		given(aiToolRepository.findById(99L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> adminAiToolService.update(
			1L, 99L,
			new AiToolUpdateRequest(null, null, null, null, null, null, null, null, null, null, null, null, null, null)
		)).isInstanceOf(CustomException.class);
	}

	@Test
	void update_allowlist에_없는_endpointUrl로_변경시_AI_TOOL_UNSAFE_ENDPOINT_예외() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "설명", null, "https://hr.example.com", "GET",
			"{\"properties\":{}}", null, AuthType.NONE, null, 5000, 100, 1L
		);
		given(aiToolRepository.findById(1L)).willReturn(Optional.of(tool));
		ssrfSafe = false;

		assertThatThrownBy(() -> adminAiToolService.update(
			1L, 1L,
			new AiToolUpdateRequest(null, null, "https://other.example.com", null, null, null, null, null, null, null, null, null, null, null)
		)).isInstanceOf(CustomException.class);
	}

	@Test
	void update_HTTP_API_Tool에_datasourceKey_설정시_거부() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "설명", null, "https://hr.example.com", "GET",
			"{\"properties\":{}}", null, AuthType.NONE, null, 5000, 100, 1L
		);
		given(aiToolRepository.findById(1L)).willReturn(Optional.of(tool));

		assertThatThrownBy(() -> adminAiToolService.update(
			1L, 1L,
			new AiToolUpdateRequest(null, null, null, null, "workipediaReadonly", null, null, null, null, null, null, null, null, null)
		)).isInstanceOf(CustomException.class);
	}

	@Test
	void update_DB_QUERY_Tool에_endpointUrl_설정시_거부() {
		AiTool tool = AiTool.createDbQueryTool(
			"휴가잔여일조회", "설명", null, "workipediaReadonly",
			"SELECT name FROM employee_vacations LIMIT 1",
			"{\"properties\":{}}", null, 3000, 10, 1L
		);
		given(aiToolRepository.findById(2L)).willReturn(Optional.of(tool));

		assertThatThrownBy(() -> adminAiToolService.update(
			1L, 2L,
			new AiToolUpdateRequest(null, null, "https://hr.example.com", null, null, null, null, null, null, null, null, null, null, null)
		)).isInstanceOf(CustomException.class);
	}
}
```

- [ ] **Step 3: 테스트 실행해서 실패 확인**

Run: `./gradlew test --tests "com.wip.workipedia.admin.aitool.service.AdminAiToolServiceTest"`
Expected: FAIL — `AdminAiToolService` 클래스가 없어 컴파일 에러

- [ ] **Step 4: AdminAiToolService 구현**

```java
// src/main/java/com/wip/workipedia/admin/aitool/service/AdminAiToolService.java
package com.wip.workipedia.admin.aitool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.workipedia.admin.aitool.dto.AiToolCreateRequest;
import com.wip.workipedia.admin.aitool.dto.AiToolResponse;
import com.wip.workipedia.admin.aitool.dto.AiToolUpdateRequest;
import com.wip.workipedia.admin.domain.AdminLog;
import com.wip.workipedia.admin.repository.AdminLogRepository;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.domain.ApprovalStatus;
import com.wip.workipedia.tool.domain.AuthType;
import com.wip.workipedia.tool.domain.ToolType;
import com.wip.workipedia.tool.executor.SsrfGuard;
import com.wip.workipedia.tool.repository.AiToolRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAiToolService {

	private static final Set<String> SUPPORTED_AUTH_TYPES = Set.of("NONE", "API_KEY", "BEARER_TOKEN");

	private final AiToolRepository aiToolRepository;
	private final AdminLogRepository adminLogRepository;
	private final ObjectMapper objectMapper;
	private final SsrfGuard ssrfGuard;

	@Transactional(readOnly = true)
	public PageResponse<AiToolResponse> findAll(Pageable pageable) {
		return PageResponse.from(aiToolRepository.findByIsDeleted("N", pageable).map(AiToolResponse::from));
	}

	@Transactional
	public AiToolResponse create(Long adminUserId, AiToolCreateRequest request) {
		validateToolType(request.toolType());
		AuthType authType = parseAuthType(request.authType());
		validateJsonSchema(request.parametersSchema());
		AiTool tool = buildTool(adminUserId, request, authType);
		aiToolRepository.save(tool);

		adminLogRepository.save(AdminLog.of(
			adminUserId, "AI_TOOL_CREATE", "AI_TOOL",
			"AI Tool 등록: " + tool.getName(),
			String.format("{\"aiToolId\":%s,\"toolType\":\"%s\"}", tool.getAiToolId(), tool.getToolType())
		));

		return AiToolResponse.from(tool);
	}

	private AiTool buildTool(Long adminUserId, AiToolCreateRequest request, AuthType authType) {
		if ("HTTP_API".equals(request.toolType())) {
			validateHttpApiConfig(request.endpointUrl(), request.httpMethod());
			validateCredentialRef(authType, request.credentialRef());
			validateEndpointHost(request.endpointUrl());
			return AiTool.createHttpApiTool(
				request.name(), request.description(), request.responseDescription(),
				request.endpointUrl(), request.httpMethod(), request.parametersSchema(), request.responseSchema(),
				authType, request.credentialRef(), request.timeoutMs(), request.maxResultCount(), adminUserId
			);
		}

		validateDbQueryConfig(authType, request.datasourceKey(), request.queryTemplate());
		return AiTool.createDbQueryTool(
			request.name(), request.description(), request.responseDescription(),
			request.datasourceKey(), request.queryTemplate(), request.parametersSchema(), request.responseSchema(),
			request.timeoutMs(), request.maxResultCount(), adminUserId
		);
	}

	@Transactional
	public AiToolResponse update(Long adminUserId, Long aiToolId, AiToolUpdateRequest request) {
		AiTool tool = findTool(aiToolId);
		validateUpdateAgainstToolType(tool.getToolType(), request);

		AuthType authType = request.authType() != null ? parseAuthType(request.authType()) : null;
		if (authType != null) {
			String credentialRef = request.credentialRef() != null ? request.credentialRef() : tool.getCredentialRef();
			validateCredentialRef(authType, credentialRef);
		}
		if (request.parametersSchema() != null) {
			validateJsonSchema(request.parametersSchema());
		}
		if (request.endpointUrl() != null) {
			validateEndpointHost(request.endpointUrl());
		}

		tool.updateConfig(
			request.description(), request.responseDescription(), request.endpointUrl(), request.httpMethod(),
			request.datasourceKey(), request.queryTemplate(), request.parametersSchema(), request.responseSchema(),
			authType, request.credentialRef(), request.timeoutMs(), request.maxResultCount(), adminUserId
		);

		if (request.approvalStatus() != null) {
			tool.changeApprovalStatus(parseApprovalStatus(request.approvalStatus()), adminUserId);
		}
		if (request.active() != null) {
			tool.changeActive(request.active(), adminUserId);
		}

		adminLogRepository.save(AdminLog.of(
			adminUserId, "AI_TOOL_UPDATE", "AI_TOOL",
			"AI Tool 설정 변경: " + tool.getName(),
			String.format("{\"aiToolId\":%s}", tool.getAiToolId())
		));

		return AiToolResponse.from(tool);
	}

	private AiTool findTool(Long aiToolId) {
		return aiToolRepository.findById(aiToolId)
			.orElseThrow(() -> new CustomException(ErrorType.AI_TOOL_NOT_FOUND));
	}

	private void validateToolType(String toolType) {
		if (!"HTTP_API".equals(toolType) && !"DB_QUERY".equals(toolType)) {
			throw new CustomException(ErrorType.AI_TOOL_INVALID_TYPE, "M2 범위에서는 HTTP_API/DB_QUERY Tool만 등록할 수 있습니다.");
		}
	}

	private void validateHttpApiConfig(String endpointUrl, String httpMethod) {
		if (endpointUrl == null || endpointUrl.isBlank() || httpMethod == null || httpMethod.isBlank()) {
			throw new CustomException(ErrorType.BAD_REQUEST, "HTTP_API Tool은 endpointUrl과 httpMethod가 필요합니다.");
		}
	}

	private void validateDbQueryConfig(AuthType authType, String datasourceKey, String queryTemplate) {
		if (authType != AuthType.NONE) {
			throw new CustomException(ErrorType.AI_TOOL_INVALID_AUTH_TYPE, "DB_QUERY Tool은 authType=NONE만 허용합니다.");
		}
		if (datasourceKey == null || datasourceKey.isBlank()) {
			throw new CustomException(ErrorType.BAD_REQUEST, "DB_QUERY Tool은 datasourceKey가 필요합니다.");
		}
		if (queryTemplate == null || queryTemplate.isBlank()) {
			throw new CustomException(ErrorType.BAD_REQUEST, "DB_QUERY Tool은 queryTemplate이 필요합니다.");
		}
	}

	// HTTP_API/DB_QUERY 필드가 섞여서 ck_ai_tools_type_configuration DB 제약 위반(처리되지 않은 SQL 예외)으로 이어지지 않게 미리 막는다.
	private void validateUpdateAgainstToolType(ToolType toolType, AiToolUpdateRequest request) {
		if (toolType == ToolType.HTTP_API && (request.datasourceKey() != null || request.queryTemplate() != null)) {
			throw new CustomException(ErrorType.BAD_REQUEST, "HTTP_API Tool은 datasourceKey/queryTemplate을 설정할 수 없습니다.");
		}
		if (toolType == ToolType.DB_QUERY && (request.endpointUrl() != null || request.httpMethod() != null)) {
			throw new CustomException(ErrorType.BAD_REQUEST, "DB_QUERY Tool은 endpointUrl/httpMethod를 설정할 수 없습니다.");
		}
	}

	private AuthType parseAuthType(String authType) {
		AuthType parsed;
		try {
			parsed = AuthType.valueOf(authType);
		} catch (IllegalArgumentException e) {
			throw new CustomException(ErrorType.AI_TOOL_INVALID_AUTH_TYPE);
		}
		if (!SUPPORTED_AUTH_TYPES.contains(parsed.name())) {
			throw new CustomException(ErrorType.AI_TOOL_INVALID_AUTH_TYPE, "M2 범위에서는 OAUTH2 인증을 지원하지 않습니다.");
		}
		return parsed;
	}

	private void validateCredentialRef(AuthType authType, String credentialRef) {
		if (authType != AuthType.NONE && (credentialRef == null || credentialRef.isBlank())) {
			throw new CustomException(ErrorType.BAD_REQUEST, "인증이 필요한 Tool은 credentialRef를 입력해야 합니다.");
		}
	}

	private void validateJsonSchema(String parametersSchema) {
		try {
			objectMapper.readTree(parametersSchema);
		} catch (Exception e) {
			throw new CustomException(ErrorType.BAD_REQUEST, "parametersSchema가 올바른 JSON 형식이 아닙니다.");
		}
	}

	private void validateEndpointHost(String endpointUrl) {
		if (!ssrfGuard.isSafe(endpointUrl)) {
			throw new CustomException(ErrorType.AI_TOOL_UNSAFE_ENDPOINT);
		}
	}

	private ApprovalStatus parseApprovalStatus(String approvalStatus) {
		try {
			return ApprovalStatus.valueOf(approvalStatus);
		} catch (IllegalArgumentException e) {
			throw new CustomException(ErrorType.BAD_REQUEST, "올바르지 않은 approvalStatus입니다.");
		}
	}
}
```

**리뷰 반영(soft delete 일관성):** `findAll`이 `findByIsDeleted("N", pageable)`을 사용해 삭제된 Tool은 관리자 목록에서도 제외한다.

- [ ] **Step 5: 테스트 실행해서 통과 확인**

Run: `./gradlew test --tests "com.wip.workipedia.admin.aitool.service.AdminAiToolServiceTest"`
Expected: PASS (12 tests)

- [ ] **Step 6: AdminAiToolController에 대한 실패하는 테스트 작성**

```java
// src/test/java/com/wip/workipedia/admin/aitool/controller/AdminAiToolControllerTest.java
package com.wip.workipedia.admin.aitool.controller;

import com.wip.workipedia.admin.aitool.dto.AiToolResponse;
import com.wip.workipedia.admin.aitool.service.AdminAiToolService;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.common.security.JwtFilter;
import com.wip.workipedia.common.security.JwtProvider;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
	value = AdminAiToolController.class,
	excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class},
	excludeFilters = @ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE,
		classes = {JwtFilter.class, JwtProvider.class}
	)
)
class AdminAiToolControllerTest {

	@Autowired MockMvc mockMvc;
	@MockitoBean AdminAiToolService adminAiToolService;

	@Test
	void findAll_목록_조회() throws Exception {
		AiToolResponse response = new AiToolResponse(
			1L, "직원정보조회", "설명", "응답설명", "HTTP_API",
			"https://hr.example.com", "GET", null, null, "NONE", null, 5000, 100,
			"DRAFT", false, LocalDateTime.now(), LocalDateTime.now()
		);
		given(adminAiToolService.findAll(any())).willReturn(
			new PageResponse<>(List.of(response), new PageResponse.PageInfo(1, 10, 1, 1, false, false))
		);

		mockMvc.perform(get("/api/v1/admin/ai-tools").with(authentication(auth(1L))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].name").value("직원정보조회"));
	}

	@Test
	void create_등록_요청() throws Exception {
		AiToolResponse response = new AiToolResponse(
			1L, "직원정보조회", "설명", "응답설명", "HTTP_API",
			"https://hr.example.com", "GET", null, null, "NONE", null, 5000, 100,
			"DRAFT", false, LocalDateTime.now(), LocalDateTime.now()
		);
		given(adminAiToolService.create(eq(1L), any())).willReturn(response);

		mockMvc.perform(post("/api/v1/admin/ai-tools")
				.with(authentication(auth(1L)))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "name": "직원정보조회",
						  "description": "직원 정보를 조회합니다.",
						  "responseDescription": "응답 필드: name(이름)",
						  "toolType": "HTTP_API",
						  "endpointUrl": "https://hr.example.com",
						  "httpMethod": "GET",
						  "parametersSchema": "{\\"properties\\":{}}",
						  "authType": "NONE",
						  "timeoutMs": 5000,
						  "maxResultCount": 100
						}
						"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value("직원정보조회"));
	}

	private UsernamePasswordAuthenticationToken auth(Long userId) {
		return new UsernamePasswordAuthenticationToken(userId, null, List.of());
	}
}
```

- [ ] **Step 7: 테스트 실행해서 실패 확인**

Run: `./gradlew test --tests "com.wip.workipedia.admin.aitool.controller.AdminAiToolControllerTest"`
Expected: FAIL — `AdminAiToolController` 클래스가 없어 컴파일 에러

- [ ] **Step 8: AdminAiToolController 구현**

```java
// src/main/java/com/wip/workipedia/admin/aitool/controller/AdminAiToolController.java
package com.wip.workipedia.admin.aitool.controller;

import com.wip.workipedia.admin.aitool.dto.AiToolCreateRequest;
import com.wip.workipedia.admin.aitool.dto.AiToolResponse;
import com.wip.workipedia.admin.aitool.dto.AiToolUpdateRequest;
import com.wip.workipedia.admin.aitool.service.AdminAiToolService;
import com.wip.workipedia.common.request.BasePageRequest;
import com.wip.workipedia.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin/ai-tools")
@RequiredArgsConstructor
public class AdminAiToolController {

	private final AdminAiToolService adminAiToolService;

	@GetMapping
	public ResponseEntity<PageResponse<AiToolResponse>> findAll(@Valid BasePageRequest pageRequest) {
		Sort sort = Sort.by(Sort.Direction.DESC, "aiToolId");
		return ResponseEntity.ok(adminAiToolService.findAll(pageRequest.toPageable(sort)));
	}

	@PostMapping
	public ResponseEntity<AiToolResponse> create(
		@AuthenticationPrincipal Long adminUserId,
		@Valid @RequestBody AiToolCreateRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(adminAiToolService.create(adminUserId, request));
	}

	@PatchMapping("/{aiToolId}")
	public ResponseEntity<AiToolResponse> update(
		@AuthenticationPrincipal Long adminUserId,
		@PathVariable Long aiToolId,
		@RequestBody AiToolUpdateRequest request
	) {
		return ResponseEntity.ok(adminAiToolService.update(adminUserId, aiToolId, request));
	}
}
```

- [ ] **Step 9: 테스트 실행해서 통과 확인**

Run: `./gradlew test --tests "com.wip.workipedia.admin.aitool.controller.AdminAiToolControllerTest"`
Expected: PASS (2 tests)

- [ ] **Step 10: 전체 빌드 확인**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL (전체 테스트 통과)

- [ ] **Step 11: Commit**

```bash
git add src/main/java/com/wip/workipedia/admin/aitool src/test/java/com/wip/workipedia/admin/aitool
git commit -m "feat: AI Tool 관리자 등록/조회/설정 변경 API 추가"
```

---

## Task 10: Tool Health-Check API

**설계:** `POST /api/v1/admin/ai-tools/{id}/health-check`는 "Tool 설정으로 실제 연결이 되는지" 만 확인한다. M4의 `POST /admin/ai-tools/{id}/test`(파라미터를 채워 실제 기능을 실행하고 데이터를 검증)와는 다르다.

```text
health-check (M2, 이번 Task): 연결 가능 여부만 확인
  HTTP_API: endpointUrl로 GET 요청 → 2xx만 성공, 3xx/4xx/5xx는 실패(redirect 미추적)
  DB_QUERY: datasourceId에 연결된 datasource로 SELECT 1 실행 → 성공/실패만 확인
  parameters/queryTemplate 실행 검증은 하지 않음

test (M4, 이번 범위 아님): parameters를 채워 Tool을 실제로 실행하고 응답 데이터까지 검증
```

- HTTP_API health-check는 M2에서 `GET`만 사용한다. 화면의 `연결 체크` 버튼과 같은 의미다.
- 3xx도 실패로 취급한다. redirect를 따라가면 SSRF 검증을 통과한 host가 아닌 곳으로 우회 호출될 수 있으므로, redirect를 비활성화한 별도 `RestClient`(`HealthCheckRestClientFactory`)를 사용한다.
- 실패 메시지는 `RestClientException.getMessage()`/`DataAccessException.getMessage()`를 그대로 노출하지 않고 일반화된 메시지로 대체한다(URL, 인증 헤더, DB 접속 정보가 예외 메시지에 섞여 노출되는 것을 방지).
- 기존 `SsrfGuard`(Task 5), `tool.db` allowlist 기반 `Map<String, NamedParameterJdbcTemplate>`(Task 5B)를 그대로 재사용한다.
- DB 마이그레이션 없음. 감사 로그(`AdminLog`)는 기록하지 않는다(우선순위 낮음으로 이번 범위에서 제외).

**Files:**
- Create: `src/main/java/com/wip/workipedia/tool/executor/HealthCheckResult.java`
- Create: `src/main/java/com/wip/workipedia/tool/executor/HealthCheckRestClientFactory.java`
- Create: `src/main/java/com/wip/workipedia/tool/executor/DefaultHealthCheckRestClientFactory.java`
- Create: `src/main/java/com/wip/workipedia/tool/executor/HttpApiHealthChecker.java`
- Create: `src/main/java/com/wip/workipedia/tool/executor/DbQueryHealthChecker.java`
- Create: `src/main/java/com/wip/workipedia/admin/aitool/dto/HealthCheckResponse.java`
- Modify: `src/main/java/com/wip/workipedia/admin/aitool/service/AdminAiToolService.java`
- Modify: `src/main/java/com/wip/workipedia/admin/aitool/controller/AdminAiToolController.java`
- Modify: `src/test/java/com/wip/workipedia/admin/aitool/service/AdminAiToolServiceTest.java`
- Modify: `src/test/java/com/wip/workipedia/admin/aitool/controller/AdminAiToolControllerTest.java`
- Test: `src/test/java/com/wip/workipedia/tool/executor/HttpApiHealthCheckerTest.java`
- Test: `src/test/java/com/wip/workipedia/tool/executor/DbQueryHealthCheckerTest.java`

- [ ] **Step 1: HealthCheckResult, HealthCheckRestClientFactory, DefaultHealthCheckRestClientFactory, HealthCheckResponse 작성**

```java
// src/main/java/com/wip/workipedia/tool/executor/HealthCheckResult.java
package com.wip.workipedia.tool.executor;

public record HealthCheckResult(boolean success, long latencyMs, String errorMessage) {

	public static HealthCheckResult success(long latencyMs) {
		return new HealthCheckResult(true, latencyMs, null);
	}

	public static HealthCheckResult failure(String errorMessage) {
		return new HealthCheckResult(false, 0, errorMessage);
	}

	public static HealthCheckResult failure(long latencyMs, String errorMessage) {
		return new HealthCheckResult(false, latencyMs, errorMessage);
	}
}
```

```java
// src/main/java/com/wip/workipedia/tool/executor/HealthCheckRestClientFactory.java
package com.wip.workipedia.tool.executor;

import org.springframework.web.client.RestClient;

public interface HealthCheckRestClientFactory {
	RestClient create(long timeoutMs);
}
```

```java
// src/main/java/com/wip/workipedia/tool/executor/DefaultHealthCheckRestClientFactory.java
package com.wip.workipedia.tool.executor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.Duration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DefaultHealthCheckRestClientFactory implements HealthCheckRestClientFactory {

	@Override
	public RestClient create(long timeoutMs) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
			@Override
			protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
				super.prepareConnection(connection, httpMethod);
				connection.setInstanceFollowRedirects(false);
			}
		};
		requestFactory.setConnectTimeout(Duration.ofMillis(timeoutMs));
		requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));
		return RestClient.builder().requestFactory(requestFactory).build();
	}
}
```

```java
// src/main/java/com/wip/workipedia/admin/aitool/dto/HealthCheckResponse.java
package com.wip.workipedia.admin.aitool.dto;

public record HealthCheckResponse(boolean success, String toolType, long latencyMs, String errorMessage) {
}
```

- [ ] **Step 2: HttpApiHealthChecker에 대한 실패하는 테스트 작성**

```java
// src/test/java/com/wip/workipedia/tool/executor/HttpApiHealthCheckerTest.java
package com.wip.workipedia.tool.executor;

import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.domain.AuthType;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class HttpApiHealthCheckerTest {

	private final RestClient.Builder builder = RestClient.builder();
	private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
	private final HealthCheckRestClientFactory restClientFactory = timeoutMs -> builder.build();
	private final SsrfGuard ssrfGuard = endpointUrl -> true;
	private final Environment environment = mock(Environment.class);
	private final HttpApiHealthChecker checker = new HttpApiHealthChecker(restClientFactory, ssrfGuard, environment);

	private AiTool httpApiTool(String httpMethod) {
		return AiTool.createHttpApiTool(
			"직원정보조회", "설명", null,
			"https://hr.example.com/api/employees", httpMethod,
			"{\"properties\":{}}", null, AuthType.NONE, null, 5000, 100, 1L
		);
	}

	@Test
	void check_2xx_응답이면_성공() {
		server.expect(MockRestRequestMatchers.requestTo("https://hr.example.com/api/employees"))
			.andRespond(MockRestResponseCreators.withSuccess());

		HealthCheckResult result = checker.check(httpApiTool("GET"));

		assertThat(result.success()).isTrue();
	}

	@Test
	void check_4xx_응답이면_실패() {
		server.expect(MockRestRequestMatchers.requestTo("https://hr.example.com/api/employees"))
			.andRespond(MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND));

		HealthCheckResult result = checker.check(httpApiTool("GET"));

		assertThat(result.success()).isFalse();
	}

	@Test
	void check_POST_Tool은_빈_JSON_body로_요청() {
		server.expect(MockRestRequestMatchers.requestTo("https://hr.example.com/api/employees"))
			.andExpect(MockRestRequestMatchers.content().json("{}"))
			.andRespond(MockRestResponseCreators.withSuccess());

		HealthCheckResult result = checker.check(httpApiTool("POST"));

		assertThat(result.success()).isTrue();
	}

	@Test
	void check_안전하지않은_endpoint면_실패() {
		SsrfGuard unsafeGuard = endpointUrl -> false;
		HttpApiHealthChecker uncheckedChecker = new HttpApiHealthChecker(restClientFactory, unsafeGuard, environment);

		HealthCheckResult result = uncheckedChecker.check(httpApiTool("GET"));

		assertThat(result.success()).isFalse();
	}
}
```

- [ ] **Step 3: 테스트 실행해서 실패 확인**

Run: `./gradlew test --tests "com.wip.workipedia.tool.executor.HttpApiHealthCheckerTest"`
Expected: FAIL — `HttpApiHealthChecker` 클래스가 없어 컴파일 에러

- [ ] **Step 4: HttpApiHealthChecker 구현**

```java
// src/main/java/com/wip/workipedia/tool/executor/HttpApiHealthChecker.java
package com.wip.workipedia.tool.executor;

import com.wip.workipedia.tool.domain.AiTool;
import com.wip.workipedia.tool.domain.AuthType;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class HttpApiHealthChecker {

	private static final Set<HttpMethod> BODY_METHODS = Set.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH);

	private final HealthCheckRestClientFactory restClientFactory;
	private final SsrfGuard ssrfGuard;
	private final Environment environment;

	public HealthCheckResult check(AiTool tool) {
		if (!ssrfGuard.isSafe(tool.getEndpointUrl())) {
			return HealthCheckResult.failure("내부망/루프백 주소로는 health-check를 수행할 수 없습니다.");
		}

		HttpMethod method = HttpMethod.valueOf(tool.getHttpMethod());
		RestClient client = restClientFactory.create(tool.getTimeoutMs());
		long startedAt = System.currentTimeMillis();

		try {
			int statusCode;
			if (BODY_METHODS.contains(method)) {
				statusCode = client.method(method)
					.uri(URI.create(tool.getEndpointUrl()))
					.headers(headers -> applyAuth(headers, tool))
					.body(Map.of())
					.exchange((request, response) -> response.getStatusCode().value());
			} else {
				statusCode = client.method(method)
					.uri(URI.create(tool.getEndpointUrl()))
					.headers(headers -> applyAuth(headers, tool))
					.exchange((request, response) -> response.getStatusCode().value());
			}

			long latencyMs = System.currentTimeMillis() - startedAt;
			if (statusCode >= 200 && statusCode < 300) {
				return HealthCheckResult.success(latencyMs);
			}
			return HealthCheckResult.failure(latencyMs, "응답 코드: " + statusCode);
		} catch (Exception e) {
			long latencyMs = System.currentTimeMillis() - startedAt;
			return HealthCheckResult.failure(latencyMs, "외부 API 호출에 실패했습니다.");
		}
	}

	private void applyAuth(HttpHeaders headers, AiTool tool) {
		AuthType authType = tool.getAuthType();
		if (authType == AuthType.NONE) {
			return;
		}

		String credential = environment.getProperty(tool.getCredentialRef());
		if (credential == null || credential.isBlank()) {
			return;
		}

		switch (authType) {
			case API_KEY -> headers.set("X-API-Key", credential);
			case BEARER_TOKEN -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + credential);
			default -> { }
		}
	}
}
```

- [ ] **Step 5: 테스트 실행해서 통과 확인**

Run: `./gradlew test --tests "com.wip.workipedia.tool.executor.HttpApiHealthCheckerTest"`
Expected: PASS (4 tests)

- [ ] **Step 6: DbQueryHealthChecker에 대한 실패하는 테스트 작성**

```java
// src/test/java/com/wip/workipedia/tool/executor/DbQueryHealthCheckerTest.java
package com.wip.workipedia.tool.executor;

import com.wip.workipedia.tool.domain.AiTool;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DbQueryHealthCheckerTest {

	@Mock NamedParameterJdbcTemplate jdbcTemplate;

	private AiTool dbQueryTool() {
		return AiTool.createDbQueryTool(
			"휴가잔여일조회", "설명", null, "workipediaReadonly",
			"SELECT name FROM employee_vacations LIMIT 1",
			"{\"properties\":{}}", null, 3000, 10, 1L
		);
	}

	@Test
	void check_SELECT_1_성공하면_성공() {
		DbQueryHealthChecker checker = new DbQueryHealthChecker(Map.of("workipediaReadonly", jdbcTemplate));
		given(jdbcTemplate.queryForObject("SELECT 1", Map.of(), Integer.class)).willReturn(1);

		HealthCheckResult result = checker.check(dbQueryTool());

		assertThat(result.success()).isTrue();
	}

	@Test
	void check_allowlist에_없는_datasource면_실패() {
		DbQueryHealthChecker checker = new DbQueryHealthChecker(Map.of());

		HealthCheckResult result = checker.check(dbQueryTool());

		assertThat(result.success()).isFalse();
	}

	@Test
	void check_DataAccessException_발생시_실패() {
		DbQueryHealthChecker checker = new DbQueryHealthChecker(Map.of("workipediaReadonly", jdbcTemplate));
		given(jdbcTemplate.queryForObject("SELECT 1", Map.of(), Integer.class))
			.willThrow(new QueryTimeoutException("timeout"));

		HealthCheckResult result = checker.check(dbQueryTool());

		assertThat(result.success()).isFalse();
	}
}
```

- [ ] **Step 7: 테스트 실행해서 실패 확인**

Run: `./gradlew test --tests "com.wip.workipedia.tool.executor.DbQueryHealthCheckerTest"`
Expected: FAIL — `DbQueryHealthChecker` 클래스가 없어 컴파일 에러

- [ ] **Step 8: DbQueryHealthChecker 구현**

```java
// src/main/java/com/wip/workipedia/tool/executor/DbQueryHealthChecker.java
package com.wip.workipedia.tool.executor;

import com.wip.workipedia.tool.domain.AiTool;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DbQueryHealthChecker {

	private final Map<String, NamedParameterJdbcTemplate> toolJdbcTemplates;

	public HealthCheckResult check(AiTool tool) {
		NamedParameterJdbcTemplate jdbcTemplate = toolJdbcTemplates.get(tool.getDatasourceKey());
		if (jdbcTemplate == null) {
			return HealthCheckResult.failure("허용되지 않은 datasource입니다: " + tool.getDatasourceKey());
		}

		long startedAt = System.currentTimeMillis();
		try {
			jdbcTemplate.queryForObject("SELECT 1", Map.of(), Integer.class);
			return HealthCheckResult.success(System.currentTimeMillis() - startedAt);
		} catch (DataAccessException e) {
			return HealthCheckResult.failure(System.currentTimeMillis() - startedAt, "DB 연결에 실패했습니다.");
		}
	}
}
```

- [ ] **Step 9: 테스트 실행해서 통과 확인**

Run: `./gradlew test --tests "com.wip.workipedia.tool.executor.DbQueryHealthCheckerTest"`
Expected: PASS (3 tests)

- [ ] **Step 10: AdminAiToolService에 healthCheck() 추가 — 실패하는 테스트 먼저 작성**

`AdminAiToolServiceTest`에 import와 필드를 추가하고(기존 파일 수정), `setUp()`과 생성자 호출을 6개 인자로 바꾼다:

```java
// import 추가
import com.wip.workipedia.admin.aitool.dto.HealthCheckResponse;
import com.wip.workipedia.tool.executor.DbQueryHealthChecker;
import com.wip.workipedia.tool.executor.HealthCheckResult;
import com.wip.workipedia.tool.executor.HttpApiHealthChecker;
```

```java
// 필드/setUp 교체
@Mock AiToolRepository aiToolRepository;
@Mock AdminLogRepository adminLogRepository;
@Spy ObjectMapper objectMapper = new ObjectMapper();
@Mock HttpApiHealthChecker httpApiHealthChecker;
@Mock DbQueryHealthChecker dbQueryHealthChecker;

private boolean ssrfSafe = true;
private final SsrfGuard ssrfGuard = endpointUrl -> ssrfSafe;

private AdminAiToolService adminAiToolService;

@BeforeEach
void setUp() {
	adminAiToolService = new AdminAiToolService(
		aiToolRepository, adminLogRepository, objectMapper, ssrfGuard, httpApiHealthChecker, dbQueryHealthChecker
	);
}
```

테스트 클래스 마지막에(닫는 `}` 앞에) 아래 3개 테스트를 추가한다:

```java
	@Test
	void healthCheck_HTTP_API_Tool은_HttpApiHealthChecker_호출() {
		AiTool tool = AiTool.createHttpApiTool(
			"직원정보조회", "설명", null, "https://hr.example.com", "GET",
			"{\"properties\":{}}", null, AuthType.NONE, null, 5000, 100, 1L
		);
		given(aiToolRepository.findById(1L)).willReturn(Optional.of(tool));
		given(httpApiHealthChecker.check(tool)).willReturn(HealthCheckResult.success(120));

		HealthCheckResponse response = adminAiToolService.healthCheck(1L);

		assertThat(response.success()).isTrue();
		assertThat(response.toolType()).isEqualTo("HTTP_API");
	}

	@Test
	void healthCheck_DB_QUERY_Tool은_DbQueryHealthChecker_호출() {
		AiTool tool = AiTool.createDbQueryTool(
			"휴가잔여일조회", "설명", null, "workipediaReadonly",
			"SELECT name FROM employee_vacations LIMIT 1",
			"{\"properties\":{}}", null, 3000, 10, 1L
		);
		given(aiToolRepository.findById(2L)).willReturn(Optional.of(tool));
		given(dbQueryHealthChecker.check(tool)).willReturn(HealthCheckResult.failure(50, "DB 연결에 실패했습니다."));

		HealthCheckResponse response = adminAiToolService.healthCheck(2L);

		assertThat(response.success()).isFalse();
		assertThat(response.toolType()).isEqualTo("DB_QUERY");
	}

	@Test
	void healthCheck_존재하지않는_Tool은_AI_TOOL_NOT_FOUND() {
		given(aiToolRepository.findById(99L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> adminAiToolService.healthCheck(99L))
			.isInstanceOf(CustomException.class);
	}
```

- [ ] **Step 11: 테스트 실행해서 실패 확인**

Run: `./gradlew test --tests "com.wip.workipedia.admin.aitool.service.AdminAiToolServiceTest"`
Expected: FAIL — 생성자 인자 불일치 컴파일 에러 + `healthCheck` 메서드 없음

- [ ] **Step 12: AdminAiToolService.healthCheck() 구현**

`AdminAiToolService.java`의 필드 선언부를 교체한다:

```java
	private final AiToolRepository aiToolRepository;
	private final AdminLogRepository adminLogRepository;
	private final ObjectMapper objectMapper;
	private final SsrfGuard ssrfGuard;
	private final HttpApiHealthChecker httpApiHealthChecker;
	private final DbQueryHealthChecker dbQueryHealthChecker;
```

import에 추가:

```java
import com.wip.workipedia.admin.aitool.dto.HealthCheckResponse;
import com.wip.workipedia.tool.executor.DbQueryHealthChecker;
import com.wip.workipedia.tool.executor.HealthCheckResult;
import com.wip.workipedia.tool.executor.HttpApiHealthChecker;
```

`findAll` 메서드 다음에 추가:

```java
	@Transactional(readOnly = true)
	public HealthCheckResponse healthCheck(Long aiToolId) {
		AiTool tool = findTool(aiToolId);
		HealthCheckResult result = tool.getToolType() == ToolType.HTTP_API
			? httpApiHealthChecker.check(tool)
			: dbQueryHealthChecker.check(tool);

		return new HealthCheckResponse(result.success(), tool.getToolType().name(), result.latencyMs(), result.errorMessage());
	}
```

- [ ] **Step 13: 테스트 실행해서 통과 확인**

Run: `./gradlew test --tests "com.wip.workipedia.admin.aitool.service.AdminAiToolServiceTest"`
Expected: PASS (15 tests)

- [ ] **Step 14: AdminAiToolController에 엔드포인트 추가 — 실패하는 테스트 먼저 작성**

`AdminAiToolControllerTest`에 import와 테스트를 추가한다:

```java
import com.wip.workipedia.admin.aitool.dto.HealthCheckResponse;
```

```java
	@Test
	void healthCheck_연결확인_요청() throws Exception {
		given(adminAiToolService.healthCheck(1L))
			.willReturn(new HealthCheckResponse(true, "HTTP_API", 120, null));

		mockMvc.perform(post("/api/v1/admin/ai-tools/1/health-check").with(authentication(auth(1L))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));
	}
```

- [ ] **Step 15: 테스트 실행해서 실패 확인**

Run: `./gradlew test --tests "com.wip.workipedia.admin.aitool.controller.AdminAiToolControllerTest"`
Expected: FAIL — `404 Not Found` (엔드포인트 없음)

- [ ] **Step 16: AdminAiToolController에 엔드포인트 구현**

`AdminAiToolController.java`에 import 추가:

```java
import com.wip.workipedia.admin.aitool.dto.HealthCheckResponse;
```

`update` 메서드 다음에 추가:

```java
	@PostMapping("/{aiToolId}/health-check")
	public ResponseEntity<HealthCheckResponse> healthCheck(@PathVariable Long aiToolId) {
		return ResponseEntity.ok(adminAiToolService.healthCheck(aiToolId));
	}
```

- [ ] **Step 17: 테스트 실행해서 통과 확인**

Run: `./gradlew test --tests "com.wip.workipedia.admin.aitool.controller.AdminAiToolControllerTest"`
Expected: PASS (3 tests)

- [ ] **Step 18: 전체 빌드 확인**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 19: Commit**

```bash
git add src/main/java/com/wip/workipedia/tool/executor/HealthCheckResult.java src/main/java/com/wip/workipedia/tool/executor/HealthCheckRestClientFactory.java src/main/java/com/wip/workipedia/tool/executor/DefaultHealthCheckRestClientFactory.java src/main/java/com/wip/workipedia/tool/executor/HttpApiHealthChecker.java src/main/java/com/wip/workipedia/tool/executor/DbQueryHealthChecker.java src/main/java/com/wip/workipedia/admin/aitool src/test/java/com/wip/workipedia/tool/executor/HttpApiHealthCheckerTest.java src/test/java/com/wip/workipedia/tool/executor/DbQueryHealthCheckerTest.java src/test/java/com/wip/workipedia/admin/aitool
git commit -m "feat: AI Tool health-check API 추가"
```

---

## 완료 기준 매핑 (이슈 #90 M2)

| 이슈 항목 | 구현 위치 |
|---|---|
| `GET /admin/ai-tools` | Task 9 `AdminAiToolController.findAll` |
| `POST /admin/ai-tools` | Task 9 `AdminAiToolController.create` |
| `PATCH /admin/ai-tools/{id}` | Task 9 `AdminAiToolController.update` |
| `POST /internal/ai-tools/{id}/execute` | Task 8 `InternalAiToolController.execute` |
| `GET /internal/ai-tools/active` | Task 8 `InternalAiToolController.getActiveTools` |
| 비활성/미승인 Tool 실행 거부 | Task 6 `ToolExecutionService.execute` (`AI_TOOL_NOT_EXECUTABLE`) |
| 외부 API 오류/timeout 구조화 응답 | Task 5/6 `ToolExecutionException` → `ToolExecuteResponse.failure` |
| DB Query Tool SQL 검증 | Task 5B `SqlTemplateValidator` |
| DB Query Tool 실행 | Task 5B/6 `DbQueryToolExecutor` → `ToolExecutionService.executeByType` |
| AI의 임의 SQL 생성 금지 | Task 5B `queryTemplate` 등록값만 실행, AI는 `parameters`만 전달 |
| read-only datasource 제한 | Task 5B `ToolDbProperties.allowedDatasources` |
| 스키마 불일치 인자 실행 전 거부 | Task 4/6 `ParameterSchemaValidator` → `AI_TOOL_PARAMETER_MISMATCH` |
| credential 미노출 | Task 6 `ActiveAiToolResponse`(credential 필드 없음), Task 5 `Environment` 조회 |
| 감사 로그(호출자/ToolID/마스킹 파라미터/결과건수/실행시간/성공여부) | Task 6 `ToolExecutionService.recordLog` + `ToolExecutionLog` |
| credential은 BE에서만 보관 | Task 5 `credentialRef`는 환경변수 이름만 DB 저장, 실제 값은 `Environment`에서 조회 |
| `POST /admin/ai-tools/{id}/health-check` (연결 확인) | Task 10 `AdminAiToolController.healthCheck` → `HttpApiHealthChecker`/`DbQueryHealthChecker` |

**제외(M4로 분리):** `POST /admin/ai-tools/{id}/test`(파라미터를 채워 실제 기능을 실행·검증하는 API — Task 10의 health-check와는 달리 연결 확인이 아니라 기능 실행 확인), OAUTH2 인증 실제 동작, AI가 SQL을 생성하거나 수정하는 기능.
