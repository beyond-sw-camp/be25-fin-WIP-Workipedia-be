# API Contract

> 臾몄꽌 ?좏삎: API Contract
> ?곹깭: Draft
> ?뺣낯 ?꾩튂: `docs/004-api/api-contract.md`
> 愿??臾몄꽌: `docs/001-reference/prd.md`, `docs/001-reference/trd.md`, `docs/006-planning/wbs.md`
> 踰꾩쟾: v0.3
> 理쒖쥌 ?섏젙: 2026-06-01

## 1. 紐⑹쟻

?꾨줎?몄뿏?쒖? 諛깆뿏?쒓? 媛숈? ?붿껌/?묐떟 ?뺤떇??湲곗??쇰줈 媛쒕컻?섍린 ?꾪븳 API 怨꾩빟 珥덉븞?대떎.

??臾몄꽌???뺤젙 API 紐낆꽭媛 ?꾨땲?? 2026-06-26 諛고룷 紐⑺몴源뚯? MVP 媛쒕컻 異⑸룎??以꾩씠湲??꾪븳 湲곗??대떎. API媛 諛붾뚮㈃ ??臾몄꽌瑜?癒쇱? ?섏젙?섍퀬 ?대떦?먯뿉寃?怨듭쑀?쒕떎.

## 2. 怨듯넻 洹쒖튃

### 2.1 Base URL

| ?섍꼍        | Base URL                       |
| ----------- | ------------------------------ |
| local       | `http://localhost:8080/api/v1` |
| dev/staging | 誘몄젙                           |
| production  | 誘몄젙                           |

### 2.2 ?몄쬆

?곕━ ?쒕퉬?ㅻ뒗 JWT(JSON Web Token) 湲곕컲 ?몄쬆 諛⑹떇???ъ슜?쒕떎.

```http
Authorization: Bearer <accessToken>
```

#### 濡쒓렇???몄쬆 ?먮쫫

1. ?ъ슜?먮뒗 ?щ쾲怨?鍮꾨?踰덊샇瑜??낅젰?섏뿬 濡쒓렇?명븳??
2. ?쒕쾭???ъ슜???뺣낫瑜?寃利앺븳 ??JWT ?좏겙??諛쒓툒?쒕떎.
3. ?몄쬆 ?깃났 ??`Access Token`? Response Body瑜??듯빐 諛섑솚?쒕떎.
4. ?몄쬆 ?깃났 ??`Refresh Token`? 荑좏궎(Set-Cookie)瑜??듯빐 諛쒓툒?쒕떎.
5. ?쒕쾭??諛쒓툒??`Refresh Token`??Redis????ν븯??愿由ы븳??
6. ?대씪?댁뼵?몃뒗 濡쒓렇???묐떟 Body?먯꽌 `Access Token`??諛쏆븘 ??ν븳??
7. ?댄썑 ?몄쬆???꾩슂??API瑜??몄텧???뚮쭏??`Authorization` ?ㅻ뜑??`Access Token`???ы븿?섏뿬 ?붿껌?쒕떎.
8. ?쒕쾭???꾨떖諛쏆? `Access Token`??寃利앺븳 ???ъ슜???몄쬆 諛?沅뚰븳 寃?щ? ?섑뻾?쒕떎.

### 2.3 怨듯넻 ?묐떟

?깃났 ?묐떟? `ResponseEntity<T>`濡?吏곸젒 諛섑솚?쒕떎.
?묐떟 Body瑜?`code`, `status`, `message`, `data` ?뺥깭??怨듯넻 媛앹껜濡?媛먯떥吏 ?딅뒗??

?묐떟 ?곗씠?곌? ?덈뒗 寃쎌슦:

```json
{
  "id": 1,
  "name": "?덉떆"
}
```

紐⑸줉 ?묐떟:

```json
[
  {
    "id": 1,
    "name": "?덉떆"
  }
]
```

?묐떟 ?곗씠?곌? ?녿뒗 寃쎌슦:

```http
200 OK
```

援ы쁽 湲곗?:

- Spring Controller??`ResponseEntity<T>`瑜?吏곸젒 諛섑솚?쒕떎.
- ?앹꽦 ?깃났? `ResponseEntity.status(HttpStatus.CREATED).body(response)`瑜??ъ슜?쒕떎.
- ?쇰컲 議고쉶/?섏젙 ?깃났? `ResponseEntity.ok(response)`瑜??ъ슜?쒕떎.
- ?묐떟 ?곗씠?곌? ?녿뒗 ?깃났 ?묐떟? `ResponseEntity.ok().build()` ?먮뒗 `ResponseEntity.noContent().build()`瑜??ъ슜?쒕떎.
- 紐⑸줉 議고쉶??諛곗뿴 ?먮뒗 ?섏씠吏 媛앹껜瑜?吏곸젒 諛섑솚?쒕떎.
- ?먮윭 ?묐떟? 怨듯넻 ?덉쇅 泥섎━ 援ъ“瑜??곕Ⅸ??
- 怨듯넻 ?먮윭 肄붾뱶??`bad_request`, `unauthorized`, `forbidden`, `not_found`, `conflict`, `internal_error`瑜??ъ슜?쒕떎.
- ?꾨찓???먮윭 肄붾뱶??`{domain}-{number}` ?뺤떇???ъ슜?쒕떎. ?? `auth-001`, `ticket-001`, `worki-001`

### 2.4 ?섏씠吏 ?묐떟

```json
{
  "code": 200,
  "status": "OK",
  "message": "?깃났",
  "data": {
    "content": [{}],
    "pageInfo": {
      "page": 1,
      "size": 10,
      "totalElements": 0,
      "totalPages": 0,
      "hasNext": false,
      "hasPrevious": false
    }
  }
}
```

## 3. ?대떦?먮퀎 API 踰붿쐞

| ?곸뿭                      | 諛깆뿏???대떦    | ?꾨줎???대떦 |
| ------------------------- | -------------- | ----------- |
| Auth                      | ?댁뒳??        | ?⑺씗??     |
| 梨쀫큸 ?몄뀡/硫붿떆吏          | ?댁뒳??        | 誘쇱젙湲?     |
| 梨쀫큸 ?듬?/RAG/?꾪솚        | 源吏꾪쁺         | 誘쇱젙湲?     |
| ?뚰궎 寃뚯떆??              | 誘쇱젙湲?        | ?⑺씗??     |
| FAQ                       | 誘쇱젙湲?        | ?⑺씗??     |
| ?뚮┝                      | ?댁뒳??        | ?⑺씗??     |
| ?곗폆                      | 源吏꾪쁺         | ?⑺씗??     |
| ?곗폆 吏?앺솕               | 源吏꾪쁺, 源媛??| ?⑺씗??     |
| 愿由ъ옄 ??쒕낫??          | 源媛??        | ?⑺씗??     |
| 愿由ъ옄 留ㅻ돱??遺???ъ슜??| 源媛??        | ?⑺씗??     |
| ?ъ씤??                   | 源媛??        | ?⑺씗??     |
| ESG ?깃툒                  | 源媛??        | ?⑺씗??     |
| ESG 吏??                 | 源媛??        | ?⑺씗??     |

## 4. Auth API

?대떦: ?댁뒳??

| Method | Path                               | ?ㅻ챸                          | ?몄쬆               |
| ------ | ---------------------------------- | ----------------------------- | ------------------ |
| GET    | `/departments`                     | ?뚯썝媛??遺??紐⑸줉 議고쉶       | 遺덊븘??            |
| POST   | `/auth/signup/code`                | ?뚯썝媛???몄쬆肄붾뱶 諛쒖넚        | 遺덊븘??            |
| POST   | `/auth/signup/code/verify`         | ?뚯썝媛???몄쬆肄붾뱶 ?뺤씤        | 遺덊븘??            |
| POST   | `/auth/signup`                     | ?뚯썝媛??                     | 遺덊븘??            |
| POST   | `/auth/login`                      | 濡쒓렇??                       | 遺덊븘??            |
| POST   | `/auth/token/refresh`              | ?좏겙 ?щ컻湲?                  | Refresh Token ?꾩슂 |
| POST   | `/auth/logout`                     | 濡쒓렇?꾩썐                      | Access Token ?꾩슂 |
| POST   | `/auth/password-reset/code`        | 鍮꾨?踰덊샇 ?ъ꽕???몄쬆肄붾뱶 諛쒖넚 | 遺덊븘??            |
| POST   | `/auth/password-reset/code/verify` | 鍮꾨?踰덊샇 ?ъ꽕???몄쬆肄붾뱶 ?뺤씤 | 遺덊븘??            |
| PATCH  | `/auth/password-reset`             | 鍮꾨?踰덊샇 ?ъ꽕??              | 蹂몄씤 ?몄쬆 ?꾩슂     |
| GET    | `/me/profile`                      | 留덉씠?섏씠吏 議고쉶               | Access Token ?꾩슂  |

### GET `/departments`

- ?뚯썝媛???붾㈃?먯꽌 遺?쒕챸 ?좏깮李쎌뿉 ?쒖떆??遺??紐⑸줉??議고쉶?쒕떎.
- 遺??紐⑸줉 媛쒖닔??DB???깅줉??遺???곗씠?곕? 湲곗??쇰줈 ?쒕떎.

Request: ?놁쓬

Response:

```json
[
  {
    "departmentId": 1,
    "departmentName": "?몄궗?"
  },
  {
    "departmentId": 2,
    "departmentName": "珥앸Т?"
  },
  {
    "departmentId": 3,
    "departmentName": "IT吏?먰?"
  }
]
```

### POST `/auth/signup/code`

- ?몄쬆肄붾뱶???レ옄 6?먮━濡??앹꽦?쒕떎.

Request:

```json
{
  "email": "user@company.com"
}
```

Response:

```http
200 OK
```

### POST `/auth/signup/code/verify`

- ?몄쬆肄붾뱶???レ옄 6?먮━濡??낅젰?쒕떎.

Request:

```json
{
  "email": "user@company.com",
  "code": "123456"
}
```

Response:

```http
200 OK
```

### POST `/auth/signup`

- ?뚯썝媛?낆? ?대찓???몄쬆肄붾뱶 ?뺤씤???꾨즺???대찓?쇱뿉 ??댁꽌留?媛?ν븯??
- `passwordConfirm`? ?꾨줎?몄뿉??`password`????쇱튂 ?щ?瑜?寃利앺븯硫?Request Body?먮뒗 ?ы븿?섏? ?딅뒗??

Request:

```json
{
  "employeeId": "20260001",
  "departmentId": 1,
  "email": "user@company.com",
  "password": "abc12345"
}
```

Response:

```json
{
  "userId": 123,
  "role": "USER",
  "nickname": "?덈Ъ?섎━?붾뜲?댁?",
  "status": "ACTIVE"
}
```

### POST `/auth/login`

Request:

```json
{
  "employeeId": "20260001",
  "password": "abc12345"
}
```

Response:

```json
{
  "accessToken": "jwt-access-token",
  "userId": 123,
  "departmentId": 1,
  "role": "USER",
  "nickname": "?덈Ъ?섎━?붾뜲?댁?",
  "status": "ACTIVE"
}
```

Response Header:

```http
Set-Cookie: refreshToken=jwt-refresh-token; HttpOnly; Secure; SameSite=Lax; Path=/api/v1/auth
```

- Refresh Token 荑좏궎??Access Token ?щ컻湲?API ?몄텧???꾪븳 媛믪씠??
- ?쇰컲 ?몄쬆 API??Refresh Token???꾨땲??`Authorization` ?ㅻ뜑??Access Token?쇰줈 ?몄쬆?쒕떎.
- ?덈? ?ㅼ뼱 `/api/v1/me`濡??쒖옉?섎뒗 留덉씠?섏씠吏 議고쉶 API??Access Token?쇰줈 ?몄쬆?쒕떎.

### POST `/auth/token/refresh`

- 濡쒓렇????諛쒓툒??Refresh Token 荑좏궎瑜?寃利앺븳 ????Access Token怨???Refresh Token???④퍡 諛쒓툒?쒕떎.
- ??Refresh Token? Redis????ν븯怨? 湲곗〈 Refresh Token? ?먭린?쒕떎.
- Access Token??留뚮즺??寃쎌슦 ?몄텧?섎ŉ, `Authorization` ?ㅻ뜑???ъ슜?섏? ?딅뒗??

Request Header:

```http
Cookie: refreshToken=jwt-refresh-token
```

Response:

```json
{
  "accessToken": "jwt-new-access-token"
}
```

Response Header:

```http
Set-Cookie: refreshToken=jwt-new-refresh-token; HttpOnly; Secure; SameSite=Lax; Path=/api/v1/auth
```

### POST `/auth/logout`

- Request Header의 Access Token을 검증하고, 토큰의 userId로 로그아웃 대상 사용자를 식별합니다.
- 식별된 userId 기준으로 Redis에 저장된 Refresh Token을 삭제합니다.
- Refresh Token 쿠키를 만료시켜 클라이언트에서 제거합니다.
- Access Token이 없거나 유효하지 않으면 `401 Unauthorized`를 반환합니다.

Request Header:

```http
Authorization: Bearer jwt-access-token
```

Response:

```http
200 OK
```

Response Header:

```http
Set-Cookie: refreshToken=; Max-Age=0; HttpOnly; Secure; SameSite=Lax; Path=/api/v1/auth
```

### POST `/auth/password-reset/code`

- 비밀번호 재설정을 위한 인증코드를 이메일로 발송한다.
- 요청한 사번과 이메일이 사용자 계정에 등록된 정보와 일치해야 한다.
- 인증코드는 숫자 6자리로 생성한다.
- 인증코드는 Redis에 TTL과 함께 저장한다.

Request:

```json
{
  "employeeId": "20260001",
  "email": "user@company.com"
}
```

Response:

```http
200 OK
```

## 5. Chatbot API

?대떦: ?댁뒳?? 源吏꾪쁺

| Method | Path                                                               | ?ㅻ챸                                             | ?몄쬆 |
| ------ | ------------------------------------------------------------------ | ------------------------------------------------ | ---- |
| POST   | `/chatbot/sessions`                                                | 梨쀫큸 ?몄뀡 ?앹꽦                                   | ?꾩슂 |
| GET    | `/chatbot/sessions`                                                | ???몄뀡 紐⑸줉                                     | ?꾩슂 |
| GET    | `/chatbot/sessions/{sessionId}/messages`                           | ?몄뀡 硫붿떆吏 議고쉶                                 | ?꾩슂 |
| POST   | `/chatbot/sessions/{sessionId}/messages`                           | 吏덈Ц ?꾩넚 諛??듬? ?앹꽦                           | ?꾩슂 |
| GET    | `/chatbot/sessions/{sessionId}/messages/{messageId}/worki-support` | ?뚰궎 吏덈Ц ?깅줉 吏??(梨쀫큸 硫붿떆吏 湲곕컲 珥덉븞 諛섑솚) | ?꾩슂 |

### AI ?댁쁺 API

?대떦: 源吏꾪쁺

| Method | Path                    | ?ㅻ챸                                         | ?몄쬆         |
| ------ | ----------------------- | -------------------------------------------- | ------------ |
| POST   | `/ai/fine-tune/trigger` | APPROVED 吏???곗씠??湲곕컲 ?뚯씤?쒕떇 ?ㅽ뻾 ?붿껌 | SYSTEM_ADMIN |
| GET    | `/ai/fine-tune/status`  | ?뚯씤?쒕떇 吏꾪뻾 ?곹깭 議고쉶                      | SYSTEM_ADMIN |
| GET    | `/ai/model/current`     | ?꾩옱 紐⑤뜽 踰꾩쟾 諛??대뙌???뺣낫 議고쉶           | SYSTEM_ADMIN |
| POST   | `/ai/prompt/update`     | base_system/admin_context 媛깆떊               | SYSTEM_ADMIN |

### POST `/chatbot/sessions/{sessionId}/messages`

Request:

```json
{
  "content": "?곗감 ?좎껌? ?대뵒???섎굹??"
}
```

Response:

```json
{
  "messageId": 101,
  "answer": "?곗감??HR ?쒖뒪?쒖뿉???좎껌?????덉뒿?덈떎.",
  "answerable": true,
  "references": [
    {
      "type": "MANUAL",
      "sourceId": 10,
      "title": "?닿? 洹쒖젙",
      "url": "/manuals/10",
      "chunkId": 1001
    }
  ],
  "nextAction": "SHOW_SOURCES"
}
```

洹쇨굅 遺議??묐떟:

```json
{
  "messageId": 102,
  "answer": "?꾩옱 ?깅줉??臾몄꽌?먯꽌 ?뺤떎???듬???李얠? 紐삵뻽?듬땲??",
  "answerable": false,
  "references": [],
  "nextAction": "CREATE_WORKI",
  "draftQuestion": {
    "title": "?곗감 ?좎껌 愿??臾몄쓽",
    "content": "?곗감 ?좎껌? ?대뵒???섎굹??"
  }
}
```

?붿껌 ?꾪솚 ?묐떟:

```json
{
  "messageId": 103,
  "answer": "臾몄꽌 寃?됰쭔?쇰줈 ?닿껐?섍린 ?대졄?듬땲?? ?대떦 遺??泥섎━媛 ?꾩슂???붿껌?쇰줈 ?꾪솚?????덉뒿?덈떎.",
  "answerable": false,
  "references": [],
  "nextAction": "CREATE_TICKET",
  "draftTicket": {
    "title": "VPN ?묒냽 ?ㅻ쪟 泥섎━ ?붿껌",
    "content": "VPN ?묒냽 ?ㅻ쪟 泥섎━瑜??붿껌?⑸땲??"
  }
}
```

## 6. Worki API

?대떦: ?댁뒳??

| Method | Path                                           | ?ㅻ챸                     | ?몄쬆 |
| ------ | ---------------------------------------------- | ------------------------ | ---- |
| GET    | `/worki/questions`                             | 吏덈Ц 紐⑸줉                | ?꾩슂 |
| POST   | `/worki/questions`                             | 吏덈Ц ?깅줉                | ?꾩슂 |
| GET    | `/worki/questions/{questionId}`                | 吏덈Ц ?곸꽭                | ?꾩슂 |
| PATCH  | `/worki/questions/{questionId}`                | 吏덈Ц ?섏젙                | ?꾩슂 |
| POST   | `/worki/questions/{questionId}/answers`        | ?듬? ?깅줉                | ?꾩슂 |
| POST   | `/worki/questions/{questionId}/ticket-answers` | ?곗폆 怨듭떇 ?듬? ?뚰궎 ?깅줉 | ?꾩슂 |
| PATCH  | `/worki/answers/{answerId}/adopt`              | ?듬? 梨꾪깮                | ?꾩슂 |
| PUT    | `/worki/questions/{questionId}/reaction`       | 醫뗭븘???レ뼱??           | ?꾩슂 |

### POST `/worki/questions`

Request:

```json
{
  "title": "?곗감 ?좎껌 愿??臾몄쓽",
  "content": "?곗감 ?좎껌? ?대뵒???섎굹??",
  "sourceChatbotMessageId": 102
}
```

Response:

```json
{
  "questionId": 1,
  "title": "?곗감 ?좎껌 愿??臾몄쓽",
  "status": "WAITING",
  "authorNickname": "?몄엲1234"
}
```

## 7. Ticket API

?대떦: 源吏꾪쁺

| Method | Path                                         | ?ㅻ챸                            | ?몄쬆       |
| ------ | -------------------------------------------- | ------------------------------- | ---------- |
| POST   | `/tickets`                                   | ?곗폆 ?앹꽦                       | ?꾩슂       |
| GET    | `/tickets`                                   | ?곗폆 紐⑸줉, ?곹깭/遺???꾪꽣 議고쉶  | ?꾩슂       |
| GET    | `/tickets/{ticketId}`                        | ?곗폆 ?곸꽭                       | ?꾩슂       |
| PATCH  | `/tickets/{ticketId}/status`                 | ?곗폆 ?곹깭 蹂寃?                 | ?꾩슂       |
| PATCH  | `/tickets/{ticketId}/assignee`               | ????대떦??諛곗젙                | TEAM_ADMIN |
| POST   | `/tickets/{ticketId}/transfer-requests`      | TEAM_ADMIN ?곗폆 ?닿? ?붿껌       | TEAM_ADMIN |
| PATCH  | `/tickets/{ticketId}/refuse`                 | ?곗폆 諛섎젮                       | TEAM_ADMIN |
| POST   | `/tickets/{ticketId}/answers`                | ?대떦 遺??怨듭떇 ?듬?             | ?꾩슂       |
| POST   | `/tickets/{ticketId}/knowledge-candidates`   | 泥섎━ ?꾨즺 ?곗폆 吏?앺솕 ?꾨낫 ?깅줉 | ?꾩슂       |
| PATCH  | `/knowledge-candidates/{candidateId}/review` | 吏?앺솕 ?꾨낫 ?뱀씤/諛섎젮           | TEAM_ADMIN |

### POST `/tickets`

Request:

```json
{
  "questionId": null,
  "sourceChatbotMessageId": 102,
  "type": "REQUEST",
  "categoryId": 3,
  "priority": "MEDIUM",
  "title": "VPN ?묒냽 ?ㅻ쪟 泥섎━ ?붿껌",
  "content": "VPN ?묒냽 ?ㅻ쪟 泥섎━瑜??붿껌?⑸땲??",
  "attachmentIds": [1, 2]
}
```

- `priority` ?덉슜媛믪? `MEDIUM`, `HIGH`?대떎. ?앸왂?섎㈃ `MEDIUM`?쇰줈 ??ν븳??

Response:

```json
{
  "ticketId": 1,
  "status": "ASSIGNED",
  "priority": "MEDIUM",
  "assignedDepartmentId": 5,
  "assignedDepartmentName": "IT吏?먰?",
  "routingConfidenceScore": 87.5,
  "routingDecision": "AUTO_ASSIGNED",
  "recommendedAssignees": [
    {
      "userId": 12,
      "nickname": "?몄엲4821",
      "completedTicketCountLast30Days": 14
    }
  ],
  "routingReasons": [
    "?ㅼ썙?? VPN, ?묒냽 ?ㅻ쪟",
    "移댄뀒怨좊━: ?쒖뒪???묎렐",
    "愿??臾몄꽌: VPN ?묒냽 ?μ븷 泥섎━ 媛?대뱶"
  ]
}
```

?좊ː????? ?붿껌 Response:

```json
{
  "ticketId": 2,
  "status": "COMMON_QUEUE",
  "priority": "MEDIUM",
  "assignedDepartmentId": null,
  "assignedDepartmentName": null,
  "routingConfidenceScore": 63.0,
  "routingDecision": "COMMON_QUEUE",
  "candidateDepartments": [
    {
      "departmentId": 2,
      "departmentName": "?먯궛愿由ы?",
      "confidenceScore": 63.0
    },
    {
      "departmentId": 6,
      "departmentName": "?뺣낫蹂댁븞?",
      "confidenceScore": 58.0
    }
  ]
}
```

### GET `/tickets`

?곗폆 紐⑸줉??議고쉶?쒕떎. ?꾨줎?몄뿏?쒕뒗 媛숈? ?붾뱶?ъ씤?몄뿉???곹깭蹂? 遺?쒕퀎 ?꾪꽣瑜?議고빀???ъ슜?쒕떎.

Query Parameters:

| ?대쫫           | ???  | ?꾩닔 | ?ㅻ챸                                                         |
| -------------- | ------ | ---- | ------------------------------------------------------------ |
| `status`       | string | ?꾨땲??| ?곗폆 ?곹깭. ?? `COMMON_QUEUE`, `ASSIGNED`, `IN_PROGRESS`     |
| `departmentId` | number | ?꾨땲??| ?대떦 遺??ID. `assignedDepartmentId` 湲곗??쇰줈 議고쉶?쒕떎.      |
| `page`         | number | ?꾨땲??| ?섏씠吏 踰덊샇. 湲곕낯媛믪? `1`?대떎.                               |
| `size`         | number | ?꾨땲??| ?섏씠吏 ?ш린. 湲곕낯媛믪? `10`?대떎.                              |

Request ?덉떆:

```http
GET /api/v1/tickets?status=COMMON_QUEUE&departmentId=1&page=1&size=10
```

Response:

```json
{
  "content": [
    {
      "ticketId": 5,
      "status": "COMMON_QUEUE",
      "priority": "MEDIUM",
      "assignedDepartmentId": null,
      "assignedDepartmentName": null,
      "routingConfidenceScore": null,
      "routingDecision": "COMMON_QUEUE",
      "routingReasons": [],
      "candidateDepartments": [],
      "questionId": null,
      "sourceChatbotMessageId": null,
      "categoryId": null,
      "title": "?뚯뒪???곗폆 ?쒕ぉ",
      "content": "?뚯뒪???곗폆 ?댁슜",
      "assigneeId": null,
      "createdAt": "2026-06-04T17:01:49",
      "updatedAt": "2026-06-04T17:01:49"
    }
  ],
  "pageInfo": {
    "page": 1,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

鍮꾧퀬:

- `departmentId`??議고쉶 ?꾪꽣?대ŉ, 遺??諛곗젙/?щ같???숈옉???섎??섏? ?딅뒗??
- 怨듯넻 ?묒닔 ?먯쓽 遺???щ같?뺤? `PATCH /admin/common-queue/tickets/{ticketId}/department`瑜??ъ슜?쒕떎.

### PATCH `/tickets/{ticketId}/assignee`

Request:

```json
{
  "assigneeId": 12,
  "memo": "VPN 怨꾩젙 ?뺤씤 ??泥섎━ 遺?곷뱶由쎈땲??"
}
```

Response:

```json
{
  "ticketId": 1,
  "status": "IN_PROGRESS",
  "priority": "MEDIUM",
  "assigneeId": 12,
  "assigneeNickname": "?몄엲4821"
}
```

### POST `/tickets/{ticketId}/transfer-requests`

Request:

```json
{
  "suggestedDepartmentId": 2,
  "reason": "踰뺣Т 寃?좉? ?꾩슂??臾몄쓽?낅땲??"
}
```

Response:

```json
{
  "requestId": 1,
  "ticketId": 1,
  "transferStatus": "REQUESTED",
  "ticketStatus": "COMMON_QUEUE",
  "fromDepartmentId": 5,
  "fromDepartmentName": "寃쎌쁺吏?먰?",
  "suggestedDepartmentId": 2,
  "suggestedDepartmentName": "踰뺣Т?"
}
```

?닿? ?붿껌 ???곗폆? ?ㅻⅨ 遺?쒕줈 吏곸젒 ?대룞?섏? ?딄퀬 怨듯넻 ?묒닔 ?먮줈 ?대룞?쒕떎. ?댄썑 `SYSTEM_ADMIN`??怨듯넻 ?묒닔 ?먯뿉???대떦 遺?쒕? ?щ같?뺥븳??

### PATCH `/admin/common-queue/tickets/{ticketId}/department`

Request:

```json
{
  "departmentId": 2,
  "comment": "?닿? ?ъ쑀 ?뺤씤 ??踰뺣Т??쇰줈 ?щ같?뺥빀?덈떎."
}
```

Response:

```json
{
  "ticketId": 1,
  "status": "ASSIGNED",
  "assignedDepartmentId": 2,
  "assignedDepartmentName": "踰뺣Т?"
}
```

### POST `/tickets/{ticketId}/knowledge-candidates`

Request:

```json
{
  "draftTitle": "VPN ?묒냽 ?ㅻ쪟 泥섎━ ?덉감",
  "draftContent": "VPN ?묒냽 ?ㅻ쪟媛 諛쒖깮?섎㈃ 怨꾩젙 ?곹깭? 蹂댁븞 ?꾨줈洹몃옩 ?ㅽ뻾 ?щ?瑜?癒쇱? ?뺤씤????IT吏?먰????붿껌?⑸땲??"
}
```

Response:

```json
{
  "candidateId": 1,
  "ticketId": 1,
  "status": "REVIEW_REQUESTED"
}
```

### PATCH `/knowledge-candidates/{candidateId}/review`

Request:

```json
{
  "decision": "APPROVE",
  "reviewComment": "媛쒖씤 ?뺣낫 ?쒓굅 ?뺤씤. ?뚰궎 諛섏쁺 ?뱀씤?⑸땲??"
}
```

Response:

```json
{
  "candidateId": 1,
  "status": "PUBLISHED",
  "publishedWorkiQuestionId": 30
}
```

### Attachment API

?대떦: 源吏꾪쁺

| Method | Path                          | ?ㅻ챸                               | ?몄쬆 |
| ------ | ----------------------------- | ---------------------------------- | ---- |
| POST   | `/attachments`                | ?대?吏 ?낅줈?? `attachmentId` 諛섑솚 | ?꾩슂 |
| GET    | `/attachments/{attachmentId}` | ?대?吏 議고쉶                        | ?꾩슂 |

### POST `/attachments`

Request: `multipart/form-data`

| Field        | Type   | ?ㅻ챸                                                                |
| ------------ | ------ | ------------------------------------------------------------------- |
| `file`       | file   | ?대?吏 ?뚯씪                                                         |
| `targetType` | string | `TICKET` ??泥⑤? ???                                              |
| `targetId`   | number | ?대? ?앹꽦????곸뿉 ?곌껐?????ъ슜. ?곗폆 ?앹꽦 ???낅줈????null 媛??|

Response:

```json
{
  "attachmentId": 1,
  "contentType": "image/png",
  "fileSize": 123456,
  "url": "/attachments/1"
}
```

## 8. FAQ API

?대떦: 誘쇱젙湲?

| Method | Path                   | ?ㅻ챸             | ?몄쬆 |
| ------ | ---------------------- | ---------------- | ---- |
| GET    | `/faq/worki/popular`   | ?멸린 ?뚰궎        | ?꾩슂 |
| GET    | `/faq/manuals/popular` | ?멸린 留ㅻ돱??     | ?꾩슂 |
| GET    | `/faq/manuals/recent`  | 理쒓렐 ?깅줉 留ㅻ돱??| ?꾩슂 |

## 9. Notification API

?대떦: 誘쇱젙湲?

| Method | Path                                   | ?ㅻ챸             | ?몄쬆 |
| ------ | -------------------------------------- | ---------------- | ---- |
| GET    | `/notifications`                       | ?뚮┝ 紐⑸줉        | ?꾩슂 |
| GET    | `/notifications/unread-count`          | 誘몄씫? ?뚮┝ 媛?닔 | ?꾩슂 |
| PATCH  | `/notifications/{notificationId}/read` | 媛쒕퀎 ?쎌쓬        | ?꾩슂 |
| PATCH  | `/notifications/read-all`              | 紐⑤몢 ?쎌쓬        | ?꾩슂 |
| DELETE | `/notifications/{notificationId}`      | ?뚮┝ ??젣        | ?꾩슂 |

> Phase 2: `GET /notifications/stream` (SSE ?ㅼ떆媛??뚮┝) ??MVP??DB ???+ 議고쉶 API 湲곕컲?쇰줈 ?쒖옉 (ADR 007)

## 9-1. Flash Chat API

?대떦: 源吏꾪쁺, 誘쇱젙湲? 源媛??

| Type            | Path                    | ?ㅻ챸                  | ?몄쬆 |
| --------------- | ----------------------- | --------------------- | ---- |
| REST GET        | `/flash-chat/messages`  | ?꾩옱 ?쒖꽦 硫붿떆吏 紐⑸줉 | ?꾩슂 |
| STOMP Subscribe | `/topic/flash-chat`     | 硫붿떆吏/諛섏쓳 ?섏떊      | ?꾩슂 |
| STOMP Send      | `/app/flash-chat/send`  | 硫붿떆吏 ?꾩넚           | ?꾩슂 |
| STOMP Send      | `/app/flash-chat/react` | 醫뗭븘??諛섏쓳           | ?꾩슂 |

### GET `/flash-chat/messages`

Response:

```json
{
  "messages": [
    {
      "id": "018f6c9d-7b4f-7a9a-9c15-1b0f4b5ad111",
      "userId": 123,
      "nickname": "?몄엲4821",
      "content": "?곗감 諛섏감 李⑥씠媛 萸먯삁??",
      "replyToId": null,
      "likeCount": 2,
      "createdAt": "2026-06-03T10:00:00",
      "expiresAt": "2026-06-03T10:10:00"
    }
  ]
}
```

### `/app/flash-chat/send`

Payload:

```json
{
  "content": "?곗감 諛섏감 李⑥씠媛 萸먯삁??",
  "replyToId": null
}
```

### `/app/flash-chat/react`

Payload:

```json
{
  "messageId": "018f6c9d-7b4f-7a9a-9c15-1b0f4b5ad111",
  "reactionType": "LIKE"
}
```

## 10. Point API

?대떦: 源媛??

| Method | Path                      | ?ㅻ챸                  | ?몄쬆 |
| ------ | ------------------------- | --------------------- | ---- |
| GET    | `/points/me`              | ???ъ씤??            | ?꾩슂 |
| GET    | `/points/histories`       | ?ъ씤??蹂???대젰 ?꾩껜 | ?꾩슂 |


## 11. ESG Metrics API

?대떦: 源媛??

| Method | Path                 | ?ㅻ챸                 | ?몄쬆                     |
| ------ | -------------------- | -------------------- | ------------------------ |
| GET    | `/esg/metrics/me`    | ??ESG/湲곗뿬 吏??    | ?꾩슂                     |
| GET    | `/admin/esg/metrics` | 愿由ъ옄 ESG ?댁쁺 吏??| TEAM_ADMIN, SYSTEM_ADMIN |

Response:

```json
{
  "knowledgeShareCount": 12,
  "acceptedAnswerCount": 4,
  "estimatedSavedMinutes": 60,
  "esgScore": 320,
  "gradeName": "SILVER",
  "sourceBackedAnswerRate": 0.85,
  "ticketCompletionRate": 0.72
}
```

## 12. Admin API

?대떦: 源媛??

? 愿由ъ옄 ??쒕낫??
| Method | Path                                                     | ?ㅻ챸                        | ?몄쬆         |
| ------ | -------------------------------------------------------- | ------------------------- | ---------- |
| GET    | `/admin/team/dashboard/knowledge-trend`                  | ?붾퀎 吏?앺솕 ?뱀씤 嫄댁닔 異붿씠 議고쉶        | TEAM_ADMIN |
| GET    | `/admin/team/dashboard/chatbot-ticket-trend`             | ?붾퀎 AI 梨쀫큸 諛곗젙 ?곗폆 嫄댁닔 異붿씠 議고쉶   | TEAM_ADMIN |
| GET    | `/admin/team/knowledge-candidates`                       | 泥섎━ ?꾨즺 ?곗폆 湲곕컲 吏?앺솕 ?꾨낫 紐⑸줉 議고쉶  | TEAM_ADMIN |
| PATCH  | `/admin/team/knowledge-candidates/{candidateId}`         | 吏?앺솕 ?꾨낫 吏덈Ц/?듬? ?섏젙           | TEAM_ADMIN |
| POST   | `/admin/team/knowledge-candidates/{candidateId}/approve` | 吏?앺솕 ?꾨낫 ?뱀씤 諛??뚰궎 寃뚯떆???깅줉     | TEAM_ADMIN |
| DELETE | `/admin/team/knowledge-candidates/{candidateId}`         | 吏?앺솕 ?꾨낫 諛섎젮 諛???젣            | TEAM_ADMIN |
| GET    | `/admin/team/tickets/summary`                            | ?곕━ 遺???곗폆 ?붿빟 ?뺣낫 議고쉶         | TEAM_ADMIN |
| GET    | `/admin/team/tickets`                                    | ?곕━ 遺??諛곗젙 ?곗폆 紐⑸줉 議고쉶         | TEAM_ADMIN |
| GET    | `/admin/team/tickets/{ticketId}`                         | ?곕━ 遺???곗폆 ?곸꽭 議고쉶            | TEAM_ADMIN |
| POST   | `/admin/team/tickets/{ticketId}/transfer`                | ?곗폆 ?닿? ?ъ쑀 ?낅젰 ??怨듯넻 ?묒닔 ?먮줈 ?대룞 | TEAM_ADMIN |

?꾩껜 愿由ъ옄 ??쒕낫??
| Method | Path                                                | ?ㅻ챸                 | ?몄쬆           |
| ------ | --------------------------------------------------- | ------------------ | ------------ |
| GET    | `/admin/dashboard/auto-routing-rate`                | ?붾퀎 梨쀫큸 ?먮룞 諛곗젙瑜?異붿씠 議고쉶 | SYSTEM_ADMIN |
| GET    | `/admin/dashboard/ticket-trend`                     | ?붾퀎 ?꾩껜 ?곗폆 諛쒗뻾 異붿씠 議고쉶  | SYSTEM_ADMIN |
| GET    | `/admin/dashboard/department-statistics`            | 遺?쒕퀎 ?곗폆 ?꾪솴 議고쉶       | SYSTEM_ADMIN |
| GET    | `/admin/dashboard/routing-statistics`               | 遺?쒕퀎 ?먮룞 諛곗젙 ?깃났瑜?議고쉶   | SYSTEM_ADMIN |
| GET    | `/admin/common-queue/tickets`                       | 怨듯넻 ?묒닔 ??紐⑸줉 議고쉶      | SYSTEM_ADMIN |
| PATCH  | `/admin/common-queue/tickets/{ticketId}/department` | 怨듯넻 ?묒닔 ???곗폆 遺??諛곗젙   | SYSTEM_ADMIN |

愿由ъ옄 ?ㅼ젙
| Method | Path                                       | ?ㅻ챸                            | ?몄쬆           |
| ------ | ------------------------------------------ | ----------------------------- | ------------ |
| GET    | `/admin/settings/summary`                  | ?꾩껜 ?ъ슜???? ?뱀씪 濡쒓렇???? 珥?臾몄꽌 ??議고쉶 | SYSTEM_ADMIN |
| GET    | `/admin/points/search`                     | ?щ쾲?쇰줈 ?ъ슜???ъ씤??議고쉶               | SYSTEM_ADMIN |
| PATCH  | `/admin/points/{employeeId}/deduct`        | ?ъ씤??李④컧                        | SYSTEM_ADMIN |
| GET    | `/admin/departments`                       | 遺??紐⑸줉 議고쉶                      | SYSTEM_ADMIN |
| POST   | `/admin/departments`                       | 遺???깅줉                         | SYSTEM_ADMIN |
| PATCH  | `/admin/departments/{departmentId}`        | 遺???뺣낫 ?섏젙                      | SYSTEM_ADMIN |
| DELETE | `/admin/departments/{departmentId}`        | 遺????젣                         | SYSTEM_ADMIN |
| GET    | `/admin/users/search`                      | ?щ쾲?쇰줈 ?ъ슜??議고쉶                   | SYSTEM_ADMIN |
| PATCH  | `/admin/users/{userId}/status`             | ?ъ슜???쒖꽦??鍮꾪솢?깊솕 蹂寃?              | SYSTEM_ADMIN |
| GET    | `/admin/manuals`                           | 留ㅻ돱??紐⑸줉 議고쉶                     | SYSTEM_ADMIN |
| POST   | `/admin/manuals`                           | 留ㅻ돱???깅줉                        | SYSTEM_ADMIN |
| GET    | `/admin/manuals/{manualId}`                | 留ㅻ돱???곸꽭 議고쉶                     | SYSTEM_ADMIN |
| PATCH  | `/admin/manuals/{manualId}`                | 留ㅻ돱???섏젙 諛??좉퇋 踰꾩쟾 ?깅줉             | SYSTEM_ADMIN |
| DELETE | `/admin/manuals/{manualId}`                | 留ㅻ돱????젣                        | SYSTEM_ADMIN |
| GET    | `/admin/flash-chat/settings`               | 梨꾪똿 ?꾪꽣 ?ㅼ젙 議고쉶                   | SYSTEM_ADMIN |
| POST   | `/admin/flash-chat/blocked-words`          | 湲덉???異붽?                        | SYSTEM_ADMIN |
| DELETE | `/admin/flash-chat/blocked-words/{wordId}` | 湲덉?????젣                        | SYSTEM_ADMIN |



## 13. 誘몄젙 ??ぉ

| ??ぉ                                   | ?곹깭                                    | 寃곗젙 ?꾩슂??    |
| -------------------------------------- | --------------------------------------- | --------------- |
| Refresh Token ??μ냼                   | Redis ?뺤젙                              | ?댁뒳??         |
| SYSTEM_ADMIN ?대떦 議곗쭅                 | 湲곕낯: 寃쎌쁺吏?먰?, ?뚯궗蹂?議곗젙 媛??     | 源媛?? ? ?꾩껜 |
| ?곗폆 ?먮룞 諛곗젙 ?먯닔 媛以묒튂             | 珥덉븞 ?뺤젙 ?꾩슂                          | 源吏꾪쁺          |
| 濡쒖뺄 ?꾨쿋??紐⑤뜽                       | 誘몄젙                                    | 源吏꾪쁺, ? ?꾩껜 |
| Elasticsearch ?몃뜳??李⑥썝??similarity | 誘몄젙 (?꾨쿋??紐⑤뜽 ?뺤젙 ??寃곗젙)         | 誘쇱젙湲? 源吏꾪쁺  |
| ?뚮┝ 援ы쁽 諛⑹떇                         | SSE ?곗꽑, ?대쭅 fallback                 | ?댁뒳?? ?⑺씗??|
| 梨쀫큸 ?몄뀡 援ъ“                         | ?몄뀡 湲곕컲 ?뺤젙, ?댁뒳?댁? 理쒖쥌 ?⑹쓽 ?꾩슂 | ?댁뒳?? 源吏꾪쁺  |
| Flash Chat 理쒕? ?쒖꽦 硫붿떆吏 ??        | 誘몄젙                                    | 源吏꾪쁺, 源媛?? |
| ?대?吏 ??μ냼                          | 濡쒖뺄 ?뚯씪?쒖뒪???먮뒗 S3                 | 源吏꾪쁺, ? ?꾩껜 |
