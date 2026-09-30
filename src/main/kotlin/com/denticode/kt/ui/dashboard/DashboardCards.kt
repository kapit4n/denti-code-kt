package com.denticode.kt.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.outlined.PersonAdd
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.StockStatus
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.cards.AppCard
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppTypography

private val CompactCardPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
private val MainRowMaxHeight = 320.dp
private val LowerRowMaxHeight = 380.dp

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
                BoxWithConstraints(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) {
                    val donutSize = minOf(maxHeight, 240.dp)
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AppointmentStatusDonutChart(slices = slices, size = donutSize)
                        DonutLegend(slices = slices, modifier = Modifier.weight(1f))
                    }
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                alerts.take(3).forEach { alert ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        shape = AppShapes.small,
                        color = alert.tint.copy(alpha = 0.12f),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("\u26A0", style = AppTypography.BodySmall)
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
                                    maxLines = 2,
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
fun DashboardStockAlertsCard(
    alerts: List<StockAlertUi>,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lowCount = alerts.count { it.status == StockStatus.LOW }
    val outCount = alerts.count { it.status == StockStatus.OUT }
    AppCard(
        modifier = modifier.heightIn(max = LowerRowMaxHeight).fillMaxHeight(),
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
                Text("Alertas de stock", style = AppTypography.CardTitle, color = MaterialTheme.colorScheme.onSurface)
                AppOutlinedButton(text = "Ver inventario", onClick = onViewAll)
            }
            if (alerts.isEmpty()) {
                Text(
                    "Sin alertas de stock: todos los insumos en óptimo nivel.",
                    style = AppTypography.BodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Text(
                    if (outCount > 0) "$lowCount líneas bajas · $outCount agotadas"
                    else "$lowCount líneas por debajo del mínimo",
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    alerts.take(6).forEach { alert ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StockAlertBadge(alert.status)
                            Text(
                                alert.productName,
                                style = AppTypography.BodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                "${alert.consultoryLabel} · ${alert.categoryLabel}",
                                style = AppTypography.Caption,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                "${alert.quantity}/${alert.minQuantity} mín",
                                style = AppTypography.Caption,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockAlertBadge(status: StockStatus, modifier: Modifier = Modifier) {
    val bg: Color
    val fg: Color
    when (status) {
        StockStatus.LOW -> {
            bg = Color(0xFFFFEDD5)
            fg = Color(0xFFB45309)
        }
        StockStatus.OUT -> {
            bg = Color(0xFFFFE4E6)
            fg = Color(0xFFEF4444)
        }
        StockStatus.OPTIMAL -> {
            bg = Color(0xFFD1FAE5)
            fg = Color(0xFF059669)
        }
    }
    Surface(
        modifier = modifier,
        shape = AppShapes.small,
        color = bg,
    ) {
        Text(
            status.labelEs,
            style = AppTypography.Caption,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}
