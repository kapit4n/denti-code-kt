package com.denticode.kt.export

import com.denticode.kt.data.AppointmentRow
import com.denticode.kt.data.FollowUp
import com.denticode.kt.data.Patient
import com.denticode.kt.data.PatientDentalRecord
import com.denticode.kt.data.PatientDocument
import com.denticode.kt.data.PatientLedgerPayment
import com.denticode.kt.data.PatientMedicalRecord
import com.denticode.kt.data.PatientNote
import com.denticode.kt.data.PatientTreatmentRow
import com.denticode.kt.data.Prescription
import com.denticode.kt.data.TreatmentPlan
import com.denticode.kt.data.TreatmentStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Todo lo necesario para exportar la ficha/resumen de un paciente. */
data class PatientSummaryBundle(
    val patient: Patient,
    val appointments: List<AppointmentRow> = emptyList(),
    val treatments: List<PatientTreatmentRow> = emptyList(),
    val payments: List<PatientLedgerPayment> = emptyList(),
    val medicalRecords: List<PatientMedicalRecord> = emptyList(),
    val dentalRecords: List<PatientDentalRecord> = emptyList(),
    val documents: List<PatientDocument> = emptyList(),
    val notes: List<PatientNote> = emptyList(),
    val prescriptions: List<Prescription> = emptyList(),
    val followUps: List<FollowUp> = emptyList(),
    val treatmentPlans: List<TreatmentPlan> = emptyList(),
) {
    val totalBillable: Double
        get() = treatments.filter { it.status != TreatmentStatus.CANCELLED }.sumOf { it.totalPrice }

    val totalPaid: Double
        get() = payments.sumOf { it.amount }

    val pendingBalance: Double
        get() = (totalBillable - totalPaid).coerceAtLeast(0.0)
}

private val exportDateTimeFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale("es", "ES"))

private fun fmtIsoDateTime(value: String): String {
    val t = value.trim()
    return try {
        val ldt = if (t.contains('T')) LocalDateTime.parse(t) else LocalDate.parse(t).atStartOfDay()
        ldt.format(exportDateTimeFmt)
    } catch (_: Exception) {
        t
    }
}

private fun fmtEpoch(epochMs: Long): String =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs), ZoneId.systemDefault()).format(exportDateTimeFmt)

private fun money(v: Double): String = ExportService.money(v)

private fun esc(value: String?): String =
    value.orEmpty()
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

// ── HTML export ────────────────────────────────────────────────────────────

private fun buildPatientTable(bundle: PatientSummaryBundle): String =
    buildString {
        appendLine("<table class=\"info\">")
        appendLine("<tr><th>ID</th><td>${bundle.patient.id}</td><th>Edad</th><td>${bundle.patient.age} años</td></tr>")
        appendLine("<tr><th>Documento</th><td>${esc(bundle.patient.documentNumber ?: "—")}</td><th>Género</th><td>${esc(bundle.patient.gender ?: "—")}</td></tr>")
        appendLine("<tr><th>Teléfono</th><td>${esc(bundle.patient.contactPhone)}</td><th>Email</th><td>${esc(bundle.patient.email ?: "—")}</td></tr>")
        appendLine("<tr><th>Dirección</th><td colspan=\"3\">${esc(bundle.patient.address ?: "—")}</td></tr>")
        bundle.patient.medicalHistorySummary?.takeIf { it.isNotBlank() }?.let {
            appendLine("<tr><th>Resumen médico</th><td colspan=\"3\">${esc(it)}</td></tr>")
        }
        appendLine("<tr><th>Registrado</th><td colspan=\"3\">${fmtEpoch(bundle.patient.createdAtEpochMs)}</td></tr>")
        appendLine("</table>")
    }

private fun buildFinancialTable(bundle: PatientSummaryBundle): String =
    buildString {
        appendLine("<table class=\"info\">")
        appendLine("<tr><th>Total facturado</th><td>${money(bundle.totalBillable)}</td></tr>")
        appendLine("<tr><th>Total pagado</th><td>${money(bundle.totalPaid)}</td></tr>")
        appendLine("<tr><th>Saldo pendiente</th><td class=\"strong\">${money(bundle.pendingBalance)}</td></tr>")
        appendLine("</table>")
    }

private fun buildTreatmentsTable(rows: List<PatientTreatmentRow>): String {
    if (rows.isEmpty()) return "<p class=\"muted\">Sin tratamientos.</p>"
    return buildString {
        appendLine("<table><thead><tr><th>Fecha</th><th>Procedimiento</th><th>Doctor</th><th>Estado</th><th>Monto</th></tr></thead>")
        appendLine("<tbody>")
        rows.sortedByDescending { it.actionAt }.forEach { t ->
            appendLine(
                "<tr><td>${esc(fmtIsoDateTime(t.actionAt))}</td><td>${esc(t.procedureTypeName)}</td><td>${esc(t.doctorName)}</td>" +
                    "<td>${esc(t.status.labelEs)}</td><td>${money(t.totalPrice)}</td></tr>",
            )
        }
        appendLine("</tbody></table>")
    }
}

private fun buildPaymentsTable(rows: List<PatientLedgerPayment>): String {
    if (rows.isEmpty()) return "<p class=\"muted\">Sin pagos.</p>"
    return buildString {
        appendLine("<table><thead><tr><th>Fecha</th><th>Detalle</th><th>Método</th><th>Monto</th></tr></thead>")
        appendLine("<tbody>")
        rows.sortedByDescending { it.paidAt }.forEach { p ->
            appendLine(
                "<tr><td>${esc(fmtIsoDateTime(p.paidAt))}</td><td>${esc(p.procedureTypeName ?: p.note ?: "—")}</td>" +
                    "<td>${esc(p.method?.displayLabel ?: "—")}</td><td>${money(p.amount)}</td></tr>",
            )
        }
        appendLine("</tbody></table>")
    }
}

private fun buildAppointmentsTable(rows: List<AppointmentRow>): String {
    if (rows.isEmpty()) return "<p class=\"muted\">Sin citas.</p>"
    return buildString {
        appendLine("<table><thead><tr><th>Fecha</th><th>Tratamiento</th><th>Doctor</th><th>Estado</th></tr></thead>")
        appendLine("<tbody>")
        rows.sortedByDescending { it.scheduledAt }.forEach { a ->
            appendLine(
                "<tr><td>${esc(fmtIsoDateTime(a.scheduledAt))}</td><td>${esc(a.procedureTypeName ?: a.purpose ?: "—")}</td>" +
                    "<td>${esc(a.doctorName)}</td><td>${esc(a.status.displayLabel)}</td></tr>",
            )
        }
        appendLine("</tbody></table>")
    }
}

private fun buildMedicalList(rows: List<PatientMedicalRecord>): String {
    if (rows.isEmpty()) return "<p class=\"muted\">Sin registros médicos.</p>"
    return buildString {
        appendLine("<ul>")
        rows.sortedByDescending { it.recordedAt }.forEach { m ->
            appendLine(
                "<li><strong>${esc(m.recordType.labelEs)}</strong> — ${esc(m.description)} " +
                    "<span class=\"muted\">(${esc(fmtIsoDateTime(m.recordedAt))}${m.doctorName?.let { " · ${esc(it)}" } ?: ""})</span>" +
                    (m.notes?.let { "<br><span class=\"muted\">${esc(it)}</span>" } ?: "") + "</li>",
            )
        }
        appendLine("</ul>")
    }
}

private fun buildDentalList(rows: List<PatientDentalRecord>): String {
    if (rows.isEmpty()) return "<p class=\"muted\">Sin registros dentales.</p>"
    return buildString {
        appendLine("<ul>")
        rows.sortedByDescending { it.recordedAt }.forEach { d ->
            val tooth = listOfNotNull(d.toothNumber, d.toothQuadrant).joinToString(" ")
            appendLine(
                "<li><strong>${if (tooth.isNotBlank()) "Pieza $tooth" else "Registro dental"}</strong> — ${esc(d.diagnosis)} " +
                    "<span class=\"muted\">(${esc(fmtIsoDateTime(d.recordedAt))}${d.doctorName?.let { " · ${esc(it)}" } ?: ""})</span>" +
                    (d.treatmentPerformed?.let { "<br>Tratamiento: ${esc(it)}" } ?: "") +
                    (d.notes?.let { "<br><span class=\"muted\">${esc(it)}</span>" } ?: "") + "</li>",
            )
        }
        appendLine("</ul>")
    }
}

private fun buildPlansList(plans: List<TreatmentPlan>): String {
    if (plans.isEmpty()) return "<p class=\"muted\">Sin planes de tratamiento.</p>"
    return buildString {
        appendLine("<ul>")
        plans.sortedByDescending { it.createdAtEpochMs }.forEach { plan ->
            appendLine("<li><strong>${esc(plan.title)}</strong> — ${esc(plan.status.labelEs)} · ${money(plan.estimatedCost)}")
            if (plan.phases.isNotEmpty()) {
                appendLine("<ul>")
                plan.phases.forEach { ph ->
                    appendLine("<li>${esc(ph.name)} — ${esc(ph.status.labelEs)} · ${money(ph.estimatedCost)}</li>")
                }
                appendLine("</ul>")
            }
            appendLine("</li>")
        }
        appendLine("</ul>")
    }
}

private fun buildPrescriptionsList(rows: List<Prescription>): String {
    if (rows.isEmpty()) return "<p class=\"muted\">Sin recetas.</p>"
    return buildString {
        appendLine("<ul>")
        rows.sortedByDescending { it.prescribedAt }.forEach { r ->
            appendLine(
                "<li><strong>${esc(r.medicine)}</strong> — ${esc(r.dosage)}, ${esc(r.frequency)} " +
                    "<span class=\"muted\">(${esc(r.status.labelEs)}${r.doctorName?.let { " · ${esc(it)}" } ?: ""})</span>" +
                    (r.instructions?.let { "<br>${esc(it)}" } ?: "") + "</li>",
            )
        }
        appendLine("</ul>")
    }
}

private fun buildFollowUpsList(rows: List<FollowUp>): String {
    if (rows.isEmpty()) return "<p class=\"muted\">Sin seguimientos.</p>"
    return buildString {
        appendLine("<ul>")
        rows.sortedByDescending { it.dueDate }.forEach { f ->
            appendLine(
                "<li><strong>${esc(fmtIsoDateTime(f.dueDate))}</strong> — ${esc(f.status.labelEs)}" +
                    (f.notes?.let { " · ${esc(it)}" } ?: "") + "</li>",
            )
        }
        appendLine("</ul>")
    }
}

private fun buildDocumentsList(rows: List<PatientDocument>): String {
    if (rows.isEmpty()) return "<p class=\"muted\">Sin documentos.</p>"
    return buildString {
        appendLine("<ul>")
        rows.sortedByDescending { it.uploadedAtEpochMs }.forEach { d ->
            appendLine(
                "<li><strong>${esc(d.title)}</strong> — ${esc(d.category.labelEs)}" +
                    " <span class=\"muted\">(${esc(d.fileName)} · ${fmtEpoch(d.uploadedAtEpochMs)})</span></li>",
            )
        }
        appendLine("</ul>")
    }
}

private fun buildNotesList(rows: List<PatientNote>): String {
    if (rows.isEmpty()) return "<p class=\"muted\">Sin notas.</p>"
    return buildString {
        appendLine("<ul>")
        rows.sortedByDescending { it.createdAtEpochMs }.forEach { n ->
            appendLine(
                "<li><strong>${esc(n.authorLabel)}</strong>${if (n.isPinned) " <span class=\"badge\">fijada</span>" else ""}" +
                    " <span class=\"muted\">(${fmtEpoch(n.createdAtEpochMs)})</span><br>${esc(n.body)}</li>",
            )
        }
        appendLine("</ul>")
    }
}

private fun htmlSection(title: String, body: String): String =
    "<section><h2>$title</h2>$body</section>\n"

/**
 * Ficha del paciente en HTML autocontenido (abre en cualquier navegador, apto para imprimir).
 */
fun renderPatientSummaryHtml(
    bundle: PatientSummaryBundle,
    clinicName: String = ExportService.clinicName,
    clinicCity: String = ExportService.clinicCity,
    clinicCountry: String = ExportService.clinicCountry,
): String =
    """
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Ficha — ${esc(bundle.patient.fullName)}</title>
<style>
  body { font-family: 'Segoe UI', Roboto, Arial, sans-serif; margin: 0; padding: 24px; color: #1f2937; background: #f5f7fa; }
  .sheet { max-width: 860px; margin: 0 auto; background: #ffffff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 32px; }
  .clinic { border-bottom: 3px solid #0d9488; padding-bottom: 12px; margin-bottom: 20px; }
  .clinic h1 { margin: 0; font-size: 20px; color: #0d9488; }
  .clinic p { margin: 2px 0 0; color: #6b7280; font-size: 13px; }
  h1.title { font-size: 22px; margin: 0 0 4px; }
  .sub { color: #6b7280; font-size: 13px; margin-bottom: 20px; }
  section { margin-top: 24px; }
  h2 { font-size: 15px; text-transform: uppercase; letter-spacing: .5px; color: #0d9488; border-bottom: 1px solid #e5e7eb; padding-bottom: 6px; }
  table { width: 100%; border-collapse: collapse; font-size: 13px; margin-top: 8px; }
  th, td { text-align: left; padding: 6px 8px; border-bottom: 1px solid #f1f5f9; }
  th { background: #f8fafc; font-weight: 600; }
  table.info th { width: 140px; background: none; color: #6b7280; font-weight: 500; }
  td.strong { font-weight: 700; color: #b45309; }
  ul { margin: 8px 0; padding-left: 20px; font-size: 13px; }
  .muted { color: #9ca3af; }
  .badge { background: #e0f2fe; color: #0369a1; border-radius: 4px; padding: 1px 6px; font-size: 11px; }
  footer { margin-top: 28px; padding-top: 12px; border-top: 1px solid #e5e7eb; color: #9ca3af; font-size: 12px; text-align: center; }
  @media print { body { background: #fff; padding: 0; } .sheet { border: none; } }
</style>
</head>
<body>
<div class="sheet">
  <div class="clinic"><h1>${esc(clinicName)}</h1><p>${esc(clinicCity)}, ${esc(clinicCountry)}</p></div>
  <h1 class="title">Ficha del paciente</h1>
  <p class="sub">${esc(bundle.patient.fullName)} · ${bundle.patient.age} años · Generada el ${fmtEpoch(System.currentTimeMillis())}</p>
  ${htmlSection("Datos del paciente", buildPatientTable(bundle))}
  ${htmlSection("Resumen financiero", buildFinancialTable(bundle))}
  ${htmlSection("Tratamientos (${bundle.treatments.size})", buildTreatmentsTable(bundle.treatments))}
  ${htmlSection("Pagos (${bundle.payments.size})", buildPaymentsTable(bundle.payments))}
  ${htmlSection("Citas (${bundle.appointments.size})", buildAppointmentsTable(bundle.appointments))}
  ${htmlSection("Historial médico (${bundle.medicalRecords.size})", buildMedicalList(bundle.medicalRecords))}
  ${htmlSection("Historial dental (${bundle.dentalRecords.size})", buildDentalList(bundle.dentalRecords))}
  ${htmlSection("Planes de tratamiento (${bundle.treatmentPlans.size})", buildPlansList(bundle.treatmentPlans))}
  ${htmlSection("Recetas (${bundle.prescriptions.size})", buildPrescriptionsList(bundle.prescriptions))}
  ${htmlSection("Seguimientos (${bundle.followUps.size})", buildFollowUpsList(bundle.followUps))}
  ${htmlSection("Documentos (${bundle.documents.size})", buildDocumentsList(bundle.documents))}
  ${htmlSection("Notas (${bundle.notes.size})", buildNotesList(bundle.notes))}
  <footer>Generado por Denti-Code KT · ${esc(clinicName)}</footer>
</div>
</body>
</html>
""".trimIndent()

// ── Plain-text export ──────────────────────────────────────────────────────

private fun textSection(title: String, lines: List<String>): String {
    if (lines.isEmpty()) return "  $title: (sin datos)\n"
    return buildString {
        appendLine("$title:")
        lines.forEach { appendLine("  $it") }
    }
}

/**
 * Resumen del paciente en texto plano condensado (imprimible).
 */
fun renderPatientSummaryText(
    bundle: PatientSummaryBundle,
    clinicName: String = ExportService.clinicName,
    clinicCity: String = ExportService.clinicCity,
    clinicCountry: String = ExportService.clinicCountry,
): String {
    val bar = "=".repeat(64)
    return buildString {
        appendLine(bar)
        appendLine("  $clinicName · $clinicCity, $clinicCountry")
        appendLine("  RESUMEN DEL PACIENTE")
        appendLine(bar)
        appendLine("${bundle.patient.fullName} (ID ${bundle.patient.id}) · ${bundle.patient.age} años")
        appendLine("Tel: ${bundle.patient.contactPhone}${bundle.patient.email?.let { " · Email: $it" } ?: ""}")
        appendLine("Generado: ${fmtEpoch(System.currentTimeMillis())}")
        appendLine()
        appendLine("Resumen financiero")
        appendLine("  Total facturado : ${money(bundle.totalBillable)}")
        appendLine("  Total pagado    : ${money(bundle.totalPaid)}")
        appendLine("  Saldo pendiente : ${money(bundle.pendingBalance)}")
        appendLine()
        append(
            textSection(
                "Tratamientos (${bundle.treatments.size})",
                bundle.treatments.sortedByDescending { it.actionAt }.map { t ->
                    "${fmtIsoDateTime(t.actionAt)} — ${t.procedureTypeName} · ${t.status.labelEs} · ${money(t.totalPrice)}"
                },
            ),
        )
        append(
            textSection(
                "Pagos (${bundle.payments.size})",
                bundle.payments.sortedByDescending { it.paidAt }.map { p ->
                    "${fmtIsoDateTime(p.paidAt)} — ${p.procedureTypeName ?: p.note ?: "—"} · ${p.method?.displayLabel ?: "—"} · ${money(p.amount)}"
                },
            ),
        )
        append(
            textSection(
                "Citas (${bundle.appointments.size})",
                bundle.appointments.sortedByDescending { it.scheduledAt }.map { a ->
                    "${fmtIsoDateTime(a.scheduledAt)} — ${a.procedureTypeName ?: a.purpose ?: "—"} · ${a.doctorName} · ${a.status.displayLabel}"
                },
            ),
        )
        append(
            textSection(
                "Historial médico (${bundle.medicalRecords.size})",
                bundle.medicalRecords.sortedByDescending { it.recordedAt }.map { m ->
                    "${m.recordType.labelEs} — ${m.description} · ${fmtIsoDateTime(m.recordedAt)}${m.doctorName?.let { " · ${it}" } ?: ""}"
                },
            ),
        )
        append(
            textSection(
                "Historial dental (${bundle.dentalRecords.size})",
                bundle.dentalRecords.sortedByDescending { it.recordedAt }.map { d ->
                    val tooth = listOfNotNull(d.toothNumber, d.toothQuadrant).joinToString(" ")
                    "${if (tooth.isNotBlank()) "Pieza $tooth — " else ""}${d.diagnosis} · ${fmtIsoDateTime(d.recordedAt)}"
                },
            ),
        )
        append(
            textSection(
                "Planes de tratamiento (${bundle.treatmentPlans.size})",
                bundle.treatmentPlans.sortedByDescending { it.createdAtEpochMs }.map { plan ->
                    "${plan.title} · ${plan.status.labelEs} · ${money(plan.estimatedCost)}" +
                        (if (plan.phases.isNotEmpty()) {
                            " — fases: " + plan.phases.joinToString(" | ") { "${it.name} (${it.status.labelEs})" }
                        } else {
                            ""
                        })
                },
            ),
        )
        append(
            textSection(
                "Recetas (${bundle.prescriptions.size})",
                bundle.prescriptions.sortedByDescending { it.prescribedAt }.map { r ->
                    "${r.medicine} — ${r.dosage}, ${r.frequency} · ${r.status.labelEs}"
                },
            ),
        )
        append(
            textSection(
                "Seguimientos (${bundle.followUps.size})",
                bundle.followUps.sortedByDescending { it.dueDate }.map { f ->
                    "${fmtIsoDateTime(f.dueDate)} · ${f.status.labelEs}${f.notes?.let { " · ${it}" } ?: ""}"
                },
            ),
        )
        append(
            textSection(
                "Documentos (${bundle.documents.size})",
                bundle.documents.sortedByDescending { it.uploadedAtEpochMs }.map { d ->
                    "${d.title} · ${d.category.labelEs} · ${d.fileName}"
                },
            ),
        )
        append(
            textSection(
                "Notas (${bundle.notes.size})",
                bundle.notes.sortedByDescending { it.createdAtEpochMs }.map { n ->
                    "[${n.authorLabel}]${if (n.isPinned) " ★" else ""} ${n.body}"
                },
            ),
        )
        appendLine(bar)
    }
}

// ── Payments CSV export ────────────────────────────────────────────────────

/** Fila exportable del listado de pagos (independiente de la capa UI). */
data class PaymentExportRow(
    val id: Int,
    val patientId: Int,
    val patientName: String,
    val detailLabel: String,
    val methodLabel: String,
    val amount: Double,
    val dateLabel: String,
    val statusLabel: String,
)

private fun csvField(value: String): String {
    val v = value.replace("\"", "\"\"")
    return if (v.contains(';') || v.contains('\n') || v.contains('"')) "\"$v\"" else v
}

/**
 * Listado de pagos en CSV (separador `;`, compatible con Excel en español).
 */
fun renderPaymentsCsv(rows: List<PaymentExportRow>): String =
    buildString {
        appendLine("ID;Paciente;Detalle;Método;Monto (Bs);Fecha;Estado")
        rows.forEach { r ->
            appendLine(
                listOf(
                    r.id.toString(),
                    csvField(r.patientName),
                    csvField(r.detailLabel),
                    csvField(r.methodLabel),
                    "%.2f".format(r.amount),
                    csvField(r.dateLabel),
                    csvField(r.statusLabel),
                ).joinToString(";"),
            )
        }
    }
