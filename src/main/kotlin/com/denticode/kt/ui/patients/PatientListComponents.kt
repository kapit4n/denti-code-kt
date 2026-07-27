@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.patients

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
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
import com.denticode.kt.ui.theme.AppElevations
import com.denticode.kt.data.PatientListStatus
import com.denticode.kt.ui.appointments.PatientAvatar
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.inputs.AppSearchField
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.formatMoney
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

object PatientsPremiumPalette {
    val primary = Color(0xFF6C63FF)
    val background = Color(0xFFF5F7FB)
    val card = Color(0xFFFFFFFF)
    val success = Color(0xFF34C759)
    val warning = Color(0xFFFFB020)
    val error = Color(0xFFFF5A5F)
    val info = Color(0xFF4DA3FF)
    val textPrimary = Color(0xFF1F2937)
    val textSecondary = Color(0xFF6B7280)
}

@Composable
fun PatientsPageHeader(
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Pacientes",
                style = AppTypography.PageTitle,
                fontWeight = FontWeight.Bold,
                color = PatientsPremiumPalette.textPrimary,
            )
            Text(
                "Administra la información de todos los pacientes de la clínica.",
                style = AppTypography.Body,
                color = PatientsPremiumPalette.textSecondary,
            )
        }
        AppButton(
            text = "Registrar cliente",
            onClick = onRegisterClick,
            minHeight = 44.dp,
            shape = RoundedCornerShape(999.dp),
            leadingIcon = {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
            },
        )
    }
}

@Composable
fun PatientMetricCard(
    title: String,
    value: String,
    trendText: String,
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val scale by animateFloatAsState(if (hovered) 1.02f else 1f, tween(180), label = "kpi")
    val elevation by animateDpAsState(if (hovered) AppElevations.cardHovered else AppElevations.cardRest, tween(180), label = "kpiElev")
    Surface(
        modifier =
            modifier
                .scale(scale)
                .hoverable(interaction),
        shape = AppShapes.medium,
        color = PatientsPremiumPalette.card,
        shadowElevation = elevation,
        tonalElevation = 0.dp,
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
            ),
    ) {
        Row(
            Modifier.padding(AppSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(26.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
                Text(
                    value,
                    style = AppTypography.MetricLarge,
                    fontWeight = FontWeight.Bold,
                    color = PatientsPremiumPalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(trendText, style = AppTypography.BodySmall, color = PatientsPremiumPalette.textSecondary, maxLines = 1)
            }
        }
    }
}

@Composable
fun PatientsKpiRow(
    total: Int,
    active: Int,
    newMonth: Int,
    scheduled: Int,
    pendingDebt: Double,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        PatientMetricCard(
            title = "Total pacientes",
            value = total.toString(),
            trendText = "Directorio completo",
            icon = Icons.Default.People,
            iconBackground = PatientsPremiumPalette.primary.copy(alpha = 0.12f),
            iconTint = PatientsPremiumPalette.primary,
            modifier = Modifier.weight(1f),
        )
        PatientMetricCard(
            title = "Pacientes activos",
            value = active.toString(),
            trendText = "Con actividad reciente",
            icon = Icons.Default.Person,
            iconBackground = PatientsPremiumPalette.success.copy(alpha = 0.12f),
            iconTint = PatientsPremiumPalette.success,
            modifier = Modifier.weight(1f),
        )
        PatientMetricCard(
            title = "Nuevos este mes",
            value = newMonth.toString(),
            trendText = "Altas del mes actual",
            icon = Icons.Default.Add,
            iconBackground = PatientsPremiumPalette.info.copy(alpha = 0.12f),
            iconTint = PatientsPremiumPalette.info,
            modifier = Modifier.weight(1f),
        )
        PatientMetricCard(
            title = "Citas programadas",
            value = scheduled.toString(),
            trendText = "Próximas en agenda",
            icon = Icons.Default.CalendarMonth,
            iconBackground = PatientsPremiumPalette.warning.copy(alpha = 0.14f),
            iconTint = PatientsPremiumPalette.warning,
            modifier = Modifier.weight(1f),
        )
        PatientMetricCard(
            title = "Deuda pendiente",
            value = formatMoney(pendingDebt),
            trendText = "Saldo estimado",
            icon = Icons.Default.Payments,
            iconBackground = Color(0xFF6366F1).copy(alpha = 0.12f),
            iconTint = Color(0xFF6366F1),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun PatientFilterDropdown(
    label: String,
    displayValue: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.widthIn(min = 140.dp, max = 200.dp)) {
        AppTextField(
            value = displayValue,
            onValueChange = {},
            label = label,
            readOnly = true,
            enabled = enabled,
            textFieldModifier =
                Modifier.clickable(enabled = enabled) {
                    if (enabled) expanded = true
                },
            trailingIcon = {
                IconButton(onClick = { if (enabled) expanded = true }, enabled = enabled) {
                    Icon(Icons.Default.FilterList, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onSelect(opt)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun PatientsFilterToolbar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    statusLabel: String,
    statusOptions: List<String>,
    onStatusSelect: (String) -> Unit,
    doctorLabel: String,
    doctorOptions: List<String>,
    onDoctorSelect: (String) -> Unit,
    ageLabel: String,
    ageOptions: List<String>,
    onAgeSelect: (String) -> Unit,
    moreFiltersCount: Int,
    onMoreFilters: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = PatientsPremiumPalette.card,
        shadowElevation = AppElevations.low,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            Modifier.padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            AppSearchField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1.2f),
                placeholder = "Buscar paciente por nombre, email o teléfono…",
                keyboardShortcutHint = null,
            )
            PatientFilterDropdown(
                label = "Estado",
                displayValue = statusLabel,
                options = statusOptions,
                onSelect = onStatusSelect,
            )
            PatientFilterDropdown(
                label = "Doctor",
                displayValue = doctorLabel,
                options = doctorOptions,
                onSelect = onDoctorSelect,
            )
            PatientFilterDropdown(
                label = "Edad",
                displayValue = ageLabel,
                options = ageOptions,
                onSelect = onAgeSelect,
            )
            AppOutlinedButton(
                text = if (moreFiltersCount > 0) "Más filtros ($moreFiltersCount)" else "Más filtros",
                onClick = onMoreFilters,
                leadingIcon = {
                    Icon(Icons.Default.FilterList, null, modifier = Modifier.size(18.dp))
                },
            )
            AppOutlinedButton(
                text = "Exportar",
                onClick = onExport,
                leadingIcon = {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                },
            )
        }
    }
}

@Composable
fun PatientStatusBadge(
    status: PatientListStatus,
    modifier: Modifier = Modifier,
) {
    val (bg, fg, dot) =
        when (status) {
            PatientListStatus.ACTIVE ->
                Triple(
                    PatientsPremiumPalette.success.copy(alpha = 0.14f),
                    PatientsPremiumPalette.success,
                    PatientsPremiumPalette.success,
                )
            PatientListStatus.INACTIVE ->
                Triple(
                    Color(0xFFE5E7EB),
                    PatientsPremiumPalette.textSecondary,
                    Color(0xFF9CA3AF),
                )
            PatientListStatus.PENDING ->
                Triple(
                    PatientsPremiumPalette.warning.copy(alpha = 0.16f),
                    Color(0xFFB45309),
                    PatientsPremiumPalette.warning,
                )
        }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = bg,
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(dot))
            Text(
                status.labelEs,
                style = AppTypography.Caption,
                fontWeight = FontWeight.SemiBold,
                color = fg,
            )
        }
    }
}

@Composable
fun ActionMenuButton(
    onViewDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, "Acciones", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Ver detalle") },
                onClick = {
                    expanded = false
                    onViewDetail()
                },
            )
            DropdownMenuItem(
                text = { Text("Nueva cita") },
                onClick = { expanded = false },
            )
        }
    }
}

@Composable
fun PatientTableHeader(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(PatientsPremiumPalette.background)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableHeaderCell("PACIENTE", Modifier.weight(1.35f))
        TableHeaderCell("CONTACTO", Modifier.weight(1.1f))
        TableHeaderCell("DOCTOR PRINCIPAL", Modifier.weight(1f))
        TableHeaderCell("ÚLTIMA CITA", Modifier.weight(1f))
        TableHeaderCell("PRÓXIMA CITA", Modifier.weight(1f))
        TableHeaderCell("ESTADO", Modifier.width(110.dp))
        TableHeaderCell("ACCIONES", Modifier.width(56.dp))
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text,
        modifier = modifier,
        style = AppTypography.Caption,
        fontWeight = FontWeight.SemiBold,
        color = PatientsPremiumPalette.textSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun PatientTableRow(
    patient: PatientUiModel,
    selected: Boolean,
    onClick: () -> Unit,
    onViewDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember(patient.id) { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg by animateColorAsState(
        when {
            selected -> PatientsPremiumPalette.primary.copy(alpha = 0.08f)
            hovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            else -> Color.Transparent
        },
        tween(140),
        label = "rowBg",
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .clickable(interactionSource = interaction, indication = null, onClick = onClick)
                    .hoverable(interaction)
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(Modifier.weight(1.35f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                PatientAvatar(patient.fullName, size = 40.dp)
                Column {
                    Text(patient.fullName, style = AppTypography.Body, fontWeight = FontWeight.SemiBold, color = PatientsPremiumPalette.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val sub = buildList { add(patient.birthDateLabel); patient.patient.documentNumber?.let { add("Doc: $it") } }
                    Text(sub.joinToString(" · "), style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Column(Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(patient.phone, style = AppTypography.BodySmall, color = PatientsPremiumPalette.textPrimary, maxLines = 1)
                Text(patient.email ?: "—", style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary, maxLines = 1)
            }
            Text(
                patient.primaryDoctorName,
                Modifier.weight(1f),
                style = AppTypography.BodySmall,
                color = PatientsPremiumPalette.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    patient.lastAppointmentDate ?: "—",
                    style = AppTypography.BodySmall,
                    fontWeight = FontWeight.Medium,
                    color = PatientsPremiumPalette.textPrimary,
                )
                Text(
                    patient.lastAppointmentTreatment ?: "Sin registro",
                    style = AppTypography.Caption,
                    color = PatientsPremiumPalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (patient.nextAppointmentDate != null) {
                    Text(patient.nextAppointmentDate, style = AppTypography.BodySmall, fontWeight = FontWeight.Medium, color = PatientsPremiumPalette.textPrimary)
                    patient.nextAppointmentTime?.let {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Schedule, null, Modifier.size(14.dp), tint = PatientsPremiumPalette.textSecondary)
                            Text(it, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
                        }
                    }
                } else {
                    Text("Sin programar", style = AppTypography.BodySmall, color = PatientsPremiumPalette.textSecondary)
                }
            }
            Box(Modifier.width(110.dp)) {
                PatientStatusBadge(patient.status)
            }
            Box(Modifier.width(56.dp), contentAlignment = Alignment.Center) {
                ActionMenuButton(onViewDetail = onViewDetail)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
fun PatientTable(
    patients: List<PatientUiModel>,
    selectedId: Int?,
    onSelect: (PatientUiModel) -> Unit,
    onViewDetail: (PatientUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = PatientsPremiumPalette.card,
        shadowElevation = AppElevations.cardRest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            item(key = "header") { PatientTableHeader() }
            items(patients, key = { it.id }) { p ->
                PatientTableRow(
                    patient = p,
                    selected = p.id == selectedId,
                    onClick = { onSelect(p) },
                    onViewDetail = { onViewDetail(p) },
                )
            }
        }
    }
}

@Composable
fun EmptyPatientsState(
    hasFilters: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(Icons.Default.People, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        Text(
            if (hasFilters) "Ningún paciente coincide con los filtros" else "Aún no hay pacientes registrados",
            style = AppTypography.Body,
            fontWeight = FontWeight.Medium,
            color = PatientsPremiumPalette.textPrimary,
        )
        Text(
            if (hasFilters) "Pruebe a ampliar la búsqueda o quitar filtros." else "Use «Registrar cliente» para añadir el primero.",
            style = AppTypography.BodySmall,
            color = PatientsPremiumPalette.textSecondary,
        )
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    pageSize: Int,
    totalItems: Int,
    rangeStart: Int,
    rangeEnd: Int,
    pageSizeOptions: List<Int>,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pageSizeMenu by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = PatientsPremiumPalette.card,
        tonalElevation = 0.dp,
        shadowElevation = AppElevations.hairline,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Mostrando $rangeStart a $rangeEnd de $totalItems pacientes",
                style = AppTypography.BodySmall,
                color = PatientsPremiumPalette.textSecondary,
                modifier = Modifier.weight(1f),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PageNavButton(enabled = currentPage > 1, onClick = { onPageChange(currentPage - 1) }) {
                    Icon(Icons.Default.ChevronLeft, "Anterior", Modifier.size(20.dp))
                }
                val window = pageWindow(currentPage, totalPages)
                window.forEach { p ->
                    if (p == null) {
                        Text("…", Modifier.padding(horizontal = 6.dp), color = PatientsPremiumPalette.textSecondary)
                    } else {
                        PageNumberChip(page = p, selected = p == currentPage, onClick = { onPageChange(p) })
                    }
                }
                PageNavButton(enabled = currentPage < totalPages, onClick = { onPageChange(currentPage + 1) }) {
                    Icon(Icons.Default.ChevronRight, "Siguiente", Modifier.size(20.dp))
                }
            }
            Box {
                AppOutlinedButton(
                    text = "$pageSize / página",
                    onClick = { pageSizeMenu = true },
                )
                DropdownMenu(expanded = pageSizeMenu, onDismissRequest = { pageSizeMenu = false }) {
                    pageSizeOptions.forEach { size ->
                        DropdownMenuItem(
                            text = { Text("$size filas") },
                            onClick = {
                                onPageSizeChange(size)
                                pageSizeMenu = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PageNavButton(
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (enabled) 0.5f else 0.2f),
        modifier = Modifier.size(36.dp),
    ) {
        Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
private fun PageNumberChip(
    page: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) PatientsPremiumPalette.primary else Color.Transparent,
        border =
            if (selected) {
                null
            } else {
                androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            },
        modifier = Modifier.size(36.dp),
    ) {
        Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
            Text(
                page.toString(),
                style = AppTypography.BodySmall,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else PatientsPremiumPalette.textPrimary,
            )
        }
    }
}

private fun pageWindow(current: Int, total: Int): List<Int?> {
    if (total <= 7) return (1..total).toList()
    val pages = linkedSetOf<Int>()
    pages.add(1)
    pages.add(total)
    for (p in (current - 1)..(current + 1)) {
        if (p in 2 until total) pages.add(p)
    }
    val sorted = pages.sorted()
    val out = mutableListOf<Int?>()
    var prev = 0
    for (p in sorted) {
        if (prev != 0 && p - prev > 1) out.add(null)
        out.add(p)
        prev = p
    }
    return out
}
