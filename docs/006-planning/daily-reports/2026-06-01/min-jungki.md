# Daily Report — 민정기 2026-06-01

> 문서 유형: Daily Report
> 상태: Draft
> 정본 위치: `docs/006-planning/daily-reports/2026-06-01/min-jungki.md`
> 관련 문서: `docs/006-planning/daily-plans/2026-06-01.md`, `docs/006-planning/weekly-wbs/2026-06-01-week1.md`, `docs/006-planning/member-wbs/min-jungki.md`
> 버전: v0.1
> 최종 수정: 2026-06-01

## 완료

- 오전 및 오후 병가로 인한 휴식

## 미완료

- 워키 엔티티 V1 통합 스키마 정합: `worki_questions.user_id`→`author_id`, `worki_answers.is_accepted`→`accepted`, `like_count` 컬럼 제거, `accepted_answer_id` 추가, `worki_answers.ticket_id`·`official` 반영, `deleted_at`→`is_deleted`·`updated_at NULL` 정책 적용, `reactions.target_type` 값을 `WORKI_QUESTION`/`WORKI_ANSWER`로 변경
- 워키 컨트롤러 5개 엔드포인트 응답을 공통 `ApiResponse(code/status/message/data)` 엔벨로프로 감싸기
- 워키-로컬 `WorkiExceptionHandler`를 글로벌 `GlobalExceptionHandler` + `CustomException`/`ErrorType`로 흡수
- 정합 작업 후 정책 단위테스트 13개 재실행하여 동작 보존 확인
- FAQ/알림 도메인 경계 확인 — `domain-guides/worki-notification.md`가 `faq` 테이블을 명시하지만 V1에 없음(팀 결정 필요)
- 기능 단위 Issue 생성 및 브랜치 분리 (Week 1 규칙)
- ADR 009 Elasticsearch 신규 담당 범위 학습 (Week 2 docker-compose 추가 준비)

## 다음 근무일 논의

- 

## API/DB/화면 영향

- 

## 관련 링크

-
