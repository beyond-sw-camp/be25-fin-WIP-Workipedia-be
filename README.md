# AI 기반 사내 지식 공유 플랫폼

<img src="images/대표이미지.png" />
</details>

<br>


## 🤝 팀원 소개
<table>
  <tr>
    <td align="center" width="16%">
      <img src="https://github.com/beyond-sw-camp/be25-fin-WIP-Workipedia-be/blob/dev/images/%ED%8C%80%EC%9B%90%20%EC%82%AC%EC%A7%84/ChatGPT%20Image%202026%EB%85%84%206%EC%9B%94%2025%EC%9D%BC%20%EC%98%A4%ED%9B%84%2003_27_59.png?raw=true"/></a>
      <b>김진혁(팀장)</b><br>
      <a href="https://github.com/jin605"><img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white"/></a>
    </td>
    <td align="center" width="16%">
      <img src="https://github.com/beyond-sw-camp/be25-fin-WIP-Workipedia-be/blob/dev/images/%ED%8C%80%EC%9B%90%20%EC%82%AC%EC%A7%84/ChatGPT%20Image%202026%EB%85%84%206%EC%9B%94%2025%EC%9D%BC%20%EC%98%A4%ED%9B%84%2003_27_56.png?raw=true"/></a>
      <b>김가영</b><br>
      <a href="https://github.com/gahyoung920-eng"><img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white"/></a>
    </td>
    <td align="center" width="16%">
      <img src="https://github.com/beyond-sw-camp/be25-fin-WIP-Workipedia-be/blob/dev/images/%ED%8C%80%EC%9B%90%20%EC%82%AC%EC%A7%84/ChatGPT%20Image%202026%EB%85%84%206%EC%9B%94%2025%EC%9D%BC%20%EC%98%A4%ED%9B%84%2003_34_45.png?raw=true"/></a>
      <b>민정기</b><br>
      <a href="https://github.com/calendar3450"><img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white"/></a>
    </td>
    <td align="center" width="16%">
      <img src="https://github.com/beyond-sw-camp/be25-fin-WIP-Workipedia-be/blob/dev/images/%ED%8C%80%EC%9B%90%20%EC%82%AC%EC%A7%84/ChatGPT%20Image%202026%EB%85%84%206%EC%9B%94%2025%EC%9D%BC%20%EC%98%A4%ED%9B%84%2003_27_49.png?raw=true"/></a>
      <b>이슬이</b><br>
      <a href="https://github.com/0lthree"><img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white"/></a>
    </td>
    <td align="center" width="16%">
      <img src="https://github.com/beyond-sw-camp/be25-fin-WIP-Workipedia-be/blob/dev/images/%ED%8C%80%EC%9B%90%20%EC%82%AC%EC%A7%84/ChatGPT%20Image%202026%EB%85%84%206%EC%9B%94%2025%EC%9D%BC%20%EC%98%A4%ED%9B%84%2003_27_53.png?raw=true"/></a>
      <b>황희수</b><br>
      <a href="https://github.com/huisu73"><img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white"/></a>
    </td>
        <td align="center" width="16%">
      <b>고명진(멘토)</b>
    </td>
  </tr>
</table>

<br>


## 🚩 목차

0. [배경](#0-배경)
1. [프로젝트 기획서](#1-프로젝트-기획서)
2. [요구사항 명세서](#2-요구사항-명세서)
3. [WBS](#3-WBS)
4. [ERD](#4-ERD)
5. [시스템 아키텍처](#5-시스템-아키텍처)
6. [화면설계서](#6-화면설계서)
7. [API 명세서](#7-API-명세서)
8. [백엔드 테스트 보고서](#8-백엔드-테스트-보고서)
9. [프론트엔드 테스트 보고서](#9-프론트엔드-테스트-보고서)
10. [통합 테스트 보고서](#10-통합-테스트-보고서)
11. [CI/CD 계획서](#11-CI/CD-계획서)

<br>


## <a id="0-배경"></a> 0. 배경

사내 지식은 매뉴얼, 부서 문서, 사내 게시판, 메신저, 이메일 등 여러 채널에 흩어져 있어 구성원이 필요한 정보를 찾는 데 많은 시간이 소요됩니다. 또한 반복되는 질문은 담당자의 업무 부담을 높이고, 실제 처리가 필요한 요청은 메신저나 메일로 오가면서 상태와 담당 부서, 처리 이력이 명확하게 남지 않는 문제가 있었습니다.

Workipedia는 이러한 문제의식에서 출발한 사내 지식 공유 플랫폼입니다. 흩어진 지식을 자체 개발 AI 챗봇 Know-it을 통해 더 쉽게 탐색하고, 답변만으로 해결되지 않는 요청은 티켓으로 연결해 처리 흐름을 남길 수 있도록 기획했습니다.

이를 통해 단순히 정보를 검색하는 도구를 넘어, 질문과 요청이 처리 결과로 이어지고 그 결과가 다시 조직 지식으로 축적되는 지식 순환 구조를 만들고자 합니다.

<br>


## <a id="1-프로젝트-기획서"></a> 1. 프로젝트 기획서

<details>
<summary>세부사항</summary>

### 1. 개요

Workipedia는 임직원이 사내 규정, 업무 매뉴얼, 시스템 사용법, 부서별 노하우를 하나의 창구에서 질문하고 확인할 수 있는 **AI 기반 사내 지식 공유 플랫폼**입니다.

사용자는 자체 개발 AI 챗봇 **Know-it**에게 자연어로 질문할 수 있으며, Know-it은 사내 매뉴얼, 규정 문서, Worki Q&A, 승인된 지식화 데이터 등을 기반으로 답변을 제공합니다. 답변에는 관련 출처와 문서 정보를 함께 제공하여 사용자가 답변의 근거를 확인할 수 있도록 합니다.

AI 답변만으로 해결되지 않는 질문은 Worki 질문 등록 또는 담당 부서 티켓 발행으로 연결됩니다. 이후 담당자가 작성한 답변과 처리 결과는 검수와 승인을 거쳐 다시 조직 지식으로 축적되며, 이후 AI 답변 품질을 높이는 데 활용됩니다.

---

### 2. 문제 정의

기존 사내 지식은 부서별 문서, PDF 매뉴얼, 메신저, 메일, 게시판 등에 분산되어 있어 사용자가 필요한 정보를 찾기 위해 여러 채널을 직접 확인해야 했습니다. 또한 동일한 질문이 반복되더라도 답변 이력이 체계적으로 축적되지 않아 담당자의 반복 응대 부담이 지속되었습니다.

특히 단순 게시판이나 키워드 검색 방식은 문서 목록을 제공하는 데 그치기 때문에, 사용자가 직접 내용을 해석하고 최신 여부를 판단해야 하는 한계가 있습니다. 업무 요청 역시 메신저나 메일로 처리될 경우 담당 부서, 처리 상태, 처리 이력이 명확히 남지 않아 추적과 재사용이 어렵습니다.

Workipedia는 이러한 문제를 해결하기 위해 **AI 질의응답, Worki Q&A, 업무 티켓, 지식화 승인, AI 재활용**을 하나의 흐름으로 연결합니다.

---

### 3. 목표

본 프로젝트의 목표는 파편화된 사내 업무 지식을 Know-it을 중심으로 연결하여, 임직원이 필요한 정보를 더 빠르고 정확하게 찾을 수 있도록 하는 것입니다.

사용자가 자연어로 질문하면 Know-it은 RAG 기반으로 사내 매뉴얼과 Worki 지식을 검색해 답변하고, 답변의 근거가 되는 출처를 함께 제공합니다. 답변으로 해결되지 않는 요청은 Worki 질문 또는 담당 부서 티켓으로 전환하여 공식적인 답변과 처리 이력을 남깁니다.

처리 완료된 티켓과 채택된 답변은 다시 지식화되어 이후 검색과 AI 답변에 재사용됩니다. 이를 통해 Workipedia는 단순 검색 서비스를 넘어, **질문 → 답변 → 처리 → 지식화 → 재활용**으로 이어지는 사내 지식 순환 구조를 만드는 것을 목표로 합니다.

주요 목표는 다음과 같습니다.

- 전사 임직원이 하나의 창구에서 사내 지식을 자연어로 질문할 수 있도록 한다.
- Know-it이 사내 매뉴얼, Worki Q&A, 승인 지식 데이터를 근거로 답변을 제공한다.
- AI 답변에 출처와 관련 문서 정보를 함께 제공해 답변 신뢰성을 높인다.
- AI 답변이 부족한 경우 Worki 질문 등록 또는 담당 부서 티켓 발행으로 연결한다.
- 임직원이 질문에 답변하고, 질문자가 유용한 답변을 채택할 수 있도록 한다.
- 업무 요청 티켓을 담당 부서로 연결하고 처리 상태와 이력을 관리한다.
- 처리 완료된 티켓과 채택 답변을 조직 지식으로 축적한다.
- 답변 작성, 답변 채택, 티켓 처리, 지식화 승인 등 지식 공유 활동에 포인트를 부여한다.
- 포인트, 리더보드, 등급을 통해 임직원의 지식 공유 참여를 유도한다.
- 관리자가 미답변 질문, 티켓 현황, 지식화 데이터, AI 동기화 상태를 관리할 수 있도록 한다.

---

### 4. 핵심 흐름

Workipedia의 핵심 흐름은 다음과 같습니다.

```text
사용자 질문
→ Know-it AI 답변
→ 출처 기반 답변 제공
→ 미해결 시 Worki 질문 또는 티켓 발행
→ 담당자 답변 또는 부서 처리
→ 답변 채택 / 티켓 완료
→ 지식화 승인
→ AI 검색 및 답변에 재활용
```

이 흐름을 통해 일회성 질문과 처리 결과가 사라지지 않고, 조직 전체가 다시 활용할 수 있는 지식 자산으로 축적됩니다.

---

### 5. ESG 가치

Workipedia의 ESG는 별도의 장식 요소가 아니라, 사내 지식 공유와 업무 효율화 과정에서 발생하는 가치를 시각화하는 방향으로 설계했습니다.

첫째, **지식 공유 활동의 시각화**입니다. 답변 작성, 답변 채택, 티켓 처리, 지식화 승인 등 사용자의 지식 공유 활동을 포인트로 환산하고, 리더보드와 등급을 통해 참여를 유도합니다.

둘째, **조직 투명성 강화**입니다. 사내 매뉴얼과 지식 데이터의 등록, 수정, 승인, 삭제 이력을 관리하고, 관리자 작업 로그와 지식화 승인 이력을 남겨 운영 과정을 추적할 수 있도록 합니다. 이는 ESG 중 Governance 가치와 연결됩니다.

셋째, **업무 절감 효과의 시각화**입니다. AI 답변을 통해 줄어든 문서 검색 시간과 반복 문의 시간을 추정하고, 이를 전력 사용량 및 CO2 절감량으로 환산하여 보여줍니다.

```text
주간 추정 업무 절감 시간(h)
= Σ 일자별·사용자별 min(인용 포함 챗봇 답변 수 × 3분, 37.8분) ÷ 60

추정 전력 절감량(kWh)
= 주간 추정 업무 절감 시간(h) × 0.08(kW)

추정 CO2 절감량(kgCO2e)
= 추정 전력 절감량(kWh) × 0.478(kgCO2e/kWh)
```

이를 통해 Workipedia는 업무 효율 개선뿐 아니라, 지식 공유 문화 확산, 운영 투명성 강화, ESG 가치 시각화를 함께 제공하는 것을 목표로 합니다.

</details>

<br>


## <a id="2-요구사항-명세서"></a> 2. 요구사항 명세서

<details>
<summary>세부사항</summary>

[🗒️ 요구사항명세서](https://docs.google.com/spreadsheets/d/1UwKgzHGSBpIbeOFRVJ_3B759vdDhf5sKBs9VNqmtCpI/edit?gid=0#gid=0)

<img src="images/요구사항명세서.png" />
</details>

<br>


## <a id="3-WBS"></a> 3. WBS

<details>
<summary>세부사항</summary>

[📅 WBS](https://playdatacademy.notion.site/358d943bcac281f39953cef849482b81?v=35ed943bcac280338131000cb1fc378e)

<img src="./images/WBS.png"/>
</details>

<br>

## <a id="4-ERD"></a> 4. ERD

<details>
<summary>세부사항</summary>

[🧩 ERD](https://www.erdcloud.com/d/N5pR99x6kArMGp4Xe)

<img src="images/ERD.png" />
</details>

<br>


## <a id="5-시스템-아키텍처"></a> 5. 시스템 아키텍처

<details>
<summary>세부사항</summary>

[🛠️ 시스템아키텍쳐](https://excalidraw.com/#json=9v80ncfjm36Hn2IHww8A4,EI-QU18LDajObNvNnOATgA)

<img src="https://github.com/beyond-sw-camp/be25-fin-WIP-Workipedia-be/blob/dev/images/%EC%8B%9C%EC%8A%A4%ED%85%9C%EC%95%84%ED%82%A4%ED%85%8D%EC%B3%90.png?raw=true"/>
</details>

<br>


## <a id="6-화면설계서"></a> 6. 화면설계서

<details>
<summary>세부사항</summary>

[📱 화면설계서](https://www.figma.com/design/jleHnh9qzkjeduukiUuJws/%ED%99%94%EB%A9%B4%EA%B8%B0%EB%8A%A5%EC%84%A4%EA%B3%84%EC%84%9C?node-id=0-1&t=Y0yJvaReqcKLiOyK-1)

<img src="https://github.com/beyond-sw-camp/be25-fin-WIP-Workipedia-be/blob/main/images/%ED%99%94%EB%A9%B4%EC%84%A4%EA%B3%84%EC%84%9C.png?raw=true"/>
</details>

<br>


## <a id="7-API-명세서"></a> 7. API 명세서

<details>
<summary>세부사항</summary>

[🌐 API명세서](https://www.notion.so/playdatacademy/367d943bcac28064b9b6c422491d86bd?v=367d943bcac280189fc1000ce027a418&source=copy_link)

</details>

<br>


## <a id="8-백엔드-테스트-보고서"></a> 8. 백엔드 테스트 보고서

<details>
<summary>세부사항</summary>

[✔️ 백엔드테스트보고서](https://docs.google.com/spreadsheets/d/1UwKgzHGSBpIbeOFRVJ_3B759vdDhf5sKBs9VNqmtCpI/edit?gid=506689780#gid=506689780)

<img src="images/백엔드테스트보고서.png" />
</details>


<br>


## <a id="9-프론트엔드-테스트-보고서"></a> 9. 프론트엔드 테스트 보고서

<details>
<summary>세부사항</summary>

[✅ 프론트엔드테스트보고서](https://docs.google.com/spreadsheets/d/1UwKgzHGSBpIbeOFRVJ_3B759vdDhf5sKBs9VNqmtCpI/edit?gid=454562383#gid=454562383)

<img src="images/프론트엔드테스트보고서.png" />
</details>

<br>


## <a id="10-통합-테스트-보고서"></a> 10. 통합 테스트 보고서

<details>
<summary>세부사항</summary>

[🚦 통합테스트보고서](https://docs.google.com/spreadsheets/d/1UwKgzHGSBpIbeOFRVJ_3B759vdDhf5sKBs9VNqmtCpI/edit?gid=2050766396#gid=2050766396)

<img src="images/통합테스트보고서.png" />
</details>

<br>


## <a id="11-CI/CD-계획서"></a> 11. CI/CD 계획서

<details>
<summary>세부사항</summary>

### 1. 개요

Workipedia는 GitHub Actions 기반의 CI/CD 파이프라인을 구축하여 코드 변경부터 빌드, 검증, 배포까지 전 과정을 자동화했습니다.

서비스는 Backend, Frontend, AI, Infrastructure 레포지토리로 분리되어 있으며, 각 레포지토리가 자신의 CI/CD 파이프라인을 독립적으로 관리합니다.

- **Workipedia-be** : 백엔드 이미지를 빌드하여 GHCR에 Push 후 운영 EC2에 배포
- **Workipedia-fe** : Vue 정적 파일을 빌드하여 AWS S3에 업로드하고 CloudFront 캐시 무효화
- **Workipedia-ai** : FastAPI 이미지를 빌드하여 GHCR에 Push 후 AWS Auto Scaling Group Instance Refresh 수행
- **Workipedia-infra** : Docker Compose 기반 운영 환경 및 배포 스크립트 관리

이를 통해 코드 레포지토리는 산출물 생성(Build) 을 담당하고, Infrastructure 레포지토리는 운영 환경(Runtime) 을 관리하도록 역할을 분리했습니다.

---

### 2. 기술 스택

> **Cloud Provider** : AWS

| 분류 | 기술 |
| :--- | :--- |
| 💻 **OS** | Ubuntu 26.04 LTS, Linux |
| 🏗 **Container** | Docker, Docker Compose |
| 🔄 **CI/CD** | GitHub Actions, GHCR, Shell Deploy Script, Auto Scaling Instance Refresh |
| ☁️ **Cloud** | EC2, Auto Scaling Group, ALB, S3, CloudFront |
| 🌐 **DNS / CDN** | Cloudflare DNS, CloudFront |
| 🗄 **Database** | RDS MariaDB |
| ⚡ **Cache** | Redis |
| 🔎 **Search Engine** | Elasticsearch (Nori Analyzer) |
| 🧠 **AI Serving** | FastAPI, Docker Compose, Internal ALB |
| 🧬 **Vector DB** | Qdrant, Docker Compose |
| 📦 **Container Registry** | GitHub Container Registry (GHCR) |
| 📁 **Object Storage** | S3 Upload Bucket |
| ☕ **Backend** | Java 21, Spring Boot 3.5.14 |
| 🖥 **Frontend** | Vue 3, Vite, Node.js 24 |
| 🐍 **AI Runtime** | Python 3.14 |

- **Backend**는 **Java 21 / Spring Boot 3.5.14** 기반으로 EC2(t3.large)에서 Docker Compose를 이용해 **Backend, Redis, Elasticsearch** 컨테이너를 함께 운영합니다.
- **AI 서버**는 **Python 3.14 / FastAPI** 기반으로 **EC2 Auto Scaling Group**에서 Docker Compose로 운영되며, Backend는 특정 AI 서버를 직접 호출하지 않고 **Internal ALB**를 통해 AI 서비스에 요청을 전달합니다.
- **Frontend**는 컨테이너를 사용하지 않고 **S3 정적 호스팅**과 **CloudFront**를 통해 서비스를 제공합니다.

---

### 3. GitHub Actions를 선택한 이유

Workipedia는 **GitHub Actions**를 CI/CD 도구로 채택했습니다.

5인 규모의 팀 프로젝트에서는 별도의 CI 서버를 운영하는 것보다, GitHub와 긴밀하게 연동되고 관리 부담이 적은 GitHub Actions가 더 적합하다고 판단했습니다.

| 항목 | Jenkins | GitHub Actions (채택) |
| :--- | :--- | :--- |
| **인프라** | 별도 서버·컨테이너 상시 운영 필요 | 별도 빌드 서버 불필요 (GitHub Hosted Runner) |
| **설정** | 플러그인 관리 및 Job 구성 필요 | 레포지토리 내 YAML Workflow로 관리 |
| **GitHub 연동** | 외부 CI 도구 | PR·브랜치와 네이티브 통합 |
| **5인 팀 운영** | 관리 비용이 상대적으로 높음 | 관리 비용이 낮고 유지보수가 용이 |

따라서 별도의 CI 서버를 구축하지 않고도 코드 변경부터 빌드, 검증, 배포까지 자동화할 수 있는 GitHub Actions를 선택했습니다.

---

### 4. 전체 파이프라인 개요

```text
CI (Pull Request)

PR (dev / main)
       │
       ▼
┌───────────────────────────────────────────────┐
│ Backend       : check.yml (bootJar 검증)      │
│ Frontend      : check.yml (Build 검증)        │
│ AI            : check.yml (Import 검증)       │
│ Infrastructure: validate.yml (Compose 검증)   │
└───────────────────────────────────────────────┘
```

```text
CD (Main Branch)

push (main)
       │
       ▼
┌──────────────────────────────────────────────────────────────────────────┐
│ Backend       : deploy.yml → Build → GHCR → SSH Deploy                   │
│ Frontend      : deploy.yml → Build → S3 → CloudFront Invalidation        │
│ AI            : deploy.yml → Build → GHCR → ASG Instance Refresh         │
│ Infrastructure: Runtime Environment & deploy.sh                          │
└──────────────────────────────────────────────────────────────────────────┘
```

- **CI (검증)** : Pull Request가 생성되면 각 레포지토리의 `check.yml`(Backend, Frontend, AI)과 `validate.yml`(Infrastructure)이 실행되어 빌드 및 Docker Compose 문법을 검증합니다.
- **CD (배포)** : `main` 브랜치에 Push되면 각 레포지토리의 `deploy.yml`이 실행되어 산출물을 빌드하고 운영 환경에 자동 배포합니다.

---

### 5. CI 파이프라인 — 빌드 검증 (`check.yml`)

Pull Request(`dev`, `main` 대상)가 생성되면 CI 파이프라인이 실행되어 **머지 전에 프로젝트가 정상적으로 빌드되는지 검증**합니다. 빌드 또는 검증 단계에서 실패하면 PR Check가 실패로 표시되어 Merge가 차단됩니다.


#### Backend Check (`Workipedia-be`)

| 단계 | 내용 |
| :--- | :--- |
| **1. Checkout** | Repository 코드를 체크아웃 (`actions/checkout@v4`) |
| **2. Set up Java** | Temurin JDK 21 설치 및 Gradle Cache 활성화 |
| **3. Grant execute permission** | `gradlew` 실행 권한 부여 |
| **4. Build Backend** | `./gradlew clean bootJar --no-daemon` 실행하여 실행 가능한 JAR 생성 여부 검증 |


#### Frontend Check (`Workipedia-fe`)

| 단계 | 내용 |
| :--- | :--- |
| **1. Checkout** | Repository 코드를 체크아웃 |
| **2. Set up Node.js** | Node.js 24 설치 및 npm Cache 활성화 |
| **3. Install Dependencies** | `npm ci`를 통해 의존성 설치 |
| **4. Type Check & Build** | `npm run build`를 실행하여 타입 체크 및 빌드 검증 |


#### AI Check (`Workipedia-ai`)

| 단계 | 내용 |
| :--- | :--- |
| **1. Checkout** | Repository 코드를 체크아웃 |
| **2. Set up Python** | Python 3.14 설치 및 pip Cache 활성화 |
| **3. Install Dependencies** | `pip install -r requirements.txt`를 통해 의존성 설치 |
| **4. Check Import** | `python -c "from app.main import app"` 실행하여 FastAPI 애플리케이션이 정상적으로 로딩되는지 검증 |
| **5. Environment** | `LLM_PROVIDER`, `EMBEDDING_PROVIDER`, `QDRANT_HOST`, `QDRANT_PORT`를 검증용 환경 변수로 주입 |

- **CI 검증 결과**: 모든 검증이 성공해야 Pull Request를 Merge할 수 있으며, 하나라도 실패하면 GitHub PR Check가 실패 상태로 표시되어 Merge가 차단됩니다.

---

### 6. Backend CD 파이프라인 — Build and Deploy (`deploy.yml`)

<img src="images/CD파이프라인1.png" />

`main` 브랜치에 Push되면 Backend 배포 파이프라인이 실행됩니다.

동일한 운영 환경에 여러 배포가 동시에 수행되지 않도록 `concurrency: production-deploy`를 적용했으며, 실행 중인 배포는 중간에 취소하지 않고 순차적으로 처리합니다.


#### 📦 Job 1. Build & Push

- Backend 애플리케이션과 Elasticsearch 이미지를 빌드하여 **GitHub Container Registry(GHCR)** 에 업로드합니다.

| 단계 | 내용 |
| :--- | :--- |
| **Checkout** | Repository 코드를 체크아웃 |
| **Docker Buildx** | 멀티 플랫폼 및 캐시 기반 빌드 환경 구성 |
| **GHCR Login** | `GITHUB_TOKEN`을 이용하여 GHCR 로그인 |
| **Backend Image Build** | Backend Docker 이미지 빌드 및 GHCR Push |
| **Image Tag** | Commit SHA와 `latest` 태그를 함께 생성 |
| **Build Cache** | GitHub Actions Cache(`type=gha`)를 활용하여 빌드 속도 향상 |
| **Elasticsearch Build** | Nori Analyzer가 포함된 Elasticsearch 이미지를 빌드하여 GHCR Push |

> 운영 환경에서는 `latest` 태그뿐만 아니라 **Commit SHA** 태그를 함께 관리하여 어떤 버전이 배포되었는지 추적할 수 있도록 구성했습니다.


#### 🚀 Job 2. Deploy

- Build가 완료되면 운영 EC2에 접속하여 최신 이미지를 배포합니다.

| 단계 | 내용 |
| :--- | :--- |
| **Configure SSH** | GitHub Secrets에 저장된 SSH 정보를 이용해 운영 서버 연결 |
| **GHCR Login** | 운영 서버에서 GHCR 로그인 후 최신 이미지 Pull |
| **Infrastructure Sync** | `git pull --ff-only`로 Workipedia-infra 최신 코드 동기화 |
| **Deploy Script** | `./scripts/deploy.sh be` 실행 |
| **Docker Compose** | Compose 검증 → Image Pull → `up -d` → 컨테이너 실행 확인 |
| **Health Check** | `/actuator/health`가 `UP` 상태가 될 때까지 최대 5분 동안 확인 |
| **Success** | 이전 Docker Image 정리 후 배포 완료 |
| **Failure** | Backend 컨테이너 로그를 출력하고 Workflow 실패 처리 |

> 배포 완료 후 Spring Boot Health Check가 정상(`UP`) 상태가 되어야 배포가 성공한 것으로 판단합니다.

---

### 7. Frontend CD 파이프라인 — Build and Deploy (`deploy.yml`)

<img src="images/CD파이프라인2.png" />

`main` 브랜치에 Push되거나 `workflow_dispatch`를 통해 수동 실행할 수 있습니다.

새로운 배포가 시작되면 이전 배포는 자동으로 취소하고 최신 배포만 유지하도록 `concurrency`를 적용했습니다.

Backend와 달리 별도의 서버 접속이나 Docker를 사용하지 않고, **빌드된 정적 파일을 AWS S3에 업로드한 뒤 CloudFront 캐시를 무효화하는 방식**으로 배포를 수행합니다.


#### 🚀 Build & Deploy

Frontend 애플리케이션을 빌드한 후 AWS S3와 CloudFront를 통해 서비스를 배포합니다.

| 단계 | 내용 |
| :--- | :--- |
| **Checkout** | Repository 코드를 체크아웃 |
| **Set up Node.js** | Node.js 24 설치 및 npm Cache 활성화 |
| **Install Dependencies** | `npm ci`를 통해 의존성 설치 |
| **Build Environment Check** | `VITE_API_BASE_URL` 환경 변수 설정 여부 확인 |
| **Build Widget** | `npm run build:widget` 실행 |
| **Build Application** | `npm run build`를 실행하여 Vue 정적 파일(`dist`) 생성 |
| **AWS Authentication** | GitHub Secrets를 이용해 AWS 인증 |
| **S3 Upload** | `aws s3 sync dist ... --delete`를 통해 최신 정적 파일 업로드 |
| **CloudFront Invalidation** | CloudFront 캐시를 무효화하여 최신 버전 즉시 반영 |

> `aws s3 sync --delete` 옵션을 사용하여 S3 버킷과 `dist` 디렉터리의 상태를 항상 동일하게 유지하며, CloudFront 캐시를 무효화하여 사용자가 최신 버전의 애플리케이션을 즉시 사용할 수 있도록 구성했습니다.


#### 🔐 GitHub Secrets & Variables

| 종류 | 키 | 용도 |
| :--- | :--- | :--- |
| **Secret** | `AWS_ACCESS_KEY` | AWS 인증 |
| **Secret** | `AWS_SECRET_KEY` | AWS 인증 |
| **Secret** | `AWS_REGION` | AWS 리전 정보 |
| **Secret** | `S3_FE_BUCKET` | Frontend 정적 파일 업로드 대상 버킷 |
| **Secret** | `CLOUDFRONT_DISTRIBUTION_ID` | CloudFront 캐시 무효화 대상 |
| **Variable** | `VITE_API_BASE_URL` | 빌드 시 주입되는 API Base URL |

---

### 8. AI CD 파이프라인 — Build and Deploy (`deploy.yml`)

<img src="images/CD파이프라인3.png" />

`main` 브랜치에 Push되면 AI 배포 파이프라인이 실행됩니다.

Backend처럼 특정 EC2에 직접 SSH로 배포하지 않고, **Docker 이미지를 GHCR에 업로드한 뒤 AWS Auto Scaling Group Instance Refresh를 통해 AI 서버를 교체 배포**하는 방식을 사용합니다.

동일한 운영 환경에 여러 배포가 동시에 수행되지 않도록 `concurrency`를 적용했으며, 실행 중인 배포는 중간에 취소하지 않고 순차적으로 처리합니다.


#### 📦 Job 1. Build & Push

FastAPI 애플리케이션 이미지를 빌드하여 **GitHub Container Registry(GHCR)** 에 업로드합니다.

| 단계 | 내용 |
| :--- | :--- |
| **Checkout** | Repository 코드를 체크아웃 |
| **Docker Buildx** | 멀티 플랫폼 및 캐시 기반 빌드 환경 구성 |
| **GHCR Login** | `GITHUB_TOKEN`을 이용하여 GHCR 로그인 |
| **FastAPI Image Build** | AI Docker 이미지를 빌드하여 GHCR Push |
| **Image Tag** | Commit SHA와 `latest` 태그를 함께 생성 |
| **Build Cache** | GitHub Actions Cache(`type=gha`)를 활용하여 빌드 속도 향상 |


#### 🚀 Job 2. Deploy to ASG

Build가 완료되면 Auto Scaling Group을 이용하여 새로운 AI 서버를 생성하고 교체 배포를 수행합니다.

| 단계 | 내용 |
| :--- | :--- |
| **AWS Authentication** | GitHub Secrets를 이용해 AWS 인증 |
| **Update Image Tag** | 배포 이미지 태그를 SSM Parameter Store에 저장 |
| **Instance Refresh** | Auto Scaling Group Instance Refresh 실행 |
| **Deployment Monitoring** | Refresh 상태를 모니터링하여 완료 여부 확인 |
| **Failure Handling** | 실패·취소·롤백 발생 시 GitHub Actions Workflow 실패 처리 |

> 현재 Launch Template은 `latest` 이미지를 사용하지만, Commit SHA를 함께 저장하여 향후 SHA 기반의 고정 버전 배포 및 롤백이 가능하도록 설계했습니다.


#### ⚙️ AI 서버 기동 과정

Auto Scaling Group이 새로운 AI EC2를 생성하면 Launch Template의 **User Data**가 자동 실행됩니다.

| 순서 | 내용 |
| :--- | :--- |
| **1** | Docker, Docker Compose, AWS CLI 설치 |
| **2** | `Workipedia-infra` Repository Clone 또는 Pull |
| **3** | SSM Parameter Store에서 Runtime Secret 조회 |
| **4** | `ai/.env` 생성 |
| **5** | GHCR 로그인 |
| **6** | `./scripts/deploy.sh ai` 실행 |
| **7** | FastAPI 컨테이너 실행 |
| **8** | Internal ALB Health Check 통과 후 서비스 시작 |


#### 🔐 GitHub Secrets & Variables

| 종류 | 키 | 용도 |
| :--- | :--- | :--- |
| **Secret** | `AWS_ACCESS_KEY` | AWS 인증 |
| **Secret** | `AWS_SECRET_KEY` | AWS 인증 |
| **Secret** | `AWS_REGION` | AWS 리전 정보 |
| **Variable** | `AI_ASG_NAME` | 배포 대상 Auto Scaling Group 이름 |
| **Variable** | `AI_IMAGE_SSM_PARAMETER` | 배포 이미지 태그를 저장하는 SSM Parameter |

> Runtime Secret(API Key, GHCR 인증 정보 등)은 GitHub가 아닌 **AWS Systems Manager Parameter Store**에서 관리하여 AI 서버 기동 시 안전하게 조회하도록 구성했습니다.

---

### 9. Infrastructure Repository (`Workipedia-infra`)

<img src="images/Infra래포지토리.png" />

`Workipedia-infra`는 애플리케이션이 실행되는 **운영 환경(Runtime Environment)** 을 관리하는 레포지토리입니다.

각 서비스 레포지토리(Backend, Frontend, AI)는 **산출물(Docker Image, 정적 파일)** 을 생성하며, Infrastructure 레포지토리는 해당 산출물을 **어떤 환경에서 어떻게 실행할지**를 관리합니다.


#### 🏗 주요 구성

| 구성 요소 | 역할 |
| :--- | :--- |
| **`be/docker-compose.yml`** | Backend, Redis, Elasticsearch 컨테이너 구성 |
| **`ai/docker-compose.yml`** | FastAPI AI 서버 구성 |
| **`qdrant/docker-compose.yml`** | Qdrant Vector DB 구성 |
| **`scripts/deploy.sh`** | Backend, AI, Qdrant 공통 배포 스크립트 |
| **`.env`** | 실행 환경 변수 관리 (`.env.example` 제공) |


#### ⚙️ Backend Docker Compose

Backend 환경은 **Spring Boot**, **Redis**, **Elasticsearch**를 하나의 Docker Compose로 구성했습니다.

| 구성 | 내용 |
| :--- | :--- |
| **Services** | Backend, Redis, Elasticsearch |
| **Health Check** | Redis Ping, Elasticsearch Cluster Health, Spring Boot `/actuator/health` |
| **Service Dependency** | `depends_on: condition: service_healthy`를 사용하여 의존 서비스가 정상 동작한 이후 Backend 실행 |
| **Network** | 외부 접근용 `app` 네트워크와 내부 통신 전용 `data (internal)` 네트워크 분리 |


#### 🚀 Deploy Script

모든 서비스는 공통 배포 스크립트를 사용합니다.

```bash
./scripts/deploy.sh <be|ai|qdrant>
```

배포 순서는 다음과 같습니다.

```text
Docker Compose Config 검증
        ↓
최신 Image Pull
        ↓
Docker Compose Up (-d)
        ↓
불필요한 컨테이너 제거
        ↓
실행 상태 확인 (docker ps)
```


#### 🔐 Environment & Secret 관리

보안을 위해 `.env` 파일과 Secret 정보는 Git 저장소에 포함하지 않습니다.

- 운영 서버 또는 AWS Systems Manager Parameter Store에서 관리
- `.env.example`을 통해 필요한 환경 변수 목록 제공


#### ✅ Infrastructure Validate (`validate.yml`)

Infrastructure 레포지토리의 Pull Request 또는 Push 시 Docker Compose 파일을 자동으로 검증합니다.

| 검증 대상 | 내용 |
| :--- | :--- |
| **Backend** | `be/docker-compose.yml` |
| **AI** | `ai/docker-compose.yml` |
| **Vector DB** | `qdrant/docker-compose.yml` |

각 Compose 파일은 Matrix Strategy를 이용하여 병렬 검증합니다.

```bash
docker compose config --quiet
```

> Docker Compose 문법 오류나 해석되지 않은 환경 변수가 운영 환경에 반영되기 전에 사전에 차단하도록 구성했습니다.

---

### 10. AWS 운영 아키텍처

Workipedia는 AWS 기반으로 운영되며 각 서비스는 다음과 같이 구성됩니다.

```text
                    Cloudflare DNS
                           │
                           ▼
                     AWS CloudFront
                  ┌────────┴────────┐
                  │                 │
                  ▼                 ▼
          S3 FE Bucket         /api/* Routing
        (Vue 정적 파일)              │
                                     ▼
                           Application Load Balancer
                                     │
                                     ▼
                           Backend EC2 (t3.large)
                    ┌────────┼────────┬────────┐
                    ▼        ▼        ▼        ▼
               Spring Boot Redis Elasticsearch RDS
                    │
                    ▼
            S3 Upload Bucket
                    │
                    ▼
               Internal ALB
                    │
                    ▼
          AI Auto Scaling Group
                    │
             FastAPI (Docker)
                    │
                    ▼
          Qdrant Vector Database
```

- **Database** : Backend는 AWS RDS MariaDB를 사용하며, 사용자 업로드 파일은 S3 Upload Bucket에 저장합니다.
- **Backend** : CloudFront의 `/api/*` 요청을 ALB가 Backend EC2의 Spring Boot 애플리케이션으로 전달합니다.
- **Frontend** : Vue 정적 파일을 S3에 업로드하고 CloudFront를 통해 서비스를 제공합니다.
- **AI** : Backend는 Internal ALB를 통해 AI Auto Scaling Group으로 요청을 전달하며, AI 서버는 Qdrant Vector DB와 연동하여 RAG 검색을 수행합니다.

---

### 11. 배포 흐름

**Backend**
```text
push(main)
    │
    ▼
deploy.yml
    │
    ├─ Backend Docker Image Build
    ├─ GHCR에 이미지 Push
    ├─ 운영 EC2에 SSH 접속
    │    ├─ Workipedia-infra 최신 코드 Pull
    │    ├─ deploy.sh be 실행
    │    ├─ Backend · Redis · Elasticsearch 재배포
    │    ├─ /actuator/health 헬스체크
    │    └─ 성공 시 이전 이미지 정리 / 실패 시 로그 출력
```

**Frontend**
```text
push(main)
    │
    ▼
deploy.yml
    │
    ├─ npm ci
    ├─ build:widget
    ├─ Vue 프로젝트 Build
    ├─ dist 파일을 S3에 Sync (--delete)
    └─ CloudFront 캐시 무효화
```

**AI**
```text
push(main)
    │
    ▼
deploy.yml
    │
    ├─ AI Docker Image Build
    ├─ GHCR에 이미지 Push
    ├─ AWS 인증
    ├─ SSM Parameter에 이미지 태그 저장
    ├─ Auto Scaling Group Instance Refresh
    └─ 새 AI EC2 생성
         ├─ Workipedia-infra Clone/Pull
         ├─ ai/.env 생성
         ├─ deploy.sh ai 실행
         └─ Internal ALB Health Check 통과
```

---

### 12. 향후 개선 사항

현재 CI/CD 환경은 자동 빌드와 배포를 지원하지만, 다음과 같은 기능을 추가하여 운영 안정성을 더욱 높일 계획입니다.

- Slack 기반 배포 성공·실패 알림 추가
- 단위 테스트 및 통합 테스트 자동 실행
- SHA 기반 자동 롤백 전략 적용
- AI 서버 이미지 버전 관리 개선

</details>

<br>

