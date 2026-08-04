@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.Consultory
import com.denticode.kt.data.TreatmentFacilityRow
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.inputs.AppDropdownField
import com.denticode.kt.ui.components.inputs.AppNumberField
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

private fun consultoryLabel(c: Consultory): String =
    buildString {
        append(c.name)
        c.shortCode?.let { append(" ($it)") }
    }

private fun facilityLabel(f: TreatmentFacilityRow): String =
    "${f.displayName} · ${f.code} · ${categoryLabelEs(f.categoryKey)}"

@Composable
fun NewStockDialog(
    consultories: List<Consultory>,
    facilities: List<TreatmentFacilityRow>,
    existingKeys: Set<Pair<Int, Int>>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (consultoryId: Int, facilityId: Int, quantity: Int) -> Unit,
) {
    var selectedConsultory by remember(consultories) { mutableStateOf(consultories.firstOrNull()) }
    val availableFacilities =
        remember(selectedConsultory, facilities, existingKeys) {
            val consultoryId = selectedConsultory?.id
            if (consultoryId == null) {
                emptyList()
            } else {
                facilities.filter { facility -> (consultoryId to facility.id) !in existingKeys }
            }
        }
    var selectedFacility by remember(availableFacilities) {
        mutableStateOf(availableFacilities.firstOrNull())
    }
    var quantity by remember { mutableStateOf("") }
    val quantityValue = quantity.toIntOrNull()

    val canSubmit =
        !isSaving &&
            selectedConsultory != null &&
            selectedFacility != null &&
            availableFacilities.isNotEmpty() &&
            quantityValue != null &&
            quantityValue >= 0

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 560.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                "Nuevo insumo en stock",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AppDropdownField(
                label = "Consultorio",
                options = consultories,
                selected = selectedConsultory,
                onSelected = {
                    selectedConsultory = it
                    selectedFacility = null
                },
                enabled = !isSaving,
                optionLabel = ::consultoryLabel,
                placeholder = "Selecciona consultorio…",
            )
            AppDropdownField(
                label = "Insumo",
                options = availableFacilities,
                selected = selectedFacility,
                onSelected = { selectedFacility = it },
                enabled = !isSaving && availableFacilities.isNotEmpty(),
                optionLabel = ::facilityLabel,
                placeholder = "Selecciona insumo…",
                searchable = true,
                searchPlaceholder = "Buscar insumo…",
            )
            AppNumberField(
                value = quantity,
                onValueChange = { quantity = it },
                label = "Cantidad inicial",
                placeholder = "0",
                enabled = !isSaving,
                allowDecimal = false,
            )
            errorMessage?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Guardando..." else "Registrar insumo",
                    onClick = {
                        val consultory = selectedConsultory ?: return@AppButton
                        val facility = selectedFacility ?: return@AppButton
                        onSubmit(consultory.id, facility.id, quantityValue ?: 0)
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}

@Composable
fun EditStockDialog(
    item: StockUiModel,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (newQuantity: Int) -> Unit,
) {
    var quantity by remember(item.lineId) { mutableStateOf(item.quantity.toString()) }
    val quantityValue = quantity.toIntOrNull()

    val canSubmit =
        !isSaving &&
            quantityValue != null &&
            quantityValue >= 0 &&
            quantityValue != item.quantity

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 480.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                "Editar stock — ${item.productName}",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Consultorio: ${item.consultoryLabel}. Stock actual: ${item.quantity} ${item.unitLabel}.",
                style = AppTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppNumberField(
                value = quantity,
                onValueChange = { quantity = it },
                label = "Nuevo total",
                placeholder = "0",
                enabled = !isSaving,
                allowDecimal = false,
            )
            errorMessage?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Guardando..." else "Guardar cambio",
                    onClick = { onSubmit(quantityValue ?: item.quantity) },
                    enabled = canSubmit,
                )
            }
        }
    }
}

@Composable
fun AdjustStockDialog(
    item: StockUiModel,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (delta: Int, note: String) -> Unit,
) {
    var delta by remember(item.lineId) { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val deltaValue = delta.toIntOrNull()
    val projected = deltaValue?.let { item.quantity + it }

    val canSubmit = !isSaving && deltaValue != null && deltaValue != 0 && (projected ?: -1) >= 0

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 520.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                "Ajustar stock — ${item.productName}",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Consultorio: ${item.consultoryLabel}. Stock actual: ${item.quantity} ${item.unitLabel}.",
                style = AppTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppNumberField(
                value = delta,
                onValueChange = { delta = it },
                label = "Cambio (usar - para descontar)",
                placeholder = "Ej. 10 o -5",
                enabled = !isSaving,
                allowDecimal = false,
                allowNegative = true,
            )
            projected?.let {
                Text(
                    "Quedaría: $it ${item.unitLabel}.",
                    style = AppTypography.BodySmall,
                    color =
                        if (it < 0) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }
            AppTextArea(
                value = note,
                onValueChange = { note = it },
                label = "Motivo (opcional)",
                placeholder = "Ej. Recepción de pedido, merma, corrección…",
                enabled = !isSaving,
                minLines = 2,
                maxLines = 4,
            )
            errorMessage?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Guardando..." else "Aplicar ajuste",
                    onClick = { onSubmit(deltaValue ?: 0, note.trim()) },
                    enabled = canSubmit,
                )
            }
        }
    }
}

@Composable
fun TransferStockDialog(
    item: StockUiModel,
    consultories: List<Consultory>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (toConsultoryId: Int, quantity: Int, note: String) -> Unit,
) {
    val targetOptions =
        remember(consultories, item) {
            consultories.filter { it.id != item.row.consultoryId }
        }
    var selectedTarget by remember(targetOptions) { mutableStateOf(targetOptions.firstOrNull()) }
    var quantity by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val quantityValue = quantity.toIntOrNull()

    val canSubmit =
        !isSaving &&
            selectedTarget != null &&
            quantityValue != null &&
            quantityValue > 0 &&
            quantityValue <= item.quantity

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 520.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                "Transferir — ${item.productName}",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Desde: ${item.consultoryLabel}. Disponible: ${item.quantity} ${item.unitLabel}.",
                style = AppTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppDropdownField(
                label = "Consultorio destino",
                options = targetOptions,
                selected = selectedTarget,
                onSelected = { selectedTarget = it },
                enabled = !isSaving && targetOptions.isNotEmpty(),
                optionLabel = ::consultoryLabel,
                placeholder = "Selecciona consultorio…",
            )
            AppNumberField(
                value = quantity,
                onValueChange = { quantity = it },
                label = "Cantidad a transferir",
                placeholder = "1..${item.quantity}",
                enabled = !isSaving,
                allowDecimal = false,
            )
            AppTextArea(
                value = note,
                onValueChange = { note = it },
                label = "Nota (opcional)",
                placeholder = "Ej. Reposición entre consultorios…",
                enabled = !isSaving,
                minLines = 2,
                maxLines = 4,
            )
            errorMessage?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Transferiendo..." else "Transferir",
                    onClick = {
                        val target = selectedTarget ?: return@AppButton
                        onSubmit(target.id, quantityValue ?: 0, note.trim())
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}
