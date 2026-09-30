@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.patientdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppElevations
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import com.denticode.kt.ui.treatments.TreatmentStatusBadge

// ── Liquidación de tratamientos ─────────────────────────────────────────────

/**
 * Muestra el cobro por tratamiento: lo facturado, lo pagado y el saldo pendiente,
 * con la opción de registrar un pago precargado con el saldo de cada tratamiento.
 */
@Composable
fun TreatmentPaymentsPanel(
    state: TreatmentPaymentUiState,
    onRegisterPayment: () -> Unit = {},
    onRegisterPaymentForTreatment: (PatientTreatmentSettlementUi) -> Unit,
    modifier: Modifier = Modifier,
    showSettled: Boolean = true,
    showHeaderButton: Boolean = true,
) {
    if (state.isEmpty) {
        return
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = PatientsPremiumPalette.card,
        shadowElevation = AppElevations.cardRest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Tratamientos y pagos",
                        style = AppTypography.SectionTitle,
                        fontWeight = FontWeight.SemiBold,
                        color = PatientsPremiumPalette.textPrimary,
                    )
                    Text(
                        "Cobro por tratamiento hasta cubrir el saldo",
                        style = AppTypography.Caption,
                        color = PatientsPremiumPalette.textSecondary,
                    )
                }
                if (showHeaderButton) {
                    AppButton(
                        text = "Registrar pago",
                        onClick = onRegisterPayment,
                        minHeight = 40.dp,
                        leadingIcon = {
                            Icon(Icons.Default.Payments, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        },
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                TreatmentMoneyChip("Facturado", state.totalBilled, PatientsPremiumPalette.primary, Modifier.weight(1f))
                TreatmentMoneyChip("Cobrado", state.totalPaid, PatientsPremiumPalette.success, Modifier.weight(1f))
                TreatmentMoneyChip("Pendiente", state.totalPending, PatientsPremiumPalette.error, Modifier.weight(1f))
            }

            if (state.pending.isNotEmpty()) {
                SettlementGroupHeader("Pendientes", state.pending.size)
                state.pending.forEachIndexed { index, settlement ->
                    SettlementPaymentRow(settlement, onRegisterPaymentForTreatment)
                    if (index < state.pending.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }

            if (showSettled && state.settled.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettlementGroupHeader("Cubiertos", state.settled.size)
                state.settled.forEachIndexed { index, settlement ->
                    SettlementPaymentRow(settlement, onRegisterPaymentForTreatment)
                    if (index < state.settled.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettlementGroupHeader(label: String, count: Int) {
    Text(
        "$label · $count",
        style = AppTypography.CardTitle,
        fontWeight = FontWeight.SemiBold,
        color = PatientsPremiumPalette.textSecondary,
    )
}

@Composable
private fun TreatmentMoneyChip(
    label: String,
    value: Double,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.small,
        color = color.copy(alpha = 0.1f),
    ) {
        Column(Modifier.padding(AppSpacing.sm), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Bs %.2f".format(value),
                style = AppTypography.MetricMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
            )
            Text(label, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
        }
    }
}

@Composable
private fun SettlementPaymentRow(
    settlement: PatientTreatmentSettlementUi,
    onRegisterPaymentForTreatment: (PatientTreatmentSettlementUi) -> Unit,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Text(
                        settlement.procedureName,
                        style = AppTypography.Body,
                        fontWeight = FontWeight.SemiBold,
                        color = PatientsPremiumPalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    TreatmentStatusBadge(settlement.status)
                }
                Text(
                    "${settlement.doctorName} · ${settlement.dateLabel}",
                    style = AppTypography.Caption,
                    color = PatientsPremiumPalette.textSecondary,
                )
                if (settlement.paymentCount > 0) {
                    Text(
                        buildString {
                            append(settlement.paymentsSummaryLabel)
                            if (settlement.sharedFromAppointment) {
                                append(" · compartido con otros tratamientos de la cita")
                            }
                        },
                        style = AppTypography.Caption,
                        color = PatientsPremiumPalette.textSecondary,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    "${settlement.paidLabel} de ${settlement.totalLabel}",
                    style = AppTypography.BodySmall,
                    color = PatientsPremiumPalette.textPrimary,
                )
                if (!settlement.isSettled) {
                    Text(
                        "Pendiente: ${settlement.pendingLabel}",
                        style = AppTypography.BodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PatientsPremiumPalette.error,
                    )
                    AppOutlinedButton(
                        text = "Registrar pago",
                        onClick = { onRegisterPaymentForTreatment(settlement) },
                        minHeight = 34.dp,
                    )
                }
            }
        }
        val progressColor =
            if (settlement.isSettled) PatientsPremiumPalette.success else PatientsPremiumPalette.warning
        LinearProgressIndicator(
            progress = { settlement.coverageRatio },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = progressColor,
            trackColor = progressColor.copy(alpha = 0.15f),
        )
    }
}