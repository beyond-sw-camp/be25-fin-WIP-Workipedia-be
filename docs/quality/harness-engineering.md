# Workipedia Harness Engineering Guide

> 목적: Workipedia의 핵심 원칙을 자동 검증 가능한 테스트 체계로 바꾼다.
> 정본 위치: `docs/quality/harness-engineering.md`
> 기준 문서: `docs/reference/constitution.md`, README 기획서, Google Sheet `Workipedia docs`
> 작성일: 2026-05-28

## 1. 하네스 엔지니어링의 목표

Workipedia의 하네스는 단순 API 테스트 묶음이 아니다. 이 프로젝트의 하네스는 다음 질문에 매번 답해야 한다.

> "사용자가 신뢰할 수 있는 사내 지식에 더 빠르고 안전하게 도달했는가?"

따라서 첫 하네스는 UI 완성도보다 아래 품질을 먼저 검증한다.

| 우선순위 | 검증 대상 | 실패로 보는 상황 |
|---|---|---|
| P0 | 개인정보 보호 | 주민번호, 연락처, 계좌 등 민감정보가 저장 또는 출력됨 |
| P0 | 출처 기반 답변 | 챗봇 답변에 출처 링크나 참조 chunk가 없음 |
| P0 | 권한/감사 | 일반 사용자가 관리자 작업을 수행하거나 로그가 남지 않음 |
| P1 | 에스컬레이션 | 챗봇 실패 후 워키/티켓으로 이어지지 않음 |
| P1 | 데이터 보존 | 질문/답변/티켓/채택 이력이 hard delete됨 |
| P2 | 참여/보상 | 포인트가 정책과 다르게 지급되거나 어뷰징 가능 |
| P2 | ESG 지표 | 측정 근거 없는 숫자나 장식성 지표가 노출됨 |

## 2. 헌법 원칙을 테스트 규칙으로 바꾸기

| 헌법 원칙 | 하네스 규칙 | 자동화 방식 |
|---|---|---|
| Source-First | RAG 답변은 `references`를 1개 이상 포함해야 한다 | API/서비스 테스트 |
| Source-First | 출처를 찾지 못하면 답변을 꾸며내지 않고 실패 응답을 반환한다 | Golden Dataset 평가 |
| Knowledge Belongs to the Org | 일반 사용자는 본인 질문도 hard delete할 수 없다 | 권한 테스트 |
| Knowledge Belongs to the Org | 관리자 삭제는 soft delete이며 `admin_logs`에 기록된다 | DB 상태 검증 |
| Trust First | 검색 신뢰도가 임계치 미만이면 답변 대신 워키 등록을 안내한다 | RAG 정책 테스트 |
| Privacy by Default | 입력 저장 전 PII가 마스킹된다 | 단위/통합 테스트 |
| Privacy by Default | 출력 응답에 PII가 포함되면 거부 또는 마스킹된다 | 안전성 테스트 |
| Explicit Authority | 역할은 `USER`, `ADMIN`만 허용한다 | 도메인/DB 테스트 |
| Always an Escalation Path | 챗봇 실패 응답에는 워키 또는 티켓 전환 액션이 포함된다 | API 계약 테스트 |
| T1 Source of Truth | Vector Store는 정본이 아니며 DB 정본에서 재구축 가능해야 한다 | 배치/재색인 테스트 |
| T2 Traceable RAG | 참조 chunk ID, 문서 ID, score가 응답 이력에 남는다 | DB 상태 검증 |

## 3. 첫 번째 하네스 범위

처음부터 모든 것을 자동화하지 않는다. 1차 하네스는 아래 4개 축으로 시작한다.

### 3.1 RAG 품질 하네스

대상 흐름:

```text
사용자 질문
-> 매뉴얼/워키 검색
-> chunk 후보 선정
-> 답변 생성
-> 출처 표시
-> chatbot_messages.references 저장
```

필수 검증:

| ID | 검증 | 통과 기준 |
|---|---|---|
| RAG-001 | 출처 포함 | 답변에 출처 링크 또는 문서 식별자가 포함된다 |
| RAG-002 | 참조 저장 | DB에 `references` JSON이 저장된다 |
| RAG-003 | 근거 부족 | 근거가 없으면 "찾지 못함" 계열 응답과 워키 등록 액션을 반환한다 |
| RAG-004 | 허위 답변 방지 | Golden Dataset의 `answerable=false` 질문에 단정 답변을 하지 않는다 |
| RAG-005 | 출처 정합성 | 답변에 사용한 문장과 참조 chunk의 주제가 일치한다 |

### 3.2 정책/보안 하네스

대상 흐름:

```text
질문/답변/티켓 작성
-> 개인정보 마스킹
-> 권한 검사
-> 저장
-> 관리자 작업 감사 로그 기록
```

필수 검증:

| ID | 검증 | 통과 기준 |
|---|---|---|
| SEC-001 | 연락처 마스킹 | `010-1234-5678`이 원문 그대로 저장되지 않는다 |
| SEC-002 | 주민번호 마스킹 | 주민번호 패턴이 저장/출력되지 않는다 |
| SEC-003 | 계좌번호 마스킹 | 은행명+계좌번호 조합이 저장/출력되지 않는다 |
| SEC-004 | 일반 사용자 삭제 제한 | USER는 질문/답변 hard delete API에 실패한다 |
| SEC-005 | 관리자 로그 | ADMIN 작업 후 `admin_logs`에 actor/action/target/time이 남는다 |

### 3.3 워키/티켓 워크플로우 하네스

대상 흐름:

```text
챗봇 미해결
-> 워키 질문 등록
-> 답변 작성
-> 질문자 채택
-> 미해결 또는 담당 판단 필요 시 티켓 전환
-> 담당 부서 처리
```

필수 검증:

| ID | 검증 | 통과 기준 |
|---|---|---|
| WF-001 | 챗봇 실패 후 워키 등록 | 실패 응답에서 워키 등록 payload를 만들 수 있다 |
| WF-002 | 답변 채택 | 질문자는 답변 1개를 채택할 수 있다 |
| WF-003 | 채택 이력 보존 | 채택 취소/변경 시 이력이 추적 가능하다 |
| WF-004 | 티켓 전환 | 워키로 해결되지 않는 질문은 티켓으로 전환된다 |
| WF-005 | 부서 배정 감사 | 자동/수동 부서 배정 근거가 남는다 |

### 3.4 데이터 정본/배치 하네스

대상 흐름:

```text
manuals/worki 데이터 변경
-> embedding batch
-> vector index 갱신
-> 검색 결과 검증
```

필수 검증:

| ID | 검증 | 통과 기준 |
|---|---|---|
| DATA-001 | 정본 우선 | Vector Store 삭제 후 DB에서 재색인 가능 |
| DATA-002 | 배치 분리 | 사용자 요청 API가 embedding batch를 직접 수행하지 않음 |
| DATA-003 | 마이그레이션 | 스키마 변경은 Flyway migration으로 반영 |
| DATA-004 | 참조 무결성 | 삭제/비활성 문서 chunk는 검색 결과에서 제외 |

## 4. Golden Dataset 템플릿

Google Sheet에 `Harness Golden Dataset` 탭을 추가해 아래 열로 관리한다.

| 컬럼 | 설명 | 예시 |
|---|---|---|
| case_id | 고유 ID | RAG-GOLD-001 |
| category | HR/복지/보안/개발/총무 등 | HR |
| user_role | USER 또는 ADMIN | USER |
| question | 사용자 질문 | 연차 신청은 어디서 하나요? |
| answerable | 정본 근거로 답변 가능한지 | TRUE |
| expected_answer_points | 답변에 포함되어야 할 핵심 요지 | HR 시스템에서 신청, 팀장 승인 필요 |
| required_sources | 반드시 참조해야 하는 문서/FAQ/chunk | manual:leave-policy |
| forbidden_content | 나오면 안 되는 내용 | 개인 연락처, 추측성 규정 |
| expected_next_action | 다음 액션 | show_sources / create_worki / create_ticket |
| pii_in_input | 입력에 개인정보가 있는지 | FALSE |
| expected_masked_input | 저장되어야 하는 형태 | 010-****-5678 |
| pass_criteria | 사람이 읽는 통과 기준 | 출처와 신청 경로를 함께 안내 |
| priority | P0/P1/P2 | P0 |
| owner | 담당자 | PO |

초기 데이터셋은 30개로 시작한다.

| 묶음 | 개수 | 목적 |
|---|---:|---|
| 답변 가능한 사내 규정 질문 | 8 | 출처 기반 답변 검증 |
| 근거 부족 질문 | 5 | 허위 답변 방지 |
| 개인정보 포함 질문 | 5 | 입력/출력 마스킹 검증 |
| 워키 전환 질문 | 5 | 챗봇 실패 후 에스컬레이션 검증 |
| 티켓 전환 질문 | 4 | 담당 부서 연결 검증 |
| 관리자 권한 질문 | 3 | 권한/감사 검증 |

## 5. Spring Boot 테스트 구조 제안

현재 프로젝트는 Spring Boot 3.5, Java 21, JPA, Redis, Kafka, Quartz, Security, Flyway 기반이다. 하네스 테스트는 아래 패키지 구조로 시작한다.

```text
src/test/java/com/wip/workipedia/
  harness/
    rag/
      RagGoldenDatasetTest.java
      RagReferencePersistenceTest.java
    security/
      PrivacyMaskingHarnessTest.java
      RoleAuthorizationHarnessTest.java
    workflow/
      ChatbotToWorkiEscalationTest.java
      WorkiToTicketEscalationTest.java
    data/
      SourceOfTruthReindexTest.java
      MigrationPolicyTest.java
```

테스트 태그는 아래처럼 구분한다.

| 태그 | 실행 시점 | 포함 테스트 |
|---|---|---|
| `unit` | 매 커밋 | 마스킹, 권한 정책, 도메인 규칙 |
| `integration` | PR | API, DB 저장, admin log |
| `harness` | PR 또는 nightly | Golden Dataset, RAG 품질 |
| `external` | nightly | LLM/Embedding 실제 벤더 호출 |

Gradle 실행 예시:

```bash
./gradlew test
./gradlew test --tests "*HarnessTest"
```

## 6. 하네스 통과 기준

1차 릴리즈 전 최소 기준:

| 영역 | 최소 기준 |
|---|---|
| P0 테스트 | 100% 통과 |
| RAG Golden Dataset | P0/P1 케이스 90% 이상 통과 |
| 개인정보 마스킹 | 저장/출력 케이스 100% 통과 |
| 권한/감사 | 관리자 기능 100% 감사 로그 기록 |
| 에스컬레이션 | 챗봇 실패 케이스 100% 다음 액션 제공 |
| 마이그레이션 | 수동 DDL 없이 Flyway로 재생성 가능 |

LLM 답변 품질은 단순 문자열 일치로 판단하지 않는다. 자동 평가는 아래 순서로 한다.

1. 출처 존재 여부
2. 금지 내용 포함 여부
3. 필수 요지 포함 여부
4. 답변 가능/불가능 판단 일치 여부
5. 사람이 보는 샘플 리뷰

## 7. 첫 스프린트 실행 계획

### Day 1: 기준 고정

- `docs/reference/constitution.md`를 기준 원칙 문서로 사용한다.
- Google Sheet의 `요구사항 명세서`, `정책 기술서`, `테이블 명세서`에서 P0/P1 항목을 표시한다.
- `Harness Golden Dataset` 탭을 생성한다.

### Day 2: Golden Dataset 30개 작성

- 답변 가능 질문 8개
- 답변 불가 질문 5개
- 개인정보 포함 질문 5개
- 워키/티켓 전환 질문 9개
- 관리자 권한 질문 3개

### Day 3: 정책 하네스 구현

- 개인정보 마스킹 테스트
- USER/ADMIN 권한 테스트
- soft delete/admin log 테스트

### Day 4: RAG 하네스 스켈레톤 구현

- LLM adapter mock
- Retriever mock
- references 저장 검증
- 근거 부족 응답 검증

### Day 5: 워크플로우 하네스 구현

- 챗봇 실패 -> 워키 등록
- 워키 미해결 -> 티켓 전환
- 채택/포인트 기본 검증

## 8. 시트별 사용 방식

| 시트 | 하네스에서의 역할 |
|---|---|
| 요구사항 명세서 | 테스트 케이스의 원천. 각 요구사항은 최소 1개 검증 방식과 연결 |
| 테이블 명세서 | DB 상태 검증, soft delete, references, admin_logs 검증 기준 |
| 정책 기술서 (팀 공유용) | 개인정보, 권한, 포인트, 에스컬레이션 정책의 기대값 |
| Harness Golden Dataset | RAG/워크플로우 품질 평가용 실행 데이터 |

요구사항 명세서에 아래 컬럼을 추가하면 추적성이 좋아진다.

| 컬럼 | 값 예시 |
|---|---|
| harness_required | TRUE/FALSE |
| harness_type | unit/integration/rag/workflow/security |
| harness_case_id | SEC-001, RAG-003 |
| priority | P0/P1/P2 |
| automated | TRUE/FALSE |

## 9. 바로 만들 테스트의 우선순위

가장 먼저 만들 테스트 10개:

| 순서 | 테스트 ID | 이유 |
|---:|---|---|
| 1 | SEC-001 | 개인정보 저장 방지 |
| 2 | SEC-004 | 권한 모델 보호 |
| 3 | SEC-005 | 관리자 책임 추적 |
| 4 | RAG-001 | 출처 없는 답변 방지 |
| 5 | RAG-002 | RAG 감사 가능성 확보 |
| 6 | RAG-003 | 허위 답변 방지 |
| 7 | WF-001 | 막다른 챗봇 흐름 방지 |
| 8 | WF-004 | 티켓 전환 보장 |
| 9 | DATA-003 | DB 변경 통제 |
| 10 | DATA-001 | Vector Store를 정본으로 오해하지 않도록 방지 |

## 10. Definition of Done

기능 PR은 아래 조건을 만족해야 병합한다.

- 관련 요구사항 ID가 있다.
- P0/P1 요구사항이면 하네스 케이스가 있다.
- 개인정보/권한/DB/외부 API 변경이면 테스트가 있다.
- RAG 답변 변경이면 Golden Dataset 결과가 첨부된다.
- DB 스키마 변경이면 Flyway migration이 있다.
- 관리자 작업이면 `admin_logs` 기록 검증이 있다.
- 챗봇 실패 흐름이면 워키/티켓 에스컬레이션 검증이 있다.

## 11. 아직 결정해야 할 것

| 항목 | 결정 필요 내용 | 추천 |
|---|---|---|
| RAG 신뢰도 임계치 | 몇 점 미만이면 답변하지 않을지 | 초기에는 보수적으로 설정 |
| 출처 표시 형식 | 문서 링크, chunk 링크, 문단 링크 중 무엇인지 | 문서 링크 + chunk ID |
| PII 마스킹 정책 | 어떤 패턴을 어디까지 마스킹할지 | 입력 저장 전, 출력 직전 모두 적용 |
| 티켓 부서 배정 | 자동 배정 기준과 감사 필드 | rule 기반 먼저, LLM은 보조 |
| 포인트 어뷰징 | 반복 답변/자기 채택 제한 | 정책 기술서에 명시 후 구현 |
| ESG 지표 | 어떤 DB 필드에서 산출할지 | 지식 공유량/검증 건수/갱신 빈도부터 시작 |
