@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.payments

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.CheckCircle
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
import com.denticode.kt.data.PaymentDisplayStatus
import com.denticode.kt.data.PaymentMethod
import com.denticode.kt.ui.appointments.PatientAvatar
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.inputs.AppDatePickerField
import com.denticode.kt.ui.components.inputs.AppSearchField
import com.denticode.kt.ui.components.inputs.defaultPaymentDateShortcuts
import com.denticode.kt.ui.components.inputs.rememberPastOrTodaySelectableDates
import com.denticode.kt.ui.formatMoney
import com.denticode.kt.ui.patients.PatientFilterDropdown
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import java.time.LocalDate

@Composable
fun PaymentsPageHeader(
    onNewPaymentClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Pagos",
                style = AppTypography.PageTitle,
                fontWeight = FontWeight.Bold,
                color = PatientsPremiumPalette.textPrimary,
            )
            Text(
                "Gestiona los cobros y pagos realizados en la clínica.",
                style = AppTypography.Body,
                color = PatientsPremiumPalette.textSecondary,
            )
        }
        AppButton(
            text = "Nuevo pago",
            onClick = onNewPaymentClick,
            minHeight = 44.dp,
            shape = RoundedCornerShape(999.dp),
            leadingIcon = {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
            },
        )
    }
}

@Composable
fun PaymentsKpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val scale by animateFloatAsState(if (hovered) 1.02f else 1f, tween(180), label = "payKpi")
    val elevation by animateDpAsState(if (hovered) AppElevations.cardHovered else AppElevations.low, tween(180), label = "payKpiElev")
    Surface(
        modifier =
            modifier
                .scale(scale)
                .hoverable(interaction),
        shape = AppShapes.medium,
        color = PatientsPremiumPalette.card,
        shadowElevation = elevation,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
                Text(
                    value,
                    style = AppTypography.MetricMedium,
                    fontWeight = FontWeight.Bold,
                    color = PatientsPremiumPalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun PaymentsKpiRow(
    totalCollected: Double,
    completedCount: Int,
    pendingCount: Int,
    averageAmount: Double,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PaymentsKpiCard(
            title = "Total cobrado",
            value = "Bs ${formatMoney(totalCollected)}",
            icon = Icons.Default.AccountBalanceWallet,
            iconBackground = PatientsPremiumPalette.info.copy(alpha = 0.12f),
            iconTint = PatientsPremiumPalette.info,
            modifier = Modifier.weight(1f),
        )
        PaymentsKpiCard(
            title = "Pagos completos",
            value = completedCount.toString(),
            icon = Icons.Outlined.CheckCircle,
            iconBackground = PatientsPremiumPalette.success.copy(alpha = 0.12f),
            iconTint = PatientsPremiumPalette.success,
            modifier = Modifier.weight(1f),
        )
        PaymentsKpiCard(
            title = "Pendientes",
            value = pendingCount.toString(),
            icon = Icons.Default.Schedule,
            iconBackground = PatientsPremiumPalette.warning.copy(alpha = 0.14f),
            iconTint = PatientsPremiumPalette.warning,
            modifier = Modifier.weight(1f),
        )
        PaymentsKpiCard(
            title = "Promedio por pago",
            value = "Bs ${formatMoney(averageAmount)}",
            icon = Icons.Default.ReceiptLong,
            iconBackground = PatientsPremiumPalette.primary.copy(alpha = 0.12f),
            iconTint = PatientsPremiumPalette.primary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun PaymentsFilterToolbar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    methodLabel: String,
    methodOptions: List<String>,
    onMethodSelect: (String) -> Unit,
    statusLabel: String,
    statusOptions: List<String>,
    onStatusSelect: (String) -> Unit,
    filterDate: LocalDate?,
    onFilterDateChange: (LocalDate) -> Unit,
    onClearDateFilter: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectableDates = rememberPastOrTodaySelectableDates()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = PatientsPremiumPalette.card,
        shadowElevation = AppElevations.low,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppSearchField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1.2f),
                placeholder = "Buscar por paciente, detalle o ID…",
                keyboardShortcutHint = null,
            )
            PatientFilterDropdown(
                label = "Método",
                displayValue = methodLabel,
                options = methodOptions,
                onSelect = onMethodSelect,
            )
            PatientFilterDropdown(
                label = "Estado",
                displayValue = statusLabel,
                options = statusOptions,
                onSelect = onStatusSelect,
            )
            Box(modifier = Modifier.widthIn(min = 160.dp, max = 220.dp)) {
                AppDatePickerField(
                    label = "Fecha",
                    value = filterDate ?: LocalDate.now(),
                    onValueChange = onFilterDateChange,
                    selectableDates = selectableDates,
                    shortcuts = defaultPaymentDateShortcuts(),
                )
            }
            if (filterDate != null) {
                AppOutlinedButton(text = "Todas", onClick = onClearDateFilter)
            }
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
fun PaymentStatusBadge(
    status: PaymentDisplayStatus,
    modifier: Modifier = Modifier,
) {
    val (bg, fg, dot) =
        when (status) {
            PaymentDisplayStatus.PAID ->
                Triple(
                    PatientsPremiumPalette.success.copy(alpha = 0.14f),
                    PatientsPremiumPalette.success,
                    PatientsPremiumPalette.success,
                )
            PaymentDisplayStatus.PENDING ->
                Triple(
                    PatientsPremiumPalette.warning.copy(alpha = 0.16f),
                    Color(0xFFB45309),
                    PatientsPremiumPalette.warning,
                )
            PaymentDisplayStatus.FAILED ->
                Triple(
                    PatientsPremiumPalette.error.copy(alpha = 0.14f),
                    PatientsPremiumPalette.error,
                    PatientsPremiumPalette.error,
                )
        }
    Surface(modifier = modifier, shape = RoundedCornerShape(999.dp), color = bg) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(dot))
            Text(status.labelEs, style = AppTypography.Caption, fontWeight = FontWeight.SemiBold, color = fg)
        }
    }
}

@Composable
private fun PaymentMethodCell(
    methodLabel: String,
    method: PaymentMethod?,
    modifier: Modifier = Modifier,
) {
    val icon =
        when (method) {
            PaymentMethod.CARD -> Icons.Default.CreditCard
            PaymentMethod.CASH -> Icons.Default.Payments
            PaymentMethod.TRANSFER -> Icons.Default.AccountBalanceWallet
            else -> Icons.Default.Payments
        }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, null, Modifier.size(16.dp), tint = PatientsPremiumPalette.textSecondary)
        Text(
            methodLabel,
            style = AppTypography.BodySmall,
            color = PatientsPremiumPalette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun PaymentTableHeader(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(PatientsPremiumPalette.background)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PaymentHeaderCell("PACIENTE", Modifier.weight(1.35f))
        PaymentHeaderCell("DETALLE", Modifier.weight(1f))
        PaymentHeaderCell("MÉTODO", Modifier.weight(1f))
        PaymentHeaderCell("MONTO", Modifier.weight(0.75f))
        PaymentHeaderCell("ESTADO", Modifier.width(100.dp))
        PaymentHeaderCell("FECHA / HORA", Modifier.weight(1f))
        PaymentHeaderCell("ACCIÓN", Modifier.width(56.dp))
    }
}

@Composable
private fun PaymentHeaderCell(
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
fun PaymentTableRow(
    payment: PaymentUiModel,
    selected: Boolean,
    onClick: () -> Unit,
    onViewDetail: () -> Unit,
    onViewReceipt: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember(payment.id) { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg by animateColorAsState(
        when {
            selected -> PatientsPremiumPalette.primary.copy(alpha = 0.08f)
            hovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            else -> Color.Transparent
        },
        tween(140),
        label = "payRowBg",
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .clickable(interactionSource = interaction, indication = null, onClick = onClick)
                    .hoverable(interaction)
                    .padding(horizontal = AppSpacing.lg, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier.weight(1.35f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                PatientAvatar(payment.patientName, size = 36.dp)
                Column {
                    Text(
                        payment.patientName,
                        style = AppTypography.Body,
                        fontWeight = FontWeight.SemiBold,
                        color = PatientsPremiumPalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "ID ${payment.patientId}",
                        style = AppTypography.Caption,
                        color = PatientsPremiumPalette.textSecondary,
                    )
                }
            }
            Text(
                payment.detailLabel,
                Modifier.weight(1f),
                style = AppTypography.BodySmall,
                color = PatientsPremiumPalette.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            PaymentMethodCell(
                methodLabel = payment.methodLabel,
                method = payment.method,
                modifier = Modifier.weight(1f),
            )
            Text(
                payment.amountLabel,
                Modifier.weight(0.75f),
                style = AppTypography.Body,
                fontWeight = FontWeight.Bold,
                color = PatientsPremiumPalette.textPrimary,
            )
            Box(Modifier.width(100.dp)) {
                PaymentStatusBadge(payment.status)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(payment.dateLabel, style = AppTypography.BodySmall, fontWeight = FontWeight.Medium, color = PatientsPremiumPalette.textPrimary)
                Text(payment.timeLabel, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
            }
            Box(Modifier.width(56.dp), contentAlignment = Alignment.Center) {
                PaymentActionMenu(onViewDetail = onViewDetail, onViewReceipt = onViewReceipt)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun PaymentActionMenu(
    onViewDetail: () -> Unit,
    onViewReceipt: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, "Acciones", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Ver paciente") },
                onClick = {
                    expanded = false
                    onViewDetail()
                },
            )
            DropdownMenuItem(
                text = { Text("Ver recibo") },
                onClick = {
                    expanded = false
                    onViewReceipt()
                },
            )
        }
    }
}

@Composable
fun PaymentTable(
    payments: List<PaymentUiModel>,
    selectedId: Int?,
    onSelect: (PaymentUiModel) -> Unit,
    onViewDetail: (PaymentUiModel) -> Unit,
    onViewReceipt: (PaymentUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().fillMaxHeight(),
        shape = AppShapes.medium,
        color = PatientsPremiumPalette.card,
        shadowElevation = AppElevations.cardRest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "header") { PaymentTableHeader() }
            items(payments, key = { it.id }) { p ->
                PaymentTableRow(
                    payment = p,
                    selected = p.id == selectedId,
                    onClick = { onSelect(p) },
                    onViewDetail = { onViewDetail(p) },
                    onViewReceipt = { onViewReceipt(p) },
                )
            }
        }
    }
}

@Composable
fun PaymentsSummaryChip(
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = tint.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = 0.25f)),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = AppTypography.Caption,
            fontWeight = FontWeight.SemiBold,
            color = tint,
        )
    }
}

@Composable
fun PaymentsFooterBar(
    rangeStart: Int,
    rangeEnd: Int,
    totalItems: Int,
    totalCollected: Double,
    pendingTotal: Double,
    currentPage: Int,
    totalPages: Int,
    pageSize: Int,
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
        shadowElevation = AppElevations.hairline,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Mostrando $rangeStart a $rangeEnd de $totalItems pagos",
                style = AppTypography.BodySmall,
                color = PatientsPremiumPalette.textSecondary,
            )
            PaymentsSummaryChip(
                label = "Total cobrado: Bs ${formatMoney(totalCollected)}",
                tint = PatientsPremiumPalette.success,
            )
            PaymentsSummaryChip(
                label = "Pendiente: Bs ${formatMoney(pendingTotal)}",
                tint = PatientsPremiumPalette.warning,
            )
            Box(modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                PaymentPageNavButton(enabled = currentPage > 1, onClick = { onPageChange(currentPage - 1) }) {
                    Icon(Icons.Default.ChevronLeft, "Anterior", Modifier.size(20.dp))
                }
                paymentPageWindow(currentPage, totalPages).forEach { p ->
                    if (p == null) {
                        Text("…", Modifier.padding(horizontal = 6.dp), color = PatientsPremiumPalette.textSecondary)
                    } else {
                        PaymentPageNumberChip(page = p, selected = p == currentPage, onClick = { onPageChange(p) })
                    }
                }
                PaymentPageNavButton(enabled = currentPage < totalPages, onClick = { onPageChange(currentPage + 1) }) {
                    Icon(Icons.Default.ChevronRight, "Siguiente", Modifier.size(20.dp))
                }
            }
            Box {
                AppOutlinedButton(text = "Filas: $pageSize", onClick = { pageSizeMenu = true })
                DropdownMenu(expanded = pageSizeMenu, onDismissRequest = { pageSizeMenu = false }) {
                    pageSizeOptions.forEach { size ->
                        DropdownMenuItem(
                            text = { Text("$size filas por página") },
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
private fun PaymentPageNavButton(
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
        Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
private fun PaymentPageNumberChip(
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

private fun paymentPageWindow(current: Int, total: Int): List<Int?> {
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

@Composable
fun EmptyPaymentsState(
    hasFilters: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(Icons.Default.Payments, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        Text(
            if (hasFilters) "Ningún pago coincide con los filtros" else "Aún no hay pagos registrados",
            style = AppTypography.Body,
            fontWeight = FontWeight.Medium,
            color = PatientsPremiumPalette.textPrimary,
        )
        Text(
            if (hasFilters) "Pruebe a ampliar la búsqueda o quitar filtros." else "Use «Nuevo pago» para registrar el primero.",
            style = AppTypography.BodySmall,
            color = PatientsPremiumPalette.textSecondary,
        )
    }
}
