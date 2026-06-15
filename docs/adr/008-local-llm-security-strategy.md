# ADR 008 - Deployment Provider and AI Data Security Strategy

> 문서 유형: ADR
> 상태: Accepted
> 정본 위치: `docs/adr/008-local-llm-security-strategy.md`
> 관련 문서: `docs/adr/002-rag-strategy.md`, `docs/reference/trd.md`, `docs/adr/013-object-storage-strategy.md`
> 버전: v0.4
> 최종 수정: 2026-06-15

## Context

고객사마다 보안 규제, 내부망 구성, 사용 가능한 AI 인프라가 다르다. 어떤 고객사는 외부 API를 허용할 수 있지만 금융·방산 고객사는 로컬 모델과 내부 저장소가 필요할 수 있다.

## Decision

- 고객사별로 Workipedia를 별도 배포한다.
- 하나의 실행 서버에서 tenant별 provider를 동적으로 바꾸지 않는다.
- LLM과 Embedding은 공통 인터페이스 뒤에 로컬/클라우드 구현체를 둔다.
- Object Storage는 `StoragePort` 뒤에 R2/S3/MinIO 구현체를 둔다.
- provider 종류, 모델명, 내부 주소와 자격 증명은 배포 설정과 Secret으로 관리하며 관리자 화면에 노출하지 않는다.
- BE RDB의 민감정보는 암호화 저장하고 권한이 있는 사용 흐름에서만 복호화한다.
- AI 서버는 검색 품질과 Tool 실행 정확도를 위해 LLM 입력과 Vector Store에 원문을 사용한다.
- AI 서버는 사용자에게 반환하는 최종 LLM 응답에만 민감정보 마스킹을 적용한다.
- 민감정보 원문과 비밀정보는 애플리케이션 로그와 감사 로그에 남기지 않는다.
- IAM/EAM 연동을 전제로 하지 않으며 Workipedia의 인증·권한 정책을 적용한다.
- 운영 환경에 mock 응답을 두지 않는다.

```text
LlmProvider
├─ LocalLlmProvider
└─ CloudLlmProvider

EmbeddingProvider
├─ LocalEmbeddingProvider
└─ CloudEmbeddingProvider

StoragePort
├─ R2StorageAdapter
├─ S3StorageAdapter
└─ MinioStorageAdapter
```

모든 구현체는 동일한 요청/응답 계약, timeout, 오류 타입을 제공해야 한다.

### 고객사별 배포 예시

| 배포 유형 | LLM/Embedding | Object Storage |
|---|---|---|
| 클라우드형 | Cloud provider 또는 관리형 모델 | R2 또는 S3 |
| 온프레미스형 | 고객사 내부 Local provider | 내부망 MinIO |

provider 선택은 고객사별 배포 프로파일에서 고정한다. 하나의 서버에서 tenant별로 런타임 전환하지 않으며, 저장소의 업로드·다운로드·삭제 계약과 보안 세부사항은 ADR 013을 따른다.

## Data Security Rules

- 주민등록번호와 카드번호는 최종 LLM 응답에서 항상 마스킹한다.
- 전화번호와 이메일은 배포 설정에 따라 최종 LLM 응답에서 선택적으로 마스킹한다.
- 계좌번호는 오탐 정책 확정 전까지 자동 마스킹 대상에 포함하지 않는다.
- 마스킹 해제 기능은 관리자 옵션으로 제공하지 않는다.
- 로그와 감사 이력에는 민감정보 원문을 남기지 않는다.
- 허용된 Tool과 파라미터만 AI에 노출한다.
- DB Query Tool의 SQL 원문과 접속정보는 개발자가 관리하며 LLM이 SQL을 생성하지 않는다.

## Consequences

- 온프레미스와 클라우드 고객사를 같은 도메인 계약으로 지원할 수 있다.
- 배포별 인프라 설정과 운영 책임을 명확히 관리해야 한다.
- AI와 파일 저장소를 독립된 provider 계약으로 교체할 수 있다.
- 원문을 사용하는 AI·Vector Store의 접근 통제와 로그 차단이 중요하며, 최종 응답 마스킹 품질 검증이 필요하다.

## Open Questions

- 민감정보 탐지 규칙과 테스트 데이터셋
- 고객사별 provider 장애·timeout 기준
- 보안 검토를 통과한 DB Query Tool 배포 절차
