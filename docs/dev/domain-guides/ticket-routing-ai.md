# Ticket Routing AI Integration Guide

> 문서 유형: Development Guide
> 상태: Draft
> 최종 수정: 2026-06-09

## 흐름

```text
티켓 내용
→ 시스템명·업무 키워드 추출
→ 구체적인 키워드에 가중치를 적용한 query vector 생성
→ 부서 R&R + 승인 라우팅 사례 Vector Search
→ 후보 부서 Top 3
→ Cross-Encoder reranking
→ topScore와 scoreMargin 검증
→ 담당 부서 또는 COMMON_QUEUE
```

AI는 부서만 추천하며 개인 담당자 배정은 TEAM_ADMIN 책임이다.

부서 R&R은 선택 설정이다. 값이 없더라도 서버는 구동되며 승인 사례가 없거나 점수가 부족하면 공통 접수 큐로 보낸다. 다만 신규 부서의 라우팅 품질을 위해 초기 R&R 등록을 권장한다.

## 2단계 라우팅

### 1차 후보 검색

티켓에서 시스템명과 업무 키워드를 추출하고 질의 벡터를 생성한다.

```text
ticket text
→ keyword extraction
→ keyword embedding
→ weighted query vector
→ department R&R and approved case similarity
→ top 3 department candidates
```

시스템명과 소업무처럼 구체적인 키워드에 더 높은 가중치를 준다.

### 2차 재정렬

후보 부서의 R&R과 승인 처리 사례를 원본 티켓과 함께 Cross-Encoder에 입력한다.

```text
(ticket, department R&R + approved cases)
→ Cross-Encoder relevance score
→ candidate reranking
```

1위 점수와 1·2위 점수 차이가 기준을 통과하면 해당 부서를 추천한다. 그렇지 않으면 공통 접수 큐로 전환한다.

## 응답 필수값

- `recommendedDepartmentId`
- `topScore`
- `scoreMargin`
- 후보별 `candidateId`, `departmentId`, 원본 `score`, `rank`
- 기준 미달 사유

```json
{
  "recommendedDepartmentId": 2,
  "topScore": 5.14,
  "scoreMargin": 1.27,
  "candidates": [
    {
      "candidateId": "department-2",
      "departmentId": 2,
      "score": 5.14,
      "rank": 1
    },
    {
      "candidateId": "department-5",
      "departmentId": 5,
      "score": 3.87,
      "rank": 2
    }
  ]
}
```

점수 범위는 모델마다 다르므로 정규화 방식이 확정되기 전까지 `0~1`로 가정하지 않는다.

## 피드백 반영

최종 부서가 처리 완료하고 TEAM_ADMIN이 승인한 경우에만 민감정보를 제거한 티켓을 해당 부서의 라우팅 사례로 동기화한다. 모델 재학습이나 벡터 직접 이동은 하지 않는다.

## Cold Start

- 신규 부서는 SYSTEM_ADMIN이 담당 서비스와 업무 키워드를 R&R로 등록한다.
- 승인 처리 사례가 없을 때는 부서 R&R 임베딩만으로 후보를 검색한다.
- R&R도 없는 부서는 서버 구동에서 제외하지 않지만 자동 추천 근거가 부족하므로 공통 접수 큐로 보낼 수 있다.
- 승인 사례가 쌓이면 R&R과 사례를 함께 검색 근거로 사용한다.

## Divergence Control

서로 관련 없는 키워드를 단순 평균하면 의미 없는 질의 벡터가 만들어질 수 있다.

- 추출 키워드 간 cosine similarity가 임계값보다 낮으면 단순 평균을 중단한다.
- 시스템명이나 가장 구체적인 소업무 키워드를 anchor로 사용한다.
- 시스템명·소업무에는 상위 업무 분류보다 높은 가중치를 줄 수 있다.
- 관련 없는 키워드가 혼합되어 판단 근거가 약하면 `COMMON_QUEUE` 또는 추가 정보 요청으로 전환한다.

## 안전장치

- AI는 부서를 추천하고 BE가 정책 검증 후 실제 배정을 저장한다.
- 초기 운영에서는 추천 결과를 SYSTEM_ADMIN 또는 공통 접수 큐에서 확인하도록 설정할 수 있다.
- 부서 내부 개인 담당자 배정은 TEAM_ADMIN만 수행한다.
- 승인 사례의 등록·비활성화·삭제 이력을 저장한다.
- 잘못 승인된 사례는 검색 대상에서 제외하고 ChromaDB를 재동기화할 수 있어야 한다.
- 후보별 Vector Search 점수, Cross-Encoder 점수, 최초 배정 부서와 최종 처리 부서를 추적한다.

## BE 저장 책임

- 최초 후보와 점수
- 실제 배정 부서
- 최종 처리 부서
- 공통 접수 큐 전환 사유
- 라우팅 사례 승인·비활성화 이력

`ticket_routing_cases`와 상세 점수 저장 컬럼은 후속 migration 대상이다.

## 남은 결정

- 승인 사례의 부서별 최대 보관 수와 시간 감쇠
- 동일·유사 사례 중복 제거 기준
- Cross-Encoder 모델과 점수 정규화 방식
- 1위 최소 점수와 1·2위 최소 점수 차이
- 자동 배정을 허용할 평가 정확도 기준
