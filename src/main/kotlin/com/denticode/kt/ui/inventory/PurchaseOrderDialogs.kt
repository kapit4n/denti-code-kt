@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.Consultory
import com.denticode.kt.data.PurchaseOrderItemRequest
import com.denticode.kt.data.PurchaseOrderRegisterRequest
import com.denticode.kt.data.PurchaseOrderRow
import com.denticode.kt.data.Supplier
import com.denticode.kt.data.TreatmentFacilityRow
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.dialogs.AppBasicDialog
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.inputs.AppDropdownField
import com.denticode.kt.ui.components.inputs.AppNumberField
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val orderDateFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es", "ES"))

private fun orderStatusLabel(status: String): String =
    when (status.uppercase()) {
        "RECEIVED" -> "Recibido"
        else -> "Pendiente"
    }

private fun orderStatusColor(status: String): Color =
    when (status.uppercase()) {
        "RECEIVED" -> Color(0xFF059669)
        else -> Color(0xFFB45309)
    }

private fun formatBs(value: Double): String = "Bs ${"%,.2f".format(value).replace(',', ' ')}"

@Composable
fun PurchaseOrdersDialog(
    orders: List<PurchaseOrderRow>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onNewOrder: () -> Unit,
    onReceive: (Int) -> Unit,
    onDelete: (Int) -> Unit,
) {
    var deleting by remember { mutableStateOf<PurchaseOrderRow?>(null) }

    deleting?.let { order ->
        AppBasicDialog(
            title = "Eliminar pedido",
            message = "¿Eliminar el pedido #${order.id}?",
            onDismissRequest = { deleting = null },
            confirmText = "Eliminar",
            onConfirm = {
                onDelete(order.id)
                deleting = null
            },
            dismissText = "Cancelar",
            onDismiss = { deleting = null },
        )
        return
    }

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 760.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Pedidos",
                        style = AppTypography.SectionTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Órdenes de compra a proveedores. Recibir un pedido acredita el stock.",
                        style = AppTypography.Caption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                AppButton(
                    text = "Nuevo pedido",
                    onClick = onNewOrder,
                    leadingIcon = { Icon(Icons.Default.ShoppingCart, null, Modifier.size(18.dp)) },
                )
            }

            errorMessage?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error)
            }

            if (orders.isEmpty()) {
                Text(
                    "No hay pedidos registrados.",
                    style = AppTypography.BodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    orders.forEach { order ->
                        val received = order.status.uppercase() == "RECEIVED"
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = orderStatusColor(order.status),
                                modifier = Modifier.size(20.dp),
                            )
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "Pedido #${order.id} · ${order.supplierName ?: "Sin proveedor"}",
                                    style = AppTypography.Body,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                val date =
                                    Instant.ofEpochMilli(order.orderDateEpochMs)
                                        .atZone(ZoneId.systemDefault())
                                        .format(orderDateFmt)
                                Text(
                                    "$date · ${order.itemCount} líneas · ${formatBs(order.totalCost)} · ${orderStatusLabel(order.status)}",
                                    style = AppTypography.Caption,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (!received) {
                                AppOutlinedButton(
                                    text = "Recibir",
                                    onClick = { onReceive(order.id) },
                                    enabled = !isSaving,
                                    minHeight = 36.dp,
                                )
                                IconButton(onClick = { deleting = order }, enabled = !isSaving) {
                                    Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cerrar", onClick = onDismiss)
            }
        }
    }
}

private class EditableOrderLine(
    var consultoryId: Int? = null,
    var facilityId: Int? = null,
    var quantity: String = "",
    var unitCost: String = "",
)

@Composable
fun NewPurchaseOrderDialog(
    suppliers: List<Supplier>,
    consultories: List<Consultory>,
    facilities: List<TreatmentFacilityRow>,
    suggestedItems: List<PurchaseOrderItemRequest>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (PurchaseOrderRegisterRequest) -> Unit,
) {
    var selectedSupplier by remember(suppliers) { mutableStateOf(suppliers.firstOrNull()) }
    var notes by remember { mutableStateOf("") }
    val lines = remember { mutableStateListOf(EditableOrderLine()) }

    fun suggestFrom(suggested: List<PurchaseOrderItemRequest>) {
        lines.clear()
        if (suggested.isEmpty()) return
        suggested.forEach { item ->
            lines.add(
                EditableOrderLine(
                    consultoryId = item.consultoryId,
                    facilityId = item.facilityId,
                    quantity = item.quantity.toString(),
                    unitCost = "0",
                ),
            )
        }
    }

    val validLines = lines.filter { it.quantity.toIntOrNull() ?: 0 > 0 && it.consultoryId != null && it.facilityId != null }
    val totalCost = validLines.sumOf { (it.quantity.toIntOrNull() ?: 0) * (it.unitCost.toDoubleOrNull() ?: 0.0) }
    val canSubmit = !isSaving && validLines.isNotEmpty()

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 860.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Nuevo pedido",
                        style = AppTypography.SectionTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Define las líneas del pedido; al recibirlo se acredita el stock.",
                        style = AppTypography.Caption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                AppOutlinedButton(
                    text = "Sugerir reposición",
                    onClick = { suggestFrom(suggestedItems) },
                    enabled = !isSaving && suggestedItems.isNotEmpty(),
                    leadingIcon = { Icon(Icons.Default.Add, null, Modifier.size(16.dp)) },
                    minHeight = 40.dp,
                )
            }

            AppDropdownField(
                label = "Proveedor",
                options = suppliers,
                selected = selectedSupplier,
                onSelected = { selectedSupplier = it },
                enabled = !isSaving,
                optionLabel = { it.name },
                placeholder = "Seleccionar proveedor…",
            )
            AppTextArea(
                value = notes,
                onValueChange = { notes = it },
                label = "Notas",
                placeholder = "Referencia del pedido…",
                enabled = !isSaving,
            )

            Text("Líneas del pedido", style = AppTypography.Body, fontWeight = FontWeight.SemiBold)

            lines.forEachIndexed { index, line ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppDropdownField(
                        label = "Consultorio",
                        options = consultories,
                        selected = consultories.find { it.id == line.consultoryId },
                        onSelected = {
                            line.consultoryId = it.id
                            line.facilityId = null
                        },
                        enabled = !isSaving,
                        optionLabel = { consultory ->
                            buildString {
                                append(consultory.name)
                                consultory.shortCode?.let { append(" ($it)") }
                            }
                        },
                        placeholder = "…",
                        modifier = Modifier.weight(1.2f),
                    )
                    AppDropdownField(
                        label = "Insumo",
                        options = facilities,
                        selected = facilities.find { it.id == line.facilityId },
                        onSelected = { line.facilityId = it.id },
                        enabled = !isSaving && line.consultoryId != null,
                        optionLabel = { f -> "${f.displayName} · ${categoryLabelEs(f.categoryKey)}" },
                        placeholder = "…",
                        searchable = true,
                        searchPlaceholder = "Buscar insumo…",
                        modifier = Modifier.weight(1.4f),
                    )
                    AppNumberField(
                        value = line.quantity,
                        onValueChange = { line.quantity = it },
                        label = "Cant.",
                        placeholder = "0",
                        enabled = !isSaving,
                        allowDecimal = false,
                        modifier = Modifier.widthIn(min = 80.dp),
                    )
                    AppNumberField(
                        value = line.unitCost,
                        onValueChange = { line.unitCost = it },
                        label = "Costo/uds",
                        placeholder = "0.00",
                        enabled = !isSaving,
                        allowDecimal = true,
                        modifier = Modifier.widthIn(min = 100.dp),
                    )
                    IconButton(onClick = { lines.removeAt(index) }, enabled = !isSaving && lines.size > 1) {
                        Icon(Icons.Default.Delete, "Quitar línea", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppOutlinedButton(
                    text = "Agregar línea",
                    onClick = { lines.add(EditableOrderLine()) },
                    enabled = !isSaving,
                )
                Text(
                    "Total: ${formatBs(totalCost)}",
                    style = AppTypography.Body,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            errorMessage?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error)
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Guardando..." else "Crear pedido",
                    onClick = {
                        onSubmit(
                            PurchaseOrderRegisterRequest(
                                supplierId = selectedSupplier?.id,
                                notes = notes.trim().ifBlank { null },
                                items =
                                    validLines.map {
                                        PurchaseOrderItemRequest(
                                            consultoryId = it.consultoryId!!,
                                            facilityId = it.facilityId!!,
                                            quantity = it.quantity.toInt(),
                                            unitCost = it.unitCost.toDoubleOrNull() ?: 0.0,
                                        )
                                    },
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}
