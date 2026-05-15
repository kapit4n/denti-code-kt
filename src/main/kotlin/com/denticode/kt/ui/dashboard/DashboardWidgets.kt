package com.denticode.kt.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.navigation.ScreenRoute
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardWelcomeHeader(modifier: Modifier = Modifier) {
    val dateFmt = remember { DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale("es", "ES")) }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Icon(
                imageVector = Icons.Default.MedicalServices,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp),
            )
            Column {
                Text(
                    "Hola, equipo clínico",
                    style = AppTypography.PageTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "Resumen operativo del día · Denti-Code",
                    style = AppTypography.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            LocalDate.now().format(dateFmt),
            style = AppTypography.BodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun DashboardMetricPill(
    title: String,
    value: String,
    subtitle: String?,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val scale by animateFloatAsState(if (hovered) 1.02f else 1f, tween(180), label = "kpi")
    Surface(
        modifier =
            modifier
                .scale(scale)
                .hoverable(interaction),
        shape = AppShapes.medium,
        shadowElevation = if (hovered) 8.dp else 3.dp,
        tonalElevation = 0.dp,
        color = Color.Transparent,
    ) {
        Row(
            modifier =
                Modifier
                    .background(Brush.linearGradient(gradient))
                    .padding(AppSpacing.lg)
                    .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = AppTypography.Caption, color = Color.White.copy(alpha = 0.9f))
                Text(value, style = AppTypography.MetricLarge, color = Color.White, maxLines = 1)
                subtitle?.let {
                    Text(it, style = AppTypography.BodySmall, color = Color.White.copy(alpha = 0.85f), maxLines = 2)
                }
            }
        }
    }
}

@Composable
fun DashboardStatusBadge(status: AppointmentDisplayStatus, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = status.color.copy(alpha = 0.14f),
    ) {
        Text(
            status.labelEs,
            style = AppTypography.Caption,
            color = status.color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun TodayAppointmentsPanel(
    rows: List<TodayAppointmentUi>,
    onViewCalendar: () -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    com.denticode.kt.ui.components.cards.AppCard(modifier = modifier, showHairlineBorder = true) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Citas de hoy", style = AppTypography.SectionTitle, color = MaterialTheme.colorScheme.onSurface)
                AppOutlinedButton(text = "Ver calendario", onClick = onViewCalendar)
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Hora", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.widthIn(48.dp))
                Text("Paciente", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Text("Trat.", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.8f))
                Text("Doctor", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Text("Estado", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            rows.take(8).forEach { r ->
                val interaction = remember(r.id) { MutableInteractionSource() }
                val hovered by interaction.collectIsHoveredAsState()
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.small)
                            .hoverable(interaction)
                            .background(
                                if (hovered) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                                } else {
                                    Color.Transparent
                                },
                            )
                            .padding(vertical = AppSpacing.sm, horizontal = AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(r.timeLabel, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.widthIn(48.dp))
                    Text(
                        r.patientName,
                        style = AppTypography.Body,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        r.treatmentLabel,
                        style = AppTypography.BodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(0.8f),
                    )
                    Text(
                        r.doctorName,
                        style = AppTypography.BodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    DashboardStatusBadge(r.displayStatus)
                }
            }
            AppOutlinedButton(text = "Ver todas las citas", onClick = onViewAll, modifier = Modifier.align(Alignment.End))
        }
    }
}

@Composable
fun ActivityFeedPanel(items: List<ActivityFeedItem>, modifier: Modifier = Modifier) {
    com.denticode.kt.ui.components.cards.AppCard(modifier = modifier, showHairlineBorder = true) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text("Actividad reciente", style = AppTypography.SectionTitle, color = MaterialTheme.colorScheme.onSurface)
            items.forEach { a ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(a.accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Search, contentDescription = null, tint = a.accent, modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(a.title, style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurface)
                        Text(a.subtitle, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(a.timeLabel, style = AppTypography.Caption, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
fun AlertsPanel(alerts: List<DashboardAlertUi>, modifier: Modifier = Modifier) {
    com.denticode.kt.ui.components.cards.AppCard(modifier = modifier, showHairlineBorder = true) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text("Alertas", style = AppTypography.SectionTitle, color = MaterialTheme.colorScheme.onSurface)
            alerts.forEach { a ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.small,
                    color = a.tint.copy(alpha = 0.12f),
                ) {
                    Row(
                        Modifier.padding(AppSpacing.md),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                a.title,
                                style = AppTypography.Body,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                a.subtitle,
                                style = AppTypography.BodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsFooter(
    onNavigate: (ScreenRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("Accesos rápidos", style = AppTypography.SectionTitle, color = MaterialTheme.colorScheme.onSurface)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuickActionChip("Nueva cita", Icons.Default.CalendarMonth) { onNavigate(ScreenRoute.Appointments) }
            QuickActionChip("Nuevo paciente", Icons.Default.People) { onNavigate(ScreenRoute.Patients) }
            QuickActionChip("Buscar paciente", Icons.Outlined.Search) { onNavigate(ScreenRoute.Patients) }
            QuickActionChip("Nuevo tratamiento", Icons.Default.MedicalServices) { onNavigate(ScreenRoute.Procedures) }
            QuickActionChip("Registrar pago", Icons.Default.Payments) { onNavigate(ScreenRoute.Payments) }
            QuickActionChip("Reporte diario", Icons.Default.Inventory2) { onNavigate(ScreenRoute.Reports) }
        }
    }
}

@Composable
private fun QuickActionChip(text: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shadowElevation = 0.dp,
        modifier = Modifier.height(40.dp),
    ) {
        Row(
            Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Text(text, style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
