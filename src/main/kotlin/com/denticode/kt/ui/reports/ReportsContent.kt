@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.ReportsOverview
import com.denticode.kt.ui.charts.BarChartEntry
import com.denticode.kt.ui.charts.DonutSegment
import com.denticode.kt.ui.charts.EnterpriseBarChart
import com.denticode.kt.ui.charts.EnterpriseDonutChart
import com.denticode.kt.ui.charts.EnterpriseLineChart
import com.denticode.kt.ui.charts.LinePoint
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.cards.AppCard
import com.denticode.kt.ui.components.cards.MetricCard
import com.denticode.kt.ui.components.dialogs.AppBasicDialog
import com.denticode.kt.ui.components.inputs.AppDatePickerField
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.components.inputs.DatePickerShortcut
import com.denticode.kt.ui.components.inputs.rememberPastOrTodaySelectableDates
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ReportsGreen = Color(0xFF22C55E)
private val ReportsBlue = Color(0xFF2563EB)
private val ReportsAmber = Color(0xFFF59E0B)
private val ReportsRed = Color(0xFFEF4444)
private val ReportsPurple = Color(0xFF8B5CF6)
private val ReportsTeal = Color(0xFF14B8A6)
private val ReportsSlate = Color(0xFF94A3B8)

private val methodColors = listOf(ReportsGreen, ReportsBlue, ReportsAmber, ReportsPurple, ReportsTeal, ReportsSlate)

private fun money(v: Double): String = "Bs. ${String.format(Locale.US, "%,.2f", v)}"

private fun reportShortDate(d: LocalDate): String =
    d.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES")))

private fun reportRangeShortcuts(): List<Pair<String, Pair<LocalDate, LocalDate>>> {
    val today = LocalDate.now()
    return listOf(
        "7 días" to (today.minusDays(6) to today),
        "30 días" to (today.minusDays(29) to today),
        "Este mes" to (today.withDayOfMonth(1) to today),
        "3 meses" to (today.minusMonths(2).withDayOfMonth(1) to today),
        "Todo" to (LocalDate.of(2000, 1, 1) to today),
    )
}

@Composable
fun ReportsContent(
    overview: ReportsOverview,
    startDate: LocalDate,
    endDate: LocalDate,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit,
    presets: List<SavedReportRange>,
    onSavePreset: (String) -> Unit,
    onRemovePreset: (String) -> Unit,
    onExportClick: () -> Unit,
    onHtmlClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(AppSpacing.lg)
                .onPreviewKeyEvent { ev ->
                    if (ev.type != KeyEventType.KeyDown || !ev.isAltPressed) return@onPreviewKeyEvent false
                    val index = ev.key.numberIndex()
                    if (index < 0) return@onPreviewKeyEvent false
                    val target =
                        if (ev.isShiftPressed) {
                            presets.getOrNull(index)
                        } else {
                            reportRangeShortcuts().getOrNull(index)?.second?.let { (s, e) -> SavedReportRange("", s, e) }
                        }
                        ?: return@onPreviewKeyEvent false
                    onStartDateChange(target.startDate)
                    onEndDateChange(target.endDate)
                    true
                },
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        ReportsHeader(onExportClick = onExportClick, onHtmlClick = onHtmlClick)

        ReportsRangeBar(
            startDate = startDate,
            endDate = endDate,
            onStartDateChange = onStartDateChange,
            onEndDateChange = onEndDateChange,
            presets = presets,
            onSavePreset = onSavePreset,
            onRemovePreset = onRemovePreset,
        )

        ReportsKpiRow(overview)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            AppCard(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(AppSpacing.lg),
            ) {
                EnterpriseLineChart(
                    title = "Ingresos por día",
                    points =
                        overview.dailyRevenue.map { point ->
                            LinePoint(reportShortDate(point.date), point.revenue.toFloat())
                        },
                    chartHeight = 190.dp,
                )
            }
            AppCard(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(AppSpacing.lg),
            ) {
                EnterpriseBarChart(
                    title = "Citas por día",
                    entries =
                        overview.appointmentsPerDay.map { point ->
                            BarChartEntry(reportShortDate(point.date), point.count.toFloat(), ReportsBlue)
                        },
                    chartHeight = 190.dp,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            AppCard(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(AppSpacing.lg),
            ) {
                val methodSegments =
                    overview.revenueByMethod.mapIndexed { index, slice ->
                        DonutSegment(slice.method, slice.revenue.toFloat(), methodColors[index % methodColors.size])
                    }
                if (methodSegments.isEmpty()) {
                    ReportsEmpty("Sin pagos en el periodo.")
                } else {
                    EnterpriseDonutChart(title = "Ingresos por método", segments = methodSegments, chartDiameter = 160.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs), modifier = Modifier.padding(top = AppSpacing.sm)) {
                        overview.revenueByMethod.forEachIndexed { index, slice ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Box(
                                        Modifier.size(10.dp).background(methodColors[index % methodColors.size]),
                                    )
                                    Text(
                                        "${slice.method} · ${slice.count}",
                                        style = AppTypography.BodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Text(
                                    money(slice.revenue),
                                    style = AppTypography.BodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }
            AppCard(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(AppSpacing.lg),
            ) {
                AppointmentsByStatus(overview)
            }
            AppCard(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(AppSpacing.lg),
            ) {
                TopProcedures(overview)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            AppCard(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(AppSpacing.lg),
            ) {
                if (overview.patientsPerMonth.isEmpty()) {
                    ReportsEmpty("Sin pacientes nuevos en el periodo.")
                } else {
                    EnterpriseBarChart(
                        title = "Pacientes nuevos por mes",
                        entries =
                            overview.patientsPerMonth.map { point ->
                                BarChartEntry(shortMonthLabel(point.month), point.count.toFloat(), ReportsGreen)
                            },
                        chartHeight = 190.dp,
                    )
                }
            }
            AppCard(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(AppSpacing.lg),
            ) {
                TopDoctors(overview)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            AppCard(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(AppSpacing.lg),
            ) {
                RevenueByCategory(overview)
            }
            AppCard(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(AppSpacing.lg),
            ) {
                RevenueVsCatalog(overview)
            }
            AppCard(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(AppSpacing.lg),
            ) {
                StockMovementsCard(overview)
            }
        }
    }
}

@Composable
private fun ReportsHeader(onExportClick: () -> Unit, onHtmlClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(
                "Reportes",
                style = AppTypography.PageTitle,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Analítica de ingreso y agendamiento con datos reales.",
                style = AppTypography.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            AppOutlinedButton(
                text = "HTML / Imprimir",
                onClick = onHtmlClick,
                minHeight = 44.dp,
                leadingIcon = {
                    Icon(Icons.Default.Print, null, Modifier.size(18.dp))
                },
            )
            AppOutlinedButton(
                text = "Exportar CSV",
                onClick = onExportClick,
                minHeight = 44.dp,
                leadingIcon = {
                    Icon(Icons.Default.Download, null, Modifier.size(18.dp))
                },
            )
        }
    }
}

@Composable
private fun ReportsRangeBar(
    startDate: LocalDate,
    endDate: LocalDate,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit,
    presets: List<SavedReportRange>,
    onSavePreset: (String) -> Unit,
    onRemovePreset: (String) -> Unit,
) {
    val shortcuts = remember { reportRangeShortcuts() }
    val selectable = rememberPastOrTodaySelectableDates()
    var showSaveDialog by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            shortcuts.forEach { (label, range) ->
                FilterChip(
                    selected = startDate == range.first && endDate == range.second,
                    onClick = {
                        onStartDateChange(range.first)
                        onEndDateChange(range.second)
                    },
                    label = { Text(label) },
                )
            }
            presets.forEach { preset ->
                FilterChip(
                    selected = startDate == preset.startDate && endDate == preset.endDate,
                    onClick = {
                        onStartDateChange(preset.startDate)
                        onEndDateChange(preset.endDate)
                    },
                    label = { Text(preset.name) },
                    trailingIcon = {
                        IconButton(
                            onClick = { onRemovePreset(preset.name) },
                            modifier = Modifier.size(22.dp),
                        ) {
                            Icon(Icons.Default.Close, "Eliminar rango ${preset.name}", Modifier.size(14.dp))
                        }
                    },
                )
            }
            FilterChip(
                selected = false,
                onClick = { showSaveDialog = true },
                leadingIcon = { Icon(Icons.Default.Add, null, Modifier.size(18.dp)) },
                label = { Text("Guardar rango") },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppDatePickerField(
                label = "Desde",
                value = startDate,
                onValueChange = onStartDateChange,
                modifier = Modifier.widthIn(max = 220.dp),
                selectableDates = selectable,
                shortcuts = listOf(
                    DatePickerShortcut("Hoy", LocalDate.now()),
                    DatePickerShortcut("Hace 30 días", LocalDate.now().minusDays(29)),
                ),
            )
            AppDatePickerField(
                label = "Hasta",
                value = endDate,
                onValueChange = onEndDateChange,
                modifier = Modifier.widthIn(max = 220.dp),
                selectableDates = selectable,
                shortcuts = listOf(
                    DatePickerShortcut("Hoy", LocalDate.now()),
                    DatePickerShortcut("Hace 30 días", LocalDate.now().minusDays(29)),
                ),
            )
            Text(
                "del ${reportShortDate(startDate)} al ${reportShortDate(endDate)}",
                style = AppTypography.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            "Atajos: Alt+1…5 = rango rápido · Alt+Mayús+1…9 = rango guardado",
            style = AppTypography.Caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (showSaveDialog) {
        SaveRangeDialog(
            startDate = startDate,
            endDate = endDate,
            onConfirm = { name ->
                onSavePreset(name)
                showSaveDialog = false
            },
            onDismiss = { showSaveDialog = false },
        )
    }
}

@Composable
private fun SaveRangeDialog(
    startDate: LocalDate,
    endDate: LocalDate,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AppBasicDialog(
        title = "Guardar rango de fechas",
        onDismissRequest = onDismiss,
        message = "Rango: ${reportShortDate(startDate)} al ${reportShortDate(endDate)}",
        dismissText = "Cancelar",
        onDismiss = onDismiss,
        confirmText = "Guardar",
        onConfirm = {
            val trimmed = name.trim()
            if (trimmed.isNotEmpty()) {
                onConfirm(trimmed)
            }
        },
    ) {
        AppTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nombre del rango",
            placeholder = "Ej.: Julio 2026, Q3…",
        )
    }
}

/** Índice 0-based del dígito principal (Alt+1 → 0), o -1 si la tecla no es un número. */
private fun Key.numberIndex(): Int =
    when (this) {
        Key.One -> 0
        Key.Two -> 1
        Key.Three -> 2
        Key.Four -> 3
        Key.Five -> 4
        Key.Six -> 5
        Key.Seven -> 6
        Key.Eight -> 7
        Key.Nine -> 8
        else -> -1
    }

@Composable
private fun ReportsKpiRow(overview: ReportsOverview) {
    val avg = if (overview.paymentCount > 0) overview.totalRevenue / overview.paymentCount else 0.0
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        MetricCard(
            label = "Ingresos del periodo",
            value = money(overview.totalRevenue),
            secondaryLabel = "${overview.paymentCount} pagos registrados",
            modifier = Modifier.weight(1f),
        )
        MetricCard(
            label = "Citas en el periodo",
            value = overview.appointmentCount.toString(),
            secondaryLabel = "${overview.completedCount} completadas · ${overview.cancelledCount} canceladas",
            modifier = Modifier.weight(1f),
        )
        MetricCard(
            label = "Nuevos pacientes",
            value = overview.newPatientsCount.toString(),
            secondaryLabel = "registrados en el rango",
            modifier = Modifier.weight(1f),
        )
        MetricCard(
            label = "Promedio por pago",
            value = money(avg),
            secondaryLabel = "ingreso / pago",
            modifier = Modifier.weight(1f),
        )
        MetricCard(
            label = "Tasa de inasistencia",
            value = noShowRate(overview),
            secondaryLabel = "${overview.noShowCount} de ${overview.appointmentCount} citas sin asistir",
            modifier = Modifier.weight(1f),
        )
    }
}

private fun noShowRate(overview: ReportsOverview): String {
    val rate =
        if (overview.appointmentCount > 0) overview.noShowCount.toDouble() / overview.appointmentCount * 100.0 else 0.0
    return String.format(Locale.US, "%.1f%%", rate)
}

@Composable
private fun statusColor(status: AppointmentStatus): Color =
    when (status) {
        AppointmentStatus.COMPLETED -> ReportsGreen
        AppointmentStatus.CONFIRMED -> ReportsBlue
        AppointmentStatus.SCHEDULED -> ReportsAmber
        AppointmentStatus.IN_PROGRESS -> ReportsPurple
        AppointmentStatus.RESCHEDULED -> ReportsTeal
        AppointmentStatus.CANCELLED -> ReportsRed
        AppointmentStatus.NO_SHOW -> ReportsSlate
    }

@Composable
private fun AppointmentsByStatus(overview: ReportsOverview) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("Citas por estado", style = AppTypography.CardTitle, color = MaterialTheme.colorScheme.onSurface)
        if (overview.appointmentByStatus.isEmpty()) {
            ReportsEmpty("Sin citas en el periodo.")
            return
        }
        val max = overview.appointmentByStatus.maxOf { it.count }.coerceAtLeast(1)
        overview.appointmentByStatus.forEach { slice ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        slice.status.displayLabel,
                        style = AppTypography.BodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        slice.count.toString(),
                        style = AppTypography.BodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth(slice.count.toFloat() / max)
                                .height(6.dp)
                                .background(statusColor(slice.status)),
                    )
                }
            }
        }
    }
}

@Composable
private fun TopProcedures(overview: ReportsOverview) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("Procedimientos top", style = AppTypography.CardTitle, color = MaterialTheme.colorScheme.onSurface)
        if (overview.topProcedures.isEmpty()) {
            ReportsEmpty("Sin procedimientos facturados en el periodo.")
            return
        }
        overview.topProcedures.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    row.name,
                    style = AppTypography.BodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${row.count} · ${money(row.revenue)}",
                    style = AppTypography.BodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TopDoctors(overview: ReportsOverview) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("Ingresos por doctor", style = AppTypography.CardTitle, color = MaterialTheme.colorScheme.onSurface)
        if (overview.topDoctors.isEmpty()) {
            ReportsEmpty("Sin pagos asociados a doctores en el periodo.")
            return
        }
        val max = overview.topDoctors.maxOf { it.revenue }.coerceAtLeast(1.0)
        overview.topDoctors.forEachIndexed { index, row ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        row.doctorName,
                        style = AppTypography.BodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "${row.count} · ${money(row.revenue)}",
                        style = AppTypography.BodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth((row.revenue / max).toFloat())
                                .height(6.dp)
                                .background(
                                    if (row.doctorName == "Sin asignar") {
                                        ReportsSlate
                                    } else {
                                        methodColors[index % methodColors.size]
                                    },
                                ),
                    )
                }
            }
        }
    }
}

@Composable
private fun RevenueByCategory(overview: ReportsOverview) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        if (overview.revenueByCategory.isEmpty()) {
            Text("Ingresos por categoría", style = AppTypography.CardTitle, color = MaterialTheme.colorScheme.onSurface)
            ReportsEmpty("Sin pagos con categoría en el periodo.")
            return
        }
        val segments =
            overview.revenueByCategory.mapIndexed { index, slice ->
                DonutSegment(slice.category, slice.revenue.toFloat(), methodColors[index % methodColors.size])
            }
        EnterpriseDonutChart(title = "Ingresos por categoría", segments = segments, chartDiameter = 140.dp)
        overview.revenueByCategory.forEachIndexed { index, slice ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        Modifier.size(10.dp).background(methodColors[index % methodColors.size]),
                    )
                    Text(
                        "${slice.category} · ${slice.count}",
                        style = AppTypography.BodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    money(slice.revenue),
                    style = AppTypography.BodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun RevenueVsCatalog(overview: ReportsOverview) {
    val catalog = overview.revenueVsCatalog
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("Ingresos vs catálogo", style = AppTypography.CardTitle, color = MaterialTheme.colorScheme.onSurface)
        if (catalog.actionCount == 0) {
            ReportsEmpty("Sin acciones realizadas en el periodo.")
            return
        }
        MetricCard(
            label = "Valor de catálogo",
            value = money(catalog.totalCatalog),
            secondaryLabel = "${catalog.actionCount} acciones realizadas",
        )
        MetricCard(
            label = "Total cobrado",
            value = money(catalog.totalCharged),
            secondaryLabel = "promedio ${money(catalog.totalCharged / catalog.actionCount)} por acción",
        )
        MetricCard(
            label = "Descuento aplicado",
            value = "${money(catalog.discount)} · ${String.format(Locale.US, "%.1f%%", catalog.discountRate)}",
            secondaryLabel = "sobre el precio de catálogo",
        )
    }
}

@Composable
private fun StockMovementsCard(overview: ReportsOverview) {
    val stock = overview.stockMovements
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("Movimientos de stock", style = AppTypography.CardTitle, color = MaterialTheme.colorScheme.onSurface)
        if (stock.movementCount == 0) {
            ReportsEmpty("Sin movimientos de inventario en el periodo.")
            return
        }
        MetricCard(
            label = "Movimientos",
            value = stock.movementCount.toString(),
            secondaryLabel = "${stock.unitsIn} unidades entradas · ${stock.unitsOut} salidas",
        )
        if (stock.byType.isEmpty()) {
            return
        }
        val max = stock.byType.maxOf { it.count }.coerceAtLeast(1)
        stock.byType.forEach { row ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        row.label,
                        style = AppTypography.BodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "${row.count} · +${row.unitsIn}/-${row.unitsOut}",
                        style = AppTypography.BodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth(row.count.toFloat() / max)
                                .height(6.dp)
                                .background(ReportsTeal),
                    )
                }
            }
        }
    }
}

private fun shortMonthLabel(month: String): String =
    runCatching {
        YearMonth.parse(month).atDay(1).format(DateTimeFormatter.ofPattern("MMM yyyy", Locale("es", "ES")))
    }.getOrDefault(month)

@Composable
private fun ReportsEmpty(message: String) {
    Text(
        message,
        style = AppTypography.BodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
