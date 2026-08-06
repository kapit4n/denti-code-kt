package com.denticode.kt.export

import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.ReportsOverview
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val reportHtmlDateFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES"))

private fun reportHtmlDate(d: LocalDate): String = d.format(reportHtmlDateFmt)

private fun htmlEsc(value: String): String =
    value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

private fun htmlMoney(v: Double): String = String.format(Locale.US, "Bs. %,.2f", v)

private fun htmlPct(v: Double): String = String.format(Locale.US, "%.1f%%", v)

private fun htmlSection(title: String, body: String): String =
    "<section><h2>$title</h2>$body</section>\n"

private fun htmlKpiCard(label: String, value: String): String =
    "<div class=\"kpi\"><span class=\"kpi-label\">$label</span><span class=\"kpi-value\">$value</span></div>\n"

private fun htmlBarRow(label: String, value: String, fraction: Float, color: String): String {
    val pct = (fraction.coerceIn(0f, 1f) * 100).coerceAtLeast(if (fraction > 0f) 2f else 0f)
    return buildString {
        appendLine("<div class=\"row\"><span class=\"row-label\">${htmlEsc(label)}</span><span class=\"row-value\">$value</span></div>")
        appendLine("<div class=\"track\"><div class=\"fill\" style=\"width:${pct}%;background:$color;\"></div></div>")
    }
}

private fun htmlTable(headers: List<String>, rows: List<List<String>>): String {
    if (rows.isEmpty()) return "<p class=\"muted\">Sin datos en el periodo.</p>"
    return buildString {
        appendLine("<table><thead><tr>" + headers.joinToString("") { "<th>${htmlEsc(it)}</th>" } + "</tr></thead><tbody>")
        rows.forEach { row ->
            appendLine("<tr>" + row.joinToString("") { "<td>${htmlEsc(it)}</td>" } + "</tr>")
        }
        appendLine("</tbody></table>")
    }
}

private val htmlStatusColor: (AppointmentStatus) -> String = { status ->
    when (status) {
        AppointmentStatus.COMPLETED -> "#16a34a"
        AppointmentStatus.CONFIRMED -> "#2563eb"
        AppointmentStatus.SCHEDULED -> "#f59e0b"
        AppointmentStatus.IN_PROGRESS -> "#8b5cf6"
        AppointmentStatus.RESCHEDULED -> "#14b8a6"
        AppointmentStatus.CANCELLED -> "#ef4444"
        AppointmentStatus.NO_SHOW -> "#94a3b8"
    }
}

private val htmlPalette = listOf("#0d9488", "#2563eb", "#f59e0b", "#8b5cf6", "#14b8a6", "#ef4444", "#16a34a", "#94a3b8")

private fun noShowPct(overview: ReportsOverview): Double =
    if (overview.appointmentCount > 0) overview.noShowCount.toDouble() / overview.appointmentCount * 100.0 else 0.0

/**
 * Reporte completo en HTML autocontenido y apto para imprimir (abre en el navegador;
 * desde el diálogo de impresión se puede guardar como PDF). Sin dependencias externas.
 */
fun renderReportsHtml(
    overview: ReportsOverview,
    clinicName: String = ExportService.clinicName,
    clinicCity: String = ExportService.clinicCity,
    clinicCountry: String = ExportService.clinicCountry,
): String {
    val maxDaily = overview.dailyRevenue.maxOfOrNull { it.revenue }?.coerceAtLeast(1.0) ?: 1.0
    val maxMethod = overview.revenueByMethod.maxOfOrNull { it.revenue }?.coerceAtLeast(1.0) ?: 1.0
    val maxDoctor = overview.topDoctors.maxOfOrNull { it.revenue }?.coerceAtLeast(1.0) ?: 1.0
    val maxCategory = overview.revenueByCategory.maxOfOrNull { it.revenue }?.coerceAtLeast(1.0) ?: 1.0
    val maxStatus = overview.appointmentByStatus.maxOfOrNull { it.count.toFloat() }?.coerceAtLeast(1f) ?: 1f
    val maxMonth = overview.patientsPerMonth.maxOfOrNull { it.count.toFloat() }?.coerceAtLeast(1f) ?: 1f
    val catalog = overview.revenueVsCatalog
    val stock = overview.stockMovements
    val maxStockType = stock.byType.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1

    val kpis =
        buildString {
            appendLine(htmlKpiCard("Ingreso total", htmlMoney(overview.totalRevenue)))
            appendLine(htmlKpiCard("Pagos registrados", overview.paymentCount.toString()))
            appendLine(htmlKpiCard("Citas en el periodo", overview.appointmentCount.toString()))
            appendLine(htmlKpiCard("Nuevos pacientes", overview.newPatientsCount.toString()))
            appendLine(htmlKpiCard("Tasa de inasistencia", "${overview.noShowCount} de ${overview.appointmentCount} (${htmlPct(noShowPct(overview))})"))
            appendLine(htmlKpiCard("Valor de catálogo", htmlMoney(catalog.totalCatalog)))
            appendLine(htmlKpiCard("Total cobrado", htmlMoney(catalog.totalCharged)))
            appendLine(htmlKpiCard("Descuento aplicado", "${htmlMoney(catalog.discount)} (${htmlPct(catalog.discountRate)})"))
        }

    val dailyRows =
        overview.dailyRevenue.map { listOf(reportHtmlDate(it.date), htmlMoney(it.revenue)) }
    val methodBars =
        overview.revenueByMethod
            .mapIndexed { i, r ->
                htmlBarRow("${r.method} · ${r.count}", htmlMoney(r.revenue), (r.revenue / maxMethod).toFloat(), htmlPalette[i % htmlPalette.size])
            }
            .joinToString("")
    val doctorBars =
        overview.topDoctors
            .mapIndexed { i, r ->
                htmlBarRow("${r.doctorName} · ${r.count}", htmlMoney(r.revenue), (r.revenue / maxDoctor).toFloat(), htmlPalette[i % htmlPalette.size])
            }
            .joinToString("")
    val categoryBars =
        overview.revenueByCategory
            .mapIndexed { i, r ->
                htmlBarRow("${r.category} · ${r.count}", htmlMoney(r.revenue), (r.revenue / maxCategory).toFloat(), htmlPalette[i % htmlPalette.size])
            }
            .joinToString("")
    val statusBars =
        overview.appointmentByStatus
            .map { s ->
                htmlBarRow(s.status.displayLabel, s.count.toString(), s.count.toFloat() / maxStatus, htmlStatusColor(s.status))
            }
            .joinToString("")
    val monthBars =
        overview.patientsPerMonth
            .map { p ->
                htmlBarRow(p.month, p.count.toString(), p.count.toFloat() / maxMonth, htmlPalette[0])
            }
            .joinToString("")
    val procedureRows =
        overview.topProcedures.map { listOf(it.name, it.count.toString(), htmlMoney(it.revenue)) }
    val catalogRows =
        listOf(
            listOf("Valor de catálogo", catalog.actionCount.toString(), htmlMoney(catalog.totalCatalog)),
            listOf("Total cobrado", "", htmlMoney(catalog.totalCharged)),
            listOf("Descuento aplicado", "", "${htmlMoney(catalog.discount)} (${htmlPct(catalog.discountRate)})"),
        )
    val stockRows =
        stock.byType
            .map { listOf(it.label, it.count.toString(), it.unitsIn.toString(), it.unitsOut.toString(), htmlMoney(it.valueIn), htmlMoney(it.valueOut)) }
    val stockBars =
        stock.byType
            .mapIndexed { i, r ->
                htmlBarRow("${r.label} · +${r.unitsIn}/-${r.unitsOut}", "${r.count} mov.", r.count.toFloat() / maxStockType, htmlPalette[i % htmlPalette.size])
            }
            .joinToString("")

    return """
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Reporte — $clinicName</title>
<style>
  body { font-family: 'Segoe UI', Roboto, Arial, sans-serif; margin: 0; padding: 24px; color: #1f2937; background: #f5f7fa; }
  .sheet { max-width: 860px; margin: 0 auto; background: #ffffff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 32px; }
  .clinic { border-bottom: 3px solid #0d9488; padding-bottom: 12px; margin-bottom: 20px; }
  .clinic h1 { margin: 0; font-size: 20px; color: #0d9488; }
  .clinic p { margin: 2px 0 0; color: #6b7280; font-size: 13px; }
  h1.title { font-size: 22px; margin: 0 0 4px; }
  .sub { color: #6b7280; font-size: 13px; margin-bottom: 20px; }
  .kpis { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin-top: 8px; }
  .kpi { background: #f8fafc; border: 1px solid #e5e7eb; border-radius: 8px; padding: 10px 12px; }
  .kpi-label { display: block; font-size: 11px; text-transform: uppercase; letter-spacing: .4px; color: #6b7280; }
  .kpi-value { display: block; font-size: 15px; font-weight: 700; color: #0f766e; margin-top: 2px; }
  section { margin-top: 24px; }
  h2 { font-size: 15px; text-transform: uppercase; letter-spacing: .5px; color: #0d9488; border-bottom: 1px solid #e5e7eb; padding-bottom: 6px; }
  table { width: 100%; border-collapse: collapse; font-size: 13px; margin-top: 8px; }
  th, td { text-align: left; padding: 6px 8px; border-bottom: 1px solid #f1f5f9; }
  th { background: #f8fafc; font-weight: 600; }
  .row { display: flex; justify-content: space-between; font-size: 13px; margin-top: 10px; }
  .row-label { color: #374151; }
  .row-value { font-weight: 600; color: #0f766e; }
  .track { height: 8px; border-radius: 4px; background: #eef2f7; margin-top: 3px; overflow: hidden; }
  .fill { height: 8px; border-radius: 4px; }
  .muted { color: #9ca3af; font-size: 13px; }
  footer { margin-top: 28px; padding-top: 12px; border-top: 1px solid #e5e7eb; color: #9ca3af; font-size: 12px; text-align: center; }
  @media print { body { background: #fff; padding: 0; } .sheet { border: none; } .kpis { grid-template-columns: repeat(4, 1fr); } }
</style>
</head>
<body>
<div class="sheet">
  <div class="clinic"><h1>${htmlEsc(clinicName)}</h1><p>${htmlEsc(clinicCity)}, ${htmlEsc(clinicCountry)}</p></div>
  <h1 class="title">Reporte Denti-Code</h1>
  <p class="sub">Periodo: ${reportHtmlDate(overview.startDate)} al ${reportHtmlDate(overview.endDate)} · Generado el ${java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale("es", "ES")))}</p>
  ${htmlSection("Resumen", "<div class=\"kpis\">\n$kpis</div>")}
  ${htmlSection("Ingresos por día", htmlTable(listOf("Fecha", "Ingreso"), dailyRows))}
  ${htmlSection("Ingresos por método", methodBars)}
  ${htmlSection("Ingresos por doctor", doctorBars)}
  ${htmlSection("Ingresos por categoría", categoryBars)}
  ${htmlSection("Citas por estado", statusBars)}
  ${htmlSection("Pacientes nuevos por mes", monthBars)}
  ${htmlSection("Procedimientos top", htmlTable(listOf("Procedimiento", "Cantidad", "Ingreso"), procedureRows))}
  ${htmlSection("Ingresos vs catálogo", htmlTable(listOf("Concepto", "Acciones", "Monto"), catalogRows))}
  ${htmlSection("Movimientos de stock", "<p class=\"muted\">${stock.movementCount} movimientos · ${stock.unitsIn} unidades entradas · ${stock.unitsOut} salidas</p>\n" + stockBars)}
  ${htmlSection("Movimientos de stock (detalle)", htmlTable(listOf("Tipo", "Mov.", "Entradas", "Salidas", "Valor entradas", "Valor salidas"), stockRows))}
  <footer>Generado por Denti-Code KT · ${htmlEsc(clinicName)}</footer>
</div>
</body>
</html>
""".trimIndent()
}
