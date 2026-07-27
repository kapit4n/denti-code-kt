package com.denticode.kt.ui.framework

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.denticode.kt.ui.components.inputs.AppNumberField
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

@Composable
fun FormPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Teléfono",
    placeholder: String = "+591 60000000",
    enabled: Boolean = true,
) {
    val error = Validators.phone(value)
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
    )
    if (error != null && value.isNotEmpty()) {
        Text(error, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier)
    }
}

@Composable
fun FormEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Correo",
    placeholder: String = "correo@ejemplo.com",
    enabled: Boolean = true,
) {
    val error = Validators.email(value)
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
    )
    if (error != null && value.isNotEmpty()) {
        Text(error, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier)
    }
}

@Composable
fun FormRequiredField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    errorMessage: String? = null,
) {
    val error = errorMessage ?: Validators.required(value, label)
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = "$label *",
        placeholder = placeholder,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
    )
    if (error != null && value.isNotEmpty()) {
        Text(error, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier)
    }
}

@Composable
fun FormNumericField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String? = null,
    enabled: Boolean = true,
    allowDecimal: Boolean = true,
) {
    AppNumberField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        enabled = enabled,
        allowDecimal = allowDecimal,
    )
}
