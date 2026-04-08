# 🚀 MoimMoim (모임모임)
> **"누구나 쉽게 만들고 참여하는 커뮤니티 모임 플랫폼"**

**MoimMoim**은 백엔드 엔지니어로서 기술적 근거를 바탕으로 최적화된 모임 관리 서비스를 제공합니다. 복잡한 참여 프로세스를 단순화하고, **SSE를 통한 실시간 알림**으로 서비스의 생동감을 높였습니다.

---

## 🛠️ Tech Stack
### **Backend**
* **Framework**: Java 17, Spring Boot 3.4.x
* **Database**: MySQL 8.0, Redis (Ranking & Cache)
* **ORM**: Spring Data JPA
* **Security**: Spring Security, **JWT** (Stateless 인증)
* **Communication**: **SSE (Server-Sent Events)**

### **Frontend**
* **Library**: React, Vite
* **Communication**: Axios, EventSource

---

## 📂 Project Structure
역할에 따른 계층 분리를 통해 유지보수성과 확장성을 고려하여 설계되었습니다.

```text
com.example.backend/
├── common/             # 글로벌 공통 모듈
│   ├── config/         # App, Swagger, Redis 등 각종 설정
│   ├── exception/      # 전역 예외 처리 (GlobalExceptionHandler)
│   └── security/       # JWT Provider 및 시큐리티 필터
├── controller/         # API 엔드포인트 레이어
├── dto/                # 요청/응답 데이터 전송 객체
├── entity/             # JPA 엔티티 도메인 모델
│   ├── Member, MeetingPost, Participation
│   └── Notification, Category, Region, BaseTimeEntity
├── enums/              # 상태 및 타입 관리를 위한 Enum 모음
├── repository/         # DB 접근을 위한 Spring Data JPA 인터페이스
└── service/            # 핵심 비즈니스 로직 및 외부 연동 (SSE 등)
```
---

## 📌 핵심 MVP 기능 (Current Status)

### 1. 실시간 알림 시스템 (SSE)
* 사용자의 참여 신청 및 방장의 승인/거절 상태를 **SSE(Server-Sent Events)**를 통해 실시간으로 전달합니다.
* 커스텀 이벤트(`newNotification`)를 정의하여 데이터 전송의 명확성을 확보했습니다.

### 2. 참여 신청 및 승인 프로세스
* 방장(작성자)은 모임 생성 시 자동으로 참여자로 등록되며, 일반 유저의 신청에 대한 승인/거절 권한을 가집니다.
* **Query Optimization**: 참여 내역 조회 시 방장 본인을 제외하는 필터링을 통해 UI상의 중복 데이터를 제거했습니다.

### 3. JWT 기반 인증 시스템
* `JwtAuthenticationFilter`를 통해 무상태(Stateless) 기반의 보안을 구축했습니다.
* 회원가입, 로그인, 로그아웃 전반의 인증 프로세스를 처리합니다.

---

## 💡 Trouble Shooting
* **SSE 이벤트 리스너 미작동**: 기본 `message` 이벤트가 아닌 커스텀 네이밍을 사용할 때, 송수신 측의 이벤트 이름 불일치 문제를 해결하여 실시간 통신을 성공시켰습니다.
* **정적 리소스 예외**: 로그아웃 엔드포인트 부재로 인한 `NoResourceFoundException`을 확인하고, API 매핑 및 시큐리티 설정을 통해 정상화했습니다.
* **참여 목록 데이터 중복**: JPQL에 작성자 제외 조건을 추가하여 비즈니스 로직과 UI 출력 간의 괴리를 해결했습니다.

---

## 📅 Roadmap (Next Steps)
- [ ] **Scheduler**: 모집 정원 충족 및 기한 만료 시 자동 마감 처리
- [ ] **Querydsl**: 카테고리/지역별 복합 필터링 및 동적 검색 도입
- [ ] **Redis**: 조회수 기반 실시간 인기 모임 랭킹 시스템
- [ ] **AWS S3**: 모임 썸네일 및 프로필 이미지 업로드 연동