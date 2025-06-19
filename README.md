# 🗣️ 시선추적, 제스처 인식, STT 기반 AI 발표 트레이너 SPitching!

## 👩🏻‍💻 SPitching Backend

> AI 발표 트레이너 웹 백엔드 – 시선 추적, 제스처 분석, 발표 유창성 피드백까지! <br>
Webhook 기반 AI 피드백 분석 데이터 처리, AWS 기반 API 서버 배포 및 CI/CD, 챗봇 기능 & 대본 유사도 측정 알고리즘<br>
`JAVA + Spring&SpringBoot + MySQL + Postman + AWS + IntelliJ`
> 

## 🎯 **Project Goal**

발표에 대한 불안감을 극복하고 자신감을 높이기 위해

**시선, 제스처, 음성** 데이터를 분석한 종합 피드백을 제공하는 **AI 발표 연습 웹 애플리케이션**입니다.

- 시선추적, 제스처, 음성 기반 AI 분석 결과 시각화
- 대본과 실제 발표의 유사도 측정
- 반복 연습에 따른 발표력 향상률 제공
- AI 기반 Q&A 챗봇으로 질의응답 연습 가능

## 🧩 Back**end Stack**

| 카테고리 | 기술 스택 |
| --- | --- |
| Framework | Spring Boot 3.2.1 |
| Language | JAVA 21 |
| Build Tool | Gradle |
| Database | MySQL  |
| ORM | Spring Data JPA |
| Security | Spring Security |
| Auth | OAuth based Google Social Login |
| API Doc | Postman |
| Deployment | AWS EC2 |
| Cloud | AWS (EC2, RDS, S3) |
| CI/CD | GitHub Actions |
| Monitoring | AWS Cloudwatch |
| AI/ML | Algorithm with JAVA, ChatGPT, Prompt-engineering |

## **📁** **Folder Structure**

```
SPitching-BE
├── 📂 scripts          # deploy 및 CI/CD 관련 파일 모음
└── 📂 src
	└── main
	    ├── java
	    │   └── djj
	    │       └── spitching_be
	    │           ├── Controller               # 클라이언트의 요청을 처리하는 REST API 컨트롤러 모음
	    │           ├── Domain                   # JPA 엔티티 및 도메인 객체 정의 (DB 테이블과 매핑)
	    │           ├── Dto                      # 클라이언트와 데이터를 주고받기 위한 DTO
	    │           ├── Repository               # DB 접근을 위한 JPA Repository 인터페이스들
	    │           ├── Service                  # 비즈니스 로직 처리 (챗봇 prompt-engineering, 대본 유사도 계산 알고리즘 포함)
	    │           ├── config                   # 보안 설정, OAuth 설정 등 전체 애플리케이션 전역 설정
	    │           └── SpitchingApplication.java # 스프링부트 메인 실행 클래스 (프로젝트 진입점)
	    │
	    └── 📂 resources
	        └── application.yml                  # DB, OAuth, AWS 등 외부 설정을 관리하는 환경설정 파일
	    ├── .gitignore                           # Git 추적 제외 파일 설정
	    ├── README.md                            # 프로젝트 소개 및 실행 가이드 문서
	    └── build.gradle                         # 빌드 설정과 의존성을 관리하는 메인 파일
```

## **🔧 How to install**

### **1. 실행 환경**

- Java 21 이상
- OS : Ubuntu 22.04
- MySQL 인스턴스 (또는 로컬 설치)
- Gradle 설치 (또는 ./gradlew 사용)
- 다음 환경 변수 필수 :
    - OpenAI API Key
    - Google OAuth 인증 정보
    - AWS S3 접근권한 필요

### **2. 환경 변수 설정**

EC2나 로컬 IDE에서 실행 시, 아래 과정을 거쳐 `application.yml`을 설정해야합니다.

```jsx
1. `.env` 파일 생성저장소 루트 경로에 `.env.template`에 있는 환경 변수들을 생성합니다. (RDS, OAuth, AWS 키 등)
2. `application.yml` 파일 생성
`application.yml.template` 파일을 `application.yml`로 이름을 바꿉니다.
3. 환경변수 입력
복사한 `application.yml` 파일 안에서 `${...}` 변수 자리에 `.env` 파일에 입력한 값들을 직접 입력합니다.

💡 주의사항
- `.env`과 `application.yml` 파일은 절대 GitHub에 업로드하지 마세요!
- `.env.template`과 `application.yml.template`은 예시 파일로 제공되며, 민감한 키 값은 포함되어 있지 않습니다.
- .env 파일이 필요하시다면 Google OAuth 로그인, ChatGPT API 등이 필요하시다면 공식 이메일로 문의 바랍니다.
```

## **🐳 How to build**

```
# Gradle 빌드
./gradlew build

# 실행
java -jar build/libs/spitching-be-0.0.1-SNAPSHOT.jar
```

## 🧪 How to test

- SPitching은 Google OAuth 기반 인증/인가 시스템을 사용합니다. 대부분의 API는 프론트엔드에서 전달받은 암호화된 구글 ID/PW 없이는 정상 동작하지 않습니다. 따라서 API 테스트를 위해서는 구글 인증 절차가 선행되어야 합니다.
- 구글 소셜 로그인을 위한 쿠키를 공유하기 위해,  DNS 설정을 vercel과 함께 했습니다. 또한 AI 분석 데이터를 수신하여 처리합니다. 따라서 반드시 프론트엔드 및 AI와 함께 테스트해야 정상 작동합니다.
- 테스트 시 서버는 다음과 같이 작동합니다.
    - 사용자의 발표 자료 및 대본, 태그를 입력받습니다. 발표 자료 PDF는 변환 처리하여 S3에 저장합니다.
    - Webhook 아키텍처를 기반으로 AI 서버에서 받은 분석 데이터를 처리하여 RDS에 저장합니다. 이 데이터를 받음과 동시에 자체 개발한 Cosine 유사도 알고리즘으로 대본 유사도를 측정합니다.
    - 챗봇 기능과 더불어, AI 분석 데이터를 처리하여 피드백 레포트에 담길 데이터를 가공 후 제공합니다.
    - 배포 환경 : Github Action과 AWS를 통해 배포 및 CI/CD를 합니다.
- 자세한 사용법은 {시제품 사용설명서 링크}를 참고해주시길 바랍니다.

## **🔗 Related Links**

- 🧠 [AI 리드미](https://github.com/2024-capstone-project/SPitching-AI_SERVER.git)
- ⛳ [프론트엔드 리드미](https://github.com/2024-capstone-project/SPitching-FE.git)
- 📋 시제품 사용설명서
