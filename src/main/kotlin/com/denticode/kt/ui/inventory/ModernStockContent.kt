package com.denticode.kt.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.InventoryLineRow
import com.denticode.kt.data.InventoryMovementRow
import com.denticode.kt.data.StockStatus
import com.denticode.kt.ui.app.LocalAppMessenger

private val StockBackground = Color(0xFFF8FAFC)

@Composable
fun ModernStockContent(
    inventoryLines: List<InventoryLineRow>,
    movements: List<InventoryMovementRow>,
    onNewItemClick: () -> Unit,
    onExportClick: () -> Unit,
    onSuppliersClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onEdit: (StockUiModel) -> Unit,
    onAdjustStock: (StockUiModel) -> Unit,
    onTransfer: (StockUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val messenger = LocalAppMessenger.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedConsultory by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<StockStatus?>(null) }
    var lowStockOnly by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(1) }
    var pageSize by remember { mutableIntStateOf(10) }
    var selectedLineId by remember { mutableStateOf<Int?>(null) }

    val baseKpis =
        remember(inventoryLines) {
            com.denticode.kt.data.InventoryDirectoryKpis(
                totalItems = inventoryLines.count { it.isActive },
                totalUnits = inventoryLines.sumOf { it.quantity },
                lowStockCount =
                    inventoryLines.count {
                        com.denticode.kt.data.resolveInventoryStockStatus(it.quantity, it.minQuantity) ==
                            StockStatus.LOW
                    },
                outOfStockCount = inventoryLines.count { it.quantity <= 0 },
            )
        }

    val consultoryOptions =
        remember(inventoryLines) {
            listOf("Todos los consultorios") +
                inventoryLines
                    .map { line ->
                        buildString {
                            append(line.consultoryName)
                            line.consultoryShortCode?.let { append(" ($it)") }
                        }
                    }
                    .distinct()
                    .sorted()
        }
    val categoryOptions =
        remember(inventoryLines) {
            listOf("Todas las categorías") +
                inventoryLines
                    .map { categoryLabelEs(it.categoryKey) }
                    .distinct()
                    .sorted()
        }
    val statusOptions =
        remember {
            listOf("Todos") + StockStatus.entries.map { it.labelEs }
        }

    val uiState =
        remember(
            inventoryLines,
            baseKpis,
            searchQuery,
            selectedConsultory,
            selectedCategory,
            selectedStatus,
            lowStockOnly,
            currentPage,
            pageSize,
        ) {
            buildStockUiState(
                lines = inventoryLines,
                kpis = baseKpis,
                searchQuery = searchQuery,
                selectedConsultory = selectedConsultory,
                selectedCategory = selectedCategory,
                selectedStatus = selectedStatus,
                lowStockOnly = lowStockOnly,
                currentPage = currentPage,
                pageSize = pageSize,
            )
        }

    val insights = remember(inventoryLines, movements) { buildStockInsights(inventoryLines, movements) }
    val categoryStats = remember(inventoryLines) { buildStockCategoryStats(inventoryLines) }

    val pageItems = remember(uiState) { paginatedStockItems(uiState) }
    val totalPages = remember(uiState) { totalStockPages(uiState) }
    val selectedItem = remember(uiState, selectedLineId) { uiState.items.find { it.lineId == selectedLineId } }
    val selectedMovements =
        remember(selectedItem, movements) {
            selectedItem?.let { item ->
                val rows =
                    movements
                        .filter { it.consultoryId == item.row.consultoryId && it.facilityId == item.row.facilityId }
                        .sortedByDescending { it.createdAtEpochMs }
                var balance = item.row.quantity
                rows.map { m ->
                    val before = balance
                    balance -= m.quantityChange
                    m.toUiModel(balance = before)
                }
            }.orEmpty()
        }

    val rangeStart =
        if (uiState.totalItems == 0) {
            0
        } else {
            (uiState.currentPage - 1) * uiState.pageSize + 1
        }
    val rangeEnd = (rangeStart + pageItems.size - 1).coerceAtLeast(0)
    val hasFilters =
        searchQuery.isNotBlank() ||
            selectedConsultory != null ||
            selectedCategory != null ||
            selectedStatus != null ||
            lowStockOnly

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(StockBackground)
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StockPageHeader(
            onNewItemClick = onNewItemClick,
            onExportClick = onExportClick,
            onSuppliersClick = onSuppliersClick,
            onOrdersClick = onOrdersClick,
        )
        StockSummaryRow(kpis = uiState.kpis)
        StockInsightsRow(insights = insights)
        StockFiltersBar(
            searchQuery = searchQuery,
            onSearchChange = {
                searchQuery = it
                currentPage = 1
            },
            consultoryLabel = selectedConsultory ?: "Todos los consultorios",
            consultoryOptions = consultoryOptions,
            onConsultorySelect = { label ->
                selectedConsultory = if (label == "Todos los consultorios") null else label
                currentPage = 1
            },
            categoryLabel = selectedCategory ?: "Todas las categorías",
            categoryOptions = categoryOptions,
            onCategorySelect = { label ->
                selectedCategory = if (label == "Todas las categorías") null else label
                currentPage = 1
            },
            statusLabel = selectedStatus?.labelEs ?: "Todos",
            statusOptions = statusOptions,
            onStatusSelect = { label ->
                selectedStatus =
                    if (label == "Todos") {
                        null
                    } else {
                        StockStatus.entries.firstOrNull { it.labelEs == label }
                    }
                currentPage = 1
            },
            lowStockOnly = lowStockOnly,
            onLowStockOnlyChange = {
                lowStockOnly = it
                currentPage = 1
            },
            onFiltersClick = {
                searchQuery = ""
                selectedConsultory = null
                selectedCategory = null
                selectedStatus = null
                lowStockOnly = false
                currentPage = 1
                messenger.showSuccess("Filtros restablecidos.")
            },
        )

        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val showSidePanel = maxWidth >= 1200.dp && selectedItem != null
            val showInsightsPanel = maxWidth >= 1000.dp && !showSidePanel
            Row(
                Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (pageItems.isEmpty()) {
                    EmptyStockState(
                        hasFilters = hasFilters || inventoryLines.isNotEmpty(),
                        onCreateFirst = onNewItemClick,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    StockTable(
                        items = pageItems,
                        selectedLineId = selectedLineId,
                        onSelect = { selectedLineId = it.lineId },
                        onViewDetail = { selectedLineId = it.lineId },
                        onEdit = onEdit,
                        onAdjustStock = onAdjustStock,
                        onTransfer = onTransfer,
                        onHistory = { selectedLineId = it.lineId },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (showSidePanel) {
                    selectedItem?.let { item ->
                        StockDetailsPanel(
                            item = item,
                            movements = selectedMovements,
                            onClose = { selectedLineId = null },
                        )
                    }
                } else if (showInsightsPanel) {
                    StockInsightsPanel(
                        insights = insights,
                        categoryStats = categoryStats,
                    )
                }
            }
        }

        StockFooterBar(
            rangeStart = rangeStart,
            rangeEnd = rangeEnd,
            totalItems = uiState.totalItems,
            currentPage = uiState.currentPage,
            totalPages = totalPages,
            pageSize = uiState.pageSize,
            pageSizeOptions = listOf(10, 25, 50),
            onPageChange = { currentPage = it },
            onPageSizeChange = {
                pageSize = it
                currentPage = 1
            },
        )
    }
}
