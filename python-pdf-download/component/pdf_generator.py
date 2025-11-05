# /python-pdf-download/component/pdf_generator.py
import os
from io import BytesIO
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle

class PdfGenerator:
    """
    PDF 생성기 클래스.
    ReportLab을 사용하여 다양한 유형의 PDF 문서를 생성합니다.
    """

    def __init__(self):
        self._register_fonts()
        self.styles = self._get_custom_styles()

    def _register_fonts(self):
        """한글 폰트를 시스템 환경에 맞게 등록합니다."""
        # TODO: 실제 운영 환경에 맞는 폰트 경로로 수정해야 합니다.
        font_path_reg = "/usr/share/fonts/truetype/noto/NotoSansKR-Regular.ttf"
        font_path_bold = "/usr/share/fonts/truetype/noto/NotoSansKR-Bold.ttf"

        if os.name == "nt":  # Windows
            font_path_reg = "c:/Windows/Fonts/malgun.ttf"
            font_path_bold = "c:/Windows/Fonts/malgunbd.ttf"

        if os.path.exists(font_path_reg) and os.path.exists(font_path_bold):
            pdfmetrics.registerFont(TTFont("KR-Regular", font_path_reg))
            pdfmetrics.registerFont(TTFont("KR-Bold", font_path_bold))
            pdfmetrics.registerFontFamily("KR", normal="KR-Regular", bold="KR-Bold")

    def _get_custom_styles(self):
        """PDF에 사용할 공통 스타일을 정의합니다."""
        styles = getSampleStyleSheet()
        styles.add(ParagraphStyle(name="TitleKR", fontName="KR-Bold", fontSize=18, alignment=1))
        styles.add(ParagraphStyle(name="HeaderKR", fontName="KR-Bold", fontSize=10, alignment=1))
        styles.add(ParagraphStyle(name="BodyKR", fontName="KR-Regular", fontSize=9))
        styles.add(ParagraphStyle(name="BodyRight", fontName="KR-Regular", fontSize=9, alignment=2))
        return styles

    def _format_number(self, value, is_percent=False):
        """숫자 포맷팅 유틸리티 (쉼표, 소수점 등)"""
        if value is None:
            return ""
        try:
            num = float(value)
            if is_percent:
                return f"{num:.1f}%"
            if num.is_integer():
                return f"{int(num):,}"
            return f"{num:,.1f}"
        except (ValueError, TypeError):
            return str(value)

    def generate_kpi_pdf(self, payload: dict) -> bytes:
        """KPI 분석 데이터를 기반으로 PDF를 생성합니다."""
        buffer = BytesIO()
        # 가로 방향으로 넓게 설정
        doc = SimpleDocTemplate(buffer, pagesize=landscape(A4), topMargin=15*mm, bottomMargin=15*mm, leftMargin=10*mm, rightMargin=10*mm)
        
        story = []

        # 1. 리포트 제목 및 검색 조건
        criteria = payload.get("criteria", {})
        title = criteria.get("title", "KPI 분석 리포트")
        period = f"기간: {criteria.get('startDate', '')} ~ {criteria.get('endDate', '')}"
        
        story.append(Paragraph(title, self.styles["TitleKR"]))
        story.append(Spacer(1, 4 * mm))
        story.append(Paragraph(period, self.styles["BodyRight"]))
        story.append(Spacer(1, 6 * mm))

        # 2. 데이터 테이블 생성
        kpi_data = payload.get("data", [])
        if not kpi_data:
            story.append(Paragraph("표시할 데이터가 없습니다.", self.styles["BodyKR"]))
            doc.build(story)
            return buffer.getvalue()

        # 테이블 헤더
        headers = ["일자", "매장명", "매출", "결제건수", "UPT", "ADS", "AUR", "전월대비", "전년대비"]
        
        # 테이블 데이터 구성
        table_data = [headers]
        for item in kpi_data:
            row = [
                item.get("date", ""),
                item.get("storeName", ""),
                self._format_number(item.get("sales")),
                self._format_number(item.get("transaction")),
                self._format_number(item.get("upt")),
                self._format_number(item.get("ads")),
                self._format_number(item.get("aur")),
                self._format_number(item.get("compMoM"), is_percent=True),
                self._format_number(item.get("compYoY"), is_percent=True),
            ]
            table_data.append(row)

        # 테이블 스타일
        table = Table(table_data, colWidths=[25*mm, 40*mm, 25*mm, 25*mm, 20*mm, 25*mm, 25*mm, 22*mm, 22*mm])
        style = TableStyle([
            ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor("#F3F3F3")),
            ('TEXTCOLOR', (0, 0), (-1, 0), colors.black),
            ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
            ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
            ('FONTNAME', (0, 0), (-1, 0), 'KR-Bold'),
            ('FONTNAME', (0, 1), (-1, -1), 'KR-Regular'),
            ('FONTSIZE', (0, 0), (-1, -1), 9),
            ('BOTTOMPADDING', (0, 0), (-1, 0), 3 * mm),
            ('TOPPADDING', (0, 0), (-1, 0), 3 * mm),
            ('GRID', (0, 0), (-1, -1), 0.5, colors.grey),
        ])
        table.setStyle(style)
        
        story.append(table)
        
        doc.build(story)
        return buffer.getvalue()