#!/usr/bin/env python3
"""Generate Denti-Code KT domain model PDF with UML diagram images."""

from __future__ import annotations

import subprocess
import sys
from datetime import date
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.platypus import (
    Image as RLImage,
    PageBreak,
    Paragraph,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)

ROOT = Path(__file__).resolve().parent
OUT_PDF = ROOT.parent / "Denti-Code-Models-UML.pdf"

DIAGRAMS = [
    (
        "01-core-er.mmd",
        "01-core-er.png",
        "1. Core persistence model (ER)",
        "SQLite schema aligned with denti-code-desktop. Primary keys and foreign keys "
        "define clinic entities: users, doctors, patients, appointments, treatments, "
        "payments, and inventory.",
    ),
    (
        "02-domain-enums.mmd",
        "02-domain-enums.png",
        "2. Domain enumerations",
        "Shared status and classification enums used across appointments, treatments, "
        "and payments.",
    ),
    (
        "03-aggregates-services.mmd",
        "03-aggregates-services.png",
        "3. Aggregates and appointment services",
        "Read models (AppointmentDetailSnapshot) and service layer over DentiRepository.",
    ),
    (
        "04-ui-view-models.mmd",
        "04-ui-view-models.png",
        "4. UI view models",
        "Presentation-layer projections from domain rows to Compose screen models.",
    ),
]

RELATIONS = [
    ("Patient", "Appointment", "1 → *", "patient_id", "A patient has many appointments."),
    ("Doctor", "Appointment", "1 → *", "primary_doctor_id", "Primary doctor per visit."),
    ("ProcedureType", "Appointment", "1 → *", "procedure_type_id", "Optional treatment catalog link."),
    ("Appointment", "Appointment", "0..1 → 0..1", "follow_up_appointment_id", "Self-reference for follow-up visits."),
    ("Appointment", "AppointmentNote", "1 → *", "appointment_id", "Structured notes on a visit."),
    ("Appointment", "AppointmentAuditEntry", "1 → *", "appointment_id", "Audit trail per action."),
    ("Patient", "PerformedAction", "1 → *", "patient_id", "Treatment performed for patient."),
    ("Appointment", "PerformedAction", "1 → 0..1", "appointment_id", "Treatment linked to visit."),
    ("Doctor", "PerformedAction", "1 → *", "performing_doctor_id", "Doctor who performed treatment."),
    ("ProcedureType", "PerformedAction", "1 → *", "procedure_type_id", "Procedure catalog reference."),
    ("Patient", "Payment", "1 → *", "patient_id", "Payments belong to patient."),
    ("Appointment", "Payment", "1 → *", "appointment_id", "Optional payment linked to visit."),
    ("PerformedAction", "Payment", "1 → *", "performed_action_id", "Payment can reference treatment."),
    ("Consultory", "MaterialInventoryLine", "1 → *", "consultory_id", "Stock per consultory."),
    ("TreatmentFacility", "MaterialInventoryLine", "1 → *", "facility_id", "Stock per facility type."),
]

ENUMS = [
    ("AppointmentStatus", "SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW, RESCHEDULED"),
    ("AppointmentSource", "MANUAL, ONLINE"),
    ("AppointmentPaymentStatus", "PAID, PENDING, PARTIAL, NONE"),
    ("TreatmentStatus", "PLANNED, IN_PROGRESS, COMPLETED, CANCELLED"),
    ("PaymentMethod", "CASH, CARD, TRANSFER, INSURANCE, OTHER"),
    ("PaymentDisplayStatus", "PAID, PENDING, FAILED (UI-derived)"),
    ("DoctorListStatus", "ACTIVE, INACTIVE, VACATION"),
    ("PatientListStatus", "ACTIVE, INACTIVE, PENDING"),
]

KOTLIN_MAP = [
    ("UsersTable", "User (DB only)"),
    ("DoctorsTable", "Doctor"),
    ("PatientsTable", "Patient"),
    ("ProcedureTypesTable", "ProcedureTypeRow"),
    ("AppointmentsTable", "AppointmentRow"),
    ("AppointmentNotesTable", "AppointmentNoteRow"),
    ("AppointmentAuditLogTable", "AppointmentAuditEntry"),
    ("PerformedActionsTable", "PatientTreatmentRow"),
    ("PaymentsTable", "PaymentRow / PatientLedgerPayment"),
    ("ConsultoriesTable", "Consultory"),
    ("TreatmentFacilitiesTable", "TreatmentFacility"),
]


def render_mermaid(mmd: Path, png: Path) -> bool:
    if png.exists() and png.stat().st_mtime >= mmd.stat().st_mtime:
        return True
    try:
        subprocess.run(
            [
                "npx",
                "-y",
                "@mermaid-js/mermaid-cli@11.4.0",
                "-i",
                str(mmd),
                "-o",
                str(png),
                "-b",
                "white",
                "-w",
                "1600",
            ],
            check=True,
            capture_output=True,
            timeout=300,
            cwd=str(ROOT),
        )
        return png.exists()
    except (subprocess.SubprocessError, FileNotFoundError, OSError):
        return False


def load_font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    candidates = [
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf" if bold else "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
        "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf" if bold else "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
    ]
    for path in candidates:
        try:
            return ImageFont.truetype(path, size)
        except OSError:
            continue
    return ImageFont.load_default()


def fallback_diagram(title: str, lines: list[str], out: Path) -> None:
    width, height = 1600, 900
    img = Image.new("RGB", (width, height), "white")
    draw = ImageDraw.Draw(img)
    title_font = load_font(32, bold=True)
    body_font = load_font(20)
    draw.rectangle([0, 0, width, 70], fill="#4338CA")
    draw.text((40, 18), title, fill="white", font=title_font)
    y = 110
    for line in lines:
        draw.text((50, y), line, fill="#1F2937", font=body_font)
        y += 34
    img.save(out, "PNG")


def ensure_diagrams() -> list[tuple[str, Path, str]]:
    ready: list[tuple[str, Path, str]] = []
    fallbacks = {
        "02-domain-enums.png": (
            "Domain enumerations",
            [
                "AppointmentStatus: SCHEDULED | CONFIRMED | IN_PROGRESS | COMPLETED | CANCELLED | NO_SHOW | RESCHEDULED",
                "AppointmentSource: MANUAL | ONLINE",
                "TreatmentStatus: PLANNED | IN_PROGRESS | COMPLETED | CANCELLED",
                "PaymentMethod: CASH | CARD | TRANSFER | INSURANCE | OTHER",
                "AppointmentPaymentStatus: PAID | PENDING | PARTIAL | NONE",
                "PaymentDisplayStatus: PAID | PENDING | FAILED",
                "DoctorListStatus: ACTIVE | INACTIVE | VACATION",
                "PatientListStatus: ACTIVE | INACTIVE | PENDING",
            ],
        ),
        "03-aggregates-services.png": (
            "Aggregates & services",
            [
                "AppointmentDetailSnapshot",
                "  ├── AppointmentRow (appointment)",
                "  ├── List<AppointmentNoteRow> (structuredNotes)",
                "  ├── AppointmentPaymentSummary (paymentSummary)",
                "  └── List<AppointmentAuditEntry> (auditLog)",
                "",
                "AppointmentDetailsService → loadDetail(), notes CRUD",
                "AppointmentActionsService → edit, reschedule, cancel, payments",
                "AppointmentWorkflowService → confirm, start, complete",
                "AppointmentReminderService → WhatsApp message / URL",
                "AppointmentAuditService → log actions",
            ],
        ),
        "04-ui-view-models.png": (
            "UI view models",
            [
                "AppointmentRow → AppointmentUiModel (Citas timeline & detail)",
                "AppointmentDetailSnapshot → AppointmentDetailPanel",
                "PatientDirectoryRow → PatientUiModel",
                "DoctorDirectoryRow → DoctorUiModel",
                "PaymentRow → PaymentUiModel",
                "Patient + rows → PatientDetailUiState",
                "AppointmentRow → PatientDetailAppointmentUi",
                "PatientLedgerPayment → PatientDetailPaymentUi",
            ],
        ),
    }

    for _mmd, png_name, section_title, _desc in DIAGRAMS:
        mmd_path = ROOT / _mmd
        png_path = ROOT / png_name
        if not render_mermaid(mmd_path, png_path) and png_name in fallbacks:
            fb_title, fb_lines = fallbacks[png_name]
            fallback_diagram(fb_title, fb_lines, png_path)
        if not png_path.exists():
            fallback_diagram(section_title, ["Diagram could not be rendered."], png_path)
        ready.append((section_title, png_path, _desc))
    return ready


def scaled_image(path: Path, max_width: float, max_height: float) -> RLImage:
    with Image.open(path) as img:
        w, h = img.size
    width_ratio = max_width / w
    height_ratio = max_height / h
    ratio = min(width_ratio, height_ratio)
    return RLImage(str(path), width=w * ratio, height=h * ratio)


def build_pdf(sections: list[tuple[str, Path, str]]) -> None:
    doc = SimpleDocTemplate(
        str(OUT_PDF),
        pagesize=A4,
        rightMargin=1.8 * cm,
        leftMargin=1.8 * cm,
        topMargin=1.5 * cm,
        bottomMargin=1.5 * cm,
        title="Denti-Code KT — Domain Models UML",
    )
    styles = getSampleStyleSheet()
    title_style = ParagraphStyle(
        "DocTitle",
        parent=styles["Title"],
        fontSize=22,
        spaceAfter=12,
        textColor=colors.HexColor("#4338CA"),
    )
    h1 = ParagraphStyle("H1", parent=styles["Heading1"], fontSize=16, spaceBefore=14, spaceAfter=8)
    h2 = ParagraphStyle("H2", parent=styles["Heading2"], fontSize=13, spaceBefore=10, spaceAfter=6)
    body = ParagraphStyle("Body", parent=styles["BodyText"], fontSize=10, leading=14)
    small = ParagraphStyle("Small", parent=styles["BodyText"], fontSize=9, leading=12, textColor=colors.grey)

    story: list = []
    story.append(Paragraph("Denti-Code KT", title_style))
    story.append(Paragraph("Domain Models — UML & Relations", h1))
    story.append(
        Paragraph(
            f"Generated on {date.today().isoformat()}. "
            "Package roots: <b>com.denticode.kt.data</b> (domain/persistence) and "
            "<b>com.denticode.kt.ui.*</b> (presentation).",
            body,
        )
    )
    story.append(Spacer(1, 0.4 * cm))

    story.append(Paragraph("Entity relationships", h1))
    rel_data = [["From", "To", "Card.", "FK column", "Meaning"]] + [
        list(r) for r in RELATIONS
    ]
    rel_table = Table(rel_data, colWidths=[2.2 * cm, 2.6 * cm, 1.4 * cm, 3.2 * cm, 6.8 * cm])
    rel_table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#4338CA")),
                ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
                ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
                ("FONTSIZE", (0, 0), (-1, -1), 8),
                ("GRID", (0, 0), (-1, -1), 0.25, colors.lightgrey),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#F5F7FB")]),
            ]
        )
    )
    story.append(rel_table)
    story.append(Spacer(1, 0.3 * cm))

    story.append(Paragraph("Domain enumerations", h2))
    enum_data = [["Enum", "Values"]] + [list(e) for e in ENUMS]
    enum_table = Table(enum_data, colWidths=[4.5 * cm, 12.5 * cm])
    enum_table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#6366F1")),
                ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
                ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
                ("FONTSIZE", (0, 0), (-1, -1), 8),
                ("GRID", (0, 0), (-1, -1), 0.25, colors.lightgrey),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
            ]
        )
    )
    story.append(enum_table)
    story.append(PageBreak())

    story.append(Paragraph("Kotlin table → model mapping", h1))
    map_data = [["SQL / Exposed table", "Kotlin data class"]] + [list(m) for m in KOTLIN_MAP]
    map_table = Table(map_data, colWidths=[6.5 * cm, 10.5 * cm])
    map_table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#4338CA")),
                ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
                ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),
                ("FONTSIZE", (0, 0), (-1, -1), 9),
                ("GRID", (0, 0), (-1, -1), 0.25, colors.lightgrey),
            ]
        )
    )
    story.append(map_table)
    story.append(PageBreak())

    max_w = doc.width
    max_h = doc.height - 3.5 * cm
    for idx, (section_title, png_path, description) in enumerate(sections):
        story.append(Paragraph(section_title, h1))
        story.append(Paragraph(description, body))
        story.append(Spacer(1, 0.2 * cm))
        story.append(scaled_image(png_path, max_w, max_h))
        story.append(Spacer(1, 0.15 * cm))
        story.append(Paragraph(f"Source: docs/uml/{png_path.name}", small))
        if idx < len(sections) - 1:
            story.append(PageBreak())

    doc.build(story)


def main() -> int:
    sections = ensure_diagrams()
    build_pdf(sections)
    print(f"PDF written to {OUT_PDF}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
