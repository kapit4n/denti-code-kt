package com.denticode.kt.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.PersonAdd
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
import com.denticode.kt.data.ClinicOverview
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.cards.AppCard
import com.denticode.kt.ui.navigation.ScreenRoute
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CompactCardPadding = PaddingValues(16.dp)
private val DashboardSectionSpacing = 12.dp
private val KpiRowHeight = 110.dp
private val MainRowMaxHeight = 320.dp
private val LowerRowMaxHeight = 240.dp

@Composable
fun DashboardWelcomeHeader(modifier: Modifier = Modifier) {
    val dateFmt = remember { DateTimeFormatter.ofPattern("EEEE d MMM yyyy", Locale("es", "ES")) }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.Default.MedicalServices,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Hola, equipo clínico",
                    style = AppTypography.CardTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "Centro operativo · Denti-Code",
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            LocalDate.now().format(dateFmt),
            style = AppTypography.Caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun DashboardKpiRow(
    overview: ClinicOverview,
    todayCount: Int,
    revenueToday: Double,
    lowStock: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(KpiRowHeight),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DashboardKpiCard(
            title = "Pacientes",
            value = overview.patientCount.toString(),
            subtitle = "Directorio activo",
            icon = Icons.Default.People,
            gradient = listOf(Color(0xFF4DA3FF), Color(0xFF6C63FF)),
            modifier = Modifier.weight(1f),
        )
        DashboardKpiCard(
            title = "Citas hoy",
            value = todayCount.toString(),
            subtitle = "Agendadas hoy",
            icon = Icons.Default.CalendarMonth,
            gradient = listOf(Color(0xFF8B80F9), Color(0xFF6C63FF)),
            modifier = Modifier.weight(1f),
        )
        DashboardKpiCard(
            title = "Ingresos hoy",
            value = "€ %.0f".format(revenueToday),
            subtitle = "Pagos registrados",
            icon = Icons.Default.Payments,
            gradient = listOf(Color(0xFF34C759), Color(0xFF2FA34A)),
            modifier = Modifier.weight(1f),
        )
        DashboardKpiCard(
            title = "Tratamientos",
            value = overview.upcomingAppointmentCount.toString(),
            subtitle = "Pendientes / en curso",
            icon = Icons.Default.MedicalServices,
            gradient = listOf(Color(0xFFFFB020), Color(0xFFFF8A3D)),
            modifier = Modifier.weight(1f),
        )
        DashboardKpiCard(
            title = "Stock",
            value = lowStock.toString(),
            subtitle = "Líneas bajo mínimo",
            icon = Icons.Default.Inventory2,
            gradient = listOf(Color(0xFFFF5A5F), Color(0xFFFF8A8E)),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DashboardKpiCard(
    title: String,
    value: String,
    subtitle: String,
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
                .fillMaxHeight()
                .scale(scale)
                .hoverable(interaction),
        shape = AppShapes.medium,
        shadowElevation = if (hovered) 6.dp else 2.dp,
        tonalElevation = 0.dp,
        color = Color.Transparent,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(gradient))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = AppTypography.Caption, color = Color.White.copy(alpha = 0.9f), maxLines = 1)
                Text(value, style = AppTypography.MetricMedium, color = Color.White, maxLines = 1)
                Text(subtitle, style = AppTypography.Caption, color = Color.White.copy(alpha = 0.85f), maxLines = 1)
            }
        }
    }
}

@Composable
fun DashboardAppointmentsCard(
    rows: List<TodayAppointmentUi>,
    onViewCalendar: () -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier.heightIn(max = MainRowMaxHeight).fillMaxHeight(),
        contentPadding = CompactCardPadding,
        showHairlineBorder = true,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Citas de hoy", style = AppTypography.CardTitle, color = MaterialTheme.colorScheme.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppOutlinedButton(text = "Ver calendario", onClick = onViewCalendar)
                    AppOutlinedButton(text = "Ver todas", onClick = onViewAll)
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Hora", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.widthIn(min = 40.dp))
                Text("Paciente", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Text("Doctor", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Text("Estado", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                rows.take(5).forEach { r ->
                    val interaction = remember(r.id) { MutableInteractionSource() }
                    val hovered by interaction.collectIsHoveredAsState()
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(AppShapes.small)
                                .hoverable(interaction)
                                .background(
                                    if (hovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                                    else Color.Transparent,
                                )
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(r.timeLabel, style = AppTypography.BodySmall, modifier = Modifier.widthIn(min = 40.dp))
                        Text(
                            r.patientName,
                            style = AppTypography.BodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
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
            }
        }
    }
}

@Composable
fun DashboardActivityCard(
    items: List<ActivityFeedItem>,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier.heightIn(max = MainRowMaxHeight).fillMaxHeight(),
        contentPadding = CompactCardPadding,
        showHairlineBorder = true,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Actividad reciente", style = AppTypography.CardTitle, color = MaterialTheme.colorScheme.onSurface)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items.take(5).forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(item.accent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                activityIconFor(item.title),
                                contentDescription = null,
                                tint = item.accent,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                        Text(
                            item.title,
                            style = AppTypography.BodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            item.timeLabel,
                            style = AppTypography.Caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun activityIconFor(title: String): ImageVector =
    when {
        title.contains("paciente", ignoreCase = true) -> Icons.Outlined.PersonAdd
        title.contains("Pago", ignoreCase = true) -> Icons.Default.Payments
        title.contains("Tratamiento", ignoreCase = true) -> Icons.Default.MedicalServices
        title.contains("cancel", ignoreCase = true) -> Icons.Default.CalendarMonth
        else -> Icons.Default.CalendarMonth
    }

@Composable
fun DashboardRevenueCard(
    weekTotalLabel: String,
    deltaLabel: String,
    values: List<Float>,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier.heightIn(max = MainRowMaxHeight).fillMaxHeight(),
        contentPadding = CompactCardPadding,
        showHairlineBorder = true,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Ingresos semanales", style = AppTypography.CardTitle, color = MaterialTheme.colorScheme.onSurface)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(weekTotalLabel, style = AppTypography.MetricMedium, color = MaterialTheme.colorScheme.primary)
                Text(deltaLabel, style = AppTypography.Caption, color = Color(0xFF34C759))
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                RevenueLineChart(
                    values = values,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 120.dp),
                )
            }
        }
    }
}

@Composable
fun DashboardStatusCard(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
) {
    val total = slices.sumOf { it.value.toDouble() }.toInt()
    AppCard(
        modifier = modifier.heightIn(max = LowerRowMaxHeight).fillMaxHeight(),
        contentPadding = CompactCardPadding,
        showHairlineBorder = true,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Estado de citas", style = AppTypography.CardTitle, color = MaterialTheme.colorScheme.onSurface)
            if (slices.isEmpty()) {
                Text(
                    "Sin citas registradas.",
                    style = AppTypography.BodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppointmentStatusDonutChart(slices = slices, size = 160.dp)
                    DonutLegend(slices = slices, modifier = Modifier.weight(1f))
                }
                Text(
                    "Total: $total citas",
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun DashboardAlertsCard(
    alerts: List<DashboardAlertUi>,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier.heightIn(max = LowerRowMaxHeight).fillMaxHeight(),
        contentPadding = CompactCardPadding,
        showHairlineBorder = true,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Alertas", style = AppTypography.CardTitle, color = MaterialTheme.colorScheme.onSurface)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                alerts.take(3).forEach { alert ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.small,
                        color = alert.tint.copy(alpha = 0.12f),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("⚠", style = AppTypography.BodySmall)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    alert.title,
                                    style = AppTypography.BodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    alert.subtitle,
                                    style = AppTypography.Caption,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowForwardIos,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardQuickActionsCard(
    onNavigate: (ScreenRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    val actions =
        remember(onNavigate) {
            listOf(
                QuickActionDef("Nueva cita", Icons.Default.CalendarMonth) { onNavigate(ScreenRoute.Appointments) },
                QuickActionDef("Nuevo paciente", Icons.Default.People) { onNavigate(ScreenRoute.Patients) },
                QuickActionDef("Registrar pago", Icons.Default.Payments) { onNavigate(ScreenRoute.Payments) },
                QuickActionDef("Nuevo tratamiento", Icons.Default.MedicalServices) { onNavigate(ScreenRoute.Procedures) },
                QuickActionDef("Buscar paciente", Icons.Outlined.Search) { onNavigate(ScreenRoute.Patients) },
                QuickActionDef("Reporte diario", Icons.Outlined.Assessment) { onNavigate(ScreenRoute.Reports) },
            )
        }
    AppCard(
        modifier = modifier.heightIn(max = LowerRowMaxHeight).fillMaxHeight(),
        contentPadding = CompactCardPadding,
        showHairlineBorder = true,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Accesos rápidos", style = AppTypography.CardTitle, color = MaterialTheme.colorScheme.onSurface)
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                actions.chunked(2).forEach { rowActions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowActions.forEach { action ->
                            DashboardQuickActionButton(
                                label = action.label,
                                icon = action.icon,
                                onClick = action.onClick,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowActions.size == 1) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

private data class QuickActionDef(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
private fun DashboardQuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val scale by animateFloatAsState(if (hovered) 1.02f else 1f, tween(150), label = "qa")
    Surface(
        modifier =
            modifier
                .height(36.dp)
                .scale(scale)
                .hoverable(interaction)
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color =
            if (hovered) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            },
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Text(label, style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
        }
    }
}

@Composable
fun ResponsiveDashboardGrid(
    todayRows: List<TodayAppointmentUi>,
    activityItems: List<ActivityFeedItem>,
    revenueValues: List<Float>,
    revenueTotalLabel: String,
    revenueDeltaLabel: String,
    donutSlices: List<DonutSlice>,
    alerts: List<DashboardAlertUi>,
    onNavigate: (ScreenRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val layout = when {
            maxWidth >= 1200.dp -> DashboardGridLayout.ThreeColumn
            maxWidth >= 800.dp -> DashboardGridLayout.TwoColumn
            else -> DashboardGridLayout.SingleColumn
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(DashboardSectionSpacing),
        ) {
            when (layout) {
                DashboardGridLayout.ThreeColumn ->
                    Row(
                        modifier = Modifier.weight(0.58f).fillMaxWidth().heightIn(max = MainRowMaxHeight),
                        horizontalArrangement = Arrangement.spacedBy(DashboardSectionSpacing),
                    ) {
                        DashboardAppointmentsCard(
                            rows = todayRows,
                            onViewCalendar = { onNavigate(ScreenRoute.Appointments) },
                            onViewAll = { onNavigate(ScreenRoute.Appointments) },
                            modifier = Modifier.weight(45f),
                        )
                        DashboardActivityCard(
                            items = activityItems,
                            modifier = Modifier.weight(25f),
                        )
                        DashboardRevenueCard(
                            weekTotalLabel = revenueTotalLabel,
                            deltaLabel = revenueDeltaLabel,
                            values = revenueValues,
                            modifier = Modifier.weight(30f),
                        )
                    }
                DashboardGridLayout.TwoColumn ->
                    Column(
                        modifier = Modifier.weight(0.58f).fillMaxWidth().heightIn(max = MainRowMaxHeight + 12.dp + MainRowMaxHeight),
                        verticalArrangement = Arrangement.spacedBy(DashboardSectionSpacing),
                    ) {
                        DashboardAppointmentsCard(
                            rows = todayRows,
                            onViewCalendar = { onNavigate(ScreenRoute.Appointments) },
                            onViewAll = { onNavigate(ScreenRoute.Appointments) },
                            modifier = Modifier.fillMaxWidth().heightIn(max = MainRowMaxHeight),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(max = MainRowMaxHeight),
                            horizontalArrangement = Arrangement.spacedBy(DashboardSectionSpacing),
                        ) {
                            DashboardActivityCard(items = activityItems, modifier = Modifier.weight(1f))
                            DashboardRevenueCard(
                                weekTotalLabel = revenueTotalLabel,
                                deltaLabel = revenueDeltaLabel,
                                values = revenueValues,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                DashboardGridLayout.SingleColumn ->
                    Column(
                        modifier = Modifier.weight(0.58f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(DashboardSectionSpacing),
                    ) {
                        DashboardAppointmentsCard(
                            rows = todayRows,
                            onViewCalendar = { onNavigate(ScreenRoute.Appointments) },
                            onViewAll = { onNavigate(ScreenRoute.Appointments) },
                            modifier = Modifier.fillMaxWidth().heightIn(max = MainRowMaxHeight),
                        )
                        DashboardActivityCard(
                            items = activityItems,
                            modifier = Modifier.fillMaxWidth().heightIn(max = MainRowMaxHeight),
                        )
                        DashboardRevenueCard(
                            weekTotalLabel = revenueTotalLabel,
                            deltaLabel = revenueDeltaLabel,
                            values = revenueValues,
                            modifier = Modifier.fillMaxWidth().heightIn(max = MainRowMaxHeight),
                        )
                    }
            }
            Row(
                modifier = Modifier.weight(0.42f).fillMaxWidth().heightIn(max = LowerRowMaxHeight),
                horizontalArrangement = Arrangement.spacedBy(DashboardSectionSpacing),
            ) {
                DashboardStatusCard(slices = donutSlices, modifier = Modifier.weight(1f))
                DashboardAlertsCard(alerts = alerts, modifier = Modifier.weight(1f))
                DashboardQuickActionsCard(onNavigate = onNavigate, modifier = Modifier.weight(1f))
            }
        }
    }
}

private enum class DashboardGridLayout {
    ThreeColumn,
    TwoColumn,
    SingleColumn,
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}
