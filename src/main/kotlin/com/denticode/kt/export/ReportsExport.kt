package com.denticode.kt.export

import com.denticode.kt.data.ReportsOverview
import java.util.Locale

private fun reportCsvField(value: String): String {
    val v = value.replace("\"", "\"\"")
    return if (v.contains(';') || v.contains('\n') || v.contains('"')) "\"$v\"" else v
}

private fun reportMoney(value: Double): String = String.format(Locale.US, "%.2f", value)

/**
 * Reporte en CSV multi-sección (separador `;`, compatible con Excel en español):
 * resumen, ingreso por día, ingreso por método, citas por estado y procedimientos top.
 */
fun renderReportsCsv(overview: ReportsOverview): String =
    buildString {
        appendLine("Reporte Denti-Code")
        appendLine("Periodo;${overview.startDate};${overview.endDate}")
        appendLine()

        appendLine("RESUMEN")
        appendLine("Concepto;Valor")
        appendLine("Ingreso total;${reportMoney(overview.totalRevenue)}")
        appendLine("Pagos registrados;${overview.paymentCount}")
        appendLine("Citas en el periodo;${overview.appointmentCount}")
        appendLine("Citas completadas;${overview.completedCount}")
        appendLine("Citas canceladas;${overview.cancelledCount}")
        appendLine("Nuevos pacientes;${overview.newPatientsCount}")
        appendLine()

        appendLine("INGRESO POR DÍA")
        appendLine("Fecha;Ingreso")
        overview.dailyRevenue.forEach { row ->
            appendLine("${row.date};${reportMoney(row.revenue)}")
        }
        appendLine()

        appendLine("INGRESO POR MÉTODO")
        appendLine("Método;Cantidad;Ingreso")
        overview.revenueByMethod.forEach { row ->
            appendLine("${reportCsvField(row.method)};${row.count};${reportMoney(row.revenue)}")
        }
        appendLine()

        appendLine("CITAS POR ESTADO")
        appendLine("Estado;Cantidad")
        overview.appointmentByStatus.forEach { row ->
            appendLine("${reportCsvField(row.status.displayLabel)};${row.count}")
        }
        appendLine()

        appendLine("PROCEDIMIENTOS TOP")
        appendLine("Procedimiento;Cantidad;Ingreso")
        overview.topProcedures.forEach { row ->
            appendLine("${reportCsvField(row.name)};${row.count};${reportMoney(row.revenue)}")
        }
    }
