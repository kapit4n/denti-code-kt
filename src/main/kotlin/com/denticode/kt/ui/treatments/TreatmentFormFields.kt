package com.denticode.kt.ui.treatments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.denticode.kt.data.ProcedureTypeRow
import com.denticode.kt.data.TreatmentStatus
import com.denticode.kt.data.treatmentStatusOptions
import com.denticode.kt.ui.components.inputs.AppDropdownField
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.formatMoney
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

@Composable
fun TreatmentPricingFields(
    procedureTypes: List<ProcedureTypeRow>,
    selectedProcedureTypeId: Int?,
    priceText: String,
    onPriceTextChange: (String) -> Unit,
    treatmentStatus: TreatmentStatus,
    onTreatmentStatusChange: (TreatmentStatus) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val statusOptions = treatmentStatusOptions()
    val standardPrice =
        selectedProcedureTypeId?.let { id -> procedureTypes.find { it.id == id }?.standardPrice }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        standardPrice?.let { sp ->
            Text(
                "Precio catálogo: Bs ${formatMoney(sp)}",
                style = AppTypography.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            AppTextField(
                value = priceText,
                onValueChange = onPriceTextChange,
                label = "Precio acordado",
                placeholder = standardPrice?.let { formatMoney(it) } ?: "120,00",
                enabled = enabled && selectedProcedureTypeId != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
            )
            AppDropdownField(
                label = "Estado del tratamiento",
                options = statusOptions,
                selected = treatmentStatus,
                onSelected = onTreatmentStatusChange,
                enabled = enabled && selectedProcedureTypeId != null,
                optionLabel = { it.labelEs },
                placeholder = "Estado…",
                modifier = Modifier.weight(1f),
            )
        }
        if (selectedProcedureTypeId == null) {
            Text(
                "Seleccione un tratamiento para registrar precio y estado clínico.",
                style = AppTypography.Caption,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

fun applyStandardPriceIfBlank(
    procedureTypes: List<ProcedureTypeRow>,
    procedureTypeId: Int?,
    currentPriceText: String,
): String {
    if (!currentPriceText.isBlank() || procedureTypeId == null) return currentPriceText
    val sp = procedureTypes.find { it.id == procedureTypeId }?.standardPrice ?: return currentPriceText
    return formatMoney(sp)
}
