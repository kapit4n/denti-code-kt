package com.denticode.kt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.denticode.kt.data.AppointmentRow
import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.Patient
import com.denticode.kt.data.PatientLedgerPayment
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.cards.AppCard
import com.denticode.kt.ui.components.feedback.StatusChip
import com.denticode.kt.ui.models.StatusKind
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PatientDetailWindow(
    repo: DentiRepository,
    patient: Patient,
    onClose: () -> Unit,
) {
    var appointments by remember { mutableStateOf<List<AppointmentRow>>(emptyList()) }
    var payments by remember { mutableStateOf<List<PatientLedgerPayment>>(emptyList()) }

    LaunchedEffect(patient.id) {
        val appts =
            withContext(Dispatchers.IO) {
                repo.listAppointmentsForPatient(patient.id)
            }
        val pays =
            withContext(Dispatchers.IO) {
                repo.listPaymentsForPatient(patient.id)
            }
        appointments = appts
        payments = pays
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        Text(
                            text = patient.fullName,
                            style = AppTypography.PageTitle,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Citas y pagos vinculados a este paciente.",
                            style = AppTypography.Body,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    AppOutlinedButton(text = "Cerrar", onClick = onClose)
                }
            }
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        Text(
                            "Nac. ${patient.dateOfBirth} · ${patient.contactPhone}",
                            style = AppTypography.Body,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        patient.email?.let {
                            Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        patient.medicalHistorySummary?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                "Resumen: $it",
                                style = AppTypography.BodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    text = "Citas (${appointments.size})",
                    style = AppTypography.SectionTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (appointments.isEmpty()) {
                item {
                    Text(
                        "No hay citas registradas para este paciente.",
                        style = AppTypography.Body,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(appointments, key = { "appointment-${it.id}" }) { a ->
                    PatientVisitCard(a)
                }
            }
            item {
                Text(
                    text = "Pagos (${payments.size})",
                    style = AppTypography.SectionTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = AppSpacing.sm),
                )
            }
            if (payments.isEmpty()) {
                item {
                    Text(
                        "No hay pagos registrados para este paciente.",
                        style = AppTypography.Body,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(payments, key = { "payment-${it.id}" }) { pay ->
                    PatientPaymentCard(pay)
                }
            }
        }
    }
}

@Composable
private fun PatientVisitCard(a: AppointmentRow) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(
                a.scheduledAt,
                style = AppTypography.CardTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                a.doctorName,
                style = AppTypography.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            a.purpose?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            a.notes?.takeIf { it.isNotBlank() }?.let {
                Text(
                    "Notas: $it",
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            val kind =
                when (a.status) {
                    AppointmentStatus.COMPLETED -> StatusKind.Success
                    AppointmentStatus.CANCELLED,
                    AppointmentStatus.NO_SHOW,
                    -> StatusKind.Error
                    AppointmentStatus.IN_PROGRESS -> StatusKind.Info
                    else -> StatusKind.Neutral
                }
            StatusChip(text = a.status.displayLabel, kind = kind)
        }
    }
}

@Composable
private fun PatientPaymentCard(p: PatientLedgerPayment) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(
                formatMoney(p.amount),
                style = AppTypography.MetricMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "${p.paidAt} · ${p.method?.displayLabel ?: "—"}",
                style = AppTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            p.note?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
