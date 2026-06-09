package com.denticode.kt.ui.inventory

import com.denticode.kt.data.InventoryDirectoryKpis
import com.denticode.kt.data.InventoryLineRow
import com.denticode.kt.data.InventoryMovementRow
import com.denticode.kt.data.StockStatus
import com.denticode.kt.data.resolveInventoryStockStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class StockUiModel(
    val lineId: Int,
    val productName: String,
    val productCode: String,
    val categoryKey: String,
    val categoryLabel: String,
    val consultoryLabel: String,
    val quantity: Int,
    val minQuantity: Int,
    val maxQuantity: Int,
    val unitLabel: String,
    val status: StockStatus,
    val updatedDateLabel: String,
    val updatedTimeLabel: String,
    val updatedByLabel: String,
    val row: InventoryLineRow,
)

data class StockMovementUiModel(
    val dateLabel: String,
    val typeLabel: String,
    val quantityLabel: String,
    val userLabel: String,
)

data class StockUiState(
    val items: List<StockUiModel>,
    val kpis: InventoryDirectoryKpis,
    val searchQuery: String,
    val selectedConsultory: String?,
    val selectedCategory: String?,
    val selectedStatus: StockStatus?,
    val lowStockOnly: Boolean,
    val currentPage: Int,
    val pageSize: Int,
    val totalItems: Int,
)

private val stockDateFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es", "ES"))

private val stockTimeFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm", Locale("es", "ES"))

fun categoryLabelEs(categoryKey: String): String =
    when (categoryKey.uppercase()) {
        "PPE" -> "Desechables"
        "INJECTION" -> "Anestesia"
        "SURGERY" -> "Suturas"
        "RESTORATIVE" -> "Restaurativos"
        "PREVENTIVE" -> "Preventivos"
        else -> categoryKey.replace('_', ' ').replaceFirstChar { it.titlecase(Locale("es", "ES")) }
    }

fun movementTypeLabelEs(type: String): String =
    when (type.uppercase()) {
        "RESTOCK" -> "Reposición"
        "ADJUSTMENT" -> "Ajuste"
        "TRANSFER" -> "Transferencia"
        "CONSUMPTION" -> "Consumo"
        else -> type
    }

fun InventoryLineRow.toUiModel(): StockUiModel {
    val updated = Instant.ofEpochMilli(lastUpdatedEpochMs).atZone(ZoneId.systemDefault())
    val consultory =
        buildString {
            append(consultoryName)
            consultoryShortCode?.let { append(" ($it)") }
        }
    return StockUiModel(
        lineId = lineId,
        productName = facilityDisplayName,
        productCode = facilityCode,
        categoryKey = categoryKey,
        categoryLabel = categoryLabelEs(categoryKey),
        consultoryLabel = consultory,
        quantity = quantity,
        minQuantity = minQuantity,
        maxQuantity = maxQuantity,
        unitLabel = unitLabel,
        status = resolveInventoryStockStatus(quantity, minQuantity),
        updatedDateLabel = updated.format(stockDateFmt),
        updatedTimeLabel = updated.format(stockTimeFmt),
        updatedByLabel = "Por: $lastUpdatedBy",
        row = this,
    )
}

fun InventoryMovementRow.toUiModel(): StockMovementUiModel {
    val dt = Instant.ofEpochMilli(createdAtEpochMs).atZone(ZoneId.systemDefault())
    val sign = if (quantityChange >= 0) "+" else ""
    return StockMovementUiModel(
        dateLabel = "${dt.format(stockDateFmt)} ${dt.format(stockTimeFmt)}",
        typeLabel = movementTypeLabelEs(type),
        quantityLabel = "$sign$quantityChange",
        userLabel = actorLabel,
    )
}

fun buildStockUiState(
    lines: List<InventoryLineRow>,
    kpis: InventoryDirectoryKpis,
    searchQuery: String,
    selectedConsultory: String?,
    selectedCategory: String?,
    selectedStatus: StockStatus?,
    lowStockOnly: Boolean,
    currentPage: Int,
    pageSize: Int,
): StockUiState {
    val q = searchQuery.trim().lowercase()
    val filtered =
        lines
            .map { it.toUiModel() }
            .filter { item ->
                val matchesSearch =
                    q.isEmpty() ||
                        item.productName.lowercase().contains(q) ||
                        item.productCode.lowercase().contains(q) ||
                        item.categoryLabel.lowercase().contains(q) ||
                        item.consultoryLabel.lowercase().contains(q)
                val matchesConsultory =
                    selectedConsultory == null || item.consultoryLabel == selectedConsultory
                val matchesCategory =
                    selectedCategory == null || item.categoryLabel == selectedCategory
                val matchesStatus = selectedStatus == null || item.status == selectedStatus
                val matchesLowOnly =
                    !lowStockOnly || item.status == StockStatus.LOW || item.status == StockStatus.OUT
                matchesSearch &&
                    matchesConsultory &&
                    matchesCategory &&
                    matchesStatus &&
                    matchesLowOnly
            }
    return StockUiState(
        items = filtered,
        kpis = kpis,
        searchQuery = searchQuery,
        selectedConsultory = selectedConsultory,
        selectedCategory = selectedCategory,
        selectedStatus = selectedStatus,
        lowStockOnly = lowStockOnly,
        currentPage = currentPage,
        pageSize = pageSize,
        totalItems = filtered.size,
    )
}

fun paginatedStockItems(state: StockUiState): List<StockUiModel> {
    if (state.totalItems == 0) return emptyList()
    val start = (state.currentPage - 1) * state.pageSize
    if (start >= state.totalItems) return emptyList()
    return state.items.subList(start, minOf(start + state.pageSize, state.totalItems))
}

fun totalStockPages(state: StockUiState): Int =
    if (state.totalItems == 0) {
        1
    } else {
        (state.totalItems + state.pageSize - 1) / state.pageSize
    }
