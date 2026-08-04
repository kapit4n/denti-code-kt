package com.denticode.kt.export

/**
 * Datos necesarios para renderizar un recibo de pago.
 */
data class ReceiptData(
    val receiptNumber: String,
    val patientId: Int,
    val patientName: String,
    val detailLabel: String,
    val methodLabel: String,
    val amount: Double,
    val dateTimeLabel: String,
    val statusLabel: String,
    val note: String? = null,
)

private fun pad(label: String, value: String, width: Int = 22): String =
    (label + ": ").padEnd(width) + value

/**
 * Recibo de pago en texto plano (ancho fijo) listo para guardar/imprimir.
 */
fun renderReceiptText(
    receipt: ReceiptData,
    clinicName: String = ExportService.clinicName,
    clinicCity: String = ExportService.clinicCity,
    clinicCountry: String = ExportService.clinicCountry,
    currencySymbol: String = ExportService.currencySymbol,
): String {
    val bar = "=".repeat(52)
    val thin = "-".repeat(52)
    return buildString {
        appendLine(bar)
        appendLine("  $clinicName".padEnd(52))
        appendLine("  $clinicCity, $clinicCountry".padEnd(52))
        appendLine(bar)
        appendLine("  RECIBO DE PAGO".padStart(40))
        appendLine(thin)
        appendLine(pad("Recibo Nro", receipt.receiptNumber))
        appendLine(pad("Fecha", receipt.dateTimeLabel))
        appendLine(thin)
        appendLine(pad("Paciente", receipt.patientName))
        appendLine(pad("ID", receipt.patientId.toString()))
        appendLine(pad("Detalle", receipt.detailLabel))
        appendLine(pad("Método", receipt.methodLabel))
        appendLine(pad("Monto", "$currencySymbol ${"%.2f".format(receipt.amount)}"))
        appendLine(pad("Estado", receipt.statusLabel))
        receipt.note?.takeIf { it.isNotBlank() }?.let {
            appendLine(pad("Nota", it))
        }
        appendLine(thin)
        appendLine("  ¡Gracias por su visita!".padStart(40))
        appendLine(bar)
        appendLine("Generado por Denti-Code KT")
    }
}
