# ADR 0001 - Database Choice

> 문서 유형: ADR
> 상태: Draft
> 정본 위치: `docs/adr/0001-database-choice.md`
> 관련 문서: `docs/reference/trd.md`, `docs/database/db-migration-guide.md`
> 버전: v0.1
> 최종 수정: 2026-05-28

## Context

현재 프로젝트는 Spring Boot, JPA, Flyway, MariaDB JDBC 의존성을 가지고 있다. TRD에는 MySQL/MariaDB 또는 PostgreSQL이 후보로 적혀 있다.

## Decision

MVP는 현재 프로젝트 의존성에 맞춰 MariaDB/MySQL 계열로 진행한다.

Vector Store는 DB 선택과 분리하고, adapter로 격리한다.

## Consequences

- 초기 개발 속도를 높일 수 있다.
- Flyway migration을 바로 적용할 수 있다.
- pgvector를 즉시 쓰지는 않는다.
- 추후 PostgreSQL/pgvector로 옮길 가능성은 남긴다.

## Open Questions

- 배포 환경 DB는 MariaDB인지 MySQL인지 확정 필요.
- Vector Store를 별도 서비스로 둘지, mock으로 발표할지 확정 필요.
