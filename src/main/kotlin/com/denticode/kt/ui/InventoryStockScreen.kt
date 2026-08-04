package com.denticode.kt.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.denticode.kt.data.Consultory
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.InventoryLineRow
import com.denticode.kt.data.InventoryMovementRow
import com.denticode.kt.data.PurchaseOrderRegisterRequest
import com.denticode.kt.data.PurchaseOrderRow
import com.denticode.kt.data.Supplier
import com.denticode.kt.data.SupplierRegisterRequest
import com.denticode.kt.data.SupplierUpdateRequest
import com.denticode.kt.data.TreatmentFacilityRow
import com.denticode.kt.export.ExportService
import com.denticode.kt.export.renderInventoryCsv
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.components.feedback.LoadingIndicator
import com.denticode.kt.ui.inventory.AdjustStockDialog
import com.denticode.kt.ui.inventory.EditStockDialog
import com.denticode.kt.ui.inventory.ModernStockContent
import com.denticode.kt.ui.inventory.NewPurchaseOrderDialog
import com.denticode.kt.ui.inventory.NewStockDialog
import com.denticode.kt.ui.inventory.PurchaseOrdersDialog
import com.denticode.kt.ui.inventory.StockUiModel
import com.denticode.kt.ui.inventory.SuppliersDialog
import com.denticode.kt.ui.inventory.TransferStockDialog
import com.denticode.kt.ui.inventory.buildReorderOrderItems
import com.denticode.kt.ui.inventory.toExportRow
import com.denticode.kt.ui.inventory.toUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@Composable
fun InventoryStockScreen(repo: DentiRepository) {
    val messenger = LocalAppMessenger.current
    val scope = rememberCoroutineScope()
    var lines by remember { mutableStateOf<List<InventoryLineRow>>(emptyList()) }
    var movements by remember { mutableStateOf<List<InventoryMovementRow>>(emptyList()) }
    var consultories by remember { mutableStateOf<List<Consultory>>(emptyList()) }
    var facilities by remember { mutableStateOf<List<TreatmentFacilityRow>>(emptyList()) }
    var suppliers by remember { mutableStateOf<List<Supplier>>(emptyList()) }
    var orders by remember { mutableStateOf<List<PurchaseOrderRow>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    var showNewDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<StockUiModel?>(null) }
    var adjustTarget by remember { mutableStateOf<StockUiModel?>(null) }
    var transferTarget by remember { mutableStateOf<StockUiModel?>(null) }
    var showSuppliersDialog by remember { mutableStateOf(false) }
    var showOrdersDialog by remember { mutableStateOf(false) }
    var showNewOrderDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    suspend fun loadData() {
        withContext(Dispatchers.IO) {
            lines = repo.loadInventoryDirectory().second
            movements = repo.listInventoryMovements(limit = 500)
            consultories = repo.listConsultories()
            facilities = repo.listTreatmentFacilities()
            suppliers = repo.listSuppliers()
            orders = repo.listPurchaseOrders()
        }
    }

    LaunchedEffect(Unit) {
        loadData()
        loaded = true
    }

    fun closeDialogs() {
        showNewDialog = false
        editTarget = null
        adjustTarget = null
        transferTarget = null
        errorMessage = null
    }

    fun perform(
        operation: suspend () -> Unit,
        successMessage: String,
    ) {
        scope.launch {
            isSaving = true
            errorMessage = null
            try {
                withContext(Dispatchers.IO) { operation() }
                loadData()
                closeDialogs()
                messenger.showSuccess(successMessage)
            } catch (e: Exception) {
                errorMessage = e.message ?: "No se pudo completar la operación."
                messenger.showError(errorMessage ?: "No se pudo completar la operación.")
            } finally {
                isSaving = false
            }
        }
    }

    fun exportInventory() {
        scope.launch {
            val file =
                ExportService.pickSaveFile("inventario-${LocalDate.now()}.csv")
                    ?: return@launch
            val content = renderInventoryCsv(lines.map { it.toUiModel().toExportRow() })
            ExportService.writeTextFile(file, content)
            messenger.showSuccess("Inventario exportado a ${file.name}.")
        }
    }

    val existingKeys =
        remember(lines) {
            lines.map { it.consultoryId to it.facilityId }.toSet()
        }

    val suggestedOrderItems =
        remember(lines) {
            buildReorderOrderItems(lines)
        }

    if (!loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator()
        }
        return
    }

    ModernStockContent(
        inventoryLines = lines,
        movements = movements,
        onNewItemClick = {
            errorMessage = null
            showNewDialog = true
        },
        onExportClick = ::exportInventory,
        onSuppliersClick = {
            errorMessage = null
            showSuppliersDialog = true
        },
        onOrdersClick = {
            errorMessage = null
            showOrdersDialog = true
        },
        onEdit = {
            errorMessage = null
            editTarget = it
        },
        onAdjustStock = {
            errorMessage = null
            adjustTarget = it
        },
        onTransfer = {
            errorMessage = null
            transferTarget = it
        },
        modifier = Modifier.fillMaxSize(),
    )

    if (showNewDialog) {
        NewStockDialog(
            consultories = consultories,
            facilities = facilities,
            existingKeys = existingKeys,
            isSaving = isSaving,
            errorMessage = errorMessage,
            onDismiss = {
                showNewDialog = false
                errorMessage = null
            },
            onSubmit = { consultoryId, facilityId, quantity ->
                perform(
                    operation = { repo.registerInventoryLine(consultoryId, facilityId, quantity) },
                    successMessage = "Insumo registrado en stock.",
                )
            },
        )
    }

    editTarget?.let { item ->
        EditStockDialog(
            item = item,
            isSaving = isSaving,
            errorMessage = errorMessage,
            onDismiss = {
                editTarget = null
                errorMessage = null
            },
            onSubmit = { newQuantity ->
                val delta = newQuantity - item.quantity
                perform(
                    operation = {
                        if (delta != 0) {
                            repo.adjustInventoryStock(
                                consultoryId = item.row.consultoryId,
                                facilityId = item.row.facilityId,
                                quantityDelta = delta,
                                note = "Edición de stock",
                            )
                        }
                    },
                    successMessage = "Stock actualizado a $newQuantity ${item.unitLabel}.",
                )
            },
        )
    }

    adjustTarget?.let { item ->
        AdjustStockDialog(
            item = item,
            isSaving = isSaving,
            errorMessage = errorMessage,
            onDismiss = {
                adjustTarget = null
                errorMessage = null
            },
            onSubmit = { delta, note ->
                perform(
                    operation = {
                        repo.adjustInventoryStock(
                            consultoryId = item.row.consultoryId,
                            facilityId = item.row.facilityId,
                            quantityDelta = delta,
                            note = note.ifBlank { "Ajuste manual de stock" },
                        )
                    },
                    successMessage = "Stock ajustado ($delta).",
                )
            },
        )
    }

    transferTarget?.let { item ->
        TransferStockDialog(
            item = item,
            consultories = consultories,
            isSaving = isSaving,
            errorMessage = errorMessage,
            onDismiss = {
                transferTarget = null
                errorMessage = null
            },
            onSubmit = { toConsultoryId, quantity, note ->
                perform(
                    operation = {
                        repo.transferInventoryStock(
                            fromConsultoryId = item.row.consultoryId,
                            fromFacilityId = item.row.facilityId,
                            toConsultoryId = toConsultoryId,
                            quantity = quantity,
                            note = note.ifBlank { "Transferencia de stock" },
                        )
                    },
                    successMessage = "Transferencia de $quantity ${item.unitLabel} realizada.",
                )
            },
        )
    }

    if (showSuppliersDialog) {
        SuppliersDialog(
            suppliers = suppliers,
            isSaving = isSaving,
            errorMessage = errorMessage,
            onDismiss = {
                showSuppliersDialog = false
                errorMessage = null
            },
            onRegister = { req ->
                perform(
                    operation = {
                        repo.findSupplierByName(req.name)
                            ?.let { throw IllegalArgumentException("Ya existe un proveedor con el nombre \"${req.name}\".") }
                        repo.registerSupplier(req)
                    },
                    successMessage = "Proveedor registrado.",
                )
            },
            onUpdate = { id, req ->
                perform(
                    operation = {
                        repo.findSupplierByName(req.name, id)
                            ?.let { throw IllegalArgumentException("Ya existe un proveedor con el nombre \"${req.name}\".") }
                        repo.updateSupplier(
                            id,
                            SupplierUpdateRequest(
                                name = req.name,
                                contactName = req.contactName,
                                phone = req.phone,
                                email = req.email,
                                address = req.address,
                                notes = req.notes,
                                isActive = req.isActive,
                            ),
                        )
                    },
                    successMessage = "Proveedor actualizado.",
                )
            },
            onDelete = { id ->
                perform(
                    operation = { repo.hardDeleteSupplier(id) },
                    successMessage = "Proveedor eliminado.",
                )
            },
        )
    }

    if (showOrdersDialog) {
        PurchaseOrdersDialog(
            orders = orders,
            isSaving = isSaving,
            errorMessage = errorMessage,
            onDismiss = {
                showOrdersDialog = false
                errorMessage = null
            },
            onNewOrder = {
                showOrdersDialog = false
                showNewOrderDialog = true
                errorMessage = null
            },
            onReceive = { orderId ->
                perform(
                    operation = { repo.receivePurchaseOrder(orderId) },
                    successMessage = "Pedido recibido: stock acreditado.",
                )
            },
            onDelete = { orderId ->
                perform(
                    operation = { repo.deletePurchaseOrder(orderId) },
                    successMessage = "Pedido eliminado.",
                )
            },
        )
    }

    if (showNewOrderDialog) {
        NewPurchaseOrderDialog(
            suppliers = suppliers,
            consultories = consultories,
            facilities = facilities,
            suggestedItems = suggestedOrderItems,
            isSaving = isSaving,
            errorMessage = errorMessage,
            onDismiss = {
                showNewOrderDialog = false
                errorMessage = null
            },
            onSubmit = { req ->
                showNewOrderDialog = false
                perform(
                    operation = { repo.registerPurchaseOrder(req) },
                    successMessage = "Pedido creado.",
                )
            },
        )
    }
}
