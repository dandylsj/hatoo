# 🏠 Hatoo — 집안일 공유 & 알림 플랫폼

> **그룹으로 집안일을 함께 관리하고, 알림과 주간 통계로 기여도를 추적하는 백엔드 API 서버**

<br>

## 📌 프로젝트 소개

Hatoo는 가족, 룸메이트 등 소규모 그룹이 집안일을 효율적으로 분담할 수 있도록 돕는 서비스입니다.
할일 등록 및 배정, FCM 푸시 알림, 주간 기여도 통계, 카카오/네이버 소셜 로그인 기능을 제공합니다.

<br>

## 👨‍👩‍👧‍👦 팀 구성

| 역할 | 인원 | 담당                                 |
|---|---|------------------------------------|
| Backend | 1명 (본인) | API 서버 설계 및 개발, DB 설계, 배포 파이프라인 구축 |
| Frontend | 1명 | Android(개발테스트 중) / iOS 앱 개발(예정)    |
| Designer | 1명 | UI/UX 디자인, 화면 설계                   |
| Project Manager | 1명 | 일정 관리, 요구사항 정의, QA                 |

<br>

## 🛠 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4.3 |
| ORM | Spring Data JPA / Hibernate |
| DB | MySQL 8 |
| 인증 | JWT (Access + Refresh Token), Spring Security |
| 소셜 로그인 | Kakao OAuth2, Naver OAuth2 |
| 푸시 알림 | Firebase Cloud Messaging (FCM) |
| 이메일 | Spring Mail (Gmail SMTP) |
| 문서화 | SpringDoc OpenAPI (Swagger UI) |
| 빌드 | Gradle |
| 배포 | GitHub Actions → GHCR → Self-hosted Runner (NAS) |

<br>

## ✨ 주요 기능

### 👥 그룹 관리
- 그룹 생성 / 초대코드 발급 / 참여 / 탈퇴 / 강제 퇴장
- 개인 그룹(혼자 사용) / 일반 그룹 구분
- 그룹별 프로필 이미지 선택

### ✅ 할일 관리
- 할일 등록 / 수정 / 삭제 / 완료 처리
- 담당자 배정 (그룹원 중 1명)
- **반복 주기 설정**: 없음 / 매시간 / 매일 / 매주 / 매달
- **마감 알림 설정**: 10분 전 / 30분 전 / 1시간 전 / 1일 전 / 1주 전

### 🔔 알림 시스템 (FCM)
![Screenshot_20260429_165022_Hatoo.jpg](../../CrossDevice/%EC%84%B8%EC%A7%84%EC%9D%98%20Z%20Fold7/storage/DCIM/Screenshots/Screenshot_20260429_165022_Hatoo.jpg)
- 할일 시작 알림 (30초 주기 스캔)
- 마감 임박 알림 (30초 주기 스캔)
- 마감 초과 알림 (30초 주기 스캔)
- 새 집안일 등록 알림 (그룹 전체)
- 할일 배정 알림 (배정받은 본인에게만)
- 새 멤버 합류 알림
- 주간 통계 공개 알림 (매주 월요일 오전 8시)
- 비활성 그룹 알림 (매월 1일)
- 알림 수신 여부를 전체/개인/그룹별로 세분화하여 설정 가능

### 📊 주간 통계
- 매주 월요일 오전 8시에 지난 주 기여도 자동 스냅샷 저장
- 그룹원별 완료율 랭킹 조회 (실시간)
- 주차별 기여도 이력 조회

### 🔐 인증
- 자체 회원가입 / 로그인 (BCrypt 암호화)
- 카카오 / 네이버 소셜 로그인
- JWT Access Token + Refresh Token
- 이메일 인증 코드 (아이디 찾기 / 비밀번호 재설정)

<br>

## 🏗 시스템 아키텍처

```
[Android / iOS 앱]
        │  HTTPS
        ▼
[Spring Boot API 서버]
        │
   ┌────┴──────────────────────┐
   │                           │
[MySQL 8]            [Firebase FCM]
                               │
                     [사용자 디바이스 푸시]

[GitHub Actions]
   └── Docker Image Build
       └── GHCR (Container Registry)
           └── Self-hosted Runner (NAS)
               └── Docker Compose 배포
```

<br>

## 📂 프로젝트 구조

```
src/main/java/com/hatoo/
├── domain/
│   ├── auth/          # 회원가입, 로그인, JWT 인증
│   ├── user/          # 유저 정보, 알림 수신 동의
│   ├── groups/        # 그룹 생성/참여/관리
│   ├── groupMember/   # 그룹 멤버
│   ├── task/          # 할일 CRUD, 반복 스케줄러, 주간 통계
│   ├── alarm/         # FCM 서비스, 알림 스케줄러, 알림 내역
│   ├── alarmUserAgree/    # 전체/개인 알림 수신 설정
│   ├── groupAlarmSetting/ # 그룹별 알림 설정
│   ├── oAuth/         # 카카오/네이버 소셜 로그인
│   ├── token/         # Refresh Token 관리
│   └── weeklyStats/   # 주간 기여도 스냅샷
├── common/
│   ├── BaseEntity.java       # createdAt, updatedAt 공통
│   ├── exception/            # 전역 예외 처리
│   └── util/                 # JWT 유틸
└── config/
    ├── SecurityConfig.java   # Spring Security, CORS
    └── FirebaseConfig.java   # FCM 초기화
```

<br>

## 🗄 ERD

> [dbdiagram.io에서 확인하기](https://dbdiagram.io) — `hatoo_dbdiagram.txt` 파일 내용을 붙여넣으면 확인할 수 있습니다.

주요 테이블:

| 테이블 | 설명 |
|---|---|
| `users` | 유저 정보, FCM 토큰, 소셜 로그인 ID |
| `groups` | 그룹 정보, 초대코드 |
| `group_members` | 유저-그룹 N:M 매핑 |
| `tasks` | 할일 정보, 반복 주기, 알림 발송 여부 |
| `task_assignees` | 할일-유저 N:M 매핑 |
| `group_tasks` | 할일-그룹 N:M 매핑 |
| `notification_history` | 알림 수신 내역 |
| `alarm_user_agree` | 전체/개인 알림 수신 설정 |
| `group_alarm_settings` | 그룹별 알림 세부 설정 |
| `weekly_stats` | 주간 기여도 스냅샷 |
| `refresh_token` | JWT 리프레시 토큰 |
| `email_verification` | 이메일 인증 코드 |

<br>

## 🔄 CI/CD 파이프라인

```
코드 Push (main 브랜치)
  └── GitHub Actions 트리거
      └── Docker Image 빌드
          └── GHCR(GitHub Container Registry)에 Push
              └── Self-hosted Runner (NAS 서버)에서 Pull
                  └── Docker Compose로 무중단 재배포
```

<br>

## 💡 기술적 고민

### 알림 다중 수신 문제
그룹 전체에게 알림을 발송할 때 배정받은 사람이 "새 할일 등록" + "나에게 배정" 두 개의 알림을 동시에 받는 중복 문제가 있었습니다. `sendTaskCreated` 메서드에 `assigneeId`를 파라미터로 추가하여 배정받은 사람은 그룹 전체 알림에서 스트림 필터로 제외하고, 개인 배정 알림만 수신하도록 분리했습니다.

### 알림 스케줄러 날짜 파싱 실패
프론트엔드가 `"2026-04-28 13:55:01.884884"` (마이크로초 포함) 또는 `"2026-04-27T01:56:04.689Z"` (ISO 8601 UTC) 형식으로 날짜를 전송하는데, 단일 패턴 파싱으로는 처리가 불가능했습니다. `Instant.parse()` → KST 변환, 다중 포맷 순차 시도, 날짜만 있는 경우 자정 처리로 폴백하는 방어 로직을 구현했습니다.

### N+1 쿼리 문제
그룹 멤버 목록 조회 후 `forEach`에서 `gm.getUser()`를 접근하는 과정에서 `@ManyToOne(fetch = LAZY)` 관계로 인해 N+1 쿼리가 발생했습니다. `JOIN FETCH` 쿼리 또는 `@EntityGraph`를 활용한 즉시 로딩으로 개선이 필요한 지점으로 파악하고 있습니다.

### 주간 통계 스냅샷 설계
실시간 조회는 DB 부하가 크고 과거 데이터를 보존할 수 없다는 문제가 있었습니다. 매주 월요일 오전 8시에 지난 주 통계를 스냅샷으로 저장하는 방식을 채택했습니다. 기존 그룹 데이터를 삭제 후 재삽입(delete-first)하여 스케줄러 재실행 시 중복 저장 없이 항상 최신 상태를 유지하도록 했습니다.

<br>

## 🚀 로컬 실행 방법

### 사전 요구사항
- Java 17
- MySQL 8
- Firebase 서비스 계정 JSON

### 1. DB 생성

```sql
CREATE DATABASE hatoo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 환경변수 설정

프로젝트 루트에 `.env` 파일 생성:

```env
DB_URL=jdbc:mysql://localhost:3306/hatoo
DB_USERNAME=root
DB_PASSWORD=비밀번호
JWT_SECRET_KEY=시크릿키
FIREBASE_PATH=/경로/service-account.json
```

### 3. 실행

```bash
./gradlew bootRun
```

### 4. API 문서 확인

```
http://localhost:8080/swagger-ui.html
```

<br>

## 📡 주요 API 목록

| Method | URL | 설명 |
|---|---|---|
| POST | `/auth/sign` | 회원가입 |
| POST | `/auth/login` | 로그인 |
| POST | `/auth/reissue` | 토큰 재발급 |
| GET | `/groups` | 내 그룹 목록 조회 |
| POST | `/groups` | 그룹 생성 |
| POST | `/tasks` | 할일 등록 |
| GET | `/tasks/group/{groupId}` | 그룹 할일 목록 조회 |
| PATCH | `/tasks/{taskId}/task-status` | 할일 완료 처리 |
| GET | `/tasks/group/{groupId}/ranking` | 실시간 랭킹 조회 |
| GET | `/tasks/group/{groupId}/weekly-stats` | 주간 기여도 통계 |
| GET | `/users/notifications` | 알림 목록 조회 |
| PATCH | `/users/notifications/read-all` | 알림 전체 읽음 처리 |

> 전체 API 목록은 Swagger UI에서 확인 가능합니다.
