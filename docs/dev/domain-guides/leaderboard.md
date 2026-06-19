# Leaderboard Domain Guide

> 문서 유형: Development Guide
> 상태: Draft
> 원본 위치: `docs/dev/domain-guides/leaderboard.md`
> 관련 도메인: User Point, ESG Grade, User, Department, Worki Answer
> 최종 수정: 2026-06-19

## ESG Environment Impact Metric

리더보드 응답에는 사내 지식 공유 활동으로 인한 추정 환경 기여 지표를 함께 내려준다.

이 지표는 실제 전력 계측값이 아니라, 챗봇 기반 지식 검색을 통해 절감된 것으로 추정되는 업무 시간을 전력 사용량 및 CO2 배출량으로 환산한 값이다. 따라서 화면 문구에서는 "실제 절감량"이 아니라 "추정 절감 효과" 또는 "환산 효과"로 표현한다.

### Response Field

`GET /api/v1/leaderboard` 응답의 `environmentImpact` 필드를 사용한다.

```json
{
  "environmentImpact": {
    "savedWorkHours": 104.20,
    "electricitySavedKwh": 8.336,
    "co2SavedKg": 3.985,
    "smartphoneChargeEquivalentCount": 321
  }
}
```

필드 의미:

- `savedWorkHours`: 추정 업무 절감 시간(h)
- `electricitySavedKwh`: 추정 전기 절감량(kWh)
- `co2SavedKg`: 추정 CO2 절감량(kgCO2e)
- `smartphoneChargeEquivalentCount`: 추정 CO2 절감량을 스마트폰 충전 횟수로 환산한 값

### Weekly Snapshot Policy

`environmentImpact`는 실시간 계산값이 아니라 주간 스냅샷 값이다.

- 스케줄러 실행 시점: 매주 월요일 00:00
- 계산 대상 기간: 직전 주 월요일 00:00 이상, 이번 주 월요일 00:00 미만
- 저장 테이블: `esg_metrics_weekly`
- 화면 조회 방식: 최신 `esg_metrics_weekly` 스냅샷을 리더보드 응답에 포함

### Saved Work Time Formula

특정 주 W의 전체 추정 업무 절감 시간은 아래 수식으로 계산한다.

```text
특정 주 W의 전체 추정 업무 절감 시간(h)
=
Σ 날짜 d∈W Σ 사용자 u min(사용자 u의 d일 인용 포함 챗봇 답변 수 × 3분, 37.8분) ÷ 60
```

인용 포함 챗봇 답변 기준:

```sql
cm.sender_type = 'ASSISTANT'
AND cm.answerable = TRUE
AND cm.deleted_at IS NULL
AND cm.is_deleted = 'N'
AND cs.deleted_at IS NULL
AND cs.is_deleted = 'N'
AND JSON_LENGTH(cm.references_json) > 0
```

계산 기준:

- 인용 포함 챗봇 답변 1건당 추정 절감 시간: 3분
- 사용자별 일간 최대 인정 절감 시간: 37.8분
- 37.8분은 McKinsey의 지식 검색 시간 절감 가능성 자료를 MVP 기준으로 보수 적용한 값이다.
  - 직원의 일일 정보 탐색 시간: 약 1.8시간 = 108분
  - 지식 공유 및 검색 환경 개선 시 최대 절감률: 35%
  - 108분 × 35% = 37.8분

### Electricity and CO2 Formula

추정 업무 절감 시간을 기준으로 전기 절감량과 CO2 절감량을 계산한다.

```text
추정 전기 절감량(kWh)
=
추정 업무 절감 시간(h) × 0.08(kW)
```

```text
추정 CO2 절감량(kgCO2e)
=
추정 전기 절감량(kWh) × 0.478(kgCO2e/kWh)
```

현재 구현 상수:

- `DEVICE_POWER_KWH_PER_HOUR = 0.08`
- `ELECTRICITY_EMISSION_FACTOR_KG_CO2E_PER_KWH = 0.478`

### Smartphone Charge Equivalent Formula

CO2 절감량은 사용자가 더 직관적으로 이해할 수 있도록 스마트폰 충전 횟수로 환산한다.

미국 EPA Greenhouse Gas Equivalencies Calculator 기준:

```text
1 smartphone charged
=
1.24 × 10^-5 metric tons CO2
=
0.0124 kgCO2
```

따라서 스마트폰 충전 환산 횟수는 아래처럼 계산한다.

```text
스마트폰 충전 환산 횟수
=
추정 CO2 절감량(kgCO2e) ÷ 0.0124(kgCO2/charge)
```

예시:

```text
3.985 kgCO2e ÷ 0.0124 kgCO2/charge
= 321.37
≈ 321회
```

프론트엔드 표시 문구는 실제 스마트폰 충전을 줄였다고 단정하지 않고, CO2 환산 표현으로 작성한다.

권장 문구:

```text
스마트폰 약 321회 충전 시 발생하는 CO2와 비슷한 양을 줄였어요.
```

또는:

```text
스마트폰 약 321회 충전 분량의 CO2 절감 효과
```

피해야 할 문구:

```text
스마트폰 321회 충전을 아꼈어요.
```

이 문구는 실제 스마트폰 충전 행위를 줄였다는 의미로 오해될 수 있다.

### References

- McKinsey Global Institute, "The social economy: Unlocking value and productivity through social technologies" (2012): employees spend about 1.8 hours per day searching and gathering information, and improved knowledge sharing/search can reduce search time by up to 35%.
- U.S. EPA, "Greenhouse Gas Equivalencies Calculator - Calculations and References": Number of smartphones charged uses `1.24 × 10^-5 metric tons CO2 / smartphone charged`.
  - https://www.epa.gov/energy/greenhouse-gas-equivalencies-calculator-calculations-and-references

## 개발 목표

리더보드는 사용자별 연간 누적 ESG 점수를 기준으로 순위를 보여주는 기능이다.

구현 대상 API는 `GET /api/v1/leaderboard`이며, 리더보드 화면은 총 4개 파트로 구성될 예정이다. 현재 문서는 첫 번째 파트인 ESG 점수 상위 3명 조회와 두 번째 파트인 로그인 사용자 본인 리더보드 요약 정보를 기준으로 정리한다.

## 핵심 개념

- 사용자의 활동에 따라 포인트가 지급된다.
- 1년 동안 누적된 포인트를 플랫폼에서는 `ESG 점수`라고 부른다.
- `user_points.current_point`는 현재 보유 포인트이다.
- `user_points.esg_score`는 연간 누적 ESG 점수이다.
- `user_points.esg_score`는 차감 대상이 아니므로 음수가 될 수 없다.
- 포인트 차감은 `current_point`에만 적용되며, `esg_score`는 연간 누적 점수로 유지된다.
- ESG 점수는 1년 단위로 초기화된다.
- ESG 등급은 항상 `0 이상`의 `esg_score` 기준으로 산정한다.
- 리더보드 순위는 `user_points.esg_score` 기준으로 산정한다.

## 첫 번째 파트: ESG 점수 TOP 3

화면에는 ESG 점수 기준 상위 1~3위 사용자를 보여준다.

표시 후보 데이터:

- 순위
- 사용자 ID
- 사용자 이름 또는 닉네임
- 부서명
- ESG 등급
- ESG 점수

조회 기준:

- 매주 월요일 00시에 생성된 리더보드 스냅샷 기준
- `user_points.esg_score` 내림차순
- 동점자는 `user_points.user_id` 오름차순
- API 응답에서는 `rank_no <= 3`인 사용자만 TOP 3로 반환
- 삭제되었거나 비활성 처리된 사용자는 스냅샷 생성 시점에 제외한다.

## 두 번째 파트: 내 리더보드 요약

두 번째 파트는 현재 로그인한 사용자 본인의 리더보드 요약 정보를 보여준다.

표시 후보 데이터:

- 내 순위
- 내 닉네임
- 내 부서명
- 내 ESG 등급
- 내 ESG 점수
- 내가 등록한 답변 수
- 내가 등록한 답변 중 채택된 답변 수

데이터 기준:

- 내 순위는 실시간 계산이 아니라 매주 월요일 00시에 생성된 리더보드 스냅샷 기준으로 보여준다.
- 내 ESG 등급은 `user_points.grade_id` 기준이다.
- 내 ESG 점수는 `user_points.esg_score` 기준이다.
- 내가 등록한 답변 수는 `worki_answers.author_id = 현재 사용자 ID` 기준으로 집계한다.
- 내가 등록한 답변 중 채택된 수는 `worki_answers.author_id = 현재 사용자 ID`이고 `worki_answers.accepted = true`인 데이터 기준으로 집계한다.
- 삭제된 답변은 집계에서 제외한다.

구현상 중요한 점:

- 첫 번째 파트만 고려하면 `leaderboard_snapshots`에 TOP 3만 저장해도 된다.
- 하지만 두 번째 파트에서 TOP 3 밖에 있는 사용자의 내 순위도 보여줘야 하므로, 스냅샷에는 전체 활성 사용자 순위를 저장해야 한다.
- API 응답에서는 전체 스냅샷 중 `rank_no <= 3`만 첫 번째 파트로 반환하고, 현재 로그인 사용자 ID에 해당하는 row를 두 번째 파트로 반환한다.
- 닉네임, 부서명, 등급명, 등급 이미지 URL은 스냅샷 생성 시점의 표시값을 `leaderboard_snapshots`에 함께 저장한다.
- 조회 API는 사용자/부서/등급 테이블을 다시 조인해서 화면 표시값을 가져오지 않고, 스냅샷에 저장된 표시값을 그대로 반환한다.

## 네 번째 파트: 전체 ESG 점수 합계

네 번째 파트는 전체 활성 사용자의 ESG 점수 총합을 보여준다.

표시 데이터:

- 전체 ESG 점수 합계

데이터 기준:

- 전체 ESG 점수 합계는 실시간 `user_points` 기준이 아니라 매주 월요일 00시에 생성된 최신 리더보드 스냅샷 기준으로 보여준다.
- 합계는 최신 `ranking_period_start`에 해당하는 `leaderboard_snapshots.esg_score`를 모두 더해서 계산한다.
- 비활성 사용자는 스냅샷 생성 시점에 제외되므로 합계에도 포함되지 않는다.
- 삭제된 스냅샷 row는 합계에서 제외한다.
- 최초 스냅샷이 없으면 `totalEsgScore = 0`으로 반환한다.

구현 기준:

```sql
SELECT COALESCE(SUM(l.esg_score), 0)
FROM leaderboard_snapshots l
WHERE l.ranking_period_start = :rankingPeriodStart
  AND l.deleted_at IS NULL
```

## ESG 등급 기준

| 등급 | `esg_grade.grade_id` | ESG 점수 범위 |
|---|---:|---|
| Lv1 | 1 | `0 <= user_points.esg_score <= 100` |
| Lv2 | 2 | `101 <= user_points.esg_score <= 300` |
| Lv3 | 3 | `301 <= user_points.esg_score <= 700` |
| Lv4 | 4 | `701 <= user_points.esg_score` |

## 순위 갱신 정책

리더보드 순위는 매주 월요일 00시 기준으로 업데이트된 값을 화면에 보여준다.

따라서 API 호출 시점마다 실시간으로 `user_points`를 정렬하는 방식이 아니라, 매주 월요일 00:00에 계산된 전체 사용자 순위 스냅샷을 저장하고 조회 API는 저장된 스냅샷을 반환하는 방식으로 구현한다.

현재 스케줄러는 프로젝트의 기존 스케줄러 스타일에 맞춰 `@Scheduled(cron = "0 0 0 * * MON")` 형태로 동작한다. 따라서 운영 환경에서는 JVM 기본 timezone이 KST 기준으로 설정되어 있어야 월요일 00시 정책과 일치한다.

이유:

- 화면에 표시되는 순위가 한 주 동안 안정적으로 유지된다.
- "월요일 00시 기준 순위"라는 정책을 명확하게 보장할 수 있다.
- 첫 번째 파트 TOP 3와 두 번째 파트 내 순위가 같은 기준을 사용하게 된다.

## 스냅샷 저장 정책

`leaderboard_snapshots`는 주차별 리더보드 결과를 저장한다.

저장 필드:

- `ranking_period_start`: 해당 스냅샷의 기준 주차 월요일 날짜
- `rank_no`: 기준 주차의 사용자 순위
- `user_id`: 사용자 ID
- `nickname`: 스냅샷 생성 시점의 사용자 닉네임
- `department_name`: 스냅샷 생성 시점의 부서명
- `grade_id`: 스냅샷 생성 시점의 ESG 등급 ID
- `grade_name`: 스냅샷 생성 시점의 ESG 등급명
- `grade_image_url`: 스냅샷 생성 시점의 ESG 등급 이미지 URL
- `esg_score`: 스냅샷 생성 시점의 ESG 점수
- `calculated_at`: 스냅샷 계산 시각

보관 정책:

- 기존 주차 스냅샷은 삭제하지 않고 주차별 이력으로 보관한다.
- 같은 `ranking_period_start`의 스냅샷이 이미 존재하면 다시 생성하지 않는다.
- 다중 인스턴스 또는 중복 실행을 막기 위해 DB advisory lock을 사용한다.
- 기존 데이터를 삭제한 뒤 다시 저장하는 방식은 조회 중 빈 화면이 생길 수 있으므로 사용하지 않는다.

## 추천 구현 방식

1. 리더보드 스냅샷 테이블을 추가한다.
2. 매주 월요일 00:00에 스케줄러가 실행된다.
3. 스케줄러는 `user_points.esg_score DESC`, `user_points.user_id ASC` 기준으로 전체 활성 사용자 순위를 계산한다.
4. 계산된 전체 사용자 순위와 화면 표시용 사용자/부서/등급 정보를 스냅샷 테이블에 저장한다.
5. `GET /api/v1/leaderboard`는 최신 스냅샷 테이블에서 데이터를 조회한다.
6. 응답의 `topRankers`에는 `rank_no <= 3` 데이터만 반환한다.
7. 응답의 `myRank` 또는 `mySummary`에는 현재 로그인 사용자 ID에 해당하는 스냅샷 row와 답변/채택 집계값을 반환한다.
8. 응답의 `calculatedAt`은 TOP 3 결과가 아니라 스냅샷 테이블의 `calculated_at` 기준으로 반환한다.

## API

```http
GET /api/v1/leaderboard
```

인증:

- Access Token 필요
- 두 번째 파트에서 현재 로그인 사용자의 리더보드 요약을 반환해야 하므로 `@AuthenticationPrincipal Long userId`가 필요하다.

예상 응답 구조:

```json
{
  "rankingPeriodStart": "2026-06-08",
  "calculatedAt": "2026-06-08T00:00:00",
  "topRankers": [
    {
      "rank": 1,
      "userId": 1,
      "nickname": "hong",
      "departmentName": "개발팀",
      "gradeId": 4,
      "gradeName": "Lv4",
      "gradeImageUrl": null,
      "esgScore": 1200
    }
  ],
  "mySummary": {
    "rank": 15,
    "userId": 10,
    "nickname": "kim",
    "departmentName": "운영팀",
    "gradeId": 2,
    "gradeName": "Lv2",
    "gradeImageUrl": null,
    "esgScore": 230,
    "answerCount": 12,
    "acceptedAnswerCount": 3
  },
  "totalEsgScore": 123456
}
```

초기 응답 정책:

- `user_points.esg_score`에 누적 데이터가 있더라도 `leaderboard_snapshots`에 스냅샷 row가 아직 없으면 빈 응답을 반환한다.
- 스케줄러가 월요일 00시에 실행되어 스냅샷을 생성한 이후부터 최신 스냅샷 데이터를 반환한다.
- 스냅샷이 없는 경우 `rankingPeriodStart = null`, `calculatedAt = null`, `topRankers = []`, `mySummary = null`, `totalEsgScore = 0`으로 응답한다.
- 로그인 사용자가 최신 스냅샷에 없으면 `mySummary = null`로 응답한다.

응답 구조는 남은 1개 파트의 요구사항이 확정되면 함께 확장한다.

## 구현 전 확인 필요

- 리더보드 세 번째 파트의 데이터 기준 및 응답 구조 확정 필요

## 현재까지의 결론

리더보드 첫 번째 파트는 `leaderboard_snapshots`에서 최신 주차의 `rank_no <= 3` 데이터를 조회하면 구현 가능하다.

두 번째 파트까지 고려하면 스냅샷 저장 범위는 TOP 3가 아니라 전체 활성 사용자 순위여야 한다. 그래야 TOP 3 밖의 사용자도 월요일 00시 기준 내 순위를 확인할 수 있다.

네 번째 파트는 최신 주차의 전체 스냅샷 `esg_score` 합계로 구현한다. 따라서 TOP 3, 내 순위, 전체 ESG 점수 합계가 모두 같은 월요일 00시 기준을 사용한다.

