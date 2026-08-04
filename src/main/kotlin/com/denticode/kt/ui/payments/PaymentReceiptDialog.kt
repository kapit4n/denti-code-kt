package com.denticode.kt.ui.payments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.denticode.kt.export.ExportService
import com.denticode.kt.export.ReceiptData
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

@Composable
fun PaymentReceiptDialog(
    receipt: ReceiptData,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (ReceiptData) -> Unit,
) {
    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 460.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    ExportService.clinicName,
                    style = AppTypography.SectionTitle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "${ExportService.clinicCity}, ${ExportService.clinicCountry}",
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                "RECIBO DE PAGO",
                style = AppTypography.CardTitle,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            ReceiptLine("Recibo Nro", receipt.receiptNumber)
            ReceiptLine("Fecha", receipt.dateTimeLabel)
            ReceiptLine("Paciente", receipt.patientName)
            ReceiptLine("ID", receipt.patientId.toString())
            ReceiptLine("Detalle", receipt.detailLabel)
            ReceiptLine("Método", receipt.methodLabel)
            ReceiptLine("Monto", "${ExportService.currencySymbol} ${"%.2f".format(receipt.amount)}", bold = true)
            ReceiptLine("Estado", receipt.statusLabel)
            receipt.note?.takeIf { it.isNotBlank() }?.let {
                ReceiptLine("Nota", it)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                "¡Gracias por su visita!",
                style = AppTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cerrar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Guardando..." else "Guardar archivo",
                    onClick = { onSave(receipt) },
                    enabled = !isSaving,
                )
            }
        }
    }
}

@Composable
private fun ReceiptLine(
    label: String,
    value: String,
    bold: Boolean = false,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            label,
            style = AppTypography.BodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.widthIn(min = 100.dp),
        )
        Text(
            value,
            style = AppTypography.BodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}
