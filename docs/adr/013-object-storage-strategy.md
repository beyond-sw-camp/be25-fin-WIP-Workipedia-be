# ADR 013 - Object Storage Provider Strategy

> 상태: Accepted  
> 결정일: 2026-06-09  
> 관련 구현: `StoragePort`, `StorageConfig`, `R2StorageAdapter`, `MinioStorageAdapter`, `S3StorageAdapter`

## Context

고객사별 배포 환경에 따라 파일 저장소 요구사항이 다르다.

- 클라우드 환경은 Cloudflare R2 또는 AWS S3를 사용할 수 있다.
- 온프레미스 환경은 고객사 내부망의 MinIO를 사용할 수 있다.
- 업무 서비스가 특정 저장소 SDK에 직접 의존하면 고객사별 배포 시 코드 변경이 필요하다.

티켓 첨부는 클라이언트가 presigned URL로 직접 업로드하고, 매뉴얼 PDF처럼 서버가 이미 보유한 파일은 서버에서 직접 업로드해야 한다.

## Decision

Object Storage는 `StoragePort`로 추상화하고 배포 환경의 `storage.provider` 설정으로 구현체를 선택한다.

지원 provider:

| 설정값 | 구현체 | 주요 환경 |
|---|---|---|
| `r2` | `R2StorageAdapter` | Cloudflare R2 기반 클라우드 배포 |
| `s3` | `S3StorageAdapter` | AWS S3 기반 클라우드 배포 |
| `minio` | `MinioStorageAdapter` | 온프레미스·내부망 배포 |

공통 계약:

- presigned upload URL 생성
- presigned download URL 생성
- 서버 직접 업로드
- object 삭제

`StorageService`와 매뉴얼·티켓 등 업무 도메인은 `StoragePort`만 사용하고 provider 구현을 직접 참조하지 않는다.

## Upload Flow

### 클라이언트 직접 업로드

```text
FE
→ BE presigned upload URL 요청
→ 선택된 Object Storage에 직접 업로드
→ BE에 objectKey 등 첨부 메타데이터 저장
```

### 서버 직접 업로드

```text
BE가 파일 수신·처리
→ StoragePort.upload(...)
→ 선택된 Object Storage에 저장
→ objectKey와 publicUrl 반환
```

매뉴얼 PDF 등록·교체는 서버 직접 업로드를 사용한다.

## Configuration

```yaml
storage:
  provider: ${STORAGE_PROVIDER}
  bucket: ${STORAGE_BUCKET}
  public-url: ${STORAGE_PUBLIC_BASE_URL}
```

provider별 인증정보는 환경변수 또는 Secret Manager/Vault로 주입하며 관리자 화면에서 변경하지 않는다.

## Security

- access key, secret key, 내부 endpoint를 API 응답과 로그에 노출하지 않는다.
- 업로드 MIME, 크기, 파일명을 서버에서 검증한다.
- 클라이언트에는 제한된 TTL의 presigned URL만 제공한다.
- DB에는 파일 바이너리가 아니라 `objectKey`, URL, MIME, 크기 등 메타데이터를 저장한다.
- 삭제·교체 시 연결된 object도 함께 정리한다.

## Consequences

- 고객사별 배포에서 코드 변경 없이 provider 설정만 교체할 수 있다.
- R2, S3, MinIO가 동일한 업무 계약을 제공한다.
- 실제 object와 RDB 메타데이터는 단일 트랜잭션으로 묶이지 않으므로 실패 보상과 orphan object 정리가 필요하다.
- provider별 endpoint, path-style, public URL 정책 차이는 adapter 내부에서 처리한다.
