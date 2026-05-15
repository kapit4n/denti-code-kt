package com.denticode.kt.ui.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

@Composable
fun AppointmentDetailPanel(
    appointment: AppointmentUiModel?,
    reminderPreview: String,
    onReminderPreviewChange: (String) -> Unit,
    onReminderSend: () -> Unit,
    onClose: () -> Unit,
    onEditAppointment: () -> Unit,
    onReschedule: () -> Unit,
    onCancelAppointment: () -> Unit,
    onViewPatient: () -> Unit,
    onClinicalHistory: () -> Unit,
    onRegisterPayment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = AppShapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
        tonalElevation = 0.dp,
    ) {
        if (appointment == null) {
            Column(
                Modifier.padding(AppSpacing.xl).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "Selecciona una cita",
                    style = AppTypography.SectionTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(AppSpacing.sm))
                Text(
                    "El detalle y las acciones rápidas aparecerán aquí.",
                    style = AppTypography.BodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            val a = appointment
            Column(
                Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(AppSpacing.md),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Cita seleccionada",
                        style = AppTypography.SectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, "Cerrar panel", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(AppSpacing.sm))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(AppSpacing.md))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PatientAvatar(a.patientName, size = 52.dp)
                    Column(Modifier.weight(1f)) {
                        Text(a.patientName, style = AppTypography.SectionTitle, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Outlined.Phone, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                a.patientPhone ?: "Sin teléfono",
                                style = AppTypography.BodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(AppSpacing.md))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(AppSpacing.md))
                DetailRow(Icons.Outlined.CalendarMonth, "Fecha", a.dateLabel)
                DetailRow(Icons.Outlined.Schedule, "Hora", "${a.timeLabel} · ${a.durationMinutes} min")
                DetailRow(Icons.Outlined.Person, "Doctor", a.doctorName)
                DetailRow(Icons.Outlined.MedicalServices, "Tratamiento", a.treatmentName)
                Row(
                    Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    Icon(Icons.AutoMirrored.Outlined.EventNote, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Column(Modifier.weight(1f)) {
                        Text("Estado", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(8.dp).background(a.statusAccentColor, shape = CircleShape))
                            Text(
                                a.displayStatusLabel,
                                style = AppTypography.Body,
                                fontWeight = FontWeight.SemiBold,
                                color = a.statusAccentColor,
                            )
                        }
                    }
                }
                DetailRow(Icons.AutoMirrored.Outlined.EventNote, "Notas", a.notes?.takeIf { it.isNotBlank() } ?: "—")
                DetailRow(Icons.Outlined.CalendarMonth, "Motivo", a.purpose?.takeIf { it.isNotBlank() } ?: "—")
                DetailRow(Icons.Outlined.Schedule, "Creada", a.createdDisplay)
                Spacer(Modifier.height(AppSpacing.md))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(AppSpacing.md))
                Text("Acciones rápidas", style = AppTypography.CardTitle, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(AppSpacing.sm))
                val rows =
                    listOf(
                        listOf(
                            Triple("Editar cita", Icons.Outlined.Edit, onEditAppointment),
                            Triple("Reprogramar", Icons.Outlined.Schedule, onReschedule),
                        ),
                        listOf(
                            Triple("Cancelar cita", Icons.AutoMirrored.Outlined.EventNote, onCancelAppointment),
                            Triple("Ver paciente", Icons.Outlined.Person, onViewPatient),
                        ),
                        listOf(
                            Triple("Historial clínico", Icons.Outlined.MedicalServices, onClinicalHistory),
                            Triple("Registrar pago", Icons.Outlined.Payments, onRegisterPayment),
                        ),
                    )
                rows.forEach { pair ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        pair.forEach { (text, icon, onClick) ->
                            QuickActionButton(
                                text = text,
                                icon = icon,
                                onClick = onClick,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(AppSpacing.lg))
                ReminderCard(
                    previewText = reminderPreview,
                    onPreviewChange = onReminderPreviewChange,
                    onSend = onReminderSend,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.sm),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.weight(1f)) {
            Text(label, style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
