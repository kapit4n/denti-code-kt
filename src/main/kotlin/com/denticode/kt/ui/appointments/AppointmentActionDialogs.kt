@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.appointments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.AppointmentRow
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.components.inputs.AppVisitDateTimeFields
import com.denticode.kt.ui.components.inputs.snapFormMinuteToStep5
import com.denticode.kt.ui.parseAppointmentScheduledAt
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ReschedulePreviewFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE d MMMM yyyy, HH:mm", Locale("es", "ES"))

@Composable
fun RescheduleAppointmentDialog(
    appointment: AppointmentRow,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (newDate: LocalDate, newHour: Int, newMinute: Int, note: String?) -> Unit,
) {
    val initial = remember(appointment.id) { parseAppointmentScheduledAt(appointment.scheduledAt) }
    val defaultDt = remember(appointment.id) { initial.plusDays(1).withSecond(0).withNano(0) }
    var visitDate by remember(appointment.id) { mutableStateOf(defaultDt.toLocalDate()) }
    var visitHour by remember(appointment.id) { mutableStateOf(defaultDt.hour) }
    var visitMinute by remember(appointment.id) { mutableStateOf(snapFormMinuteToStep5(defaultDt.minute)) }
    var note by remember(appointment.id) { mutableStateOf("") }

    val visitDateTime =
        remember(visitDate, visitHour, visitMinute) {
            LocalDateTime.of(visitDate, LocalTime.of(visitHour, visitMinute))
        }
    val isFuture = remember(visitDateTime) { !visitDateTime.isBefore(LocalDateTime.now().minusMinutes(1)) }

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 520.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    "Reprogramar cita",
                    style = AppTypography.SectionTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "El doctor y el tratamiento se mantienen. Elija la nueva fecha y hora.",
                    style = AppTypography.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AppVisitDateTimeFields(
                date = visitDate,
                onDateChange = { visitDate = it },
                hour = visitHour,
                minute = visitMinute,
                onTimeChange = { h, m ->
                    visitHour = h
                    visitMinute = m
                },
                enabled = !isSaving,
                dateLabel = "Nueva fecha",
                timeLabel = "Nueva hora",
            )
            Text(
                "Nueva cita: ${visitDateTime.format(ReschedulePreviewFormatter)}",
                style = AppTypography.BodySmall,
                color =
                    if (isFuture) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
            )
            AppTextArea(
                value = note,
                onValueChange = { note = it },
                label = "Nota (opcional)",
                placeholder = "Motivo del cambio de horario…",
                enabled = !isSaving,
                minLines = 2,
                maxLines = 4,
            )
            errorMessage?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Guardando..." else "Confirmar",
                    onClick = {
                        onConfirm(
                            visitDate,
                            visitHour,
                            visitMinute,
                            note.trim().takeIf { it.isNotEmpty() },
                        )
                    },
                    enabled = !isSaving && isFuture,
                )
            }
        }
    }
}

@Composable
fun CancelAppointmentConfirmDialog(
    patientName: String,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (reason: String?) -> Unit,
) {
    var reason by remember { mutableStateOf("") }

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 440.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                "Cancelar cita",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "¿Desea cancelar la cita de $patientName?",
                style = AppTypography.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppTextArea(
                value = reason,
                onValueChange = { reason = it },
                label = "Motivo (opcional)",
                placeholder = "Paciente solicitó cancelación…",
                enabled = !isSaving,
                minLines = 2,
                maxLines = 4,
            )
            errorMessage?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Volver", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Cancelando..." else "Confirmar cancelación",
                    onClick = { onConfirm(reason.trim().takeIf { it.isNotEmpty() }) },
                    enabled = !isSaving,
                )
            }
        }
    }
}
