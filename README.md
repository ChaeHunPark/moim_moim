# 📂 MoimMoim (모임모임)
> **"모으고, 모이고, 즐기다."** > 약속 잡기부터 투명한 정산까지, 모임의 모든 과정을 책임지는 올인원 모임 관리 플랫폼

<br/>

## 🔗 Links
- **Live Demo**: [서비스 연결 링크]
- **Github (Frontend)**: [프론트엔드 레포 링크]
- **Github (Backend)**: [백엔드 레포 링크]

<br/>

## 🚀 Project Overview
- **진행 기간**: 202X.XX ~ 202X.XX (X주)
- **핵심 목표**: 사용자 간 위치 기반 최적 장소 추천 및 복잡한 정산 프로세스 자동화
- **인프라 특징**: **Nginx** 리버스 프록시를 활용한 프론트/백엔드 통합 라우팅 환경 구축

<br/>

## 🛠 Tech Stack

### **Frontend**
<img src="https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black"> <img src="https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white"> <img src="https://img.shields.io/badge/Styled_Components-DB7093?style=for-the-badge&logo=styled-components&logoColor=white"> <img src="https://img.shields.io/badge/Axios-5A29E4?style=for-the-badge&logo=axios&logoColor=white">

### **Backend & Infra**
<img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white"> <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white">

<br/>

## ✨ Key Features (주요 기능)

| 기능 | 상세 설명 |
| :--- | :--- |
| **📍 중간 지점 추천** | Kakao Map API를 연동하여 멤버별 위치 기반 최적의 모임 장소 추천 |
| **📅 일정 조율** | 전원이 가능한 시간대를 시각화하여 모임 날짜 확정 기능 제공 |
| **💰 스마트 정산** | 1/N 자동 계산 및 개인별 정산 상태 실시간 추적 |
| **🌐 통합 라우팅** | Nginx를 통한 단일 진입점 관리로 보안 및 서비스 운영 효율 증대 |

<br/>

## 🔥 Technical Decision & Trouble Shooting

### **1. Nginx 리버스 프록시 도입 및 라우팅 최적화**
- **Issue**: 프론트엔드와 백엔드의 도메인/포트가 달라 발생하는 CORS 이슈와 사용자 엔드포인트 관리의 복잡성 발생.
- **Solution**: **Nginx**를 리버스 프록시 서버로 설정하여 `/api` 경로는 백엔드로, 그 외 요청은 프론트엔드로 라우팅하도록 구성했습니다.
- **Result**: 클라이언트에서 CORS 설정을 단순화하고, 단일 도메인을 통해 서비스를 안정적으로 운영할 수 있게 되었습니다.

### **2. Vite 기반의 빠른 개발 환경 구축**
- **Decision**: 대규모 라이브러리 로드 시에도 빠른 HMR(Hot Module Replacement)을 유지하기 위해 Vite를 선택했습니다.
- **Result**: 초기 구동 속도를 크게 개선하여 UI 컴포넌트 개발 효율을 극대화했습니다.

### **3. Styled Components를 통한 테마 시스템**
- **Decision**: `ThemeProvider`를 활용하여 컬러 팔레트와 폰트 사이즈를 중앙 관리함으로써, 모임 서비스의 아이덴티티를 유지하고 유지보수성을 높였습니다.

<br/>

## 🏗 System Architecture



```mermaid
graph LR
    %% 전체 노드 스타일 정의
    classDef default fill:#fbfbfb,stroke:#2a2a2a,stroke-width:1px,color:#2a2a2a;
    classDef infra fill:#2a2a2a,stroke:#2a2a2a,stroke-width:2px,color:#fff;
    classDef front fill:#e1f5fe,stroke:#01579b,stroke-width:1px,color:#01579b;
    classDef back fill:#e8f5e9,stroke:#1b5e20,stroke-width:1px,color:#1b5e20;
    classDef db fill:#e0f2f1,stroke:#00695c,stroke-width:2px,color:#00695c;
    classDef point fill:#fff3e0,stroke:#e65100,stroke-width:2px,color:#e65100,stroke-dasharray: 5 5;

    %% 외부 사용자
    User((User))

    %% 클라우드 인프라 (AWS EC2)
    subgraph AWS_EC2 [Amazon EC2 Instance]
        direction TB
        
        %% 단일 진입점 (Nginx)
        Nginx{Nginx Proxy}
        
        %% 애플리케이션 서비스 (Docker 컨테이너)
        subgraph App_Layer [Docker Containers]
            FE[React + Vite <br/> :5173]
            BE[Spring Boot <br/> :8080]
        end
    end

    %% 데이터 저장소
    DB[(AWS RDS <br/> MySQL)]

    %% 데이터 흐름 (시인성 중심 배치)
    User -->|HTTP/HTTPS: 80, 443| Nginx
    
    Nginx ---->|Static Hosting| FE
    Nginx ---->|Reverse Proxy| BE
    
    FE -.->|Axios Request| Nginx
    BE ---->|JPA / JDBC| DB

    %% 스타일 적용
    class AWS_EC2 point;
    class Nginx infra;
    class FE front;
    class BE back;
    class DB db;
    class User default;