package com.denticode.kt.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.InventoryLineRow
import com.denticode.kt.data.InventoryMovementRow
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.inventory.ModernStockContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun InventoryStockScreen(repo: DentiRepository) {
    val messenger = LocalAppMessenger.current
    var lines by remember { mutableStateOf<List<InventoryLineRow>>(emptyList()) }
    var movements by remember { mutableStateOf<List<InventoryMovementRow>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            lines = repo.loadInventoryDirectory().second
            movements = repo.listInventoryMovements(limit = 500)
        }
    }

    ModernStockContent(
        inventoryLines = lines,
        movements = movements,
        onNewItemClick = {
            messenger.showSuccess("Registro de insumos próximamente.")
        },
        onExportClick = {
            messenger.showSuccess("Exportación de inventario próximamente.")
        },
        onItemAction = { _, _ -> },
        modifier = Modifier.fillMaxSize(),
    )
}
