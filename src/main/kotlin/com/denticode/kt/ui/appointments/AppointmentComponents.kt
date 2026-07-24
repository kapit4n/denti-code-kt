@file:OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class,
)

package com.denticode.kt.ui.appointments

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.combinedClickable

import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HeaderActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg by animateColorAsState(
        if (hovered) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surface,
        tween(160),
        label = "hbtn",
    )
    Surface(
        modifier =
            modifier
                .height(40.dp)
                .clip(AppShapes.medium)
                .hoverable(interaction)
                .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        color = bg,
        shape = AppShapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 0.dp,
        shadowElevation = if (hovered) 2.dp else 0.dp,
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            leadingIcon?.let { Icon(it, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary) }
            Text(text, style = AppTypography.Caption, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun PatientAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 44.dp,
) {
    val initials =
        remember(name) {
            val p = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            when (p.size) {
                0 -> "?"
                1 -> p[0].take(2).uppercase(Locale.getDefault())
                else -> (p[0].first().toString() + p[1].first().toString()).uppercase(Locale.getDefault())
            }
        }
    val palette =
        remember {
            listOf(
                Color(0xFFE8E6FF) to Color(0xFF4338CA),
                Color(0xFFDCEBFF) to Color(0xFF1D4ED8),
                Color(0xFFE0F2FE) to Color(0xFF0369A1),
                Color(0xFFD1FAE5) to Color(0xFF047857),
                Color(0xFFFEF3C7) to Color(0xFFB45309),
                Color(0xFFFFE4E6) to Color(0xFFBE123C),
            )
        }
    val idx = remember(name) { kotlin.math.abs(name.hashCode()) % palette.size }
    val bg = palette[idx].first
    val fg = palette[idx].second
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, style = AppTypography.Body, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
fun TimelineDot(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
    )
}

@Composable
fun AppointmentStatusBadge(
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(32.dp).widthIn(min = 100.dp),
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(alpha = 0.14f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(accent))
            Text(
                label,
                style = AppTypography.Caption,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun QuickFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null,
) {
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
        tween(140),
        label = "qfc",
    )
    val fg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        tween(140),
        label = "qfcf",
    )
    val border = if (selected) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }
    Surface(
        modifier = modifier.height(32.dp),
        shape = RoundedCornerShape(999.dp),
        color = bg,
        border = border,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        onClick = onClick,
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                label,
                style = AppTypography.Caption,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = fg,
                maxLines = 1,
            )
            count?.let {
                Surface(
                    shape = CircleShape,
                    color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        it.toString(),
                        Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                        style = AppTypography.Caption.copy(fontSize = 10.sp),
                        color = fg,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
fun AppointmentHoverActions(
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onReschedule: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(
            Triple(Icons.AutoMirrored.Filled.OpenInNew, "Abrir", onOpen),
            Triple(Icons.Default.Edit, "Editar", onEdit),
            Triple(Icons.Default.CheckCircle, "Completar", onComplete),
            Triple(Icons.Default.Close, "Cancelar", onCancel),
            Triple(Icons.Default.Schedule, "Reprogramar", onReschedule),
        ).forEach { (icon, desc, action) ->
            IconButton(
                onClick = action,
                modifier = Modifier.size(28.dp),
            ) {
                Icon(icon, desc, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun FilterDropdown(
    label: String,
    displayValue: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int? = null,
    menuContent: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        val interaction = remember { MutableInteractionSource() }
        val hovered by interaction.collectIsHoveredAsState()
        val borderColor by animateColorAsState(
            if (hovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant,
            tween(140),
            label = "fdb",
        )
        Surface(
            onClick = { onExpandedChange(!expanded) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(AppShapes.medium),
            shape = AppShapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, borderColor),
            shadowElevation = 0.dp,
        ) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (badgeCount != null && badgeCount > 0) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                        Text(
                            badgeCount.toString(),
                            Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = AppTypography.Caption,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    Icon(Icons.Default.FilterList, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(Modifier.weight(1f)) {
                    Text(label, style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    Text(displayValue, style = AppTypography.BodySmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            menuContent()
        }
    }
}

@Composable
fun MiniCalendar(
    month: YearMonth,
    selectedDate: LocalDate,
    onMonthChange: (YearMonth) -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    appointmentCountsByDate: Map<LocalDate, Int> = emptyMap(),
    highlightedDate: LocalDate? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onMonthChange(month.minusMonths(1)) }) {
                    Icon(Icons.Default.ChevronLeft, "Mes anterior")
                }
                Text(
                    month.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("es", "ES")).replaceFirstChar { it.uppercase() } + " ${month.year}",
                    style = AppTypography.CardTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = { onMonthChange(month.plusMonths(1)) }) {
                    Icon(Icons.Default.ChevronRight, "Mes siguiente")
                }
            }
            val first = month.atDay(1)
            val pad = (first.dayOfWeek.value + 6) % 7
            val daysInMonth = month.lengthOfMonth()
            val headers = listOf("L", "M", "X", "J", "V", "S", "D")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                headers.forEach { h ->
                    Text(h, Modifier.weight(1f), textAlign = TextAlign.Center, style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            val cells = buildList {
                repeat(pad) { add(null) }
                for (d in 1..daysInMonth) {
                    add(month.atDay(d))
                }
                while (size % 7 != 0) {
                    add(null)
                }
            }
            cells.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    week.forEach { cell ->
                        Box(Modifier.weight(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                            if (cell != null) {
                                val sel = cell == selectedDate
                                val highlighted = highlightedDate == cell && !sel
                                val hasAppointments = (appointmentCountsByDate[cell] ?: 0) > 0
                                val interaction = remember(cell) { MutableInteractionSource() }
                                val hovered by interaction.collectIsHoveredAsState()
                                val today = cell == LocalDate.now()
                                val bg by animateColorAsState(
                                    when {
                                        sel -> MaterialTheme.colorScheme.primary
                                        hovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                                        else -> Color.Transparent
                                    },
                                    tween(120),
                                    label = "cal",
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .then(
                                                if (!sel && today) {
                                                    Modifier.border(
                                                        1.5.dp,
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                                        CircleShape,
                                                    )
                                                } else if (highlighted) {
                                                    Modifier.border(
                                                        1.5.dp,
                                                        AppointmentPremiumPalette.warning.copy(alpha = 0.7f),
                                                        CircleShape,
                                                    )
                                                } else {
                                                    Modifier
                                                },
                                            )
                                            .background(bg)
                                            .clickable { onSelectDate(cell) },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            cell.dayOfMonth.toString(),
                                            style = AppTypography.BodySmall,
                                            fontWeight = if (sel || highlighted) FontWeight.Bold else FontWeight.Medium,
                                            color =
                                                if (sel) {
                                                    Color.White
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                        )
                                    }
                                    if (hasAppointments) {
                                        Box(
                                            Modifier
                                                .padding(top = 2.dp)
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (sel) {
                                                        Color.White.copy(alpha = 0.9f)
                                                    } else {
                                                        AppointmentPremiumPalette.primary
                                                    },
                                                ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class DaySummaryStats(
    val total: Int,
    val inProgress: Int,
    val completed: Int,
    val cancelled: Int,
)

@Composable
fun DaySummaryCard(
    stats: DaySummaryStats,
    onViewAgenda: () -> Unit,
    modifier: Modifier = Modifier,
    caption: String? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 0.dp,
    ) {
        Column(Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Text("Resumen del día", style = AppTypography.CardTitle, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            caption?.let {
                Text(
                    it,
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                KpiCard(
                    icon = Icons.Default.Event,
                    count = stats.total,
                    label = "Programadas",
                    tint = AppointmentPremiumPalette.primary,
                    modifier = Modifier.weight(1f),
                )
                KpiCard(
                    icon = Icons.Default.PlayCircle,
                    count = stats.inProgress,
                    label = "En curso",
                    tint = AppointmentPremiumPalette.info,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                KpiCard(
                    icon = Icons.Default.CheckCircle,
                    count = stats.completed,
                    label = "Completadas",
                    tint = AppointmentPremiumPalette.success,
                    modifier = Modifier.weight(1f),
                )
                KpiCard(
                    icon = Icons.Default.Cancel,
                    count = stats.cancelled,
                    label = "Canceladas",
                    tint = AppointmentPremiumPalette.error,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(AppSpacing.xs))
            AppOutlinedButton(text = "Ver agenda del día", onClick = onViewAgenda, modifier = Modifier.fillMaxWidth(), minHeight = 36.dp)
        }
    }
}

@Composable
private fun KpiCard(
    icon: ImageVector,
    count: Int,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.small,
        color = tint.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.15f)),
        shadowElevation = 0.dp,
    ) {
        Row(
            Modifier.padding(horizontal = AppSpacing.sm, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, null, Modifier.size(20.dp), tint = tint)
            Column {
                Text(
                    count.toString(),
                    style = AppTypography.CardTitle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    label,
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun AppointmentTimelineDayHeader(
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppointmentPremiumPalette.background,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
            Text(
                label,
                style = AppTypography.Caption,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 280.dp),
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
        }
    }
}

@Composable
private fun TimelineListEntryRow(
    entry: TimelineListEntry,
    selectedAppointmentId: Int?,
    onAppointmentClick: (Int) -> Unit,
    onAppointmentDoubleClick: (Int) -> Unit,
    onContextAction: (Int, AppointmentQuickActionKind) -> Unit,
) {
    when (entry) {
        is TimelineListEntry.DayHeader ->
            AppointmentTimelineDayHeader(entry.label)
        is TimelineListEntry.Appointment ->
            AppointmentTimelineCard(
                item = entry.model,
                selected = entry.model.id == selectedAppointmentId,
                onClick = { onAppointmentClick(entry.model.id) },
                onDoubleClick = { onAppointmentDoubleClick(entry.model.id) },
                onContextAction = { action -> onContextAction(entry.model.id, action) },
            )
    }
}

@Composable
fun AppointmentsTimelineList(
    entries: List<TimelineListEntry>,
    selectedAppointmentId: Int?,
    onAppointmentClick: (Int) -> Unit,
    onAppointmentDoubleClick: (Int) -> Unit = onAppointmentClick,
    onContextAction: (Int, AppointmentQuickActionKind) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    useLazyColumn: Boolean = true,
    listState: LazyListState = rememberLazyListState(),
) {
    if (useLazyColumn) {
        LazyColumn(
            modifier = modifier,
            state = listState,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            entries.forEach { entry ->
                when (entry) {
                    is TimelineListEntry.DayHeader -> {
                        item(key = "day-${entry.date}-${entry.headerIndex}") {
                            AppointmentTimelineDayHeader(entry.label)
                        }
                    }
                    is TimelineListEntry.Appointment -> {
                        item(key = "appt-${entry.model.id}") {
                            TimelineListEntryRow(
                                entry,
                                selectedAppointmentId,
                                onAppointmentClick,
                                onAppointmentDoubleClick,
                                onContextAction,
                            )
                        }
                    }
                }
            }
        }
    } else {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            entries.forEach { entry ->
                TimelineListEntryRow(
                    entry,
                    selectedAppointmentId,
                    onAppointmentClick,
                    onAppointmentDoubleClick,
                    onContextAction,
                )
            }
        }
    }
}

@Composable
fun AppointmentSelectionConnector(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!visible) {
        Spacer(modifier.width(4.dp))
        return
    }
    Box(
        modifier = modifier.width(18.dp).fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .width(3.dp)
                .fillMaxHeight(0.62f)
                .clip(RoundedCornerShape(999.dp))
                .background(AppointmentPremiumPalette.primary.copy(alpha = 0.55f)),
        )
    }
}

@Composable
fun AppointmentTimelineCard(
    item: AppointmentUiModel,
    selected: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit = onClick,
    onContextAction: (AppointmentQuickActionKind) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val contextMenuItems =
        remember(item.status) {
            listOf(
                AppointmentQuickActionKind.EDIT to "Editar",
                AppointmentQuickActionKind.RESCHEDULE to "Reprogramar",
                AppointmentQuickActionKind.CONFIRM to "Confirmar",
                AppointmentQuickActionKind.START to "Iniciar cita",
                AppointmentQuickActionKind.COMPLETE to "Completar",
                AppointmentQuickActionKind.CANCEL to "Cancelar",
                AppointmentQuickActionKind.VIEW_PATIENT to "Ver paciente",
                AppointmentQuickActionKind.REGISTER_PAYMENT to "Registrar pago",
                AppointmentQuickActionKind.REGISTER_TREATMENT to "Registrar tratamiento",
            ).filter { (kind, _) -> item.status.isContextMenuActionEnabled(kind) }
        }
    val interaction = remember(item.id) { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val elevation by animateDpAsState(
        targetValue = when {
            selected -> 4.dp
            hovered -> 3.dp
            else -> 2.dp
        },
        animationSpec = tween(200),
        label = "elev",
    )
    val borderWidth by animateDpAsState(
        targetValue = if (selected) 2.dp else 1.dp,
        animationSpec = tween(200),
        label = "brdW",
    )
    val borderColor by animateColorAsState(
        when {
            selected -> AppointmentPremiumPalette.primary
            hovered -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        tween(200),
        label = "brd",
    )
    val bg by animateColorAsState(
        when {
            selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
            hovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            else -> MaterialTheme.colorScheme.surface
        },
        tween(200),
        label = "bg",
    )
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(elevation, AppShapes.medium, clip = false),
    ) {
        Box(Modifier.fillMaxWidth()) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.medium)
                        .border(borderWidth, borderColor, AppShapes.medium)
                        .combinedClickable(
                            interactionSource = interaction,
                            indication = null,
                            onClick = onClick,
                            onDoubleClick = onDoubleClick,
                        )
                        .onPointerEvent(PointerEventType.Press) { event ->
                            if (event.buttons.isSecondaryPressed) {
                                menuExpanded = true
                            }
                        }
                        .hoverable(interaction),
                shape = AppShapes.medium,
                color = bg,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Box(Modifier.fillMaxWidth()) {
                    if (selected) {
                        Box(
                            Modifier
                                .align(Alignment.CenterStart)
                                .width(4.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                                .background(AppointmentPremiumPalette.primary),
                        )
                    }
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                start = if (selected) AppSpacing.md + 4.dp else AppSpacing.md,
                                end = 14.dp,
                                top = AppSpacing.sm,
                                bottom = if (hovered && !selected) AppSpacing.xs else AppSpacing.sm,
                            ),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        ) {
                            Column(
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier.width(52.dp),
                            ) {
                                Text(
                                    item.timeLabel,
                                    style = AppTypography.CardTitle.copy(fontSize = 18.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    "${item.durationMinutes} min",
                                    style = AppTypography.Caption,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            PatientAvatar(item.patientName, size = 40.dp)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    item.patientName,
                                    style = AppTypography.CardTitle.copy(fontSize = 16.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        Icons.Default.MedicalServices,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        item.treatmentName,
                                        style = AppTypography.Caption.copy(fontSize = 13.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        Icons.Default.PersonOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    )
                                    Text(
                                        item.doctorName,
                                        style = AppTypography.Caption.copy(fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                            ) {
                                AppointmentStatusBadge(
                                    item.displayStatusLabel,
                                    item.statusAccentColor,
                                )
                                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.MoreVert, "Menú", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        if (hovered && !selected) {
                            Spacer(Modifier.height(4.dp))
                            AppointmentHoverActions(
                                onOpen = { onClick() },
                                onEdit = { onContextAction(AppointmentQuickActionKind.EDIT) },
                                onComplete = { onContextAction(AppointmentQuickActionKind.COMPLETE) },
                                onCancel = { onContextAction(AppointmentQuickActionKind.CANCEL) },
                                onReschedule = { onContextAction(AppointmentQuickActionKind.RESCHEDULE) },
                            )
                        }
                    }
                }
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                contextMenuItems.forEach { (kind, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            menuExpanded = false
                            onContextAction(kind)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    outlined: Boolean = true,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val active = enabled && !loading
    val bg by animateColorAsState(
        if (hovered && active) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent,
        tween(140),
        label = "qab",
    )
    if (outlined) {
        Surface(
            modifier =
                modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(AppShapes.small)
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        enabled = active,
                        onClick = onClick,
                    )
                    .hoverable(interaction, enabled = active),
            shape = AppShapes.small,
            color = bg,
            border =
                BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = if (active) 0.35f else 0.18f),
                ),
        ) {
            Row(
                Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    icon,
                    null,
                    Modifier.size(18.dp),
                    tint =
                        if (active) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        },
                )
                Text(
                    if (loading) "Cargando..." else text,
                    style = AppTypography.BodySmall,
                    fontWeight = FontWeight.Medium,
                    color =
                        if (active) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun ReminderCard(
    previewText: String,
    onPreviewChange: (String) -> Unit,
    onCopy: () -> Unit,
    onOpenWhatsApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = AppointmentPremiumPalette.primary.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, AppointmentPremiumPalette.primary.copy(alpha = 0.2f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Icon(Icons.AutoMirrored.Outlined.Chat, null, tint = AppointmentPremiumPalette.primary, modifier = Modifier.size(22.dp))
                Text(
                    "Enviar recordatorio por WhatsApp",
                    style = AppTypography.CardTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text("Vista previa del mensaje", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = previewText,
                onValueChange = onPreviewChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Hola, le recordamos su cita…") },
                minLines = 2,
                shape = AppShapes.small,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppointmentPremiumPalette.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    ),
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Copiar", onClick = onCopy, minHeight = 40.dp)
                AppButton(text = "Abrir WhatsApp", onClick = onOpenWhatsApp, minHeight = 40.dp)
            }
        }
    }
}

@Composable
fun AppointmentViewModeToggle(
    mode: AppointmentViewMode,
    onModeChange: (AppointmentViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    Surface(
        modifier = modifier.height(40.dp).fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = border,
        shadowElevation = 0.dp,
    ) {
        Row(Modifier.fillMaxWidth().fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
            listOf(
                AppointmentViewMode.ALL to "Todas",
                AppointmentViewMode.DAY to "Día",
                AppointmentViewMode.WEEK to "Semana",
                AppointmentViewMode.MONTH to "Mes",
            ).forEach { (m, label) ->
                val selected = mode == m
                val bg by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    tween(140),
                    label = "vmt",
                )
                val fg by animateColorAsState(
                    if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                    tween(140),
                    label = "vmf",
                )
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .clickable { onModeChange(m) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        style = AppTypography.Caption,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = fg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
