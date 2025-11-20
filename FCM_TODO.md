# FCM 기능 TODO 리스트

이 문서는 ICT 최종 프로젝트(HQ /admin 모듈)의 FCM 기능 완료 및 향상을 위한 작업을 요약합니다.

## 1. 재고/유통임박 자동 스캐너 구현/연동 (HqInventoryAlertScanner + HqInventoryAlertService)
- [x] 기존 `HqInventoryScanScheduler`, `HqInventoryScanService`, `HqInventoryScannerRepository` 구현 확인 및 적합성 검토 (완료)

## 2. HQ 사용자별 알림 선호도 API & 화면
- [x] 기존 `FcmPreferenceController`, `FcmPreferenceService`, `FcmPreference` 엔티티, `FcmPreferenceRepository` 구현 확인 및 적합성 검토 (완료)
- [x] Thymeleaf 기반 알림 설정 화면 템플릿 (`src/main/resources/templates/fcm/preference.html`) 구현 (완료)

## 3. HQ 웹프론트(Service Worker, JS)에서 브라우저 푸시 수신 플로우
- [x] `src/main/resources/templates/fragments/head.html` 수정
    - [x] 하드코딩된 Firebase config 및 `initializeApp` 호출 제거
    - [x] Firebase config 속성들을 Thymeleaf를 통해 전역 JavaScript 변수로 주입
    - [x] `firebase-messaging-compat.js` ES 모듈 임포트 추가
- [x] `src/main/resources/static/js/fcm/fcm-web.js` 수정
    - [x] `firebaseConfig` 및 `VAPID_KEY` 정의 제거 (전역 변수 및 메타 태그 사용)
    - [x] `window.firebaseConfig`를 사용하여 Firebase 초기화
    - [x] `csrfHeaders()` 함수 제거 (csrf-attach.js 자동 처리)
    - [x] `axios` 호출에서 `headers: csrfHeaders()` 제거
    - [x] `axios` 직접 사용 확인
    - [x] `enableWebPush` 및 `unregisterWebPush` 함수 구현 및 내보내기
    - [x] `onMessage` 및 `onTokenRefresh` 처리 로직 확인
- [x] `src/main/resources/static/firebase-messaging-sw.js` 생성
    - [x] Firebase SDK 임포트
    - [x] `firebaseConfig` 직접 정의 (서비스 워커용)
    - [x] Firebase 초기화
    - [x] 백그라운드 메시지 (`onBackgroundMessage`) 처리 로직 구현

## 4. 발송 이력/에러 로깅 고도화
- [x] 기존 `FcmSendLog` 엔티티, `FcmSendLogRepository`, `FcmService`의 `persistLog` 메서드, `HqFcmLogController` 구현 확인 및 적합성 검토 (완료)

---

# FCM 기능 테스트 가이드

FCM 기능 테스트를 위한 단계별 가이드입니다.

---

### 1. Firebase 프로젝트 설정 확인 (Backend `application.properties`)

`src/main/resources/application.properties` (또는 개발 환경에 맞는 `application-dev.properties` 등) 파일에 다음 Firebase 설정들이 올바르게 추가되었는지 확인합니다. 이 값들은 **Firebase Console**에서 프로젝트 설정 > 일반 > 내 앱 > 웹 앱 설정에서 확인할 수 있습니다. `firebase.web.vapid-key`는 클라우드 메시징 탭의 웹 구성 > 웹 푸시 인증서 섹션에서 생성하거나 가져올 수 있습니다.

```properties
# Firebase Project Configuration
firebase.api-key=AIzaSyA7m5jVdo-w7TBG6h6wW4h6mc5gbNjqYlU
firebase.auth-domain=ict05-final.firebaseapp.com
firebase.project-id=ict05-final
firebase.storage-bucket=ict05-final.firebasestorage.app
firebase.messaging-sender-id=382264607725
firebase.app-id=1:382264607725:web:da28516c4a49f92e045de4
firebase.measurement-id=G-YEHZ8996H8
```
**주의:** 위 값들은 이전에 `head.html`에 하드코딩되어 있던 예시 값입니다. 실제 Firebase 프로젝트의 값으로 대체되었는지 다시 한번 확인해야 합니다.

---

### 2. Spring Boot 애플리케이션 빌드 및 실행

프로젝트 루트 디렉토리에서 다음 명령어를 사용하여 애플리케이션을 빌드하고 실행합니다.

```bash
./gradlew clean build
java -jar build/libs/ict05_final_admin-0.0.1-SNAPSHOT.jar
```
또는 IDE(IntelliJ IDEA 등)에서 애플리케이션을 실행할 수 있습니다.

---

### 3. FCM 알림 설정 페이지 접속

브라우저를 열고 다음 URL로 접속합니다 (애플리케이션이 기본 포트 8080으로 실행 중이라고 가정):

`http://localhost:8081/admin/fcm/pref`

이 페이지는 `src/main/resources/templates/fcm/preference.html` 템플릿을 통해 렌더링됩니다.

---

### 4. 브라우저에서 웹 푸시 활성화

1.  **알림 권한 부여:** `http://localhost:8080/fcm/pref` 페이지에 접속하면 브라우저에서 알림 권한을 요청하는 팝업이 나타날 수 있습니다. **'허용'**을 클릭하여 알림 권한을 부여합니다.
2.  **FCM 토큰 등록 확인:** 알림 권한을 허용하면 `fcm-web.js` 스크립트가 자동으로 FCM 토큰을 가져와 백엔드(`/fcm/register` API)에 등록을 시도합니다. 브라우저 개발자 도구(F12)의 콘솔 탭에서 `[FCM] token registered ...`와 같은 로그를 확인할 수 있습니다.
3.  **알림 설정 저장:** 페이지에서 원하는 알림 수신 설정을 선택하고 **'설정 저장'** 버튼을 클릭합니다. 이 작업은 `/fcm/pref/me` API를 호출하여 사용자 선호도를 저장하고, 선택에 따라 FCM 토픽 구독/해제를 처리합니다.

---

### 5. 테스트 알림 전송

다음 방법 중 하나를 사용하여 알림을 테스트할 수 있습니다.

#### 5.1. 관리자 API를 통한 테스트 전송 (Postman, cURL 등 사용)

`HqFcmAdminController`의 `/fcm/send/test` 엔드포인트를 사용하여 특정 토픽이나 토큰으로 알림을 보낼 수 있습니다.

**예시 (cURL):**
(로그인 후 CSRF 토큰을 헤더에 포함해야 합니다. 브라우저 개발자 도구에서 CSRF 토큰을 확인할 수 있습니다.)

```bash

# 토픽으로 테스트 알림 전송 (예: hq-all 토픽)
curl -X POST "http://localhost:8081/admin/fcm/send/test" \
-H "Content-Type: application/json" \
-H "X-CSRF-TOKEN: YOUR_CSRF_TOKEN" \
-d 
'{ 
  "tokenOrTopic": "hq-all",
  "topic": true,
  "title": "테스트 알림",
  "body": "이것은 HQ-ALL 토픽으로 전송된 테스트 메시지입니다.",
  "data": {"link": "/admin/fcm/logs"}
}'

# 특정 토큰으로 테스트 알림 전송 (YOUR_FCM_TOKEN은 브라우저 콘솔에서 확인한 토큰)
curl -X POST "http://localhost:8081/admin/fcm/send/test" \
-H "Content-Type: application/json" \
-H "X-CSRF-TOKEN: YOUR_CSRF_TOKEN" \
-d 
'{ 
  "tokenOrTopic": "YOUR_FCM_TOKEN",
  "topic": false,
  "title": "개인 테스트 알림",
  "body": "이것은 당신의 토큰으로 전송된 개인 메시지입니다.",
  "data": {"link": "/admin/mypage"}
}'
```

#### 5.2. 스텁 API를 통한 재고/유통기한 임박 알림 테스트 (Postman, cURL 등 사용)

`HqAlertStubController`의 엔드포인트를 사용하여 재고 부족 및 유통기한 임박 알림을 시뮬레이션할 수 있습니다.

```bash


# 재고 부족 알림 테스트
curl -X POST "http://localhost:8081/admin/fcm/hq-alert/test/stock-low?materialName=우유&qty=5&threshold=10" \
-H "X-CSRF-TOKEN: YOUR_CSRF_TOKEN"

# 유통기한 임박 알림 테스트
curl -X POST "http://localhost:8081/admin/fcm/hq-alert/test/expire-soon?materialName=빵&days=2&lot=LOT12345" \
-H "X-CSRF-TOKEN: YOUR_CSRF_TOKEN"
```

#### 5.3. 스케줄러를 통한 자동 알림 테스트

`HqInventoryScanController`의 엔드포인트를 호출하여 재고/유통기한 임박 스캔을 수동으로 트리거할 수 있습니다.

```bash


# 모든 스캔 실행 (재고 부족 + 유통기한 임박)
curl -X POST "http://localhost:8081/admin/fcm/hq-scan/run" \
-H "X-CSRF-TOKEN: YOUR_CSRF_TOKEN"

# 재고 부족 스캔만 실행
curl -X POST "http://localhost:8081/admin/fcm/hq-scan/stock-low" \
-H "X-CSRF-TOKEN: YOUR_CSRF_TOKEN"

# 유통기한 임박 스캔만 실행
curl -X POST "http://localhost:8081/admin/fcm/hq-scan/expire-soon" \
-H "X-CSRF-TOKEN: YOUR_FCM_TOKEN"
```
**참고:** 스케줄러가 실제로 알림을 보내려면 `application.properties`에서 `fcm.scanner.enabled=true`로 설정되어 있어야 합니다.

---

### 6. FCM 전송 로그 확인

알림 전송 후, 다음 URL에서 전송 로그를 확인할 수 있습니다.

`http://localhost:8081/admin/fcm/logs`

이 페이지에서 전송된 알림의 제목, 본문, 결과(성공/실패) 등을 확인할 수 있습니다.

---

이 단계들을 따라 FCM 기능이 올바르게 작동하는지 테스트해 보십시오. 추가적인 질문이 있다면 언제든지 알려주세요.

## 5. `http://localhost:8081/admin/fcm/pref` 페이지 접근 문제 진단 및 해결
- [x] `HqFcmPreferenceController.java` 검토
    - [x] `@RequestMapping` 및 `@PreAuthorize` 설정 확인
    - [x] 메서드 시그니처 및 로직 오류 확인
- [x] `HqFcmAdminController.java` (참고용) 검토
- [x] 메인 애플리케이션 클래스 (`Ict05FinalAdminApplication.java`) 검토
    - [x] `@EnableMethodSecurity` (또는 `@EnableGlobalMethodSecurity`) 존재 여부 확인
    - [x] 컴포넌트 스캐닝 설정 확인
- [x] `build.gradle` 의존성 확인 (Spring Security, Spring Web, Thymeleaf)
- [x] 테스트 시나리오 재검토 (로그인 상태, 사용자 역할 등)
- [x] `HqFcmViewController.java` 생성 (FCM 설정 페이지 뷰 제공)