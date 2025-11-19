# 작업 목록

- [x] `AnalyticsService`에 재료 분석 엑셀 다운로드 기능(`downloadExcelMaterials`) 추가
- [x] `AnalyticsRestController`에 재료 분석 엑셀 다운로드 API 엔드포인트 추가
- [x] `AnalyticsService`에 재료 분석 PDF 다운로드 기능(`downloadPdfMaterials`) 추가
- [x] `AnalyticsRestController`에 재료 분석 PDF 다운로드 API 엔드포인트 추가
- [x] `materials.html`에 엑셀 및 PDF 다운로드 버튼 추가
- [x] 기능 구현 완료 후 테스트

---

# Python PDF 생성 작업

- [x] `python-pdf-download/component/material_analytics.py` 파일 생성
- [x] `material_analytics.py`에 PDF 생성 로직 구현 (기존 `order_analytics.py` 참고)
- [x] `app.py`에 `MaterialsRow`, `MaterialsPayload` Pydantic 모델 추가
- [x] `app.py`에 `/pdf/materials` 엔드포인트 추가 및 `material_analytics` 연동
- [x] 기능 구현 완료 후 테스트
