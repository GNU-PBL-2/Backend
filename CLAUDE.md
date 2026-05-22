# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "gnu.project.pbl2.SomeTest"

# QueryDSL Q클래스 생성
./gradlew compileJava

# 로컬 프로파일로 실행 (H2 인메모리 DB 사용)
SPRING_PROFILES_ACTIVE=local JWT_SECRET_KEY=... KAKAO_CLIENT_ID=... KAKAO_CLIENT_SECRET=... ./gradlew bootRun
```

### 필수 환경변수
- `SPRING_PROFILES_ACTIVE` — `local` (H2) 또는 prod (MySQL)
- `JWT_SECRET_KEY` — JWT 서명 키
- `GEMINI_API_KEY` — Gemini AI API 키
- `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET` — 카카오 OAuth 앱 자격증명

### 로컬 환경 (local 프로파일)
H2 인메모리 DB 사용, `ddl-auto: create-drop`, SQL 출력 활성화, H2 콘솔: `/h2-console`  
FastAPI 서버는 `http://localhost:8000`에 별도 실행 필요 (Gemini 레시피 추출용)

---

## 아키텍처 개요

### 인증 흐름

1. **카카오 OAuth**: `KakaoProvider` → 카카오 서버에 WebClient로 액세스 토큰 요청 → 유저 정보 조회
2. **OauthUserFactory**: socialId+provider 기준으로 기존 사용자 조회 또는 신규 생성
3. **JWT 발급**: `JwtProvider`가 `userId`, `socialId`, `userRole`을 담은 토큰 발급
4. **요청 인증**: `JwtInterceptor`가 Bearer 토큰 파싱 → request attribute에 `userId`, `socialId`, `userRole` 저장 → 토큰 없으면 `GUEST` 역할 부여
5. **컨트롤러 주입**: `LoginArgumentResolver`가 `@Auth Accessor` 파라미터를 request attribute에서 resolve

### 권한 제어 (AOP)
- `@OnlyUser` / `@OnlyAdmin` 어노테이션 → `UserTypeAuthorizationAspect`가 메서드 인자의 `Accessor`를 검사
- 권한 부족 시 `AuthException(AUTH_FORBIDDEN)` 발생

### 레시피 검색 (QueryDSL)
`RecipeCustomRepositoryImpl`에서 4가지 탭 처리:
- `ALL` — 전체, 키워드 필터
- `COOKABLE` — 냉장고 재료로 조리 가능 (`noMissingIngredient` 서브쿼리: 대체 불가 재료 중 냉장고에 없는 것이 없는 레시피)
- `EXPIRING` — 유통기한 +3일 이내 재료 포함 레시피, 임박 재료 수 내림차순 정렬
- `FAVORITE` — 사용자 즐겨찾기

### 레시피 가져오기 (Admin)
`AdminRecipeController` → `RecipeImportService` → `GeminiService` (YouTube URL → FastAPI → Gemini AI → `GeminiRecipeDto`)  
FastAPI 서버가 Gemini API를 호출하고 레시피 정보를 추출함.

### 예외 처리
계층화된 예외: `BusinessException`, `AuthException`, `NotFoundException` → 모두 `GlobalExceptionHandler`에서 `ErrorCode` enum 기반 `ErrorResponse`로 변환  
새 에러 코드는 `ErrorCode` enum에 추가.

### Soft Delete
`BaseEntity`에 `isDeleted` 필드 있음. `RecipeCustomRepositoryImpl`은 쿼리에서 `recipe.isDeleted.isFalse()` 명시적으로 적용. `User`는 `UserRepository`에서 `findActiveById()` 사용.

---

## 주요 패턴 & 주의사항

- **QueryDSL Q클래스**: `src/main/generated`에 생성됨 (git 제외, `compileJava` 태스크로 생성)
- **Swagger**: 컨트롤러 docs 인터페이스 (`*Docs.java`) 별도 작성 후 컨트롤러에서 implements
- **WebClient block()**: `KakaoProvider`와 `GeminiService`에서 Reactive WebClient를 동기 블로킹(`block()`)으로 사용 중 — 수정 시 주의
- **CORS**: `WebConfig`에서 `allowedOriginPatterns("*")` 설정 — 프로덕션 배포 전 도메인 제한 필요
- **JWT 만료 시간**: `application.yml`의 `accessToken-expiration-millis: 10000000000` (약 317년) — 실제 운영 전 수정 필요