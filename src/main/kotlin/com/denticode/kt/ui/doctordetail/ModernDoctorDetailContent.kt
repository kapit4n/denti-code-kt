package com.denticode.kt.ui.doctordetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.DoctorListStatus
import com.denticode.kt.ui.appointments.PatientAvatar
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.doctors.DoctorStatusChip
import com.denticode.kt.ui.patientdetail.PatientDetailMetricCard
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppElevations
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

@Composable
fun ModernDoctorDetailContent(
    uiState: DoctorDetailUiState,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onViewSchedule: () -> Unit,
    onToggleActive: () -> Unit,
    onArchive: () -> Unit = {},
    onDelete: () -> Unit = {},
    isArchived: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val doctor = uiState.doctor
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(PatientsPremiumPalette.background)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.medium,
            color = PatientsPremiumPalette.card,
            shadowElevation = AppElevations.cardRest,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Text("Perfil del doctor", style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
                    AppOutlinedButton(text = "Cerrar", onClick = onClose, minHeight = 36.dp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md), verticalAlignment = Alignment.CenterVertically) {
                    PatientAvatar(doctor.fullName, size = 72.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            doctor.fullName,
                            style = AppTypography.PageTitle,
                            fontWeight = FontWeight.Bold,
                            color = PatientsPremiumPalette.textPrimary,
                        )
                        Text(
                            doctor.specialization?.trim()?.takeIf { it.isNotEmpty() } ?: "General",
                            style = AppTypography.Body,
                            color = PatientsPremiumPalette.textSecondary,
                        )
                        DoctorStatusChip(status = uiState.status)
                    }
                }
                DoctorInfoRow(Icons.Default.Email, doctor.email)
                doctor.contactPhone?.let { DoctorInfoRow(Icons.Default.Phone, it) }
                doctor.licenseNumber?.let { DoctorInfoRow(Icons.Outlined.Badge, "Colegiado: $it") }
                doctor.officeRoom
                    ?.takeIf { !it.equals("VACATION", ignoreCase = true) }
                    ?.let { DoctorInfoRow(Icons.Default.MedicalServices, "Consultorio: $it") }
                doctor.address?.let { DoctorInfoRow(Icons.Default.LocationOn, it) }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            PatientDetailMetricCard(
                title = "Citas hoy",
                value = uiState.todayAppointmentsCount.toString(),
                subtitle = "Agenda",
                icon = Icons.Default.Event,
                iconBackground = PatientsPremiumPalette.primary.copy(alpha = 0.12f),
                iconTint = PatientsPremiumPalette.primary,
                modifier = Modifier.weight(1f),
            )
            PatientDetailMetricCard(
                title = "Total citas",
                value = uiState.totalAppointmentsCount.toString(),
                subtitle = "Activas",
                icon = Icons.Default.CalendarMonth,
                iconBackground = PatientsPremiumPalette.info.copy(alpha = 0.12f),
                iconTint = PatientsPremiumPalette.info,
                modifier = Modifier.weight(1f),
            )
            PatientDetailMetricCard(
                title = "Experiencia",
                value = "${uiState.yearsExperience}a",
                subtitle = "Estimada",
                icon = Icons.Default.MedicalServices,
                iconBackground = PatientsPremiumPalette.success.copy(alpha = 0.12f),
                iconTint = PatientsPremiumPalette.success,
                modifier = Modifier.weight(1f),
            )
        }

        if (doctor.workingDays != null || doctor.workingHours != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.medium,
                color = PatientsPremiumPalette.card,
                shadowElevation = AppElevations.low,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Text("Horario de atención", style = AppTypography.CardTitle, fontWeight = FontWeight.SemiBold)
                    doctor.workingDays?.let { DoctorInfoRow(Icons.Default.Event, "Días: $it") }
                    doctor.workingHours?.let { DoctorInfoRow(Icons.Default.Schedule, "Horario: $it") }
                }
            }
        }

        doctor.notes?.let { notes ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.medium,
                color = PatientsPremiumPalette.card,
                shadowElevation = AppElevations.low,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Text("Notas", style = AppTypography.CardTitle, fontWeight = FontWeight.SemiBold)
                    Text(notes, style = AppTypography.Body, color = PatientsPremiumPalette.textSecondary)
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.medium,
            color = PatientsPremiumPalette.card,
            shadowElevation = AppElevations.low,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Text("Acciones", style = AppTypography.CardTitle, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    AppOutlinedButton(
                        text = "Editar",
                        onClick = onEdit,
                        minHeight = 40.dp,
                        leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Edit, null) },
                    )
                    AppButton(
                        text = "Ver agenda",
                        onClick = onViewSchedule,
                        minHeight = 40.dp,
                        leadingIcon = { androidx.compose.material3.Icon(Icons.Default.CalendarMonth, null) },
                    )
                    AppOutlinedButton(
                        text = if (doctor.isActive) "Desactivar" else "Reactivar",
                        onClick = onToggleActive,
                        minHeight = 40.dp,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    AppOutlinedButton(
                        text = if (isArchived) "Restaurar" else "Archivar",
                        onClick = onArchive,
                        minHeight = 40.dp,
                    )
                    AppOutlinedButton(
                        text = "Eliminar",
                        onClick = onDelete,
                        minHeight = 40.dp,
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.medium,
            color = PatientsPremiumPalette.card,
            shadowElevation = AppElevations.low,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Text(
                    "Próximas citas (${uiState.upcomingAppointments.size})",
                    style = AppTypography.CardTitle,
                    fontWeight = FontWeight.SemiBold,
                )
                if (uiState.upcomingAppointments.isEmpty()) {
                    Text(
                        "No hay citas próximas para este doctor.",
                        style = AppTypography.Body,
                        color = PatientsPremiumPalette.textSecondary,
                        modifier = Modifier.padding(vertical = AppSpacing.md),
                    )
                } else {
                    uiState.upcomingAppointments.forEachIndexed { index, appt ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(appt.patientName, style = AppTypography.Body, fontWeight = FontWeight.Medium)
                                Text(appt.treatmentName, style = AppTypography.BodySmall, color = PatientsPremiumPalette.textSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(appt.scheduledLabel, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
                                Text(appt.status.displayLabel, style = AppTypography.Caption, color = PatientsPremiumPalette.primary)
                            }
                        }
                        if (index < uiState.upcomingAppointments.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DoctorInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.material3.Icon(icon, null, tint = PatientsPremiumPalette.textSecondary, modifier = Modifier.padding(end = 2.dp))
        Text(text, style = AppTypography.Body, color = PatientsPremiumPalette.textPrimary)
    }
}
