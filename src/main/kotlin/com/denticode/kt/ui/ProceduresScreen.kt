@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.ProcedureTypeRegisterRequest
import com.denticode.kt.data.ProcedureTypeRow
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.cards.AppCard
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.navigation.PageHeader
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProceduresScreen(repo: DentiRepository) {
    var rows by remember { mutableStateOf<List<ProcedureTypeRow>>(emptyList()) }
    var refreshNonce by remember { mutableStateOf(0) }
    var showRegister by remember { mutableStateOf(false) }
    var registerBusy by remember { mutableStateOf(false) }
    var registerError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(refreshNonce) {
        rows = withContext(Dispatchers.IO) { repo.listProcedureTypes() }
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        PageHeader(
            title = "Catálogo clínico",
            subtitle = "Tratamientos (procedure types): precio estándar, duración y vínculo con citas y pagos.",
            modifier = Modifier.fillMaxWidth(),
            actions = {
                AppButton(
                    text = "Registrar tratamiento",
                    onClick = {
                        registerError = null
                        showRegister = true
                    },
                )
            },
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            items(rows, key = { it.id }) { pr ->
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        Text(pr.name, style = AppTypography.CardTitle, color = MaterialTheme.colorScheme.onSurface)
                        pr.description?.let {
                            Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        val price = pr.standardPrice?.let { formatMoney(it) } ?: "—"
                        val dur = pr.defaultDurationMinutes?.let { formatDuration(it) } ?: "—"
                        Text(
                            "$dur · $price · diente: ${if (pr.requiresToothSpecification) "sí" else "no"}",
                            style = AppTypography.Body,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        pr.category?.let {
                            Text("Categoría: $it", style = AppTypography.Caption, color = MaterialTheme.colorScheme.outline)
                        }
                        if (!pr.isActive) {
                            Text("Inactivo", style = AppTypography.Caption, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showRegister) {
        RegisterProcedureDialog(
            isSaving = registerBusy,
            errorMessage = registerError,
            onDismiss = {
                if (!registerBusy) {
                    showRegister = false
                    registerError = null
                }
            },
            onSubmit = { req ->
                registerBusy = true
                registerError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) { repo.registerProcedureType(req) }
                    }.onSuccess {
                        refreshNonce++
                        showRegister = false
                    }.onFailure { e ->
                        registerError = e.message ?: "No se pudo registrar el tratamiento."
                    }
                    registerBusy = false
                }
            },
        )
    }
}

@Composable
private fun RegisterProcedureDialog(
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (ProcedureTypeRegisterRequest) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var requiresTooth by remember { mutableStateOf(false) }
    var isActive by remember { mutableStateOf(true) }

    val durationMinutes = durationText.trim().toIntOrNull()
    val price = parseMoneyAmount(priceText)
    val canSubmit = name.trim().isNotEmpty() && !isSaving

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 520.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                text = "Nuevo tratamiento",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Se guardará en el catálogo y podrá vincularse a citas y pagos.",
                style = AppTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nombre",
                placeholder = "Limpieza, endodoncia…",
                enabled = !isSaving,
            )
            AppTextArea(
                value = description,
                onValueChange = { description = it },
                label = "Descripción (opcional)",
                enabled = !isSaving,
                minLines = 2,
                maxLines = 4,
            )
            AppTextField(
                value = durationText,
                onValueChange = { durationText = it.filter { ch -> ch.isDigit() }.take(4) },
                label = "Duración por defecto (minutos)",
                placeholder = "45",
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            AppTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = "Precio estándar (opcional)",
                placeholder = "120,00",
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            AppTextField(
                value = category,
                onValueChange = { category = it },
                label = "Categoría (opcional)",
                enabled = !isSaving,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Checkbox(checked = requiresTooth, onCheckedChange = { requiresTooth = it }, enabled = !isSaving)
                Text("Requiere especificar diente", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurface)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Checkbox(checked = isActive, onCheckedChange = { isActive = it }, enabled = !isSaving)
                Text("Activo en el catálogo", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurface)
            }
            errorMessage?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Guardando…" else "Registrar",
                    onClick = {
                        onSubmit(
                            ProcedureTypeRegisterRequest(
                                name = name.trim(),
                                description = description.trim().takeIf { it.isNotEmpty() },
                                defaultDurationMinutes = durationMinutes,
                                standardPrice = price,
                                requiresToothSpecification = requiresTooth,
                                category = category.trim().takeIf { it.isNotEmpty() },
                                isActive = isActive,
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}
