# PDF 생성 기능 추가 작업 목록

- [ ] **1. KPI 분석 PDF 다운로드 기능 추가**
    - [ ] **1.1. Python PDF 생성 서비스 구성**
        - [x] 1.1.1. `python-pdf-download/component` 디렉토리 생성 - 2025-11-04 00:00:00
        - [x] 1.1.2. `python-pdf-download/component/pdf_generator.py` 파일 생성 (PDF 생성 로직) - 2025-11-04 00:00:00
        - [x] 1.1.3. `python-pdf-download/app.py` 파일 생성 (FastAPI 엔드포인트) - 2025-11-04 00:00:00
        - [x] 1.1.4. `python-pdf-download/requirements.txt` 파일 생성 (의존성 관리) - 2025-11-04 00:00:00
    - [ ] **1.2. Spring Boot와 Python 서비스 연동**
        - [x] 1.2.1. Python 통신 클라이언트 파일 확인 (`PythonPdfClient.java`) - 완료 (기존 파일 활용)
        - [x] 1.2.2. `AnalyticsRestController`에 PDF 다운로드 엔드포인트 추가 - 2025-11-04 00:00:00
        - [x] 1.2.3. `AnalyticsService`에 PDF 생성 로직 추가 - 2025-11-04 00:00:00
        - [x] 1.2.4. `application.properties`에 Python 서비스 URL 설정 - 2025-11-04 00:00:00
    - [ ] **1.3. 프론트엔드 수정**
        - [x] 1.3.1. `kpi.html`에 PDF 다운로드 버튼 추가 - 2025-11-04 00:00:00

- [ ] **2. (추후) 다른 분석 페이지 PDF 다운로드 기능 추가**

- [ ] **3. 테스트 및 검증**
    - [x] 3.1. Python 서비스 단독 실행 및 Postman 테스트 - 2025-11-04 00:00:00
    - [ ] 3.2. Spring Boot 연동 테스트