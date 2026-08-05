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
 * resumen, ingreso por día/método/doctor, citas por estado, pacientes nuevos por mes
 * y procedimientos top.
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
        appendLine("Citas sin asistir;${overview.noShowCount}")
        appendLine("Tasa de inasistencia;${reportNoShowRate(overview)}")
        appendLine("Nuevos pacientes;${overview.newPatientsCount}")
        appendLine("Valor de catálogo;${reportMoney(overview.revenueVsCatalog.totalCatalog)}")
        appendLine("Total cobrado;${reportMoney(overview.revenueVsCatalog.totalCharged)}")
        appendLine("Acciones realizadas;${overview.revenueVsCatalog.actionCount}")
        appendLine("Descuento aplicado;${reportMoney(overview.revenueVsCatalog.discount)}")
        appendLine("Descuento medio;${reportCatalogDiscountRate(overview)}")
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

        appendLine("INGRESO POR DOCTOR")
        appendLine("Doctor;Cantidad;Ingreso")
        overview.topDoctors.forEach { row ->
            appendLine("${reportCsvField(row.doctorName)};${row.count};${reportMoney(row.revenue)}")
        }
        appendLine()

        appendLine("CITAS POR ESTADO")
        appendLine("Estado;Cantidad")
        overview.appointmentByStatus.forEach { row ->
            appendLine("${reportCsvField(row.status.displayLabel)};${row.count}")
        }
        appendLine()

        appendLine("PACIENTES NUEVOS POR MES")
        appendLine("Mes;Cantidad")
        overview.patientsPerMonth.forEach { row ->
            appendLine("${row.month};${row.count}")
        }
        appendLine()

        appendLine("PROCEDIMIENTOS TOP")
        appendLine("Procedimiento;Cantidad;Ingreso")
        overview.topProcedures.forEach { row ->
            appendLine("${reportCsvField(row.name)};${row.count};${reportMoney(row.revenue)}")
        }
        appendLine()

        appendLine("INGRESO POR CATEGORÍA")
        appendLine("Categoría;Cantidad;Ingreso")
        overview.revenueByCategory.forEach { row ->
            appendLine("${reportCsvField(row.category)};${row.count};${reportMoney(row.revenue)}")
        }
        appendLine()

        appendLine("MOVIMIENTOS DE STOCK")
        appendLine(
            "Concepto;Cantidad;Entradas;Salidas;Valor entradas;Valor salidas",
        )
        appendLine(
            "Total;${overview.stockMovements.movementCount};${overview.stockMovements.unitsIn};" +
                "${overview.stockMovements.unitsOut};${reportMoney(overview.stockMovements.valueIn)};" +
                "${reportMoney(overview.stockMovements.valueOut)}",
        )
        overview.stockMovements.byType.forEach { row ->
            appendLine(
                "${reportCsvField(row.label)};${row.count};${row.unitsIn};${row.unitsOut};" +
                    "${reportMoney(row.valueIn)};${reportMoney(row.valueOut)}",
            )
        }
    }

private fun reportNoShowRate(overview: ReportsOverview): String {
    val rate =
        if (overview.appointmentCount > 0) overview.noShowCount.toDouble() / overview.appointmentCount * 100.0 else 0.0
    return String.format(Locale.US, "%.1f%%", rate)
}

private fun reportCatalogDiscountRate(overview: ReportsOverview): String =
    String.format(Locale.US, "%.1f%%", overview.revenueVsCatalog.discountRate)
