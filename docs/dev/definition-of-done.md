# Definition of Done — Workipedia

> 문서 유형: Definition of Done
> 상태: Draft
> 정본 위치: `docs/dev/definition-of-done.md`
> 관련 문서: `docs/api/api-contract.md`, `docs/dev/db-migration-guide.md`, `docs/dev/harness-engineering.md`, `docs/dev/development-guide.md`
> 버전: v0.2
> 최종 수정: 2026-06-04

본 문서는 팀원과 에이전트가 "작업 완료"를 같은 기준으로 판단하기 위한 완료 기준이다.
기능 구현이 끝났다고 말하기 전에 아래 항목을 확인한다.

## 1. 공통 완료 기준

- [ ] 관련 Issue 또는 작업 목적이 명확하다.
- [ ] 기능 코드 또는 문서 변경 범위가 PR 설명에 적혀 있다.
- [ ] API path, request, response, status code, enum이 바뀌면 `docs/api/api-contract.md`에 반영했다.
- [ ] 요구사항, 우선순위, 사용자 시나리오가 바뀌면 `docs/reference/prd.md`에 반영했다.
- [ ] 담당 도메인 구현 범위, 완료 기준, 논의 사항이 바뀌면 `docs/dev/domain-guides/{domain}.md`에 반영했다.
- [ ] 사용자 흐름이나 채널 구조가 바뀌면 `docs/reference/service-flow.md`에 반영했다.
- [ ] DB 테이블/컬럼/상태값이 바뀌면 migration 필요 여부를 확인했다.
- [ ] 프론트 화면, 버튼, 상태값에 영향이 있으면 담당 프론트 개발자에게 공유했다.
- [ ] 권한이 필요한 API는 USER/TEAM_ADMIN/SYSTEM_ADMIN 기준을 확인했다.
- [ ] 개인정보 또는 보안 영향이 있으면 마스킹/토큰/쿠키 정책을 확인했다.
- [ ] 작업 종료 후 daily report를 작성했다.

## 2. Auth 완료 기준

- [ ] 회원가입 request/response가 API 계약에 반영되어 있다.
- [ ] 로그인 성공/실패 응답이 API 계약에 반영되어 있다.
- [ ] Access Token 전달 방식이 명확하다.
- [ ] Refresh Token 쿠키/Redis 저장 정책이 명확하다.
- [ ] 로컬 개발 환경과 배포 환경의 Secure Cookie 정책을 구분했다.
- [ ] 권한별 접근 기준을 확인했다.

## 3. Chatbot/RAG 완료 기준

- [ ] 챗봇 답변에는 매뉴얼/워키 출처가 포함된다.
- [ ] 출처가 없으면 그럴듯한 답변을 생성하지 않는다.
- [ ] 답변 실패/불충분/공식 처리 필요 시 요청 티켓 전환 액션을 제공한다.
- [ ] `chatbot_messages.references_json` 저장 구조를 확인했다.
- [ ] 개인정보 포함 질문에 대한 마스킹/거부 정책을 확인했다.

## 4. Ticket 완료 기준

- [ ] 요청 티켓 생성 API가 정의되어 있다.
- [ ] 라우팅 신뢰도에 따라 담당 부서 큐 또는 공통 접수 큐로 분기된다.
- [ ] 티켓 상태값이 API/DB/프론트에서 동일하다.
- [ ] TEAM_ADMIN만 이관 요청을 할 수 있다.
- [ ] 이관 요청 시 티켓은 공통 접수 큐로 이동한다.
- [ ] 처리 완료 후 TEAM_ADMIN 지식화 승인과 비동기 동기화 흐름을 확인했다.

## 5. Worki 완료 기준

- [ ] 워키 질문 등록/목록/상세/수정 기준이 명확하다.
- [ ] 답변 등록/채택 정책이 반영되어 있다.
- [ ] 채택 후 추가 답변 가능 여부 정책을 확인했다.
- [ ] 삭제는 soft delete와 관리자 권한 기준을 따른다.
- [ ] 챗봇/RAG 재사용 대상 여부를 확인했다.

## 6. Admin/Point/ESG Grade/ESG 완료 기준

- [ ] TEAM_ADMIN 팀 큐와 SYSTEM_ADMIN 공통 접수 큐가 구분되어 있다.
- [ ] 관리자 작업은 `admin_logs` 기록 기준을 따른다.
- [ ] 포인트 이벤트와 지급 조건이 정의되어 있다.
- [ ] ESG 등급 산정 조건이 정의되어 있다.
- [ ] ESG 지표는 수치 산출 근거를 포함한다.
- [ ] 개인 평가로 오해될 수 있는 지표는 팀/운영 단위로 표현한다.

## 7. Frontend 완료 기준

- [ ] Figma Make 화면이 최신 기획 문서와 맞는다.
- [ ] 질문/요청/Flash Chat 진입이 분리되어 있다.
- [ ] 챗봇 답변 출처가 화면에 표시된다.
- [ ] 요청 티켓 생성/상태 확인 흐름이 보인다.
- [ ] 권한별 메뉴(USER/TEAM_ADMIN/SYSTEM_ADMIN)가 구분된다.
- [ ] mock API 형태가 백엔드 계약과 맞는다.

## 8. PR 완료 기준

- [ ] PR 제목이 `type: 작업 요약` 형식이다.
- [ ] 변경 API/DB/화면 영향이 PR 본문에 적혀 있다.
- [ ] 관련 Issue가 있으면 연결했다.
- [ ] 코드 변경 PR과 daily report PR을 섞지 않았다.
- [ ] 리뷰 코멘트가 있으면 반영 또는 답변했다.
