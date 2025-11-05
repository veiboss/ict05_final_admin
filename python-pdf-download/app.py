# app.py

# 로컬 개발시:
# uvicorn app:app --host 0.0.0.0 --port 8000 --reload

from fastapi import FastAPI, Response, HTTPException
from pydantic import BaseModel, Field
from typing import List, Dict, Any, Optional

from component import kpi_analytics, order_analytics

app = FastAPI(title="PDF Generation Service")

# ---------- KPI ----------
class KpiRow(BaseModel):
    storeName: Optional[str] = None
    sales: Optional[float] = None
    transaction: Optional[int] = None
    upt: Optional[float] = None
    ads: Optional[float] = None
    aur: Optional[float] = None
    compMoM: Optional[float] = None
    compYoY: Optional[float] = None
    date: Optional[str] = None
    ratioVisit: Optional[float] = None
    ratioTakeout: Optional[float] = None
    ratioDelivery: Optional[float] = None

class KpiPayload(BaseModel):
    criteria: Dict[str, Any] = Field(default_factory=dict)
    data: List[KpiRow] = Field(default_factory=list)

@app.post("/pdf/kpi-report", summary="KPI 분석 리포트 PDF 생성")
def create_kpi_report(payload: KpiPayload):
    pdf_bytes = kpi_analytics.generate_kpi_pdf(payload.dict())
    return Response(content=pdf_bytes, media_type="application/pdf")

import logging
logger = logging.getLogger("orders-pdf")

# ---------- Orders ----------
class OrdersRow(BaseModel):
    date: Optional[str] = None
    orderDate: Optional[str] = None
    storeName: Optional[str] = None
    category: Optional[str] = None
    menu: Optional[str] = None
    menuCount: Optional[int] = 0
    menuSales: Optional[float] = 0
    orderCount: Optional[int] = 0
    orderSales: Optional[float] = 0
    orderType: Optional[str] = None

class OrdersPayload(BaseModel):
    criteria: Dict[str, Any] = Field(default_factory=dict)
    data: List[OrdersRow] = Field(default_factory=list)

@app.post("/pdf/orders", summary="주문 분석 리포트 PDF 생성")
def create_orders_report(payload: OrdersPayload):
    pdf_bytes = order_analytics.generate_orders_pdf(payload.dict())
    logger.info("orders.pdf length = %s bytes", 0 if not pdf_bytes else len(pdf_bytes))
    if not pdf_bytes:
        # 빈 PDF는 바로 발견되도록 500으로 돌려버리는 편이 디버그에 유리
        raise HTTPException(status_code=500, detail="Empty PDF generated")
    return Response(content=pdf_bytes, media_type="application/pdf")


# @app.post("/pdf/orders", summary="주문 분석 리포트 PDF 생성")
# def create_orders_report(payload: OrdersPayload):
#     pdf_bytes = order_analytics.generate_orders_pdf(payload.dict())
#     return Response(content=pdf_bytes, media_type="application/pdf")


