# Project `TapToon`: 웹툰 창작의 꿈을 잇다

[//]: # (<img width="700" alt="Image" src="https://cdn.gamma.app/hxt1ktlqn5booma/5eb69b0ff23e4914b73e6f870018251e/original/image.png" />)
[//]: # (<img width="700" alt="Image" src="https://cdn.gamma.app/hxt1ktlqn5booma/9e35fd7dbaa848ec84c390fe2b11004a/original/image.png" />)
[//]: # (<img width="700" alt="Image" src="https://github.com/user-attachments/assets/e0847133-ca68-45a0-a925-1bf35f0fab49" />)

<br>
<img width="1000" src="https://github.com/user-attachments/assets/2d014f23-75fc-4015-ad37-f7b6dc880272" />


<br>


* [프로젝트 개요](#프로젝트-개요)
* [프로젝트 사용 흐름](#프로젝트-사용-흐름)
* [이용 방법](#이용-방법)
* [프로젝트 구조](#프로젝트-구조)
* [기능 목록](#기능-목록)
* [Support](#support)

## 프로젝트 개요
웹툰 작가와 글 작가를 연결하는 매칭 플랫폼입니다.
아이디어만 있다면 그림 작가를, 뛰어난 그림 실력을 갖췄다면 글 작가를 찾아보세요!
당신의 재능을 마음껏 뽐내고, 최고의 파트너와 함께 새로운 이야기를 만들어 보세요. 🚀

### 작업 기간
***2025.02.10 - 2025.03.16***

## 📚 Used Stacks

<br>

<div align=center>
  <img src="https://img.shields.io/badge/java%2017-007396?style=for-the-badge&logo=java&logoColor=white">
  <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white">
  <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white">
  <br>
  <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">
  <img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white">
  <img src="https://img.shields.io/badge/OpenFeign-E50914?style=for-the-badge&logo=netflix&logoColor=white">
  <img src="https://img.shields.io/badge/JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white">
  <br>

  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
  <img src="https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white">
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">
  <br>

  <img src="https://img.shields.io/badge/ElasticSearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white">
  <img src="https://img.shields.io/badge/Kibana-005571?style=for-the-badge&logo=kibana&logoColor=white">
  <img src="https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=socket.io&logoColor=white">
  <br>

  <img src="https://img.shields.io/badge/OAuth%202.0-4285F4?style=for-the-badge&logo=google&logoColor=white">
  <img src="https://img.shields.io/badge/Naver-03C75A?style=for-the-badge&logo=naver&logoColor=white">
  <img src="https://img.shields.io/badge/Google-4285F4?style=for-the-badge&logo=google&logoColor=white">
  <br>

  <img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white">
  <img src="https://img.shields.io/badge/EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white">
  <img src="https://img.shields.io/badge/RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white">
  <br>

  <img src="https://img.shields.io/badge/Route%2053-8C4FFF?style=for-the-badge&logo=amazonroute53&logoColor=white">
  <img src="https://img.shields.io/badge/CloudFront-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white">
  <img src="https://img.shields.io/badge/ELB-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white">
  <br>

  <img src="https://img.shields.io/badge/Lambda-FF9900?style=for-the-badge&logo=awslambda&logoColor=white">
  <img src="https://img.shields.io/badge/S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white">
  <img src="https://img.shields.io/badge/ElastiCache-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white">
  <img src="https://img.shields.io/badge/ECR-527FFF?style=for-the-badge&logo=amazonaws&logoColor=white">
  <br>

  <img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white">
  <br>
</div>

## 🏰️ 프로젝트 구조

### 𓊍 프로젝트 아키텍처
<img width="1051" alt="Image" src="https://github.com/user-attachments/assets/21d42bc0-6cd2-4fa0-a248-43278d7949ba" />


### ⛁ ERD (RDMBS)

```mermaid
erDiagram
    member ||--o{ portfolio : owner_id
    member ||--o{ refresh_token : member_id
    member ||--o{ matching_post : user_id
    member ||--o{ comment : member_id
    
    category ||--o{ matching_post : category_id
    
    portfolio ||--o{ portfolio_file : portfolio_id
    
    matching_post ||--o{ matching_post_image : matching_post_id
    matching_post ||--o{ comment : matching_post_id
    
    comment ||--o{ comment : parent_id

    %% 엔터티 정의
    member {
        bigint id PK
        bit is_deleted
        datetime created_at
        datetime updated_at
        varchar email
        varchar name
        varchar nickname
        varchar password
        bigint provider_id
        enum grade "basic, plus, pro"
        enum provider "naver, google"
    }
    
    category {
        bigint id PK
        bigint member_id FK
        enum genre "fantasy, romance, comic, thriller, action, drama"
    }
    
    portfolio {
        bigint id PK
        bigint owner_id FK
        datetime created_at
        datetime updated_at
        text content
        varchar title
        enum status "pending, registered, deleting, deleted"
    }
    
    portfolio_file {
        bigint id PK
        bigint portfolio_id FK
        datetime created_at
        datetime updated_at
        varchar file_url
        varchar thumbnail_url
        varchar file_name
        enum file_type "pending, registered, deleted"
    }
    
    refresh_token {
        bigint id PK
        bigint member_id FK
        datetime created_at
        datetime expires_at
        datetime updated_at
        varchar device_info
        varchar token
    }
    
    matching_post {
        bigint id PK
        bigint user_id FK
        datetime created_at
        bigint view_count
        varchar description
        varchar file_url
        varchar title
        enum artist_type "writer, illustrator"
        enum status "pending, registered, deleted"
        enum work_type "online, offline, hybrid"
    }
    
    matching_post_image {
        bigint id PK
        bigint matching_post_id FK
        datetime created_at
        varchar original_image_url
        varchar thumbnail_image_url
        varchar file_name
        enum status "pending, deleted"
    }
    
    comment {
        bigint id PK
        bigint matching_post_id FK
        bigint member_id FK
        bigint parent_id FK
        bit is_deleted
        datetime created_at
        text content
    }
```

[//]: # (![Image]&#40;https://github.com/user-attachments/assets/7f7382d3-ebf3-4040-80bb-3311350545d8&#41;)

### MongoDB Schema

[//]: # (<img width="519" alt="Image" src="https://github.com/user-attachments/assets/54ecdea4-6236-44c5-a711-45552357f091" />)
```mermaid
classDiagram
    class chat_room {
        +_id : objectid PK
        +class : string
        +is_deleted : boolean
        +member_ids : array
    }

    class chat_message {
        +_id : objectid PK
        +chat_room_id : string FK
        +message : string
        +class : string
        +created_at : isodate
        +sender_id : int64
        +is_deleted : boolean
        +unread_count : int32
    }

    chat_room "1" -- "0..*" chat_message : chat_room_id
```


### 🔁 프로젝트 사용 흐름


```mermaid
flowchart TD
    A[시작] --> B[회원 가입]
    B --> C[포트폴리오 작성]
    C --> K[포트폴리오 수정/삭제]
    B --> D[구인 글 작성]
    D --> E[구인 글 수정/삭제]
    D --> F[다른 사용자와 채팅]
    D --> G[구인 글 검색]
    D --> H[다른 사람의 프로필 조회]
    H --> L[다른 사람의 포트폴리오 조회]
    F --> I[채팅 종료]
    H --> J[프로필 닫기]
    L --> M[포트폴리오 닫기]
```

## 🧩 API 명세서
[👉 API 명세서 바로가기](https://api.taptoon.site/swagger-ui/index.html)

## 이용 방법

### 1. 회원가입
![회원가입](https://github.com/user-attachments/assets/d94d6417-16dc-42f1-8788-2b306bd49485)
* 사용자는 일반 로그인, 소셜 로그인을 선택하여 회원가입 또는 로그인 할 수 있습니다.
* 사용자는 마이 페이지에서 본인의 닉네임, 비밀번호를 수정할 수 있습니다.
* 소셜 로그인으로 가입 한 사용자에 한하여 최초 1회 이메일과 비밀번호를 설정할 수 있습니다.
  * 이메일과 비밀번호를 모두 설정하면 일반 로그인도 가능합니다.
  * 이메일은 최초 1회 바꾼 후에는 수정이 불가능합니다.
  * 닉네임을 수정하지 않으면 "null"으로 표시됩니다.
* 사용자가 로그인을 해야 다른 기능을 사용할 수 있습니다.
* 닉네임이나 이름으로 사용자 검색이 가능합니다.(완벽하게 일치해야 검색 가능)

### 2. 검색
![검색](https://github.com/user-attachments/assets/1db3c2cc-554b-4c51-b724-adee69647e6c)

### 포트폴리오
* 사용자는 마이 페이지에서 개인의 역량을 나타낼 수 있는 포트폴리오를 작성할 수 있습니다.
* 포트폴리오는 글, 그림 모두 가능합니다.
* 포트폴리오는 최대 5개 까지 작성 가능합니다.(이상은 VIP 서비스 예정)
* 포트폴리오에 이미지는 최대 3개까지만 첨부할 수 있습니다.


### 구인 글
* 사용자는 원하는 파트너를 구인하는 글을 작성할 수 있습니다.
* 구인하는 글에는 본인을 간략히 나타낼 만한 그림/글을 첨부할 수 있습니다.
* 구인하는 글의 제목이나 내용으로 검색할 수 있습니다.
  * 구인 글 검색은 자동완성 기능을 지원합니다.
  * 구인 글은 여러 조건으로 검색이 가능합니다.


### 채팅
* 사용자는 원하는 파트너와 컨택하기 위해 채팅을 진행할 수 있습니다.
* 채팅은 1:1 채팅, 그룹 채팅 모두 가능합니다.
* 채팅의 읽음 여부도 확인할 수 있습니다.

## 기능 목록
* 인증/인가
  * 소셜 로그인
  * security
* 이미지 등록
* 채팅
* 검색

## Dev log
* [📌 Elasticsearch 클러스터링 적용기 🚀](/devlog/강성욱/elasticsearch_고가용성을_위한_클러스터링_적용기.md)
* [📌 Elasticsearch 적용기 🚀](/devlog/강성욱/elasticsearch_고군분투_적용기.md)
* [📌 조회수 동시성 문제 해결 여정 🚀](/devlog/강성욱/조회수_동시성_문제_해결_여정.md)
* [📌 인덱스 최적화로 검색 API 성능 개선하기 🚀](/devlog/강성욱/인덱스를_활용한_검색_속도_향상_여정.md)
* [📌 CI/CD 개발노트 🚀](devlog/김창현/개발노트-CI,CD.md)
* [📌 이미지 개발노트 🚀](devlog/김창현/개발노트-이미지.md)
* [📌 인증/인가 개발노트 🚀](devlog/김창현/개발노트-인증,인가.md)

## Developed by
<table>
  <tr>
    <td align="center">
      <b><a href="https://github.com/chk223">김창현</a></b><br>
      <a href="https://github.com/chk223">
        <img src="https://avatars.githubusercontent.com/u/104356399?v=4" width="100px" />
      </a><br>
      <b>팀장</b>
    </td>
    <td align="center">
      <b><a href="https://github.com/freedrawing">강성욱</a></b><br>
      <a href="https://github.com/freedrawing">
        <img src="https://avatars.githubusercontent.com/u/43941383?v=4" width="100px" />
      </a><br>
      <b>부팀장</b>
    </td>
    <td align="center">
      <b><a href="https://github.com/leithharbor">이상구</a></b><br>
      <a href="https://github.com/leithharbor">
        <img src="https://avatars.githubusercontent.com/u/185915561?v=4" width="100px" />
      </a><br>
      <b>팀원</b>
    </td>
    <td align="center">
      <b><a href="https://github.com/dllll2">이진영</a></b><br>
      <a href="https://github.com/dllll2">
        <img src="https://avatars.githubusercontent.com/u/105922173?v=4" width="100px" />
      </a><br>
      <b>팀원</b>
    </td>
  </tr>
</table>
