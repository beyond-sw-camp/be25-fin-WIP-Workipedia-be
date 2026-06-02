# TRD — Workipedia (사내 지식 공유 플랫폼)

> 문서 유형: Technical Requirements Document
> 상태: Draft
> 정본 위치: `docs/001-reference/trd.md`
> 관련 문서: `docs/001-reference/constitution.md`, `docs/001-reference/service-flow.md`, `docs/001-reference/prd.md`
> 버전: v0.3
> 최종 수정: 2026-06-01

---

## 1. 문서 개요

본 문서는 Workipedia의 기술 아키텍처, 데이터 모델, 외부 의존성, 인터페이스 정의를 기술한다. PRD와 함께 읽어야 한다.

---

## 2. 시스템 아키텍처

### 2.1 컴포넌트 구성 (제안)

```
┌────────────┐    ┌─────────────────────┐    ┌──────────────────┐
│  Frontend  │───▶│   API Gateway/BFF    │───▶│  Backend (REST)  │
│ (React 등) │    │                     │    │   (Spring Boot)  │
└────────────┘    └─────────────────────┘    └────────┬─────────┘
                                                      │
            ┌─────────────────────────────────────────┼─────────────┐
            ▼                ▼                        ▼             ▼
      ┌──────────┐    ┌──────────────┐         ┌──────────┐  ┌────────────┐
      │   RDB    │    │ Vector Store │         │  LLM API │  │ Embedding  │
      │ (MySQL/  │    │ (pgvector /  │         │ (OpenAI/ │  │   Model    │
      │ Postgres)│    │  OpenSearch) │         │  사내LLM)│  │            │
      └──────────┘    └──────────────┘         └──────────┘  └────────────┘
                                ▲
                                │ 1일 1회 배치
                                │
                        ┌──────────────┐
                        │ Batch Worker │
                        │ (워키 임베딩) │
                        └──────────────┘
```

### 2.2 기술 스택 (제안)
| 계층 | 후보 |
|---|---|
| Frontend | React + TypeScript, TanStack Query, TailwindCSS |
| Backend | Spring Boot 3.x (Java 21) |
| ORM | JPA(Hibernate) |
| RDB | MariaDB/MySQL 계열 |
| Vector Store | Elasticsearch (kNN 검색, 민정기 담당) — ADR 009 참조 |
| 인증 | JWT (Access + Refresh), 비밀번호 BCrypt |
| 세션 저장 | Redis (Refresh Token 저장) — ADR 003 참조 |
| LLM | 로컬 LLM 또는 검색 결과 기반 template 답변, 외부 LLM은 후순위 |
| Embedding | 로컬 임베딩 모델 우선 |
| 메시지 브로커 | Kafka (이벤트 기반 알림 등) |
| 배치 | Spring Scheduler/Quartz 우선 |
| 인프라 | Docker, Kubernetes(선택), CI/CD: GitHub Actions |
| 모니터링 | Prometheus + Grafana, 로그: ELK / Loki |

### 2.3 RAG 파이프라인

1. **인덱싱(배치, 1일 1회)** — KNOIT_006
   - `manuals`, `worki_questions`, `worki_answers` 중 신규/수정/채택된 데이터 조회
   - 문장 단위로 분할(chunk) → `worki_chunks` 등 chunk 테이블에 저장
   - 로컬 임베딩 생성 → local vector adapter 또는 Vector Store에 upsert

2. **질의 처리(실시간)** — KNOIT_001~003
   - 사용자 질문 → 개인정보 마스킹/필터(KNOIT_007/008)
   - 질문 로컬 임베딩 → Vector Store 유사도 검색(top-k)
   - 검색된 chunk + 원본 매뉴얼/워키 메타 → LLM 프롬프트 컨텍스트 구성
   - LLM 응답 생성 + 출처 메타 함께 반환(KNOIT_003)
   - 채팅 메시지 저장(KNOIT_004) → `chatbot_sessions`, `chatbot_messages`

3. **실패 / 불만족 / 요청 전환 흐름** — KNOIT_005
   - LLM이 답변 불가 또는 사용자 불만족 피드백 → 워키 질문 등록 흐름으로 분기
   - 실제 처리나 공식 확인이 필요한 경우 → 요청 티켓 생성 흐름으로 분기, 챗봇 입력 내용을 요청 초안으로 전달

---

## 3. 데이터 모델

### 3.1 주요 테이블

| 테이블 | 용도 |
|---|---|
| `users` | 사용자 계정 (사번, 부서, role) |
| `departments` | 부서 마스터 |
| `worki_questions` | 워키 질문 |
| `worki_answers` | 워키 답변 |
| `reaction` | 좋아요/싫어요 |
| `manuals` | 사내 매뉴얼/규정 |
| `chatbot_sessions` | 챗봇 대화 세션 |
| `chatbot_messages` | 챗봇 메시지(질문/답변 단위) |
| `tickets` | 부서 배정 티켓 |
| `ticket_transfer_requests` | 티켓 이관 요청 및 처리 이력 |
| `ticket_assignments` | 티켓 담당자 배정 이력 |
| `ticket_routing_logs` | 자동 배정 점수와 근거 |
| `knowledge_candidates` | 처리 완료 티켓의 지식화 후보 |
| `user_points` / `point_history` / `points_daily_limit` | 사용자 현재 포인트, 포인트 적립 이력, 일일 적립 한도 |
| `esg_grade` | ESG 점수 기반 등급 기준 |
| `notifications` | 알림 |
| `worki_chunks` | 워키 문장 조각 (검색·인용 단위) |
| `worki_search_logs` | 워키 검색어와 선택한 검색 결과 로그 |
| `manual_chunks` | 매뉴얼 문장 조각 (검색·인용 단위) |
| `manual_versions` | 매뉴얼 버전 이력 |
| `manual_citations` | RAG/답변에서 참조한 매뉴얼 조각 인용 이력 |
| `admin_logs` | 관리자 작업 로그 |

### 3.2 핵심 컬럼 메모

#### users
- `user_id` BIGINT PK (AUTO_INCREMENT)
- `department_id` BIGINT FK → departments
- `role` VARCHAR(20) CHECK IN ('USER','TEAM_ADMIN','SYSTEM_ADMIN'), 기본 USER
- `employee_id` VARCHAR(100) UNIQUE NOT NULL — 사번
- `email`, `password_hash`, `nickname` VARCHAR(100) NOT NULL 중복 허용, `is_active`, 시간컬럼

#### worki_questions
- `question_id` PK, `user_id` FK, `title`, `content`
- `status` (WAITING / IN_PROGRESS / ANSWERED / TICKETED 등)
- `view_count`, `like_count`, `created_at` 등

#### worki_answers
- `answer_id` PK, `question_id` FK, `author_id` FK
- `ticket_id` FK NULL (티켓 공식 답변에서 생성된 경우)
- `content`, `official` BOOLEAN, `accepted` BOOLEAN, `accepted_at`

#### tickets
- `ticket_id` PK
- `requester_id` FK → users
- `question_id` FK → worki_questions NULL 허용
- `source_chatbot_message_id` FK → chatbot_messages NULL 허용
- `category_id` FK → categories NULL 허용
- `title`, `content`
- `assignee_id` FK → users NULL 허용 (TEAM_ADMIN이 담당 팀원 배정)
- `assigned_department_id` FK → departments NULL 허용
- `routing_confidence_score` DECIMAL(5,2)
- `routing_decision` (AUTO_ASSIGNED / ADMIN_REVIEW / COMMON_QUEUE / NEED_MORE_INFO)
- `status` (RECEIVED / COMMON_QUEUE / ASSIGNED / IN_PROGRESS / COMPLETED / REJECTED / DELETED)
- `completed_at`, 시간컬럼

#### ticket_assignments
- `assignment_id` PK
- `ticket_id` FK → tickets
- `assignee_id` FK → users
- `assigned_by` FK → users (TEAM_ADMIN 또는 SYSTEM_ADMIN)
- `assigned_at`, `memo`

#### ticket_routing_logs
- `routing_log_id` PK
- `ticket_id` FK → tickets
- `confidence_score` DECIMAL(5,2)
- `candidate_departments_json` JSON
- `reasons_json` JSON
- `routed_by` FK → users NULL 허용
- `decision` (AUTO_ASSIGNED / ADMIN_REVIEW / COMMON_QUEUE / NEED_MORE_INFO)
- `created_at`

#### knowledge_candidates
- `candidate_id` PK
- `ticket_id` FK → tickets
- `draft_title`, `draft_content`
- `created_by` FK → users (담당자)
- `reviewed_by` FK → users NULL 허용 (TEAM_ADMIN)
- `status` (DRAFT / REVIEW_REQUESTED / APPROVED / REJECTED / PUBLISHED)
- `published_worki_question_id` 또는 `manual_id` NULL 허용

#### ticket_transfer_requests
- `transfer_request_id` PK
- `ticket_id` FK → tickets
- `from_department_id`, `suggested_department_id` NULL 허용
- `requester_id` FK → users (TEAM_ADMIN)
- `status` (REQUESTED / ASSIGNED_FROM_QUEUE / REJECTED)
- `reason`, 시간컬럼
- 이관 요청이 생성되면 티켓은 `COMMON_QUEUE` 상태로 이동하며, `SYSTEM_ADMIN`이 공통 접수 큐에서 담당 부서를 재배정한다.

#### chatbot_messages
- `message_id` PK, `session_id` FK
- `sender_type` (USER / ASSISTANT / SYSTEM)
- `content`
- `answerable` BOOLEAN NULL
- `next_action` (SHOW_SOURCES / CREATE_WORKI / CREATE_TICKET)
- `references_json` JSON (참조한 매뉴얼/워키 chunk 목록)
- `source_worki_question_id` FK NULL (워키 질문으로 전환된 경우)
- `source_ticket_id` FK NULL (요청 티켓으로 전환된 경우)
- `created_at`, `updated_at`, `deleted_at`

#### worki_chunks / manual_chunks
- `chunk_id` PK
- `question_id` / `answer_id` / `manual_id` FK
- `target_type` CHECK IN ('QUESTION','ANSWER') (worki_chunks의 경우)
- `content_text` TEXT
- `embedding` VECTOR(차원수) — Vector Store에서 관리

#### admin_logs
- `admin_log_id` PK, `user_id` (관리자) FK
- `action_type` CHECK IN ('USER_DEACTIVATE','WORKI_READ','WORKI_UPDATE','WORKI_DELETE','MANUAL_UPDATE','MANUAL_DELETE','TICKET_ASSIGN','TICKET_TRANSFER_REQUEST','TICKET_ROUTE_OVERRIDE','COMMON_QUEUE_ASSIGN','KNOWLEDGE_REVIEW','KNOWLEDGE_PUBLISH', ...)
- `action_detail` TEXT, `created_at`

> 상세 컬럼·제약은 별도 `테이블_명세서`를 정본으로 한다.

---

## 4. API 설계 원칙

### 4.1 공통
- REST + JSON
- 인증: `Authorization: Bearer <JWT>`
- 페이지네이션: `?page=&size=&sort=`
- 응답 표준: `{ "data": ..., "error": null, "meta": {...} }`

### 4.2 주요 엔드포인트 (예시)

| Method | Path | 설명 |
|---|---|---|
| POST | `/auth/signup` | 회원가입 |
| POST | `/auth/login` | 로그인 |
| POST | `/auth/logout` | 로그아웃 |
| GET  | `/me` | 내 정보 |
| POST | `/chatbot/sessions` | 세션 생성 |
| POST | `/chatbot/sessions/{id}/messages` | 챗봇 질의 |
| GET  | `/chatbot/sessions/{id}/messages` | 세션 내 메시지 조회 |
| GET  | `/worki/questions` | 질문 목록 (검색·필터·정렬) |
| POST | `/worki/questions` | 질문 등록 |
| PATCH | `/worki/questions/{id}` | 질문 수정 (WAITING 상태만) |
| POST | `/worki/questions/{id}/answers` | 답변 등록 |
| POST | `/worki/answers/{id}/accept` | 답변 채택 |
| POST | `/worki/{target}/{id}/reactions` | 좋아요/싫어요 |
| GET  | `/tickets` | 티켓 목록 (본인/부서) |
| POST | `/tickets` | 요청 티켓 생성 |
| PATCH | `/tickets/{id}/status` | 상태 변경 |
| PATCH | `/tickets/{id}/assignee` | 팀원 담당자 배정 |
| POST | `/tickets/{id}/transfer-requests` | TEAM_ADMIN 티켓 이관 요청 |
| PATCH | `/admin/common-queue/tickets/{id}/department` | SYSTEM_ADMIN 공통 접수 큐 티켓 부서 재배정 |
| POST | `/tickets/{id}/knowledge-candidates` | 처리 완료 티켓 지식화 후보 등록 |
| PATCH | `/knowledge-candidates/{id}/review` | 지식화 후보 승인/반려 |
| GET  | `/manuals` / GET `/manuals/{id}` | 매뉴얼 조회 |
| GET  | `/esg/metrics/me` | 내 ESG 지표 조회 |
| GET  | `/admin/dashboard` | 관리자 대시보드 데이터 |
| DELETE | `/admin/worki/{id}` | 워키 삭제(관리자 전용) |

---

## 5. 보안 요구사항

| 항목 | 요구사항 |
|---|---|
| 비밀번호 | 8자 이상 영문+숫자 / BCrypt 저장 |
| 인증 | JWT(짧은 Access + Refresh), HttpOnly 쿠키 또는 헤더 |
| 권한 검사 | 모든 변경 API에서 USER/TEAM_ADMIN/SYSTEM_ADMIN/부서원 권한 명시 검증 |
| 개인정보 마스킹 | KNOIT_007 — 챗봇 입력에서 주민번호/연락처/계좌 등 패턴 마스킹 |
| 개인정보 답변 거부 | KNOIT_008 — LLM 응답 후처리에 개인정보 유출 검사 |
| 관리자 추적 | 모든 TEAM_ADMIN/SYSTEM_ADMIN 작업은 `admin_logs`에 기록 |
| 퇴사자 차단 | `users.is_active = false` 사용자는 로그인 거부 |
| 데이터 보존 | 워키 게시글은 USER가 직접 삭제 불가, soft delete 사용 |

---

## 6. 비기능 요구사항(NFR)

| 항목 | 목표 |
|---|---|
| 챗봇 응답 시간 (p95) | 5초 이내 |
| 워키 목록 조회 (p95) | 500ms 이내 |
| 가용성 | 평일 09:00~19:00 사내 SLA 99.5% |
| 임베딩 배치 | 일 1회, 1만 chunk 기준 30분 이내 |
| 동시 사용자 | 사내 동시 접속 500명 기준 |
| 로깅 | 모든 챗봇 질의/응답, 관리자 작업, 인증 이벤트 기록 |

---

## 7. 외부 의존성

| 의존성 | 사용 목적 | 위험 / 대응 |
|---|---|---|
| LLM API | RAG 답변 생성 | 장애 시 워키 등록 흐름으로 우회. 응답 캐싱 검토. |
| Embedding API | 문서·질문 임베딩 | 비용/속도 제한. 사내 모델 fallback 검토. |
| 사내 인증 시스템 | (선택) SSO 연동 | 별도 인증 도입 시 마이그레이션 계획 수립 |

---

## 8. 운영 / 배포

### 8.1 환경 분리
- `dev` / `staging` / `prod` 3단계
- DB 마이그레이션: Flyway / Liquibase
- 비밀 키: AWS Secrets Manager / HashiCorp Vault

### 8.2 모니터링·알람
- 챗봇 응답 시간, LLM 호출 실패율, 임베딩 배치 성공 여부, 티켓 SLA 초과 건수 등을 대시보드화
- 핵심 지표 임계치 초과 시 운영 채널(Slack 등) 알람

### 8.3 백업
- RDB 일 1회 풀백업 + 시간 단위 증분
- Vector Store: 재구축 가능 구조이므로 백업 우선순위는 낮음 (단, 임베딩 비용 절감 위해 주기적 스냅샷 권장)

---

## 9. 마이그레이션 / 데이터 초기화

- 매뉴얼 초기 데이터 적재: 사내 규정 문서 → 관리자 페이지 일괄 업로드 또는 운영 스크립트
- 부서 마스터 적재: 인사팀 데이터 기준
- 초기 사용자: 사번 기반 일괄 등록 + 첫 로그인 시 비밀번호 설정 흐름

---

## 10. 미해결 기술 이슈

1. Vector Store 선택 — pgvector vs OpenSearch (운영 부담 vs 검색 품질)
2. 매뉴얼 PDF 등 비정형 문서의 chunking 전략
3. 티켓 자동 배정 알고리즘 — 키워드, 문서 유사도, 카테고리 매핑, 과거 티켓, LLM 분류 점수 가중치
4. 워키 답변 우선순위 (정책 미확정 — PRD §7 참조)
5. 챗봇 응답 캐싱 정책 (질문 유사도 기반 캐시 hit 조건)
