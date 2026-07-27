@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.denticode.kt.ui.patientdetail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import com.denticode.kt.ui.utils.AppAnimations
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.ui.appointments.PatientAvatar
import com.denticode.kt.ui.appointments.TimelineDot
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.data.PatientTreatmentRow
import com.denticode.kt.ui.treatments.TreatmentsTable
import com.denticode.kt.ui.formatMoney
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppElevations
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

@Composable
fun DetailStatusBadge(
    label: String,
    color: Color,
    showDot: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.14f),
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (showDot) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(color))
            }
            Text(label, style = AppTypography.Caption, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

@Composable
fun PatientInfoRow(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(icon, null, Modifier.size(18.dp), tint = PatientsPremiumPalette.textSecondary)
        Text(text, style = AppTypography.BodySmall, color = PatientsPremiumPalette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun DoctorAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
) {
    PatientAvatar(name, modifier = modifier, size = size)
}

@Composable
fun PatientDetailMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val scale by animateFloatAsState(if (hovered) 1.02f else 1f, AppAnimations.smoothTween(durationMillis = AppAnimations.FocusDurationMs), label = "dm")
    val elevation by animateDpAsState(if (hovered) AppElevations.cardHovered else AppElevations.low, AppAnimations.smoothTween(durationMillis = AppAnimations.FocusDurationMs), label = "dmE")
    Surface(
        modifier = modifier.scale(scale).hoverable(interaction),
        shape = AppShapes.medium,
        color = PatientsPremiumPalette.card,
        shadowElevation = elevation,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Text(title, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
            Text(value, style = AppTypography.MetricMedium, fontWeight = FontWeight.Bold, color = PatientsPremiumPalette.textPrimary, maxLines = 1)
            Text(subtitle, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary, maxLines = 1)
        }
    }
}

@Composable
fun PatientHeaderCard(
    patient: PatientDetailUiModel,
    kpis: PatientDetailKpis,
    onViewFullProfile: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = PatientsPremiumPalette.card,
        shadowElevation = AppElevations.cardRest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text("Ficha del paciente", style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
                AppOutlinedButton(text = "Cerrar", onClick = onClose, minHeight = 36.dp)
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md), verticalAlignment = Alignment.CenterVertically) {
                        PatientAvatar(patient.fullName, size = 72.dp)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                patient.fullName,
                                style = AppTypography.PageTitle,
                                fontWeight = FontWeight.Bold,
                                color = PatientsPremiumPalette.textPrimary,
                            )
                            DetailStatusBadge(
                                label = if (patient.isActive) "Activo" else "Inactivo",
                                color = if (patient.isActive) PatientsPremiumPalette.success else PatientsPremiumPalette.textSecondary,
                            )
                        }
                    }
                    val ageSuffix = patient.ageYears?.let { " · $it años" } ?: ""
                    PatientInfoRow(Icons.Default.Event, "${patient.birthDateLabel}$ageSuffix")
                    PatientInfoRow(Icons.Default.Phone, patient.phone)
                    patient.email?.let { PatientInfoRow(Icons.Default.Email, it) }
                    patient.medicalSummary?.let {
                        Text(
                            it,
                            style = AppTypography.BodySmall,
                            color = PatientsPremiumPalette.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    AppOutlinedButton(
                        text = "Ver perfil completo",
                        onClick = onViewFullProfile,
                        minHeight = 40.dp,
                        leadingIcon = {
                            Icon(Icons.Default.Person, null, Modifier.size(18.dp))
                        },
                    )
                }
                Row(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    PatientDetailMetricCard(
                        title = "Citas totales",
                        value = kpis.totalAppointments.toString(),
                        subtitle = "Historial",
                        icon = Icons.Default.CalendarMonth,
                        iconBackground = PatientsPremiumPalette.primary.copy(alpha = 0.12f),
                        iconTint = PatientsPremiumPalette.primary,
                        modifier = Modifier.weight(1f),
                    )
                    PatientDetailMetricCard(
                        title = "Completadas",
                        value = kpis.completedAppointments.toString(),
                        subtitle = "Finalizadas",
                        icon = Icons.Default.CheckCircle,
                        iconBackground = PatientsPremiumPalette.success.copy(alpha = 0.12f),
                        iconTint = PatientsPremiumPalette.success,
                        modifier = Modifier.weight(1f),
                    )
                    PatientDetailMetricCard(
                        title = "Próxima cita",
                        value = if (kpis.nextAppointmentLabel == "Sin programar") "—" else kpis.nextAppointmentLabel.substringBefore(" ·"),
                        subtitle = kpis.nextAppointmentLabel,
                        icon = Icons.Default.Schedule,
                        iconBackground = PatientsPremiumPalette.warning.copy(alpha = 0.14f),
                        iconTint = PatientsPremiumPalette.warning,
                        modifier = Modifier.weight(1f),
                    )
                    PatientDetailMetricCard(
                        title = "Saldo pendiente",
                        value = formatMoney(kpis.pendingBalance),
                        subtitle = "Por cobrar",
                        icon = Icons.Default.Payments,
                        iconBackground = Color(0xFF6366F1).copy(alpha = 0.12f),
                        iconTint = Color(0xFF6366F1),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = AppTypography.SectionTitle, fontWeight = FontWeight.SemiBold, color = PatientsPremiumPalette.textPrimary)
        trailing?.invoke()
    }
}

@Composable
fun DetailActionMenuButton(
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, "Menú", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Ver detalle") },
                onClick = {
                    expanded = false
                    onPrimary()
                },
            )
        }
    }
}

@Composable
fun AppointmentItemCard(
    appointment: PatientDetailAppointmentUi,
    modifier: Modifier = Modifier,
) {
    val interaction = remember(appointment.id) { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg by animateColorAsState(
        if (hovered) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else Color.Transparent,
        AppAnimations.smoothTween(durationMillis = AppAnimations.FocusDurationMs),
        label = "apptBg",
    )
    val elevation by animateDpAsState(if (hovered) AppElevations.cardRest else AppElevations.none, AppAnimations.smoothTween(durationMillis = AppAnimations.FocusDurationMs), label = "apptEl")
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .hoverable(interaction),
        shape = AppShapes.small,
        color = bg,
        shadowElevation = elevation,
        tonalElevation = 0.dp,
    ) {
        Row(
            Modifier.padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Column(Modifier.width(72.dp), horizontalAlignment = Alignment.Start) {
                Text(appointment.dateLabel, style = AppTypography.BodySmall, fontWeight = FontWeight.SemiBold, color = PatientsPremiumPalette.textPrimary)
                Text(appointment.timeLabel, style = AppTypography.CardTitle, fontWeight = FontWeight.Bold, color = PatientsPremiumPalette.textPrimary)
                Text(appointment.durationLabel, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
                Spacer(Modifier.height(6.dp))
                TimelineDot(appointment.statusColor)
            }
            DoctorAvatar(appointment.doctorName)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(appointment.doctorName, style = AppTypography.Body, fontWeight = FontWeight.SemiBold, color = PatientsPremiumPalette.textPrimary, maxLines = 1)
                Text(appointment.treatmentName, style = AppTypography.BodySmall, color = PatientsPremiumPalette.textSecondary, maxLines = 1)
                appointment.notes?.let {
                    Text(it, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary, maxLines = 1)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                DetailStatusBadge(appointment.statusLabel, appointment.statusColor)
                DetailActionMenuButton(onPrimary = {})
            }
        }
    }
}

@Composable
fun PaymentItemCard(
    payment: PatientDetailPaymentUi,
    modifier: Modifier = Modifier,
) {
    val interaction = remember(payment.id) { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg by animateColorAsState(
        if (hovered) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else Color.Transparent,
        AppAnimations.smoothTween(durationMillis = AppAnimations.FocusDurationMs),
        label = "payBg",
    )
    val (icon, iconBg, iconTint) =
        when (payment.status) {
            PatientPaymentDisplayStatus.PAID ->
                Triple(Icons.Default.CheckCircle, PatientsPremiumPalette.success.copy(alpha = 0.14f), PatientsPremiumPalette.success)
            PatientPaymentDisplayStatus.PENDING ->
                Triple(Icons.Default.Schedule, PatientsPremiumPalette.warning.copy(alpha = 0.14f), PatientsPremiumPalette.warning)
            PatientPaymentDisplayStatus.OVERDUE ->
                Triple(Icons.Default.Warning, PatientsPremiumPalette.error.copy(alpha = 0.14f), PatientsPremiumPalette.error)
        }
    val statusColor =
        when (payment.status) {
            PatientPaymentDisplayStatus.PAID -> PatientsPremiumPalette.success
            PatientPaymentDisplayStatus.PENDING -> PatientsPremiumPalette.warning
            PatientPaymentDisplayStatus.OVERDUE -> PatientsPremiumPalette.error
        }
    Surface(
        modifier = modifier.fillMaxWidth().hoverable(interaction),
        shape = AppShapes.small,
        color = bg,
    ) {
        Row(
            Modifier.padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(AppShapes.small)
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(payment.amountLabel, style = AppTypography.MetricMedium, fontWeight = FontWeight.Bold, color = PatientsPremiumPalette.textPrimary)
                Text(payment.dateLabel, style = AppTypography.BodySmall, color = PatientsPremiumPalette.textSecondary)
                Text(payment.methodLabel, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
                payment.treatmentLabel?.let {
                    Text(it, style = AppTypography.Caption, color = PatientsPremiumPalette.primary, maxLines = 1)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                DetailStatusBadge(payment.status.labelEs, statusColor)
                DetailActionMenuButton(onPrimary = {})
            }
        }
    }
}

@Composable
fun PaymentSummaryCard(
    summary: PaymentSummaryUiModel,
    onViewHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = PatientsPremiumPalette.background,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text("Resumen de pagos", style = AppTypography.CardTitle, fontWeight = FontWeight.SemiBold, color = PatientsPremiumPalette.textPrimary)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
                SummaryMetric("Total pagado", formatMoney(summary.totalPaid), PatientsPremiumPalette.success, Modifier.weight(1f))
                SummaryMetric("Pendiente", formatMoney(summary.pending), PatientsPremiumPalette.warning, Modifier.weight(1f))
                SummaryMetric("Total general", formatMoney(summary.total), PatientsPremiumPalette.textPrimary, Modifier.weight(1f))
            }
            AppOutlinedButton(
                text = "Ver historial completo",
                onClick = onViewHistory,
                trailingIcon = {
                    Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp), tint = PatientsPremiumPalette.primary)
                },
            )
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
        Text(value, style = AppTypography.MetricLarge, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun AppointmentsPanel(
    appointments: List<PatientDetailAppointmentUi>,
    totalCount: Int,
    statusFilterLabel: String,
    statusOptions: List<String>,
    onStatusSelect: (String) -> Unit,
    onRegisterAppointment: () -> Unit,
    onViewAll: () -> Unit,
    registerEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var statusMenu by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = PatientsPremiumPalette.card,
        shadowElevation = AppElevations.cardRest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Citas ($totalCount)", style = AppTypography.SectionTitle, fontWeight = FontWeight.SemiBold, color = PatientsPremiumPalette.textPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        AppOutlinedButton(
                            text = statusFilterLabel,
                            onClick = { statusMenu = true },
                            minHeight = 36.dp,
                        )
                        DropdownMenu(expanded = statusMenu, onDismissRequest = { statusMenu = false }) {
                            statusOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        onStatusSelect(opt)
                                        statusMenu = false
                                    },
                                )
                            }
                        }
                    }
                    AppButton(
                        text = "Registrar cita",
                        onClick = onRegisterAppointment,
                        enabled = registerEnabled,
                        minHeight = 40.dp,
                        leadingIcon = {
                            Icon(Icons.Default.Event, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        },
                    )
                }
            }
            if (appointments.isEmpty()) {
                Text(
                    "No hay citas con el filtro seleccionado.",
                    style = AppTypography.Body,
                    color = PatientsPremiumPalette.textSecondary,
                    modifier = Modifier.padding(vertical = AppSpacing.lg),
                )
            } else {
                appointments.forEachIndexed { index, appt ->
                    AppointmentItemCard(appt)
                    if (index < appointments.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
            AppOutlinedButton(
                text = "Ver todas las citas",
                onClick = onViewAll,
                trailingIcon = {
                    Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp), tint = PatientsPremiumPalette.primary)
                },
            )
        }
    }
}

@Composable
fun TreatmentsPanel(
    treatments: List<PatientTreatmentRow>,
    onRegisterTreatment: () -> Unit,
    registerTreatmentEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = PatientsPremiumPalette.card,
        shadowElevation = AppElevations.cardRest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Tratamientos (${treatments.size})",
                    style = AppTypography.SectionTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = PatientsPremiumPalette.textPrimary,
                )
                AppButton(
                    text = "Registrar tratamiento",
                    onClick = onRegisterTreatment,
                    enabled = registerTreatmentEnabled,
                    minHeight = 40.dp,
                    leadingIcon = {
                        Icon(Icons.Outlined.MedicalServices, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    },
                )
            }
            TreatmentsTable(
                treatments = treatments,
                showPatientColumn = false,
                emptyMessage = "No hay tratamientos vinculados a este paciente.",
                embeddedInScroll = true,
            )
        }
    }
}

@Composable
fun PaymentsPanel(
    payments: List<PatientDetailPaymentUi>,
    summary: PaymentSummaryUiModel,
    onRegisterPayment: () -> Unit,
    onViewHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.medium,
            color = PatientsPremiumPalette.card,
            shadowElevation = AppElevations.cardRest,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Pagos (${payments.size})", style = AppTypography.SectionTitle, fontWeight = FontWeight.SemiBold, color = PatientsPremiumPalette.textPrimary)
                    AppButton(
                        text = "Registrar pago",
                        onClick = onRegisterPayment,
                        minHeight = 40.dp,
                        leadingIcon = {
                            Icon(Icons.Default.Payments, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        },
                    )
                }
                if (payments.isEmpty()) {
                    Text(
                        "No hay pagos registrados.",
                        style = AppTypography.Body,
                        color = PatientsPremiumPalette.textSecondary,
                        modifier = Modifier.padding(vertical = AppSpacing.md),
                    )
                } else {
                    payments.forEachIndexed { index, pay ->
                        PaymentItemCard(pay)
                        if (index < payments.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
        PaymentSummaryCard(summary = summary, onViewHistory = onViewHistory)
    }
}

@Composable
fun QuickActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg by animateColorAsState(
        if (hovered) PatientsPremiumPalette.primary.copy(alpha = 0.06f) else Color.Transparent,
        AppAnimations.smoothTween(durationMillis = AppAnimations.FocusDurationMs),
        label = "qa",
    )
    Surface(
        modifier =
            modifier
                .hoverable(interaction)
                .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        shape = AppShapes.small,
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, null, Modifier.size(18.dp), tint = PatientsPremiumPalette.primary)
            Text(text, style = AppTypography.BodySmall, fontWeight = FontWeight.Medium, color = PatientsPremiumPalette.textPrimary)
        }
    }
}

@Composable
fun QuickActionsFooter(
    onEditPatient: () -> Unit,
    onNewAppointment: () -> Unit,
    onRegisterTreatment: () -> Unit,
    onRegisterPayment: () -> Unit,
    onClinicalHistory: () -> Unit,
    onSendReminder: () -> Unit,
    onMoreActions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var moreMenu by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = PatientsPremiumPalette.card,
        shadowElevation = AppElevations.low,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text("Acciones rápidas", style = AppTypography.CardTitle, fontWeight = FontWeight.SemiBold, color = PatientsPremiumPalette.textPrimary)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                QuickActionButton("Editar paciente", Icons.Default.Edit, onEditPatient)
                QuickActionButton("Nueva cita", Icons.Default.Event, onNewAppointment)
                QuickActionButton("Registrar tratamiento", Icons.Outlined.MedicalServices, onRegisterTreatment)
                QuickActionButton("Registrar pago", Icons.Default.Payments, onRegisterPayment)
                QuickActionButton("Historial clínico", Icons.Default.History, onClinicalHistory)
                QuickActionButton("Enviar recordatorio", Icons.Outlined.Chat, onSendReminder)
                Box {
                    QuickActionButton("Más acciones", Icons.Default.MoreVert, onClick = { moreMenu = true })
                    DropdownMenu(expanded = moreMenu, onDismissRequest = { moreMenu = false }) {
                        DropdownMenuItem(text = { Text("Exportar ficha") }, onClick = { moreMenu = false; onMoreActions() })
                        DropdownMenuItem(text = { Text("Imprimir resumen") }, onClick = { moreMenu = false })
                    }
                }
            }
        }
    }
}
