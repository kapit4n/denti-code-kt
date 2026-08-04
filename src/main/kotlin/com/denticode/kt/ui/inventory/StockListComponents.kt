@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.inventory

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
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
import com.denticode.kt.data.InventoryDirectoryKpis
import com.denticode.kt.data.StockStatus
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.theme.AppElevations
import com.denticode.kt.ui.components.inputs.AppSearchField
import com.denticode.kt.ui.patients.PatientFilterDropdown
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

private val StockBackground = Color(0xFFF8FAFC)
private val StockSuccess = Color(0xFF22C55E)
private val StockWarning = Color(0xFFF59E0B)
private val StockDanger = Color(0xFFEF4444)

@Composable
fun StockPageHeader(
    onNewItemClick: () -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Stock de insumos",
                style = AppTypography.PageTitle,
                fontWeight = FontWeight.Bold,
                color = PatientsPremiumPalette.textPrimary,
            )
            Text(
                "Gestiona el inventario de materiales e insumos por consultorio.",
                style = AppTypography.Body,
                color = PatientsPremiumPalette.textSecondary,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            AppOutlinedButton(
                text = "Exportar",
                onClick = onExportClick,
                minHeight = 44.dp,
                leadingIcon = {
                    Icon(Icons.Default.Download, null, Modifier.size(18.dp))
                },
            )
            AppButton(
                text = "+ Nuevo insumo",
                onClick = onNewItemClick,
                leadingIcon = {
                    Icon(Icons.Default.Add, null, Modifier.size(20.dp))
                },
            )
        }
    }
}

@Composable
fun StockSummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val scale by animateFloatAsState(if (hovered) 1.02f else 1f, tween(180), label = "stockKpi")
    val elevation by animateDpAsState(if (hovered) AppElevations.cardHovered else AppElevations.low, tween(180), label = "stockKpiElev")
    Surface(
        modifier =
            modifier
                .scale(scale)
                .hoverable(interaction),
        shape = AppShapes.medium,
        color = Color.White,
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
fun StockSummaryRow(
    kpis: InventoryDirectoryKpis,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StockSummaryCard(
            title = "Total insumos",
            value = kpis.totalItems.toString(),
            icon = Icons.Default.Inventory2,
            iconBackground = PatientsPremiumPalette.primary.copy(alpha = 0.12f),
            iconTint = PatientsPremiumPalette.primary,
            modifier = Modifier.weight(1f),
        )
        StockSummaryCard(
            title = "Stock total",
            value = kpis.totalUnits.toString(),
            icon = Icons.Outlined.CheckCircle,
            iconBackground = StockSuccess.copy(alpha = 0.12f),
            iconTint = StockSuccess,
            modifier = Modifier.weight(1f),
        )
        StockSummaryCard(
            title = "Stock bajo",
            value = kpis.lowStockCount.toString(),
            icon = Icons.Outlined.WarningAmber,
            iconBackground = StockWarning.copy(alpha = 0.14f),
            iconTint = StockWarning,
            modifier = Modifier.weight(1f),
        )
        StockSummaryCard(
            title = "Agotados",
            value = kpis.outOfStockCount.toString(),
            icon = Icons.Outlined.ErrorOutline,
            iconBackground = StockDanger.copy(alpha = 0.12f),
            iconTint = StockDanger,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun StockFiltersBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    consultoryLabel: String,
    consultoryOptions: List<String>,
    onConsultorySelect: (String) -> Unit,
    categoryLabel: String,
    categoryOptions: List<String>,
    onCategorySelect: (String) -> Unit,
    statusLabel: String,
    statusOptions: List<String>,
    onStatusSelect: (String) -> Unit,
    lowStockOnly: Boolean,
    onLowStockOnlyChange: (Boolean) -> Unit,
    onFiltersClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppSearchField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1.4f),
                placeholder = "Buscar insumo, categoría o consultorio…",
                keyboardShortcutHint = null,
            )
            PatientFilterDropdown(
                label = "Consultorio",
                displayValue = consultoryLabel,
                options = consultoryOptions,
                onSelect = onConsultorySelect,
                modifier = Modifier.weight(0.9f),
            )
            PatientFilterDropdown(
                label = "Categoría",
                displayValue = categoryLabel,
                options = categoryOptions,
                onSelect = onCategorySelect,
                modifier = Modifier.weight(0.85f),
            )
            PatientFilterDropdown(
                label = "Estado",
                displayValue = statusLabel,
                options = statusOptions,
                onSelect = onStatusSelect,
                modifier = Modifier.weight(0.75f),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.widthIn(min = 120.dp),
            ) {
                Checkbox(checked = lowStockOnly, onCheckedChange = onLowStockOnlyChange)
                Text(
                    "Solo stock bajo",
                    style = AppTypography.Caption,
                    color = PatientsPremiumPalette.textPrimary,
                    maxLines = 1,
                )
            }
            AppOutlinedButton(
                text = "Filtros",
                onClick = onFiltersClick,
                minHeight = 40.dp,
                leadingIcon = {
                    Icon(Icons.Default.FilterList, null, Modifier.size(18.dp))
                },
            )
        }
    }
}

@Composable
fun StockStatusBadge(
    status: StockStatus,
    modifier: Modifier = Modifier,
) {
    val colors =
        when (status) {
            StockStatus.OPTIMAL ->
                AssistChipDefaults.assistChipColors(
                    containerColor = StockSuccess.copy(alpha = 0.14f),
                    labelColor = StockSuccess,
                )
            StockStatus.LOW ->
                AssistChipDefaults.assistChipColors(
                    containerColor = StockWarning.copy(alpha = 0.16f),
                    labelColor = Color(0xFFB45309),
                )
            StockStatus.OUT ->
                AssistChipDefaults.assistChipColors(
                    containerColor = StockDanger.copy(alpha = 0.14f),
                    labelColor = StockDanger,
                )
        }
    AssistChip(
        onClick = {},
        label = {
            Text(status.labelEs, style = AppTypography.Caption, fontWeight = FontWeight.SemiBold)
        },
        modifier = modifier,
        enabled = false,
        colors = colors,
        border = null,
    )
}

@Composable
internal fun categoryColors(categoryKey: String): Pair<Color, Color> =
    when (categoryKey.uppercase()) {
        "RESTORATIVE" -> Color(0xFFDBEAFE) to Color(0xFF2563EB)
        "PPE" -> Color(0xFFE0E7FF) to Color(0xFF4F46E5)
        "INJECTION" -> Color(0xFFFCE7F3) to Color(0xFFDB2777)
        "PREVENTIVE" -> Color(0xFFD1FAE5) to Color(0xFF059669)
        "SURGERY" -> Color(0xFFFFEDD5) to Color(0xFFEA580C)
        else -> MaterialTheme.colorScheme.surfaceVariant to PatientsPremiumPalette.textSecondary
    }

@Composable
fun StockCategoryChip(
    label: String,
    categoryKey: String,
    modifier: Modifier = Modifier,
) {
    val (bg, fg) = categoryColors(categoryKey)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = bg,
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = AppTypography.Caption,
            fontWeight = FontWeight.SemiBold,
            color = fg,
        )
    }
}

private fun stockQuantityColor(status: StockStatus): Color =
    when (status) {
        StockStatus.OPTIMAL -> StockSuccess
        StockStatus.LOW -> StockWarning
        StockStatus.OUT -> StockDanger
    }

@Composable
private fun StockProductThumbnail(
    categoryKey: String,
    modifier: Modifier = Modifier,
) {
    val (bg, fg) = categoryColors(categoryKey)
    val icon =
        when (categoryKey.uppercase()) {
            "INJECTION" -> Icons.Default.Vaccines
            else -> Icons.Default.MedicalServices
        }
    Box(
        modifier =
            modifier
                .size(40.dp)
                .clip(AppShapes.small)
                .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun StockActionsMenu(
    onViewDetail: () -> Unit,
    onEdit: () -> Unit,
    onAdjustStock: () -> Unit,
    onTransfer: () -> Unit,
    onHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, "Acciones", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Ver detalle") }, onClick = { expanded = false; onViewDetail() })
            DropdownMenuItem(text = { Text("Editar") }, onClick = { expanded = false; onEdit() })
            DropdownMenuItem(text = { Text("Ajustar stock") }, onClick = { expanded = false; onAdjustStock() })
            DropdownMenuItem(text = { Text("Transferir") }, onClick = { expanded = false; onTransfer() })
            DropdownMenuItem(text = { Text("Historial movimientos") }, onClick = { expanded = false; onHistory() })
        }
    }
}

@Composable
private fun StockTableHeader(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(StockBackground)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StockHeaderCell("INSUMO", Modifier.weight(1.35f))
        StockHeaderCell("CATEGORÍA", Modifier.weight(0.9f))
        StockHeaderCell("CONSULTORIO", Modifier.weight(1f))
        StockHeaderCell("STOCK ACTUAL", Modifier.width(108.dp))
        StockHeaderCell("STOCK MÍN.", Modifier.width(88.dp))
        StockHeaderCell("ESTADO", Modifier.width(108.dp))
        StockHeaderCell("ÚLT. ACTUALIZACIÓN", Modifier.weight(1f))
        StockHeaderCell("ACCIÓN", Modifier.width(56.dp))
    }
}

@Composable
private fun StockHeaderCell(
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
private fun StockTableRow(
    item: StockUiModel,
    selected: Boolean,
    onClick: () -> Unit,
    onViewDetail: () -> Unit,
    onEdit: () -> Unit,
    onAdjustStock: () -> Unit,
    onTransfer: () -> Unit,
    onHistory: () -> Unit,
) {
    val interaction = remember(item.lineId) { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg by animateColorAsState(
        when {
            selected -> PatientsPremiumPalette.primary.copy(alpha = 0.08f)
            hovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            else -> Color.Transparent
        },
        tween(140),
        label = "stockRowBg",
    )
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
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
                StockProductThumbnail(categoryKey = item.categoryKey)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        item.productName,
                        style = AppTypography.Body,
                        fontWeight = FontWeight.SemiBold,
                        color = PatientsPremiumPalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "Código: ${item.productCode}",
                        style = AppTypography.Caption,
                        color = PatientsPremiumPalette.textSecondary,
                    )
                }
            }
            Box(Modifier.weight(0.9f)) {
                StockCategoryChip(label = item.categoryLabel, categoryKey = item.categoryKey)
            }
            Text(
                item.consultoryLabel,
                Modifier.weight(1f),
                style = AppTypography.BodySmall,
                color = PatientsPremiumPalette.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${item.quantity} ${item.unitLabel}",
                Modifier.width(108.dp),
                style = AppTypography.Body,
                fontWeight = FontWeight.Bold,
                color = stockQuantityColor(item.status),
            )
            Text(
                "${item.minQuantity} ${item.unitLabel}",
                Modifier.width(88.dp),
                style = AppTypography.BodySmall,
                color = PatientsPremiumPalette.textSecondary,
            )
            Box(Modifier.width(108.dp)) {
                StockStatusBadge(status = item.status)
            }
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    "${item.updatedDateLabel} ${item.updatedTimeLabel}",
                    style = AppTypography.BodySmall,
                    color = PatientsPremiumPalette.textPrimary,
                )
                Text(
                    item.updatedByLabel,
                    style = AppTypography.Caption,
                    color = PatientsPremiumPalette.textSecondary,
                )
            }
            Box(Modifier.width(56.dp), contentAlignment = Alignment.Center) {
                StockActionsMenu(
                    onViewDetail = onViewDetail,
                    onEdit = onEdit,
                    onAdjustStock = onAdjustStock,
                    onTransfer = onTransfer,
                    onHistory = onHistory,
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
fun StockTable(
    items: List<StockUiModel>,
    selectedLineId: Int?,
    onSelect: (StockUiModel) -> Unit,
    onViewDetail: (StockUiModel) -> Unit,
    onEdit: (StockUiModel) -> Unit,
    onAdjustStock: (StockUiModel) -> Unit,
    onTransfer: (StockUiModel) -> Unit,
    onHistory: (StockUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth().fillMaxHeight(),
        shape = AppShapes.medium,
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "header") { StockTableHeader() }
            items(items, key = { it.lineId }) { item ->
                StockTableRow(
                    item = item,
                    selected = item.lineId == selectedLineId,
                    onClick = { onSelect(item) },
                    onViewDetail = { onViewDetail(item) },
                    onEdit = { onEdit(item) },
                    onAdjustStock = { onAdjustStock(item) },
                    onTransfer = { onTransfer(item) },
                    onHistory = { onHistory(item) },
                )
            }
        }
    }
}

@Composable
fun StockDetailsPanel(
    item: StockUiModel,
    movements: List<StockMovementUiModel>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxHeight().widthIn(min = 320.dp, max = 380.dp),
        shape = AppShapes.medium,
        color = Color.White,
        shadowElevation = AppElevations.cardRest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Detalle del insumo",
                        style = AppTypography.SectionTitle,
                        fontWeight = FontWeight.SemiBold,
                    )
                    AppOutlinedButton(text = "Cerrar", onClick = onClose, minHeight = 36.dp)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Text("Información del producto", style = AppTypography.CardTitle, fontWeight = FontWeight.SemiBold)
                    DetailLine("Nombre", item.productName)
                    DetailLine("Código", item.productCode)
                    DetailLine("Categoría", item.categoryLabel)
                    DetailLine("Unidad", item.unitLabel)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Text("Inventario", style = AppTypography.CardTitle, fontWeight = FontWeight.SemiBold)
                    DetailLine("Stock actual", "${item.quantity} ${item.unitLabel}")
                    DetailLine("Stock mínimo", "${item.minQuantity} ${item.unitLabel}")
                    DetailLine("Stock máximo", "${item.maxQuantity} ${item.unitLabel}")
                    StockStatusBadge(status = item.status)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Text("Consultorio", style = AppTypography.CardTitle, fontWeight = FontWeight.SemiBold)
                    DetailLine("Ubicación", item.consultoryLabel)
                }
            }
            item {
                Text("Movimientos recientes", style = AppTypography.CardTitle, fontWeight = FontWeight.SemiBold)
            }
            if (movements.isEmpty()) {
                item {
                    Text(
                        "Sin movimientos registrados.",
                        style = AppTypography.BodySmall,
                        color = PatientsPremiumPalette.textSecondary,
                    )
                }
            } else {
                items(movements.take(8), key = { it.dateLabel + it.quantityLabel + it.note }) { mv ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(mv.dateLabel, style = AppTypography.Caption, color = PatientsPremiumPalette.textPrimary)
                            Text(mv.typeLabel, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
                            mv.note?.let {
                                Text(
                                    it,
                                    style = AppTypography.Caption,
                                    color = PatientsPremiumPalette.textSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(mv.quantityLabel, style = AppTypography.BodySmall, fontWeight = FontWeight.SemiBold)
                            mv.balance?.let { b ->
                                Text("Stock: $b", style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
                            }
                            Text(mv.userLabel, style = AppTypography.Caption, color = PatientsPremiumPalette.textSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = AppTypography.BodySmall, color = PatientsPremiumPalette.textSecondary)
        Text(
            value,
            style = AppTypography.BodySmall,
            fontWeight = FontWeight.Medium,
            color = PatientsPremiumPalette.textPrimary,
            modifier = Modifier.widthIn(max = 220.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun EmptyStockState(
    hasFilters: Boolean,
    onCreateFirst: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(
            Icons.Default.Inventory2,
            null,
            Modifier.size(52.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
        )
        Text(
            if (hasFilters) "Ningún insumo coincide con los filtros" else "No hay insumos registrados",
            style = AppTypography.Body,
            fontWeight = FontWeight.Medium,
            color = PatientsPremiumPalette.textPrimary,
        )
        if (!hasFilters) {
            AppButton(
                text = "Crear primer insumo",
                onClick = onCreateFirst,
                leadingIcon = {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                },
                modifier = Modifier.padding(top = AppSpacing.sm),
            )
        }
    }
}

@Composable
fun StockFooterBar(
    rangeStart: Int,
    rangeEnd: Int,
    totalItems: Int,
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
        color = Color.White,
        shadowElevation = AppElevations.low,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                if (totalItems == 0) {
                    "Mostrando 0 insumos"
                } else {
                    "Mostrando $rangeStart–$rangeEnd de $totalItems insumos"
                },
                style = AppTypography.BodySmall,
                color = PatientsPremiumPalette.textSecondary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                StockPageNavButton(enabled = currentPage > 1, onClick = { onPageChange(currentPage - 1) }) {
                    Icon(Icons.Default.ChevronLeft, "Anterior", Modifier.size(20.dp))
                }
                stockPageWindow(currentPage, totalPages).forEach { p ->
                    if (p == null) {
                        Text("…", Modifier.padding(horizontal = 6.dp), color = PatientsPremiumPalette.textSecondary)
                    } else {
                        StockPageNumberChip(page = p, selected = p == currentPage, onClick = { onPageChange(p) })
                    }
                }
                StockPageNavButton(enabled = currentPage < totalPages, onClick = { onPageChange(currentPage + 1) }) {
                    Icon(Icons.Default.ChevronRight, "Siguiente", Modifier.size(20.dp))
                }
            }
            Box {
                AppOutlinedButton(text = "$pageSize / pág.", onClick = { pageSizeMenu = true })
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
private fun StockPageNavButton(
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
private fun StockPageNumberChip(
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

private fun stockPageWindow(current: Int, total: Int): List<Int?> {
    if (total <= 7) return (1..total).toList()
    val pages = linkedSetOf<Int>()
    pages.add(1)
    pages.add(total)
    for (p in (current - 1)..(current + 1)) {
        if (p in 2 until total) pages.add(p)
    }
    val sorted = pages.sorted()
    val result = mutableListOf<Int?>()
    var prev = 0
    for (p in sorted) {
        if (prev != 0 && p - prev > 1) result.add(null)
        result.add(p)
        prev = p
    }
    return result
}
