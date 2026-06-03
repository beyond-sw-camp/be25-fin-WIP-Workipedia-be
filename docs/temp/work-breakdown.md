# Workipedia 전체 작업 목록 (FE + BE)

> 작성일: 2026-06-03
> 기준: 기능 추가(Flash Chat + 모바일) 반영 후

---

## 범례

| 상태 | 의미 |
|---|---|
| ✅ 완료 | dev 브랜치 머지됨 |
| 🔄 진행 중 | 개발 중 |
| ⬜ 미시작 | 아직 시작 안 함 |
| 🆕 신규 | 이번 추가 기능 |

---

## BACKEND

### 김진혁 (티켓, RAG/챗봇, 지식화, CI/CD)

| 상태 | 작업 | 비고 |
|---|---|---|
| ✅ | 티켓 CRUD | PR #29, dev 머지 |
| ⬜ | RAG 파이프라인 | 임베딩 생성, 벡터 검색, 청크 저장 |
| ⬜ | 챗봇 응답 생성 | LLM 연동, 출처 포함 답변, 개인정보 마스킹 |
| ⬜ | 지식화 후보 등록/검수 API | `knowledge_candidates` |
| 🆕⬜ | Flash Chat WebSocket | Spring WebSocket + STOMP, Redis TTL |
| 🆕⬜ | 사진 첨부 API | multipart upload, `attachments` 테이블 |
| 🆕⬜ | CORS 화이트리스트 | 스크립트 주입용 클라이언트 도메인 관리 |
| 🆕⬜ | 티켓 담당자 추천 | 부서별 최근 30일 처리 건수 TOP 3 |
| 🆕⬜ | 티켓 중요도(priority) | V2 migration, LOW/MEDIUM/HIGH/CRITICAL |
| ⬜ | CI/CD | GitHub Actions, Docker |

### 이슬이 (Auth, 챗봇 세션/메시지)

| 상태 | 작업 | 비고 |
|---|---|---|
| ⬜ | 회원가입/로그인 | 사번 기반, BCrypt, JWT |
| ⬜ | JWT Access/Refresh 발급 | Redis Refresh Token |
| ⬜ | 계정 비활성화 (퇴사자 차단) | `is_active` |
| ⬜ | 챗봇 세션 생성/조회 API | `chatbot_sessions` |
| ⬜ | 챗봇 메시지 저장/조회 API | `chatbot_messages` |
| ⬜ | 챗봇 → 워키/티켓 전환 흐름 | `next_action` 처리 |
| ⬜ | 알림 생성/조회/읽음 API | `notifications` |

### 민정기 (워키, FAQ, 알림, Elasticsearch)

| 상태 | 작업 | 비고 |
|---|---|---|
| ⬜ | 워키 질문 CRUD | 상태 관리 (WAITING → ANSWERED) |
| ⬜ | 워키 답변 등록/채택 | `official`, `accepted` 처리 |
| ⬜ | 좋아요/싫어요 반응 | `reaction` 테이블 |
| ⬜ | Elasticsearch 연동 | 워키 검색, kNN 벡터 검색 |
| ⬜ | FAQ/인기 워키·매뉴얼 API | 인용수, 좋아요 기준 |
| 🆕⬜ | CDN 스크립트 주입 CORS 설정 | 민정기 담당 도메인 연계 |

### 김가영 (관리자 대시보드, 포인트, ESG)

| 상태 | 작업 | 비고 |
|---|---|---|
| ⬜ | TEAM_ADMIN 티켓 큐 API | 부서별 큐 조회, 담당자 배정 |
| ⬜ | SYSTEM_ADMIN 공통 접수 큐 API | 재배정, 이관 처리 |
| ⬜ | 지식화 후보 검수 API | APPROVED/REJECTED |
| ⬜ | 관리자 작업 로그 | `admin_logs` |
| ⬜ | 포인트 적립/조회/랭킹 API | `user_points`, `point_history` |
| ⬜ | ESG 점수/등급 산정 API | `esg_grade` |
| ⬜ | ESG 지표 카드 API | 절감 시간, 지식화 전환율 등 |
| 🆕⬜ | Flash Chat 관리자 설정 API | TTL, 쿨다운, 금지어, 강제삭제 |

---

## FRONTEND

> 담당: 황희수 (메인), 민정기 (BE 완료 후 합류)

### 황희수 — 메인 프론트엔드

| 상태 | 작업 | 비고 |
|---|---|---|
| ⬜ | 공통 레이아웃/네비게이션 | 헤더, 사이드바, 라우팅 |
| ⬜ | Auth UI | 로그인, 회원가입 폼 |
| ⬜ | 챗봇 UI | 노잇 채팅창, 메시지 버블, 출처 표시, 워키/티켓 전환 프롬프트 |
| ⬜ | 티켓 발행 UI | 폼, 중요도 선택, 사진 첨부 |
| ⬜ | 티켓 목록/상세 UI | 상태 뱃지, 담당자 추천 표시 |
| ⬜ | 관리자 대시보드 UI | 큐 목록, 배정, 지식화 검수 |
| ⬜ | 포인트/랭킹 UI | 포인트 내역, 순위 |
| ⬜ | ESG 지표 카드 UI | 수치 시각화 |
| ⬜ | 알림 UI | 뱃지, 드롭다운, 읽음 처리 |
| 🆕⬜ | 사진 첨부 UI | file input + 카메라 캡처 |

### 민정기 — BE 완료 후 프론트 합류

| 상태 | 작업 | 비고 |
|---|---|---|
| ⬜ | 워키 게시판 UI | 질문 목록/상세, 답변, 채택, 반응 |
| ⬜ | 워키 검색 UI | Elasticsearch 연동 |
| ⬜ | FAQ/메인 UI | 인기 워키·매뉴얼 노출 |

### 미배정 (내일 팀 회의 후 결정)

| 상태 | 작업 | 비고 |
|---|---|---|
| 🆕⬜ | Flash Chat UI | 실시간 채팅창, 메시지, ↩ 답장, 👍 반응 |
| 🆕⬜ | 모바일 반응형 + Capacitor | Tailwind 반응형 + Capacitor로 iOS/Android 앱 래핑 |
| 🆕⬜ | CDN 챗봇 컴포넌트 | Vue IIFE 빌드, S3 업로드, 스크립트 주입 가이드 |

---

## 요약

| 담당 | 미시작 BE | 미시작 FE | 신규 추가 |
|---|---|---|---|
| 김진혁 | 6개 | — | 5개 |
| 이슬이 | 6개 | — | 0개 |
| 민정기 | 7개 | 4개 | 1개 |
| 김가영 | 7개 | — | 1개 |
| 황희수 | — | 12개 | 3개 |

> 황희수 부담이 가장 큼. 민정기 BE 완료 시점이 프론트 병목 해소 시점.

---

## 브랜치 전략

```
feat/flash-chat-be       → dev  (김진혁)
feat/flash-chat-fe       → dev  (미정)
feat/mobile-capacitor    → dev  (미정)
feat/attachments         → dev  (김진혁 BE, 황희수 FE)
feat/cdn-script-inject   → dev  (민정기)
feat/ticket-priority     → dev  (김진혁)
feat/ticket-assignee-recommend → dev (김진혁)
```
