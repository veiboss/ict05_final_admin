# component/kpi_analytics.py
from io import BytesIO
from typing import Dict, Any, Optional

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.units import mm
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle

from .pdf_generator import PdfGenerator

_GEN = PdfGenerator()

def _fmt(v: Optional[float], percent: bool = False) -> str:
    if v is None:
        return ""
    try:
        n = float(v)
        if percent:
            return f"{n:.1f}%"
        if n.is_integer():
            return f"{int(n):,}"
        return f"{n:,.1f}"
    except (TypeError, ValueError):
        return str(v)

def generate_kpi_pdf(payload: Dict[str, Any]) -> bytes:
    """KPI 분석 데이터를 기반으로 PDF 생성."""
    styles = _GEN.styles
    crit   = payload.get("criteria", {}) or {}
    rows   = payload.get("data", []) or []

    buf = BytesIO()
    doc = SimpleDocTemplate(
        buf, pagesize=landscape(A4),
        leftMargin=10*mm, rightMargin=10*mm, topMargin=15*mm, bottomMargin=15*mm
    )

    story = []
    title  = crit.get("title", "KPI 분석 리포트")
    period = f"기간: {crit.get('startDate', '')} ~ {crit.get('endDate', '')}"

    story.append(Paragraph(title, styles["TitleKR"]))
    story.append(Spacer(1, 4*mm))
    story.append(Paragraph(period, styles["BodyRight"]))
    story.append(Spacer(1, 6*mm))

    if not rows:
        story.append(Paragraph("표시할 데이터가 없습니다.", styles["BodyKR"]))
        doc.build(story)
        return buf.getvalue()

    headers = ["일자", "매장명", "매출", "결제건수", "UPT", "ADS", "AUR", "전월대비", "전년대비"]
    data = [headers]
    for r in rows:
        data.append([
            r.get("date",""),
            r.get("storeName",""),
            _fmt(r.get("sales")),
            _fmt(r.get("transaction")),
            _fmt(r.get("upt")),
            _fmt(r.get("ads")),
            _fmt(r.get("aur")),
            _fmt(r.get("compMoM"), True),
            _fmt(r.get("compYoY"), True),
        ])

    table = Table(
        data,
        colWidths=[25*mm, 40*mm, 25*mm, 25*mm, 20*mm, 25*mm, 25*mm, 22*mm, 22*mm],
        repeatRows=1,   # 각 페이지에서 헤더 반복
    )
    table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor("#F3F3F3")),
        ('TEXTCOLOR',  (0, 0), (-1, 0), colors.black),
        ('GRID',       (0, 0), (-1, -1), 0.5, colors.grey),
        ('FONTNAME',   (0, 0), (-1, 0), 'KR-Bold'),
        ('FONTNAME',   (0, 1), (-1, -1), 'KR-Regular'),
        ('FONTSIZE',   (0, 0), (-1, -1), 9),
        ('ALIGN',      (0, 0), (-1, 0), 'CENTER'),
        ('ALIGN',      (0, 1), (1, -1), 'LEFT'),   # 일자/매장명 좌측정렬
        ('ALIGN',      (2, 1), (-1, -1), 'RIGHT'), # 수치/퍼센트 우측정렬
        ('VALIGN',     (0, 0), (-1, -1), 'MIDDLE'),
        ('BOTTOMPADDING', (0, 0), (-1, 0), 3*mm),
        ('TOPPADDING',    (0, 0), (-1, 0), 3*mm),
    ]))

    story.append(table)
    doc.build(story)
    return buf.getvalue()
