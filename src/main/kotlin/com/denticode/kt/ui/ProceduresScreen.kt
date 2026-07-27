@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.denticode.kt.data.PatientTreatmentRow
import com.denticode.kt.data.ProcedureTypeRegisterRequest
import com.denticode.kt.data.ProcedureTypeRow
import com.denticode.kt.data.ProcedureTypeUpdateRequest
import com.denticode.kt.data.TreatmentStatus
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.cards.AppCard
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.feedback.LoadingIndicator
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.navigation.PageHeader
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import com.denticode.kt.ui.treatments.TreatmentsTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProceduresScreen(repo: DentiRepository) {
    var rows by remember { mutableStateOf<List<ProcedureTypeRow>>(emptyList()) }
    var performedTreatments by remember { mutableStateOf<List<PatientTreatmentRow>>(emptyList()) }
    var refreshNonce by remember { mutableStateOf(0) }
    var showRegister by remember { mutableStateOf(false) }
    var registerBusy by remember { mutableStateOf(false) }
    var registerError by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var editingProcedure by remember { mutableStateOf<ProcedureTypeRow?>(null) }
    var editBusy by remember { mutableStateOf(false) }
    var editError by remember { mutableStateOf<String?>(null) }

    var archivingProcedure by remember { mutableStateOf<ProcedureTypeRow?>(null) }
    var deletingProcedure by remember { mutableStateOf<ProcedureTypeRow?>(null) }
    var deleteBusy by remember { mutableStateOf(false) }

    var treatmentStatusDialog by remember { mutableStateOf<PatientTreatmentRow?>(null) }
    var statusBusy by remember { mutableStateOf(false) }
    var deletingTreatment by remember { mutableStateOf<PatientTreatmentRow?>(null) }
    var treatmentDeleteBusy by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val messenger = LocalAppMessenger.current

    LaunchedEffect(refreshNonce) {
        withContext(Dispatchers.IO) {
            rows = repo.listProcedureTypes()
            performedTreatments = repo.listAllTreatments(200)
        }
        loaded = true
    }

    val filteredRows = remember(rows, searchQuery) {
        if (searchQuery.isBlank()) rows
        else rows.filter { pr ->
            pr.name.contains(searchQuery, ignoreCase = true) ||
                pr.description.orEmpty().contains(searchQuery, ignoreCase = true) ||
                pr.category.orEmpty().contains(searchQuery, ignoreCase = true)
        }
    }

    if (!loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator()
        }
    } else {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
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
            AppTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Buscar tratamiento",
                placeholder = "Nombre, descripción o categoría…",
                modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg),
            )
            LazyColumn(
                modifier = Modifier.weight(0.45f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                items(filteredRows, key = { it.id }) { pr ->
                    ProcedureTypeCard(
                        procedureType = pr,
                        onEdit = {
                            editError = null
                            editingProcedure = pr
                        },
                        onArchive = { archivingProcedure = pr },
                        onRestore = {
                            scope.launch {
                                runCatching {
                                    withContext(Dispatchers.IO) { repo.restoreProcedureType(pr.id) }
                                }.onSuccess {
                                    refreshNonce++
                                    messenger.showSuccess("Tratamiento restaurado.")
                                }.onFailure { e ->
                                    messenger.showError(e.message ?: "Error al restaurar.")
                                }
                            }
                        },
                        onDelete = { deletingProcedure = pr },
                    )
                }
                if (filteredRows.isEmpty() && searchQuery.isNotBlank()) {
                    item {
                        Text(
                            "No se encontraron tratamientos para \"$searchQuery\".",
                            modifier = Modifier.padding(AppSpacing.lg),
                            style = AppTypography.BodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text(
                "Tratamientos realizados en clínica",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            TreatmentsTable(
                treatments = performedTreatments,
                showPatientColumn = true,
                modifier = Modifier.weight(0.55f),
                emptyMessage = "Aún no hay tratamientos vinculados a pacientes. Regístrelos al crear citas con tratamiento.",
                onStatusChange = { treatment -> treatmentStatusDialog = treatment },
                onDelete = { treatment -> deletingTreatment = treatment },
            )
        }
    }

    if (showRegister) {
        ProcedureTypeFormDialog(
            title = "Nuevo tratamiento",
            subtitle = "Se guardará en el catálogo y podrá vincularse a citas y pagos.",
            submitLabel = "Registrar",
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
                        withContext(Dispatchers.IO) {
                            repo.findProcedureTypeByName(req.name)?.let {
                                throw IllegalArgumentException("Ya existe un tratamiento con el nombre \"${req.name}\".")
                            }
                            repo.registerProcedureType(
                                ProcedureTypeRegisterRequest(
                                    name = req.name,
                                    description = req.description,
                                    defaultDurationMinutes = req.defaultDurationMinutes,
                                    standardPrice = req.standardPrice,
                                    requiresToothSpecification = req.requiresToothSpecification,
                                    category = req.category,
                                    isActive = req.isActive,
                                ),
                            )
                        }
                    }.onSuccess {
                        refreshNonce++
                        showRegister = false
                        messenger.showSuccess("Tratamiento registrado correctamente.")
                    }.onFailure { e ->
                        registerError = e.message ?: "No se pudo registrar el tratamiento."
                    }
                    registerBusy = false
                }
            },
        )
    }

    editingProcedure?.let { proc ->
        ProcedureTypeFormDialog(
            title = "Editar tratamiento",
            subtitle = "Actualizar información del catálogo.",
            submitLabel = "Guardar",
            isSaving = editBusy,
            errorMessage = editError,
            initialName = proc.name,
            initialDescription = proc.description.orEmpty(),
            initialDurationText = proc.defaultDurationMinutes?.toString().orEmpty(),
            initialPriceText = proc.standardPrice?.let { formatMoney(it) }.orEmpty(),
            initialCategory = proc.category.orEmpty(),
            initialRequiresTooth = proc.requiresToothSpecification,
            initialIsActive = proc.isActive,
            onDismiss = {
                if (!editBusy) {
                    editingProcedure = null
                    editError = null
                }
            },
            onSubmit = { req ->
                editBusy = true
                editError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.findProcedureTypeByName(req.name, excludeId = proc.id)?.let {
                                throw IllegalArgumentException("Ya existe otro tratamiento con el nombre \"${req.name}\".")
                            }
                            repo.updateProcedureType(proc.id, req)
                        }
                    }.onSuccess {
                        refreshNonce++
                        editingProcedure = null
                        messenger.showSuccess("Tratamiento actualizado correctamente.")
                    }.onFailure { e ->
                        editError = e.message ?: "No se pudo actualizar el tratamiento."
                    }
                    editBusy = false
                }
            },
        )
    }

    archivingProcedure?.let { proc ->
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { repo.archiveProcedureType(proc.id) }
            }.onSuccess {
                archivingProcedure = null
                refreshNonce++
                messenger.showSuccess("Tratamiento archivado.")
            }.onFailure { e ->
                messenger.showError(e.message ?: "Error al archivar tratamiento.")
                archivingProcedure = null
            }
        }
        archivingProcedure = null
    }

    deletingProcedure?.let { proc ->
        ProcedureTypeDeleteDialog(
            procedureName = proc.name,
            isSaving = deleteBusy,
            onDismiss = {
                if (!deleteBusy) {
                    deletingProcedure = null
                }
            },
            onConfirm = {
                deleteBusy = true
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.hardDeleteProcedureType(proc.id)
                        }
                    }.onSuccess {
                        deletingProcedure = null
                        refreshNonce++
                        messenger.showSuccess("Tratamiento eliminado permanentemente.")
                    }.onFailure { e ->
                        messenger.showError(e.message ?: "Error al eliminar tratamiento.")
                    }
                    deleteBusy = false
                }
            },
        )
    }

    treatmentStatusDialog?.let { treatment ->
        TreatmentStatusEditDialog(
            currentStatus = treatment.status,
            isSaving = statusBusy,
            onDismiss = {
                if (!statusBusy) {
                    treatmentStatusDialog = null
                }
            },
            onConfirm = { newStatus ->
                statusBusy = true
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.updateTreatmentStatus(treatment.id, newStatus)
                        }
                    }.onSuccess {
                        treatmentStatusDialog = null
                        refreshNonce++
                        messenger.showSuccess("Estado del tratamiento actualizado.")
                    }.onFailure { e ->
                        messenger.showError(e.message ?: "Error al actualizar estado.")
                    }
                    statusBusy = false
                }
            },
        )
    }

    deletingTreatment?.let { treatment ->
        TreatmentDeleteDialog(
            treatmentLabel = treatment.procedureTypeName,
            patientName = treatment.patientName,
            isSaving = treatmentDeleteBusy,
            onDismiss = {
                if (!treatmentDeleteBusy) {
                    deletingTreatment = null
                }
            },
            onConfirm = {
                treatmentDeleteBusy = true
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.deleteTreatment(treatment.id)
                        }
                    }.onSuccess {
                        deletingTreatment = null
                        refreshNonce++
                        messenger.showSuccess("Tratamiento eliminado.")
                    }.onFailure { e ->
                        messenger.showError(e.message ?: "Error al eliminar tratamiento.")
                    }
                    treatmentDeleteBusy = false
                }
            },
        )
    }
}

@Composable
private fun ProcedureTypeCard(
    procedureType: ProcedureTypeRow,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    procedureType.name,
                    style = AppTypography.CardTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Box {
                    AppOutlinedButton(
                        text = "⋯",
                        onClick = { showMenu = true },
                    )
                    ProcedureTypeActionsDropdown(
                        visible = showMenu,
                        onDismiss = { showMenu = false },
                        onEdit = { showMenu = false; onEdit() },
                        onArchive = { showMenu = false; onArchive() },
                        onRestore = { showMenu = false; onRestore() },
                        onDelete = { showMenu = false; onDelete() },
                        isArchived = procedureType.isArchived,
                    )
                }
            }
            procedureType.description?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val price = procedureType.standardPrice?.let { formatMoney(it) } ?: "—"
            val dur = procedureType.defaultDurationMinutes?.let { formatDuration(it) } ?: "—"
            Text(
                "$dur · $price · diente: ${if (procedureType.requiresToothSpecification) "sí" else "no"}",
                style = AppTypography.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            procedureType.category?.let {
                Text("Categoría: $it", style = AppTypography.Caption, color = MaterialTheme.colorScheme.outline)
            }
            if (!procedureType.isActive) {
                Text("Inactivo", style = AppTypography.Caption, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ProcedureTypeActionsDropdown(
    visible: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    isArchived: Boolean,
) {
    androidx.compose.material3.DropdownMenu(
        expanded = visible,
        onDismissRequest = onDismiss,
    ) {
        if (isArchived) {
            androidx.compose.material3.DropdownMenuItem(
                text = { Text("Restaurar") },
                onClick = onRestore,
            )
        } else {
            androidx.compose.material3.DropdownMenuItem(
                text = { Text("Editar") },
                onClick = onEdit,
            )
            androidx.compose.material3.DropdownMenuItem(
                text = { Text("Archivar") },
                onClick = onArchive,
            )
        }
        androidx.compose.material3.DropdownMenuItem(
            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
            onClick = onDelete,
        )
    }
}

@Composable
private fun ProcedureTypeDeleteDialog(
    procedureName: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 440.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = "Eliminar tratamiento",
                    style = AppTypography.SectionTitle,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = "¿Estás seguro de que deseas eliminar permanentemente \"$procedureName\" del catálogo? Esta acción no se puede deshacer.",
                    style = AppTypography.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Eliminando…" else "Eliminar permanentemente",
                    onClick = onConfirm,
                    enabled = !isSaving,
                )
            }
        }
    }
}

@Composable
private fun TreatmentStatusEditDialog(
    currentStatus: TreatmentStatus,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (TreatmentStatus) -> Unit,
) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    val statusOptions = com.denticode.kt.data.treatmentStatusOptions()

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 400.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                text = "Cambiar estado del tratamiento",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Selecciona el nuevo estado clínico.",
                style = AppTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            com.denticode.kt.ui.components.inputs.AppDropdownField(
                label = "Estado",
                options = statusOptions,
                selected = selectedStatus,
                onSelected = { selectedStatus = it },
                enabled = !isSaving,
                optionLabel = { it.labelEs },
                placeholder = "Estado…",
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Guardando…" else "Guardar",
                    onClick = { onConfirm(selectedStatus) },
                    enabled = !isSaving,
                )
            }
        }
    }
}

@Composable
private fun TreatmentDeleteDialog(
    treatmentLabel: String,
    patientName: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 440.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = "Eliminar tratamiento",
                    style = AppTypography.SectionTitle,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = "¿Estás seguro de que deseas eliminar \"$treatmentLabel\" de $patientName? Se desvinculará de la cita asociada.",
                    style = AppTypography.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Eliminando…" else "Eliminar tratamiento",
                    onClick = onConfirm,
                    enabled = !isSaving,
                )
            }
        }
    }
}

@Composable
private fun ProcedureTypeFormDialog(
    title: String,
    subtitle: String,
    submitLabel: String,
    isSaving: Boolean,
    errorMessage: String?,
    initialName: String = "",
    initialDescription: String = "",
    initialDurationText: String = "",
    initialPriceText: String = "",
    initialCategory: String = "",
    initialRequiresTooth: Boolean = false,
    initialIsActive: Boolean = true,
    onDismiss: () -> Unit,
    onSubmit: (ProcedureTypeUpdateRequest) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    var durationText by remember { mutableStateOf(initialDurationText) }
    var priceText by remember { mutableStateOf(initialPriceText) }
    var category by remember { mutableStateOf(initialCategory) }
    var requiresTooth by remember { mutableStateOf(initialRequiresTooth) }
    var isActive by remember { mutableStateOf(initialIsActive) }

    val durationMinutes = durationText.trim().toIntOrNull()
    val price = parseMoneyAmount(priceText)
    val canSubmit = name.trim().isNotEmpty() && !isSaving

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 520.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                text = title,
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
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
                    text = if (isSaving) "Guardando…" else submitLabel,
                    onClick = {
                        onSubmit(
                            ProcedureTypeUpdateRequest(
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
