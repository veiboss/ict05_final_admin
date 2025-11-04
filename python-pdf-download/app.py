# /python-pdf-download/app.py
from fastapi import FastAPI, Response
from pydantic import BaseModel, Field
from typing import List, Dict, Any

from component.pdf_generator import PdfGenerator

# --- Pydantic 모델 정의 ---
class KpiRow(BaseModel):
    """KPI 데이터 행 모델"""
    storeName: str | None = None
    sales: float | None = None
    transaction: int | None = None
    upt: float | None = None
    ads: float | None = None
    aur: float | None = None
    compMoM: float | None = None
    compYoY: float | None = None
    date: str | None = None
    ratioVisit: float | None = None
    ratioTakeout: float | None = None
    ratioDelivery: float | None = None

class KpiPayload(BaseModel):
    """PDF 생성을 위한 페이로드 모델"""
    criteria: Dict[str, Any] = Field(default_factory=dict)
    data: List[KpiRow] = Field(default_factory=list)

# --- FastAPI 앱 설정 ---
app = FastAPI(title="PDF Generation Service")
pdf_service = PdfGenerator()

@app.post("/pdf/kpi-report",
          summary="KPI 분석 리포트 PDF 생성",
          description="KPI 데이터와 검색 조건을 받아 PDF 리포트를 생성하여 반환합니다.")
def create_kpi_report(payload: KpiPayload):
    """
    KPI 분석 데이터를 받아 PDF 리포트를 생성합니다.

    - **payload**: KPI 데이터 목록과 제목, 기간 등 검색 조건을 포함합니다.
    - **returns**: 생성된 PDF 파일을 `application/pdf` 미디어 타입으로 반환합니다.
    """
    # Pydantic 모델을 dict로 변환하여 pdf_generator에 전달
    pdf_bytes = pdf_service.generate_kpi_pdf(payload.dict())
    
    return Response(content=pdf_bytes, media_type="application/pdf")

# --- 서버 실행 (로컬 테스트용) ---
# uvicorn app:app --host 0.0.0.0 --port 8000 --reload