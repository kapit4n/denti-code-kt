@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.denticode.kt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.InventoryCategoryRegisterRequest
import com.denticode.kt.data.InventoryCategoryUpdateRequest
import com.denticode.kt.data.InventoryMovementStats
import com.denticode.kt.data.InventoryMovementType
import com.denticode.kt.data.InventoryProduct
import com.denticode.kt.data.InventoryProductCategory
import com.denticode.kt.data.InventoryProductKpis
import com.denticode.kt.data.InventoryProductMovementRow
import com.denticode.kt.data.InventoryProductRegisterRequest
import com.denticode.kt.data.InventoryProductStatus
import com.denticode.kt.data.InventoryProductSummary
import com.denticode.kt.data.InventoryProductUpdateRequest
import com.denticode.kt.data.Supplier
import com.denticode.kt.data.SupplierRegisterRequest
import com.denticode.kt.data.SupplierUpdateRequest
import com.denticode.kt.data.resolveInventoryProductStatus
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.inputs.AppDropdownField
import com.denticode.kt.ui.components.inputs.AppNumberField
import com.denticode.kt.ui.components.inputs.AppSearchField
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.parseHexColor
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import com.denticode.kt.ui.framework.Validators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val PRESET_COLORS = listOf(
    "#4CAF50", "#2196F3", "#FF9800", "#F44336", "#9C27B0",
    "#E91E63", "#00BCD4", "#607D8B", "#795548", "#FF5722",
    "#3F51B5", "#8BC34A",
)

@Composable
fun InventoryScreen(repo: DentiRepository) {
    val messenger = LocalAppMessenger.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    var refreshNonce by remember { mutableIntStateOf(0) }

    var products by remember { mutableStateOf<List<InventoryProduct>>(emptyList()) }
    var categories by remember { mutableStateOf<List<InventoryProductCategory>>(emptyList()) }
    var suppliers by remember { mutableStateOf<List<Supplier>>(emptyList()) }
    var kpis by remember { mutableStateOf(InventoryProductKpis(0, 0, 0, 0, 0, 0.0)) }
    var movements by remember { mutableStateOf<List<InventoryProductMovementRow>>(emptyList()) }
    var movementStats by remember { mutableStateOf(InventoryMovementStats(0, 0, 0, 0.0, 0, 0)) }
    var recentMovementsWithProduct by remember { mutableStateOf<List<Pair<InventoryProductMovementRow, InventoryProductSummary>>>(emptyList()) }

    LaunchedEffect(refreshNonce) {
        withContext(Dispatchers.IO) {
            val (k, p) = repo.loadInventoryProductDirectory()
            kpis = k
            products = p
            categories = repo.listAllInventoryCategoriesIncludingArchived()
            suppliers = repo.listAllSuppliersIncludingArchived()
            movements = repo.listInventoryProductMovements(limit = 500)
            movementStats = repo.getInventoryMovementStats()
            recentMovementsWithProduct = repo.getRecentMovementsWithProduct(limit = 10)
        }
    }

    val tabs = listOf("Productos", "Categorías", "Proveedores", "Movimientos")

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Inventario", style = AppTypography.PageTitle, fontWeight = FontWeight.Bold)
                Text("Gestión de productos, stock y proveedores", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (selectedTab == 0) {
            ProductDashboard(kpis = kpis, movementStats = movementStats, recentMovementsWithProduct = recentMovementsWithProduct)
        }

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
            }
        }

        when (selectedTab) {
            0 -> ProductsTab(
                repo = repo, products = products, categories = categories, suppliers = suppliers,
                movements = movements, onRefresh = { refreshNonce++ }, messenger = messenger, scope = scope,
            )
            1 -> CategoriesTab(repo = repo, categories = categories, onRefresh = { refreshNonce++ }, messenger = messenger, scope = scope)
            2 -> SuppliersTab(repo = repo, suppliers = suppliers, onRefresh = { refreshNonce++ }, messenger = messenger, scope = scope)
            3 -> MovementsTab(
                repo = repo, movements = movements, products = products, onRefresh = { refreshNonce++ }, messenger = messenger, scope = scope,
            )
        }
    }
}

// ── Dashboard ────────────────────────────────────────────────────────────

@Composable
private fun ProductDashboard(kpis: InventoryProductKpis, movementStats: InventoryMovementStats, recentMovementsWithProduct: List<Pair<InventoryProductMovementRow, InventoryProductSummary>>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            KpiCard("Total productos", kpis.totalProducts.toString(), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            KpiCard("Unidades", kpis.totalUnits.toString(), Color(0xFF4CAF50), Modifier.weight(1f))
            KpiCard("Stock bajo", kpis.lowStockCount.toString(), Color(0xFFF59E0B), Modifier.weight(1f))
            KpiCard("Agotados", kpis.outOfStockCount.toString(), Color(0xFFEF4444), Modifier.weight(1f))
            KpiCard("Por vencer", kpis.expiringSoonCount.toString(), Color(0xFFFF9800), Modifier.weight(1f))
            KpiCard("Valor total", "Bs ${String.format("%.0f", kpis.totalValue)}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TodayStatsCard("Entradas hoy", movementStats.todayEntries.toString(), Color(0xFF4CAF50), Modifier.weight(1f))
            TodayStatsCard("Consumos hoy", movementStats.todayConsumptions.toString(), Color(0xFFEF4444), Modifier.weight(1f))
            TodayStatsCard("Ajustes hoy", movementStats.todayAdjustments.toString(), Color(0xFF2196F3), Modifier.weight(1f))
            TodayStatsCard("Valor entradas", "Bs ${String.format("%.0f", movementStats.todayValue)}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            TodayStatsCard("Alertas stock", movementStats.lowStockAlerts.toString(), Color(0xFFF59E0B), Modifier.weight(1f))
        }
        if (recentMovementsWithProduct.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Actividad reciente", style = AppTypography.SectionTitle, fontWeight = FontWeight.Bold)
                    recentMovementsWithProduct.forEach { (mv, prod) ->
                        MovementTimelineItem(movement = mv, product = prod)
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(title: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(MaterialTheme.shapes.medium)
            .background(accent.copy(alpha = 0.08f)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, style = AppTypography.SectionTitle, fontWeight = FontWeight.Bold, color = accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun TodayStatsCard(title: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(MaterialTheme.shapes.medium)
            .background(accent.copy(alpha = 0.08f)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, style = AppTypography.SectionTitle, fontWeight = FontWeight.Bold, color = accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun MovementTimelineItem(movement: InventoryProductMovementRow, product: InventoryProductSummary) {
    val typeInfo = when (movement.type) {
        "PURCHASE" -> "Compra" to Color(0xFF4CAF50)
        "TREATMENT_CONSUMPTION" -> "Consumo" to Color(0xFFEF4444)
        "CONSUMPTION" -> "Consumo" to Color(0xFFEF4444)
        "MANUAL_ADJUSTMENT" -> "Ajuste" to Color(0xFF2196F3)
        "EXPIRED_DAMAGED_LOST" -> "Vencido/Dañado" to Color(0xFFF59E0B)
        "INVENTORY_CORRECTION" -> "Corrección" to Color(0xFF9C27B0)
        "RETURN_TO_SUPPLIER" -> "Devolución" to Color(0xFFFF9800)
        "INITIAL" -> "Inicial" to Color(0xFF607D8B)
        "INITIAL_INVENTORY" -> "Inicial" to Color(0xFF607D8B)
        "RESTOCK" -> "Entrada" to Color(0xFF4CAF50)
        "STOCK_TRANSFER" -> "Transferencia" to Color(0xFF3F51B5)
        "ADJUSTMENT" -> "Ajuste" to Color(0xFF2196F3)
        "TRANSFER" -> "Transferencia" to Color(0xFF3F51B5)
        else -> movement.type to Color(0xFF607D8B)
    }
    val sign = if (movement.quantityChange >= 0) "+" else ""
    val timeStr = java.time.Instant.ofEpochMilli(movement.createdAtEpochMs)
        .atZone(java.time.ZoneId.systemDefault())
        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm"))

    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(typeInfo.second))
        Column(Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(product.name, style = AppTypography.Body, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                FilterChip(
                    selected = false, onClick = {},
                    label = { Text(typeInfo.first, style = AppTypography.Caption) },
                    enabled = false,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = typeInfo.second.copy(alpha = 0.1f),
                        disabledContainerColor = typeInfo.second.copy(alpha = 0.1f),
                        labelColor = typeInfo.second,
                        disabledLabelColor = typeInfo.second,
                    ),
                    border = null,
                )
            }
            movement.reason?.let { Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) }
        }
        Text(
            "$sign${movement.quantityChange} ${product.unit}",
            style = AppTypography.Body, fontWeight = FontWeight.Bold,
            color = if (movement.quantityChange >= 0) Color(0xFF4CAF50) else Color(0xFFEF4444),
        )
        Text(timeStr, style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Products Tab ─────────────────────────────────────────────────────────

@Composable
private fun ProductsTab(
    repo: DentiRepository, products: List<InventoryProduct>, categories: List<InventoryProductCategory>,
    suppliers: List<Supplier>, movements: List<InventoryProductMovementRow>,
    onRefresh: () -> Unit, messenger: com.denticode.kt.ui.app.AppMessenger, scope: kotlinx.coroutines.CoroutineScope,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var showStatusFilter by remember { mutableStateOf(false) }
    var showLowStockOnly by remember { mutableStateOf(false) }
    var showRegister by remember { mutableStateOf(false) }
    var showCategoryManager by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<InventoryProduct?>(null) }
    var showStockAdjust by remember { mutableStateOf<InventoryProduct?>(null) }
    var showHistoryForProduct by remember { mutableStateOf<InventoryProduct?>(null) }
    var deletingProduct by remember { mutableStateOf<InventoryProduct?>(null) }

    val filtered = remember(products, searchQuery, selectedCategoryId, showLowStockOnly) {
        val q = searchQuery.trim().lowercase()
        products.filter { p ->
            val matchesSearch = q.isEmpty() || p.name.lowercase().contains(q) || p.code.lowercase().contains(q)
                || (p.categoryName?.lowercase()?.contains(q) == true) || (p.supplierName?.lowercase()?.contains(q) == true)
            val matchesCategory = selectedCategoryId == null || p.categoryId == selectedCategoryId
            val matchesLow = !showLowStockOnly || resolveInventoryProductStatus(p.currentStock, p.minStock, p.expirationDate) in listOf(InventoryProductStatus.LOW_STOCK, InventoryProductStatus.OUT_OF_STOCK)
            matchesSearch && matchesCategory && matchesLow
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AppSearchField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.weight(1f), placeholder = "Buscar producto, código, categoría...")
            AppDropdownField(
                label = "Categoría", options = categories.filter { !it.isArchived },
                selected = categories.find { it.id == selectedCategoryId },
                onSelected = { selectedCategoryId = it.id },
                optionLabel = { it.name }, placeholder = "Todas",
                modifier = Modifier.widthIn(min = 180.dp),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = showLowStockOnly, onCheckedChange = { showLowStockOnly = it })
                Text("Stock bajo", style = AppTypography.Caption)
            }
            AppOutlinedButton(text = "Categorías", onClick = { showCategoryManager = true }, leadingIcon = { Icon(Icons.Default.Category, null, Modifier.size(18.dp)) })
            AppButton(text = "+ Nuevo", onClick = { showRegister = true }, leadingIcon = { Icon(Icons.Default.Add, null, Modifier.size(18.dp)) })
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                Text("No hay productos registrados", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                item {
                    Row(Modifier.fillMaxWidth().background(Color.Gray.copy(alpha = 0.05f)).padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text("PRODUCTO", Modifier.weight(1.5f), style = AppTypography.Caption, fontWeight = FontWeight.SemiBold)
                        Text("CATEGORÍA", Modifier.weight(1f), style = AppTypography.Caption, fontWeight = FontWeight.SemiBold)
                        Text("STOCK", Modifier.width(80.dp), style = AppTypography.Caption, fontWeight = FontWeight.SemiBold)
                        Text("MÍN", Modifier.width(60.dp), style = AppTypography.Caption, fontWeight = FontWeight.SemiBold)
                        Text("ESTADO", Modifier.width(100.dp), style = AppTypography.Caption, fontWeight = FontWeight.SemiBold)
                        Text("PRECIO VTA", Modifier.width(90.dp), style = AppTypography.Caption, fontWeight = FontWeight.SemiBold)
                        Text("PROVEEDOR", Modifier.weight(1f), style = AppTypography.Caption, fontWeight = FontWeight.SemiBold)
                        Text("", Modifier.width(40.dp))
                    }
                }
                items(filtered, key = { it.id }) { product ->
                    ProductRow(
                        product = product, onEdit = { editingProduct = product },
                        onAdjustStock = { showStockAdjust = product },
                        onHistory = { showHistoryForProduct = product },
                        onArchive = {
                            scope.launch {
                                runCatching { withContext(Dispatchers.IO) { repo.archiveInventoryProduct(product.id) } }
                                    .onSuccess { onRefresh(); messenger.showSuccess("Producto archivado.") }
                                    .onFailure { messenger.showError(it.message ?: "Error.") }
                            }
                        },
                        onDelete = { deletingProduct = product },
                    )
                }
            }
        }
    }

    if (showRegister) ProductFormDialog(
        title = "Nuevo producto", submitLabel = "Registrar", categories = categories.filter { !it.isArchived },
        suppliers = suppliers.filter { !it.isArchived },
        onDismiss = { showRegister = false },
        onSubmit = { req ->
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        repo.findInventoryProductByName(req.name)?.let { throw IllegalArgumentException("Ya existe un producto con el nombre \"${req.name}\".") }
                        repo.findInventoryProductByCode(req.code)?.let { throw IllegalArgumentException("Ya existe un producto con el código \"${req.code}\".") }
                        repo.registerInventoryProduct(req)
                    }
                }.onSuccess { showRegister = false; onRefresh(); messenger.showSuccess("Producto registrado.") }
                .onFailure { messenger.showError(it.message ?: "Error al registrar.") }
            }
        },
    )

    editingProduct?.let { product ->
        ProductFormDialog(
            title = "Editar producto", submitLabel = "Guardar", categories = categories.filter { !it.isArchived },
            suppliers = suppliers.filter { !it.isArchived },
            initialName = product.name, initialCode = product.code, initialDescription = product.description.orEmpty(),
            initialCategoryId = product.categoryId, initialUnit = product.unit,
            initialPurchasePrice = product.purchasePrice.toString(), initialSellingPrice = product.sellingPrice.toString(),
            initialCurrentStock = product.currentStock.toString(), initialMinStock = product.minStock.toString(),
            initialMaxStock = product.maxStock.toString(), initialSupplierId = product.supplierId,
            initialExpirationDate = product.expirationDate.orEmpty(), initialBarcode = product.barcode.orEmpty(),
            initialColor = product.color, initialNotes = product.notes.orEmpty(), initialIsActive = product.isActive,
            onDismiss = { editingProduct = null },
            onSubmit = { req ->
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.findInventoryProductByName(req.name, product.id)?.let { throw IllegalArgumentException("Ya existe un producto con el nombre \"${req.name}\".") }
                            repo.findInventoryProductByCode(req.code, product.id)?.let { throw IllegalArgumentException("Ya existe un producto con el código \"${req.code}\".") }
                            repo.updateInventoryProduct(product.id, InventoryProductUpdateRequest(
                                name = req.name, code = req.code, description = req.description,
                                categoryId = req.categoryId, unit = req.unit,
                                purchasePrice = req.purchasePrice, sellingPrice = req.sellingPrice,
                                currentStock = req.currentStock, minStock = req.minStock, maxStock = req.maxStock,
                                supplierId = req.supplierId, expirationDate = req.expirationDate,
                                barcode = req.barcode, color = req.color, icon = req.icon,
                                notes = req.notes, isActive = req.isActive,
                            ))
                        }
                    }.onSuccess { editingProduct = null; onRefresh(); messenger.showSuccess("Producto actualizado.") }
                    .onFailure { messenger.showError(it.message ?: "Error al actualizar.") }
                }
            },
        )
    }

    showStockAdjust?.let { product -> StockAdjustmentDialog(product = product, onDismiss = { showStockAdjust = null },
        onSubmit = { qty, type, reason, note, unitCost, referenceNumber ->
            scope.launch {
                runCatching { withContext(Dispatchers.IO) { repo.adjustInventoryProductStock(product.id, qty, type, note, reason, unitCost, referenceNumber) } }
                    .onSuccess { showStockAdjust = null; onRefresh(); messenger.showSuccess("Stock ajustado.") }
                    .onFailure { messenger.showError(it.message ?: "Error al ajustar stock.") }
            }
        })
    }

    showHistoryForProduct?.let { product ->
        StockHistoryDialog(product = product, movements = movements.filter { it.productId == product.id }, onDismiss = { showHistoryForProduct = null })
    }

    deletingProduct?.let { product ->
        InventoryDeleteDialog(
            title = "Eliminar producto", message = "¿Estás seguro de eliminar \"${product.name}\"? Esta acción no se puede deshacer.",
            onDismiss = { deletingProduct = null },
            onConfirm = {
                scope.launch {
                    runCatching { withContext(Dispatchers.IO) { repo.hardDeleteInventoryProduct(product.id) } }
                        .onSuccess { deletingProduct = null; onRefresh(); messenger.showSuccess("Producto eliminado.") }
                        .onFailure { messenger.showError(it.message ?: "Error al eliminar.") }
                }
            },
        )
    }

    if (showCategoryManager) InventoryCategoryManagerDialog(
        repo = repo, categories = categories, onDismiss = { showCategoryManager = false },
        onRefresh = { onRefresh() }, messenger = messenger, scope = scope,
    )
}

@Composable
private fun ProductRow(
    product: InventoryProduct, onEdit: () -> Unit, onAdjustStock: () -> Unit,
    onHistory: () -> Unit, onArchive: () -> Unit, onDelete: () -> Unit,
) {
    val status = resolveInventoryProductStatus(product.currentStock, product.minStock, product.expirationDate)
    var menuExpanded by remember { mutableStateOf(false) }

    Column {
        Row(
            Modifier.fillMaxWidth().clickable { onEdit() }.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val dotColor = parseHexColor(product.color) ?: MaterialTheme.colorScheme.primary
                Box(Modifier.size(10.dp).clip(CircleShape).background(dotColor))
                Column {
                    Text(product.name, style = AppTypography.Body, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Cód: ${product.code}", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(product.categoryName ?: "—", Modifier.weight(1f), style = AppTypography.BodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${product.currentStock} ${product.unit}", Modifier.width(80.dp), style = AppTypography.Body, fontWeight = FontWeight.Bold,
                color = when (status) { InventoryProductStatus.OUT_OF_STOCK -> Color(0xFFEF4444); InventoryProductStatus.LOW_STOCK -> Color(0xFFF59E0B); else -> Color(0xFF4CAF50) })
            Text("${product.minStock}", Modifier.width(60.dp), style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ProductStatusChip(status = status, modifier = Modifier.width(100.dp))
            Text("Bs ${String.format("%.2f", product.sellingPrice)}", Modifier.width(90.dp), style = AppTypography.BodySmall)
            Text(product.supplierName ?: "—", Modifier.weight(1f), style = AppTypography.BodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Box(Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, "Acciones", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Editar") }, onClick = { menuExpanded = false; onEdit() })
                    DropdownMenuItem(text = { Text("Ajustar stock") }, onClick = { menuExpanded = false; onAdjustStock() })
                    DropdownMenuItem(text = { Text("Historial") }, onClick = { menuExpanded = false; onHistory() })
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text("Archivar", color = Color(0xFFF59E0B)) }, onClick = { menuExpanded = false; onArchive() })
                    DropdownMenuItem(text = { Text("Eliminar", color = Color(0xFFEF4444)) }, onClick = { menuExpanded = false; onDelete() })
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
private fun ProductStatusChip(status: InventoryProductStatus, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status) {
        InventoryProductStatus.ACTIVE -> Color(0xFFD1FAE5) to Color(0xFF059669)
        InventoryProductStatus.LOW_STOCK -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        InventoryProductStatus.OUT_OF_STOCK -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
        InventoryProductStatus.EXPIRED -> Color(0xFFFCE7F3) to Color(0xFFDB2777)
    }
    FilterChip(selected = false, onClick = {}, label = { Text(status.labelEs, style = AppTypography.Caption, fontWeight = FontWeight.SemiBold) },
        modifier = modifier, enabled = false,
        colors = FilterChipDefaults.filterChipColors(containerColor = bg, disabledContainerColor = bg, labelColor = fg, disabledLabelColor = fg),
        border = null,
    )
}

// ── Product Form Dialog ──────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductFormDialog(
    title: String, submitLabel: String, categories: List<InventoryProductCategory>, suppliers: List<Supplier>,
    initialName: String = "", initialCode: String = "", initialDescription: String = "",
    initialCategoryId: Int? = null, initialUnit: String = "uds",
    initialPurchasePrice: String = "", initialSellingPrice: String = "",
    initialCurrentStock: String = "", initialMinStock: String = "", initialMaxStock: String = "",
    initialSupplierId: Int? = null, initialExpirationDate: String = "", initialBarcode: String = "",
    initialColor: String? = null, initialNotes: String = "", initialIsActive: Boolean = true,
    onDismiss: () -> Unit, onSubmit: (InventoryProductRegisterRequest) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var code by remember { mutableStateOf(initialCode) }
    var description by remember { mutableStateOf(initialDescription) }
    var selectedCategoryId by remember { mutableStateOf(initialCategoryId) }
    var unit by remember { mutableStateOf(initialUnit) }
    var purchasePriceText by remember { mutableStateOf(initialPurchasePrice) }
    var sellingPriceText by remember { mutableStateOf(initialSellingPrice) }
    var currentStockText by remember { mutableStateOf(initialCurrentStock) }
    var minStockText by remember { mutableStateOf(initialMinStock) }
    var maxStockText by remember { mutableStateOf(initialMaxStock) }
    var selectedSupplierId by remember { mutableStateOf(initialSupplierId) }
    var expirationDate by remember { mutableStateOf(initialExpirationDate) }
    var barcode by remember { mutableStateOf(initialBarcode) }
    var color by remember { mutableStateOf(initialColor) }
    var notes by remember { mutableStateOf(initialNotes) }
    var isActive by remember { mutableStateOf(initialIsActive) }

    val nameError = Validators.required(name, "Nombre") ?: Validators.length(name, 255, "Nombre")
    val codeError = Validators.required(code, "Código") ?: Validators.length(code, 128, "Código")
    val purchasePriceError = Validators.positiveNumber(purchasePriceText, "Precio de compra")
    val sellingPriceError = Validators.positiveNumber(sellingPriceText, "Precio de venta")
    val stockError = Validators.positiveNumber(currentStockText, "Stock actual")
    val minStockError = Validators.positiveNumber(minStockText, "Stock mínimo")
    val canSubmit = nameError == null && codeError == null && purchasePriceError == null && sellingPriceError == null && stockError == null && minStockError == null

    AppSurfaceDialog(onDismissRequest = onDismiss, modifier = Modifier.widthIn(max = 640.dp)) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            item {
                Text(title, style = AppTypography.SectionTitle, color = MaterialTheme.colorScheme.onSurface)
            }
            item {
                AppTextField(value = name, onValueChange = { name = it }, label = "Nombre *", placeholder = "Resina composite, guantes...")
                nameError?.let { if (name.isNotEmpty()) Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.error) }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    AppTextField(value = code, onValueChange = { code = it }, label = "Código *", placeholder = "MAT-001", modifier = Modifier.weight(1f))
                    AppDropdownField(label = "Categoría", options = categories, selected = categories.find { it.id == selectedCategoryId },
                        onSelected = { selectedCategoryId = it.id }, optionLabel = { it.name }, placeholder = "Seleccionar...", modifier = Modifier.weight(1f))
                }
                codeError?.let { if (code.isNotEmpty()) Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.error) }
            }
            item { AppTextArea(value = description, onValueChange = { description = it }, label = "Descripción", minLines = 2, maxLines = 3) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    AppTextField(value = purchasePriceText, onValueChange = { purchasePriceText = it.filter { ch -> ch.isDigit() || ch == '.' }.take(10) },
                        label = "Precio compra (Bs)", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                    AppTextField(value = sellingPriceText, onValueChange = { sellingPriceText = it.filter { ch -> ch.isDigit() || ch == '.' }.take(10) },
                        label = "Precio venta (Bs)", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    AppTextField(value = currentStockText, onValueChange = { currentStockText = it.filter { ch -> ch.isDigit() }.take(8) }, label = "Stock actual", modifier = Modifier.weight(1f))
                    AppTextField(value = minStockText, onValueChange = { minStockText = it.filter { ch -> ch.isDigit() }.take(8) }, label = "Stock mínimo", modifier = Modifier.weight(1f))
                    AppTextField(value = maxStockText, onValueChange = { maxStockText = it.filter { ch -> ch.isDigit() }.take(8) }, label = "Stock máximo", modifier = Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    AppDropdownField(label = "Unidad", options = listOf("uds", "kg", "litro", "tubo", "caja", "rollo", "bolsa", "jeringa", "kit", "hoja", "cartucho"),
                        selected = unit, onSelected = { unit = it }, optionLabel = { it }, modifier = Modifier.weight(1f))
                    AppDropdownField(label = "Proveedor", options = suppliers, selected = suppliers.find { it.id == selectedSupplierId },
                        onSelected = { selectedSupplierId = it.id }, optionLabel = { it.name }, placeholder = "Seleccionar...", modifier = Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    AppTextField(value = expirationDate, onValueChange = { expirationDate = it }, label = "Vencimiento (YYYY-MM-DD)", placeholder = "2026-12-31", modifier = Modifier.weight(1f))
                    AppTextField(value = barcode, onValueChange = { barcode = it }, label = "Código de barras", modifier = Modifier.weight(1f))
                }
            }
            item {
                Text("Color", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    PRESET_COLORS.forEach { c ->
                        val isSelected = color == c
                        val bg = parseHexColor(c) ?: Color.Gray
                        Box(Modifier.size(28.dp).clip(CircleShape).background(bg)
                            .then(if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                            .clickable { color = if (isSelected) null else c })
                    }
                }
            }
            item { AppTextArea(value = notes, onValueChange = { notes = it }, label = "Notas", minLines = 1, maxLines = 2) }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isActive, onCheckedChange = { isActive = it })
                    Text("Activo", style = AppTypography.Body)
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    AppOutlinedButton(text = "Cancelar", onClick = onDismiss)
                    Spacer(Modifier.width(AppSpacing.sm))
                    AppButton(text = submitLabel, onClick = {
                        onSubmit(InventoryProductRegisterRequest(
                            name = name.trim(), code = code.trim(), description = description.trim().ifBlank { null },
                            categoryId = selectedCategoryId, unit = unit,
                            purchasePrice = purchasePriceText.toDoubleOrNull() ?: 0.0,
                            sellingPrice = sellingPriceText.toDoubleOrNull() ?: 0.0,
                            currentStock = currentStockText.toIntOrNull() ?: 0,
                            minStock = minStockText.toIntOrNull() ?: 0,
                            maxStock = maxStockText.toIntOrNull() ?: 0,
                            supplierId = selectedSupplierId, expirationDate = expirationDate.trim().ifBlank { null },
                            barcode = barcode.trim().ifBlank { null }, color = color, notes = notes.trim().ifBlank { null },
                            isActive = isActive,
                        ))
                    }, enabled = canSubmit)
                }
            }
        }
    }
}

// ── Stock Adjustment Dialog ───────────────────────────────────────────────

@Composable
private fun StockAdjustmentDialog(product: InventoryProduct, onDismiss: () -> Unit, onSubmit: (Int, String, String?, String?, Double, String?) -> Unit) {
    var quantityText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("PURCHASE") }
    var reason by remember { mutableStateOf("") }
    var unitCostText by remember { mutableStateOf("") }
    var referenceNumber by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val movementTypes = InventoryMovementType.entries.map { it.name to it.labelEs }
    val typeLabel = movementTypes.find { it.first == type }?.second ?: type
    val quantity = quantityText.toIntOrNull() ?: 0
    val unitCost = unitCostText.toDoubleOrNull() ?: 0.0
    val canSubmit = quantity != 0 && reason.isNotBlank()

    AppSurfaceDialog(onDismissRequest = onDismiss, modifier = Modifier.widthIn(max = 480.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text("Ajustar stock — ${product.name}", style = AppTypography.SectionTitle)
            Text("Stock actual: ${product.currentStock} ${product.unit}", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            AppDropdownField(label = "Tipo de movimiento", options = movementTypes.map { it.first }, selected = type, onSelected = { type = it },
                optionLabel = { t -> movementTypes.find { it.first == t }?.second ?: t })
            AppTextField(value = quantityText, onValueChange = { quantityText = it.filter { ch -> ch.isDigit() || ch == '-' }.take(8) },
                label = "Cantidad *", placeholder = "Ej: 10 o -5", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            AppTextArea(value = reason, onValueChange = { reason = it }, label = "Motivo *", minLines = 1, maxLines = 2, placeholder = "Motivo del ajuste...")
            AppTextField(value = unitCostText, onValueChange = { unitCostText = it.filter { ch -> ch.isDigit() || ch == '.' }.take(10) },
                label = "Costo unitario (Bs)", placeholder = "0.00", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            AppTextField(value = referenceNumber, onValueChange = { referenceNumber = it }, label = "N° Referencia", placeholder = "N° factura, orden, etc.")
            AppTextArea(value = note, onValueChange = { note = it }, label = "Nota adicional", minLines = 1, maxLines = 2)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss)
                Spacer(Modifier.width(AppSpacing.sm))
                AppButton(text = "Aplicar", onClick = {
                    val qty = quantity
                    val adjustedQty = if (type == "TREATMENT_CONSUMPTION" || type == "CONSUMPTION" || type == "EXPIRED_DAMAGED_LOST" || type == "RETURN_TO_SUPPLIER") -kotlin.math.abs(qty) else qty
                    onSubmit(adjustedQty, type, reason.trim().ifBlank { null }, note.trim().ifBlank { null }, unitCost, referenceNumber.trim().ifBlank { null })
                }, enabled = canSubmit)
            }
        }
    }
}

// ── Stock History Dialog ─────────────────────────────────────────────────

@Composable
private fun StockHistoryDialog(product: InventoryProduct, movements: List<InventoryProductMovementRow>, onDismiss: () -> Unit) {
    AppSurfaceDialog(onDismissRequest = onDismiss, modifier = Modifier.widthIn(max = 600.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text("Historial — ${product.name}", style = AppTypography.SectionTitle)
            if (movements.isEmpty()) {
                Text("Sin movimientos registrados.", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.heightIn(max = 500.dp)) {
                    items(movements) { mv ->
                        val typeInfo = when (mv.type) {
                            "PURCHASE" -> "Compra" to Color(0xFF4CAF50)
                            "TREATMENT_CONSUMPTION" -> "Consumo tratamiento" to Color(0xFFEF4444)
                            "CONSUMPTION" -> "Consumo" to Color(0xFFEF4444)
                            "MANUAL_ADJUSTMENT" -> "Ajuste manual" to Color(0xFF2196F3)
                            "EXPIRED_DAMAGED_LOST" -> "Vencido/Dañado/Perdido" to Color(0xFFF59E0B)
                            "INVENTORY_CORRECTION" -> "Corrección" to Color(0xFF9C27B0)
                            "RETURN_TO_SUPPLIER" -> "Devolución proveedor" to Color(0xFFFF9800)
                            "INITIAL" -> "Inicial" to Color(0xFF607D8B)
                            "INITIAL_INVENTORY" -> "Inventario inicial" to Color(0xFF607D8B)
                            "RESTOCK" -> "Entrada" to Color(0xFF4CAF50)
                            "STOCK_TRANSFER" -> "Transferencia" to Color(0xFF3F51B5)
                            "ADJUSTMENT" -> "Ajuste" to Color(0xFF2196F3)
                            "TRANSFER" -> "Transferencia" to Color(0xFF3F51B5)
                            else -> mv.type to Color(0xFF607D8B)
                        }
                        val timeStr = java.time.Instant.ofEpochMilli(mv.createdAtEpochMs)
                            .atZone(java.time.ZoneId.systemDefault())
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        val sign = if (mv.quantityChange >= 0) "+" else ""

                        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(Modifier.size(8.dp).clip(CircleShape).background(typeInfo.second))
                                    Column {
                                        Text(typeInfo.first, style = AppTypography.Body, fontWeight = FontWeight.SemiBold, color = typeInfo.second)
                                        mv.reason?.let { Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) }
                                    }
                                }
                                Text(
                                    "$sign${mv.quantityChange} ${product.unit}",
                                    style = AppTypography.Body, fontWeight = FontWeight.Bold,
                                    color = if (mv.quantityChange >= 0) Color(0xFF4CAF50) else Color(0xFFEF4444),
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(timeStr, style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (mv.previousStock != 0 || mv.currentStock != 0) {
                                    Text("Stock: ${mv.previousStock} → ${mv.currentStock}", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (mv.unitCost > 0) {
                                    Text("Costo: Bs ${String.format("%.2f", mv.unitCost)}", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                mv.referenceNumber?.let { Text("Ref: $it", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { AppOutlinedButton(text = "Cerrar", onClick = onDismiss) }
        }
    }
}

// ── Movements Tab ─────────────────────────────────────────────────────────

@Composable
private fun MovementsTab(
    repo: DentiRepository, movements: List<InventoryProductMovementRow>, products: List<InventoryProduct>,
    onRefresh: () -> Unit, messenger: com.denticode.kt.ui.app.AppMessenger, scope: kotlinx.coroutines.CoroutineScope,
) {
    var selectedTypeFilter by remember { mutableStateOf<String?>(null) }
    val typeFilters = InventoryMovementType.entries.map { it.name to it.labelEs }
    val filteredMovements = remember(movements, selectedTypeFilter) {
        if (selectedTypeFilter == null) movements
        else movements.filter { it.type == selectedTypeFilter }
    }
    val productMap = remember(products) { products.associateBy { it.id } }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Movimientos de inventario", style = AppTypography.SectionTitle, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("${filteredMovements.size} movimientos", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedTypeFilter == null,
                onClick = { selectedTypeFilter = null },
                label = { Text("Todos") },
            )
            typeFilters.forEach { (type, label) ->
                FilterChip(
                    selected = selectedTypeFilter == type,
                    onClick = { selectedTypeFilter = if (selectedTypeFilter == type) null else type },
                    label = { Text(label) },
                )
            }
        }
        if (filteredMovements.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                Text("No hay movimientos registrados", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(filteredMovements.take(100)) { mv ->
                    val product = productMap[mv.productId]
                    val typeInfo = when (mv.type) {
                        "PURCHASE" -> "Compra" to Color(0xFF4CAF50)
                        "TREATMENT_CONSUMPTION" -> "Consumo tratamiento" to Color(0xFFEF4444)
                        "CONSUMPTION" -> "Consumo" to Color(0xFFEF4444)
                        "MANUAL_ADJUSTMENT" -> "Ajuste manual" to Color(0xFF2196F3)
                        "EXPIRED_DAMAGED_LOST" -> "Vencido/Dañado/Perdido" to Color(0xFFF59E0B)
                        "INVENTORY_CORRECTION" -> "Corrección" to Color(0xFF9C27B0)
                        "RETURN_TO_SUPPLIER" -> "Devolución proveedor" to Color(0xFFFF9800)
                        "INITIAL" -> "Inicial" to Color(0xFF607D8B)
                        "INITIAL_INVENTORY" -> "Inventario inicial" to Color(0xFF607D8B)
                        "RESTOCK" -> "Entrada" to Color(0xFF4CAF50)
                        "STOCK_TRANSFER" -> "Transferencia" to Color(0xFF3F51B5)
                        "ADJUSTMENT" -> "Ajuste" to Color(0xFF2196F3)
                        "TRANSFER" -> "Transferencia" to Color(0xFF3F51B5)
                        else -> mv.type to Color(0xFF607D8B)
                    }
                    val timeStr = java.time.Instant.ofEpochMilli(mv.createdAtEpochMs)
                        .atZone(java.time.ZoneId.systemDefault())
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    val sign = if (mv.quantityChange >= 0) "+" else ""
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(typeInfo.second))
                        Column(Modifier.weight(1f)) {
                            Text(product?.name ?: "Producto #${mv.productId}", style = AppTypography.Body, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                FilterChip(
                                    selected = false, onClick = {},
                                    label = { Text(typeInfo.first, style = AppTypography.Caption) },
                                    enabled = false,
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = typeInfo.second.copy(alpha = 0.1f),
                                        disabledContainerColor = typeInfo.second.copy(alpha = 0.1f),
                                        labelColor = typeInfo.second,
                                        disabledLabelColor = typeInfo.second,
                                    ),
                                    border = null,
                                )
                                mv.reason?.let { Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            }
                        }
                        Text(
                            "$sign${mv.quantityChange} ${product?.unit ?: ""}",
                            style = AppTypography.Body, fontWeight = FontWeight.Bold,
                            color = if (mv.quantityChange >= 0) Color(0xFF4CAF50) else Color(0xFFEF4444),
                        )
                        Text(timeStr, style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

// ── Categories Tab ───────────────────────────────────────────────────────

@Composable
private fun CategoriesTab(
    repo: DentiRepository, categories: List<InventoryProductCategory>,
    onRefresh: () -> Unit, messenger: com.denticode.kt.ui.app.AppMessenger, scope: kotlinx.coroutines.CoroutineScope,
) {
    var showCreate by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<InventoryProductCategory?>(null) }
    var deletingCategory by remember { mutableStateOf<InventoryProductCategory?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${categories.size} categorías", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            AppButton(text = "+ Nueva categoría", onClick = { showCreate = true }, leadingIcon = { Icon(Icons.Default.Add, null, Modifier.size(18.dp)) })
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(categories, key = { it.id }) { cat ->
                val dotColor = parseHexColor(cat.color) ?: Color.Gray
                Row(Modifier.fillMaxWidth().clickable { editingCategory = cat }.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(12.dp).clip(CircleShape).background(dotColor))
                        Column {
                            Text(cat.name, style = AppTypography.Body, fontWeight = FontWeight.SemiBold)
                            cat.description?.let { Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (cat.isArchived) {
                            AppOutlinedButton(text = "Restaurar", onClick = { scope.launch { withContext(Dispatchers.IO) { repo.restoreInventoryCategory(cat.id) }; onRefresh(); messenger.showSuccess("Restaurada.") } }, minHeight = 32.dp)
                        }
                        IconButton(onClick = { deletingCategory = cat }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }

    if (showCreate) InventoryCategoryFormDialog("Nueva categoría", "Crear", onDismiss = { showCreate = false },
        onSubmit = { req -> scope.launch { runCatching { withContext(Dispatchers.IO) { repo.findInventoryCategoryByName(req.name)?.let { throw IllegalArgumentException("Ya existe una categoría con el nombre \"${req.name}\".") }; repo.registerInventoryCategory(req) } }
            .onSuccess { showCreate = false; onRefresh(); messenger.showSuccess("Categoría creada.") }
            .onFailure { messenger.showError(it.message ?: "Error.") } } })

    editingCategory?.let { cat ->
        InventoryCategoryFormDialog("Editar categoría", "Guardar", initialName = cat.name, initialDescription = cat.description.orEmpty(),
            initialIcon = cat.icon.orEmpty(), initialColor = cat.color, initialSortOrder = cat.sortOrder, initialIsActive = cat.isActive,
            onDismiss = { editingCategory = null },
            onSubmit = { req -> scope.launch { runCatching { withContext(Dispatchers.IO) { repo.findInventoryCategoryByName(req.name, cat.id)?.let { throw IllegalArgumentException("Ya existe una categoría con el nombre \"${req.name}\".") }; repo.updateInventoryCategory(cat.id, InventoryCategoryUpdateRequest(name = req.name, description = req.description, icon = req.icon, color = req.color, sortOrder = req.sortOrder, isActive = req.isActive)) } }
                .onSuccess { editingCategory = null; onRefresh(); messenger.showSuccess("Categoría actualizada.") }
                .onFailure { messenger.showError(it.message ?: "Error.") } } })
    }

    deletingCategory?.let { cat ->
        InventoryDeleteDialog(title = "Eliminar categoría", message = "¿Eliminar \"${cat.name}\"?", onDismiss = { deletingCategory = null },
            onConfirm = { scope.launch { runCatching { withContext(Dispatchers.IO) { repo.hardDeleteInventoryCategory(cat.id) } }
                .onSuccess { deletingCategory = null; onRefresh(); messenger.showSuccess("Eliminada.") }
                .onFailure { messenger.showError(it.message ?: "Error.") } } })
    }
}

// ── Category Form Dialog ─────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InventoryCategoryFormDialog(
    title: String, submitLabel: String,
    initialName: String = "", initialDescription: String = "", initialIcon: String = "",
    initialColor: String? = null, initialSortOrder: Int = 0, initialIsActive: Boolean = true,
    onDismiss: () -> Unit, onSubmit: (InventoryCategoryRegisterRequest) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    var icon by remember { mutableStateOf(initialIcon) }
    var color by remember { mutableStateOf(initialColor) }
    var sortOrder by remember { mutableStateOf(initialSortOrder.toString()) }
    var isActive by remember { mutableStateOf(initialIsActive) }
    val nameError = Validators.required(name, "Nombre") ?: Validators.length(name, 255, "Nombre")
    val canSubmit = nameError == null

    AppSurfaceDialog(onDismissRequest = onDismiss, modifier = Modifier.widthIn(max = 480.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(title, style = AppTypography.SectionTitle)
            AppTextField(value = name, onValueChange = { name = it }, label = "Nombre *", placeholder = "Material restaurativo...")
            nameError?.let { if (name.isNotEmpty()) Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.error) }
            AppTextArea(value = description, onValueChange = { description = it }, label = "Descripción", minLines = 1, maxLines = 2)
            AppTextField(value = icon, onValueChange = { icon = it }, label = "Icono", placeholder = "build, shield...")
            Text("Color", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                PRESET_COLORS.forEach { c ->
                    val isSelected = color == c
                    val bg = parseHexColor(c) ?: Color.Gray
                    Box(Modifier.size(28.dp).clip(CircleShape).background(bg)
                        .then(if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                        .clickable { color = if (isSelected) null else c })
                }
            }
            AppTextField(value = sortOrder, onValueChange = { sortOrder = it.filter { ch -> ch.isDigit() }.take(3) }, label = "Orden")
            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = isActive, onCheckedChange = { isActive = it }); Text("Activa") }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss)
                Spacer(Modifier.width(AppSpacing.sm))
                AppButton(text = submitLabel, onClick = {
                    onSubmit(InventoryCategoryRegisterRequest(name = name.trim(), description = description.trim().ifBlank { null },
                        icon = icon.trim().ifBlank { null }, color = color, sortOrder = sortOrder.toIntOrNull() ?: 0, isActive = isActive))
                }, enabled = canSubmit)
            }
        }
    }
}

// ── Suppliers Tab ────────────────────────────────────────────────────────

@Composable
private fun SuppliersTab(
    repo: DentiRepository, suppliers: List<Supplier>,
    onRefresh: () -> Unit, messenger: com.denticode.kt.ui.app.AppMessenger, scope: kotlinx.coroutines.CoroutineScope,
) {
    var showCreate by remember { mutableStateOf(false) }
    var editingSupplier by remember { mutableStateOf<Supplier?>(null) }
    var deletingSupplier by remember { mutableStateOf<Supplier?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${suppliers.size} proveedores", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            AppButton(text = "+ Nuevo proveedor", onClick = { showCreate = true }, leadingIcon = { Icon(Icons.Default.Add, null, Modifier.size(18.dp)) })
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(suppliers, key = { it.id }) { sup ->
                Row(Modifier.fillMaxWidth().clickable { editingSupplier = sup }.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.LocalShipping, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Column {
                            Text(sup.name, style = AppTypography.Body, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                sup.contactName?.let { Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                sup.phone?.let { Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                sup.email?.let { Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (sup.isArchived) {
                            AppOutlinedButton(text = "Restaurar", onClick = { scope.launch { withContext(Dispatchers.IO) { repo.restoreSupplier(sup.id) }; onRefresh(); messenger.showSuccess("Restaurado.") } }, minHeight = 32.dp)
                        }
                        IconButton(onClick = { deletingSupplier = sup }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }

    if (showCreate) SupplierFormDialog("Nuevo proveedor", "Crear", onDismiss = { showCreate = false },
        onSubmit = { req -> scope.launch { runCatching { withContext(Dispatchers.IO) { repo.findSupplierByName(req.name)?.let { throw IllegalArgumentException("Ya existe un proveedor con el nombre \"${req.name}\".") }; repo.registerSupplier(req) } }
            .onSuccess { showCreate = false; onRefresh(); messenger.showSuccess("Proveedor creado.") }
            .onFailure { messenger.showError(it.message ?: "Error.") } } })

    editingSupplier?.let { sup ->
        SupplierFormDialog("Editar proveedor", "Guardar", initialName = sup.name, initialContactName = sup.contactName.orEmpty(),
            initialPhone = sup.phone.orEmpty(), initialEmail = sup.email.orEmpty(), initialAddress = sup.address.orEmpty(),
            initialNotes = sup.notes.orEmpty(), initialIsActive = sup.isActive,
            onDismiss = { editingSupplier = null },
            onSubmit = { req -> scope.launch { runCatching { withContext(Dispatchers.IO) { repo.findSupplierByName(req.name, sup.id)?.let { throw IllegalArgumentException("Ya existe un proveedor con el nombre \"${req.name}\".") }; repo.updateSupplier(sup.id, SupplierUpdateRequest(name = req.name, contactName = req.contactName, phone = req.phone, email = req.email, address = req.address, notes = req.notes, isActive = req.isActive)) } }
                .onSuccess { editingSupplier = null; onRefresh(); messenger.showSuccess("Proveedor actualizado.") }
                .onFailure { messenger.showError(it.message ?: "Error.") } } })
    }

    deletingSupplier?.let { sup ->
        InventoryDeleteDialog(title = "Eliminar proveedor", message = "¿Eliminar \"${sup.name}\"?", onDismiss = { deletingSupplier = null },
            onConfirm = { scope.launch { runCatching { withContext(Dispatchers.IO) { repo.hardDeleteSupplier(sup.id) } }
                .onSuccess { deletingSupplier = null; onRefresh(); messenger.showSuccess("Eliminado.") }
                .onFailure { messenger.showError(it.message ?: "Error.") } } })
    }
}

// ── Supplier Form Dialog ─────────────────────────────────────────────────

@Composable
private fun SupplierFormDialog(
    title: String, submitLabel: String,
    initialName: String = "", initialContactName: String = "", initialPhone: String = "",
    initialEmail: String = "", initialAddress: String = "", initialNotes: String = "", initialIsActive: Boolean = true,
    onDismiss: () -> Unit, onSubmit: (SupplierRegisterRequest) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var contactName by remember { mutableStateOf(initialContactName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var email by remember { mutableStateOf(initialEmail) }
    var address by remember { mutableStateOf(initialAddress) }
    var notes by remember { mutableStateOf(initialNotes) }
    var isActive by remember { mutableStateOf(initialIsActive) }
    val nameError = Validators.required(name, "Nombre") ?: Validators.length(name, 255, "Nombre")
    val canSubmit = nameError == null

    AppSurfaceDialog(onDismissRequest = onDismiss, modifier = Modifier.widthIn(max = 520.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(title, style = AppTypography.SectionTitle)
            AppTextField(value = name, onValueChange = { name = it }, label = "Nombre *", placeholder = "Dental Supply Bolivia...")
            nameError?.let { if (name.isNotEmpty()) Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.error) }
            AppTextField(value = contactName, onValueChange = { contactName = it }, label = "Contacto", placeholder = "Nombre del contacto")
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                AppTextField(value = phone, onValueChange = { phone = it }, label = "Teléfono", placeholder = "+591 2 2345678", modifier = Modifier.weight(1f))
                AppTextField(value = email, onValueChange = { email = it }, label = "Correo", placeholder = "ventas@ejemplo.com", modifier = Modifier.weight(1f))
            }
            AppTextArea(value = address, onValueChange = { address = it }, label = "Dirección", minLines = 1, maxLines = 2)
            AppTextArea(value = notes, onValueChange = { notes = it }, label = "Notas", minLines = 1, maxLines = 2)
            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = isActive, onCheckedChange = { isActive = it }); Text("Activo") }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss)
                Spacer(Modifier.width(AppSpacing.sm))
                AppButton(text = submitLabel, onClick = {
                    onSubmit(SupplierRegisterRequest(name = name.trim(), contactName = contactName.trim().ifBlank { null },
                        phone = phone.trim().ifBlank { null }, email = email.trim().ifBlank { null },
                        address = address.trim().ifBlank { null }, notes = notes.trim().ifBlank { null }, isActive = isActive))
                }, enabled = canSubmit)
            }
        }
    }
}

// ── Delete Confirmation Dialog ───────────────────────────────────────────

@Composable
private fun InventoryDeleteDialog(title: String, message: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AppSurfaceDialog(onDismissRequest = onDismiss, modifier = Modifier.widthIn(max = 440.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444))
                Text(title, style = AppTypography.SectionTitle, color = Color(0xFFEF4444))
            }
            Text(message, style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss)
                Spacer(Modifier.width(AppSpacing.sm))
                AppButton(text = "Eliminar", onClick = onConfirm, enabled = true)
            }
        }
    }
}

// ── Category Manager Dialog ──────────────────────────────────────────────

@Composable
private fun InventoryCategoryManagerDialog(
    repo: DentiRepository, categories: List<InventoryProductCategory>,
    onDismiss: () -> Unit, onRefresh: () -> Unit,
    messenger: com.denticode.kt.ui.app.AppMessenger, scope: kotlinx.coroutines.CoroutineScope,
) {
    var showCreate by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<InventoryProductCategory?>(null) }

    AppSurfaceDialog(onDismissRequest = onDismiss, modifier = Modifier.widthIn(max = 560.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Gestionar categorías", style = AppTypography.SectionTitle)
                AppButton(text = "+ Nueva", onClick = { showCreate = true }, leadingIcon = { Icon(Icons.Default.Add, null, Modifier.size(16.dp)) })
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.heightIn(max = 400.dp)) {
                items(categories, key = { it.id }) { cat ->
                    val dotColor = parseHexColor(cat.color) ?: Color.Gray
                    Row(Modifier.fillMaxWidth().clickable { editingCategory = cat }.padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(dotColor))
                            Text(cat.name, style = AppTypography.Body)
                            if (cat.isArchived) Text("(archivada)", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (cat.isArchived) {
                            IconButton(onClick = { scope.launch { withContext(Dispatchers.IO) { repo.restoreInventoryCategory(cat.id) }; onRefresh(); messenger.showSuccess("Restaurada.") } }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Restore, "Restaurar", modifier = Modifier.size(16.dp))
                            }
                        } else {
                            IconButton(onClick = { scope.launch { withContext(Dispatchers.IO) { repo.archiveInventoryCategory(cat.id) }; onRefresh(); messenger.showSuccess("Archivada.") } }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Archive, "Archivar", tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { AppOutlinedButton(text = "Cerrar", onClick = onDismiss) }
        }
    }

    if (showCreate) InventoryCategoryFormDialog("Nueva categoría", "Crear", onDismiss = { showCreate = false },
        onSubmit = { req -> scope.launch { runCatching { withContext(Dispatchers.IO) { repo.findInventoryCategoryByName(req.name)?.let { throw IllegalArgumentException("Ya existe.") }; repo.registerInventoryCategory(req) } }
            .onSuccess { showCreate = false; onRefresh(); messenger.showSuccess("Categoría creada.") }
            .onFailure { messenger.showError(it.message ?: "Error.") } } })

    editingCategory?.let { cat ->
        InventoryCategoryFormDialog("Editar categoría", "Guardar", initialName = cat.name, initialDescription = cat.description.orEmpty(),
            initialIcon = cat.icon.orEmpty(), initialColor = cat.color, initialSortOrder = cat.sortOrder, initialIsActive = cat.isActive,
            onDismiss = { editingCategory = null },
            onSubmit = { req -> scope.launch { runCatching { withContext(Dispatchers.IO) { repo.findInventoryCategoryByName(req.name, cat.id)?.let { throw IllegalArgumentException("Ya existe.") }; repo.updateInventoryCategory(cat.id, InventoryCategoryUpdateRequest(name = req.name, description = req.description, icon = req.icon, color = req.color, sortOrder = req.sortOrder, isActive = req.isActive)) } }
                .onSuccess { editingCategory = null; onRefresh(); messenger.showSuccess("Categoría actualizada.") }
                .onFailure { messenger.showError(it.message ?: "Error.") } } })
    }
}
