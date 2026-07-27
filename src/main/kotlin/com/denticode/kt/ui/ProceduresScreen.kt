@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.CategoryRegisterRequest
import com.denticode.kt.data.CategoryUpdateRequest
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.PatientTreatmentRow
import com.denticode.kt.data.ProcedureTypeRegisterRequest
import com.denticode.kt.data.ProcedureTypeRow
import com.denticode.kt.data.ProcedureTypeUpdateRequest
import com.denticode.kt.data.TreatmentCategory
import com.denticode.kt.data.TreatmentStatus
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.cards.AppCard
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.feedback.LoadingIndicator
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.framework.Validators
import com.denticode.kt.ui.navigation.PageHeader
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import com.denticode.kt.ui.treatments.TreatmentsTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val PRESET_COLORS =
    listOf("#4CAF50", "#2196F3", "#FF9800", "#F44336", "#9C27B0", "#795548", "#E91E63", "#00BCD4", "#8BC34A", "#FF5722", "#607D8B", "#3F51B5")

private val PRESET_ICONS =
    listOf("shield", "search", "build", "local_hospital", "content_cut", "construction", "palette", "straighten", "spa", "emergency", "event_repeat", "favorite", "star", "bolt", "healing", "medical_services")

private enum class SortField { NAME, PRICE, DURATION, CATEGORY, STATUS }
private enum class SortDirection { ASC, DESC }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProceduresScreen(repo: DentiRepository) {
    var rows by remember { mutableStateOf<List<ProcedureTypeRow>>(emptyList()) }
    var categories by remember { mutableStateOf<List<TreatmentCategory>>(emptyList()) }
    var performedTreatments by remember { mutableStateOf<List<PatientTreatmentRow>>(emptyList()) }
    var refreshNonce by remember { mutableStateOf(0) }
    var loaded by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var filterCategoryId by remember { mutableStateOf<Int?>(null) }
    var filterStatus by remember { mutableStateOf<String?>(null) }
    var filterFavoritesOnly by remember { mutableStateOf(false) }
    var sortField by remember { mutableStateOf(SortField.NAME) }
    var sortDirection by remember { mutableStateOf(SortDirection.ASC) }

    var showRegister by remember { mutableStateOf(false) }
    var registerBusy by remember { mutableStateOf(false) }
    var registerError by remember { mutableStateOf<String?>(null) }

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

    var showCategoryManager by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<TreatmentCategory?>(null) }
    var categoryDialogBusy by remember { mutableStateOf(false) }
    var categoryDialogError by remember { mutableStateOf<String?>(null) }
    var deletingCategory by remember { mutableStateOf<TreatmentCategory?>(null) }
    var categoryDeleteBusy by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val messenger = LocalAppMessenger.current

    LaunchedEffect(refreshNonce) {
        withContext(Dispatchers.IO) {
            rows = repo.listProcedureTypes()
            categories = repo.listCategories()
            performedTreatments = repo.listAllTreatments(200)
        }
        loaded = true
    }

    val filteredRows = remember(rows, searchQuery, filterCategoryId, filterStatus, filterFavoritesOnly, sortField, sortDirection) {
        rows.filter { pr ->
            (searchQuery.isBlank() || pr.name.contains(searchQuery, ignoreCase = true) ||
                pr.description.orEmpty().contains(searchQuery, ignoreCase = true) ||
                pr.category.orEmpty().contains(searchQuery, ignoreCase = true)) &&
                (filterCategoryId == null || pr.categoryId == filterCategoryId) &&
                (filterStatus == null || (filterStatus == "active" && pr.isActive) || (filterStatus == "inactive" && !pr.isActive)) &&
                (!filterFavoritesOnly || pr.isFavorite)
        }.sortedWith(
            when (sortField) {
                SortField.NAME -> compareBy<ProcedureTypeRow> { it.name.lowercase() }
                SortField.PRICE -> compareBy { it.standardPrice ?: 0.0 }
                SortField.DURATION -> compareBy { it.defaultDurationMinutes ?: 0 }
                SortField.CATEGORY -> compareBy { it.category.orEmpty().lowercase() }
                SortField.STATUS -> compareBy { !it.isActive }
            }.let { if (sortDirection == SortDirection.DESC) it.reversed() else it }
        )
    }

    if (!loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingIndicator() }
    } else {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            PageHeader(
                title = "Catálogo clínico",
                subtitle = "Tratamientos: precio, duración, categorías y gestión completa.",
                modifier = Modifier.fillMaxWidth(),
                actions = {
                    AppOutlinedButton(text = "Categorías", onClick = { showCategoryManager = true })
                    AppButton(text = "Registrar tratamiento", onClick = { registerError = null; showRegister = true })
                },
            )
            SearchAndFiltersBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                categories = categories,
                filterCategoryId = filterCategoryId,
                onFilterCategoryChange = { filterCategoryId = it },
                filterStatus = filterStatus,
                onFilterStatusChange = { filterStatus = it },
                filterFavoritesOnly = filterFavoritesOnly,
                onFilterFavoritesChange = { filterFavoritesOnly = it },
                sortField = sortField,
                onSortFieldChange = { sortField = it },
                sortDirection = sortDirection,
                onSortDirectionChange = { sortDirection = it },
            )
            LazyColumn(
                modifier = Modifier.weight(0.45f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                items(filteredRows, key = { it.id }) { pr ->
                    ProcedureTypeCard(
                        procedureType = pr,
                        categories = categories,
                        onEdit = { editError = null; editingProcedure = pr },
                        onArchive = { archivingProcedure = pr },
                        onRestore = {
                            scope.launch {
                                runCatching { withContext(Dispatchers.IO) { repo.restoreProcedureType(pr.id) } }
                                    .onSuccess { refreshNonce++; messenger.showSuccess("Tratamiento restaurado.") }
                                    .onFailure { messenger.showError(it.message ?: "Error al restaurar.") }
                            }
                        },
                        onDelete = { deletingProcedure = pr },
                        onToggleFavorite = {
                            scope.launch {
                                withContext(Dispatchers.IO) { repo.toggleFavoriteProcedureType(pr.id) }
                                refreshNonce++
                            }
                        },
                    )
                }
                if (filteredRows.isEmpty()) {
                    item {
                        Text(
                            if (searchQuery.isNotBlank() || filterCategoryId != null || filterFavoritesOnly) "No se encontraron tratamientos con estos filtros." else "No hay tratamientos registrados.",
                            modifier = Modifier.padding(AppSpacing.lg),
                            style = AppTypography.BodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text("Tratamientos realizados en clínica", style = AppTypography.SectionTitle, color = MaterialTheme.colorScheme.onSurface)
            TreatmentsTable(
                treatments = performedTreatments,
                showPatientColumn = true,
                modifier = Modifier.weight(0.55f),
                emptyMessage = "Aún no hay tratamientos vinculados a pacientes.",
                onStatusChange = { treatmentStatusDialog = it },
                onDelete = { deletingTreatment = it },
            )
        }
    }

    if (showRegister) {
        ProcedureTypeFormDialog(
            title = "Nuevo tratamiento", subtitle = "Se guardará en el catálogo.", submitLabel = "Registrar",
            isSaving = registerBusy, errorMessage = registerError, categories = categories,
            onDismiss = { if (!registerBusy) { showRegister = false; registerError = null } },
            onSubmit = { req ->
                registerBusy = true; registerError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.findProcedureTypeByName(req.name)?.let { throw IllegalArgumentException("Ya existe un tratamiento con el nombre \"${req.name}\".") }
                            repo.registerProcedureType(ProcedureTypeRegisterRequest(
                                name = req.name, description = req.description,
                                defaultDurationMinutes = req.defaultDurationMinutes, standardPrice = req.standardPrice,
                                requiresToothSpecification = req.requiresToothSpecification,
                                category = req.category, categoryId = req.categoryId, currency = req.currency,
                                color = req.color, icon = req.icon, isFavorite = req.isFavorite,
                                notes = req.notes, isActive = req.isActive,
                            ))
                        }
                    }.onSuccess { refreshNonce++; showRegister = false; messenger.showSuccess("Tratamiento registrado.") }
                    .onFailure { registerError = it.message ?: "Error al registrar." }
                    registerBusy = false
                }
            },
        )
    }

    editingProcedure?.let { proc ->
        ProcedureTypeFormDialog(
            title = "Editar tratamiento", subtitle = "Actualizar información.", submitLabel = "Guardar",
            isSaving = editBusy, errorMessage = editError, categories = categories,
            initialName = proc.name, initialDescription = proc.description.orEmpty(),
            initialDurationText = proc.defaultDurationMinutes?.toString().orEmpty(),
            initialPriceText = proc.standardPrice?.let { formatMoney(it) }.orEmpty(),
            initialCategoryText = proc.category.orEmpty(), initialCategoryId = proc.categoryId,
            initialCurrency = proc.currency, initialColor = proc.color, initialIcon = proc.icon,
            initialRequiresTooth = proc.requiresToothSpecification, initialIsActive = proc.isActive,
            initialNotes = proc.notes.orEmpty(),
            onDismiss = { if (!editBusy) { editingProcedure = null; editError = null } },
            onSubmit = { req ->
                editBusy = true; editError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.findProcedureTypeByName(req.name, excludeId = proc.id)?.let { throw IllegalArgumentException("Ya existe otro tratamiento con el nombre \"${req.name}\".") }
                            repo.updateProcedureType(proc.id, req)
                        }
                    }.onSuccess { refreshNonce++; editingProcedure = null; messenger.showSuccess("Tratamiento actualizado.") }
                    .onFailure { editError = it.message ?: "Error al actualizar." }
                    editBusy = false
                }
            },
        )
    }

    archivingProcedure?.let { proc ->
        scope.launch {
            runCatching { withContext(Dispatchers.IO) { repo.archiveProcedureType(proc.id) } }
                .onSuccess { archivingProcedure = null; refreshNonce++; messenger.showSuccess("Tratamiento archivado.") }
                .onFailure { messenger.showError(it.message ?: "Error al archivar."); archivingProcedure = null }
        }
        archivingProcedure = null
    }

    deletingProcedure?.let { proc ->
        ProcedureTypeDeleteDialog(proc.name, deleteBusy,
            onDismiss = { if (!deleteBusy) deletingProcedure = null },
            onConfirm = {
                deleteBusy = true
                scope.launch {
                    runCatching { withContext(Dispatchers.IO) { repo.hardDeleteProcedureType(proc.id) } }
                        .onSuccess { deletingProcedure = null; refreshNonce++; messenger.showSuccess("Tratamiento eliminado.") }
                        .onFailure { messenger.showError(it.message ?: "Error al eliminar.") }
                    deleteBusy = false
                }
            },
        )
    }

    treatmentStatusDialog?.let { treatment ->
        TreatmentStatusEditDialog(treatment.status, statusBusy,
            onDismiss = { if (!statusBusy) treatmentStatusDialog = null },
            onConfirm = { newStatus ->
                statusBusy = true
                scope.launch {
                    runCatching { withContext(Dispatchers.IO) { repo.updateTreatmentStatus(treatment.id, newStatus) } }
                        .onSuccess { treatmentStatusDialog = null; refreshNonce++; messenger.showSuccess("Estado actualizado.") }
                        .onFailure { messenger.showError(it.message ?: "Error al actualizar.") }
                    statusBusy = false
                }
            },
        )
    }

    deletingTreatment?.let { treatment ->
        TreatmentDeleteDialog(treatment.procedureTypeName, treatment.patientName, treatmentDeleteBusy,
            onDismiss = { if (!treatmentDeleteBusy) deletingTreatment = null },
            onConfirm = {
                treatmentDeleteBusy = true
                scope.launch {
                    runCatching { withContext(Dispatchers.IO) { repo.deleteTreatment(treatment.id) } }
                        .onSuccess { deletingTreatment = null; refreshNonce++; messenger.showSuccess("Tratamiento eliminado.") }
                        .onFailure { messenger.showError(it.message ?: "Error al eliminar.") }
                    treatmentDeleteBusy = false
                }
            },
        )
    }

    if (showCategoryManager) {
        CategoryManagerDialog(
            repo = repo,
            onDismiss = { showCategoryManager = false; refreshNonce++ },
        )
    }

    editingCategory?.let { cat ->
        CategoryFormDialog(
            title = "Editar categoría", submitLabel = "Guardar",
            isSaving = categoryDialogBusy, errorMessage = categoryDialogError,
            initialName = cat.name, initialIcon = cat.icon, initialColor = cat.color,
            initialSortOrder = cat.sortOrder, initialIsActive = cat.isActive,
            onDismiss = { if (!categoryDialogBusy) { editingCategory = null; categoryDialogError = null } },
            onSubmit = { req ->
                categoryDialogBusy = true; categoryDialogError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.findCategoryByName(req.name, excludeId = cat.id)?.let { throw IllegalArgumentException("Ya existe una categoría con el nombre \"${req.name}\".") }
                            repo.updateCategory(cat.id, req)
                        }
                    }.onSuccess { editingCategory = null; messenger.showSuccess("Categoría actualizada.") }
                    .onFailure { categoryDialogError = it.message ?: "Error al actualizar." }
                    categoryDialogBusy = false
                }
            },
        )
    }

    deletingCategory?.let { cat ->
        CategoryDeleteDialog(cat.name, categoryDeleteBusy,
            onDismiss = { if (!categoryDeleteBusy) deletingCategory = null },
            onConfirm = {
                categoryDeleteBusy = true
                scope.launch {
                    runCatching { withContext(Dispatchers.IO) { repo.hardDeleteCategory(cat.id) } }
                        .onSuccess { deletingCategory = null; messenger.showSuccess("Categoría eliminada.") }
                        .onFailure { messenger.showError(it.message ?: "Error al eliminar.") }
                    categoryDeleteBusy = false
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchAndFiltersBar(
    searchQuery: String, onSearchQueryChange: (String) -> Unit,
    categories: List<TreatmentCategory>, filterCategoryId: Int?, onFilterCategoryChange: (Int?) -> Unit,
    filterStatus: String?, onFilterStatusChange: (String?) -> Unit,
    filterFavoritesOnly: Boolean, onFilterFavoritesChange: (Boolean) -> Unit,
    sortField: SortField, onSortFieldChange: (SortField) -> Unit,
    sortDirection: SortDirection, onSortDirectionChange: (SortDirection) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        AppTextField(value = searchQuery, onValueChange = onSearchQueryChange, label = "Buscar", placeholder = "Nombre, descripción, categoría…")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            FilterChip(selected = filterCategoryId == null, onClick = { onFilterCategoryChange(null) }, label = { Text("Todas") })
            categories.forEach { cat ->
                FilterChip(selected = filterCategoryId == cat.id, onClick = { onFilterCategoryChange(if (filterCategoryId == cat.id) null else cat.id) }, label = { Text(cat.name) })
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            FilterChip(selected = filterStatus == null, onClick = { onFilterStatusChange(null) }, label = { Text("Todos") })
            FilterChip(selected = filterStatus == "active", onClick = { onFilterStatusChange(if (filterStatus == "active") null else "active") }, label = { Text("Activos") })
            FilterChip(selected = filterStatus == "inactive", onClick = { onFilterStatusChange(if (filterStatus == "inactive") null else "inactive") }, label = { Text("Inactivos") })
            FilterChip(selected = filterFavoritesOnly, onClick = { onFilterFavoritesChange(!filterFavoritesOnly) }, label = { Text("★ Favoritos") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PatientsPremiumPalette.primary.copy(alpha = 0.12f)))
            Spacer(Modifier.width(12.dp))
            Text("Ordenar:", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterVertically))
            listOf(SortField.NAME to "Nombre", SortField.PRICE to "Precio", SortField.DURATION to "Duración", SortField.CATEGORY to "Categoría", SortField.STATUS to "Estado").forEach { (field, label) ->
                FilterChip(selected = sortField == field, onClick = {
                    if (sortField == field) onSortDirectionChange(if (sortDirection == SortDirection.ASC) SortDirection.DESC else SortDirection.ASC)
                    else { onSortFieldChange(field); onSortDirectionChange(SortDirection.ASC) }
                }, label = { Text(if (sortField == field) "$label ${if (sortDirection == SortDirection.ASC) "↑" else "↓"}" else label) })
            }
        }
    }
}

@Composable
private fun ProcedureTypeCard(
    procedureType: ProcedureTypeRow,
    categories: List<TreatmentCategory>,
    onEdit: () -> Unit, onArchive: () -> Unit, onRestore: () -> Unit, onDelete: () -> Unit, onToggleFavorite: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    val color = procedureType.color?.let { parseHexColor(it) }
    val catColor = procedureType.categoryId?.let { id -> categories.find { it.id == id }?.color }?.let { c -> parseHexColor(c) }
    val displayColor = color ?: catColor

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    displayColor?.let {
                        Box(Modifier.size(12.dp).clip(CircleShape).background(it))
                        Spacer(Modifier.width(AppSpacing.sm))
                    }
                    Text(procedureType.name, style = AppTypography.CardTitle, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(if (procedureType.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline, contentDescription = "Favorito",
                            tint = if (procedureType.isFavorite) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) { Text("⋯", style = AppTypography.Body) }
                        ProcedureTypeActionsDropdown(showMenu, { showMenu = false }, onEdit, onArchive, onRestore, onDelete, procedureType.isArchived)
                    }
                }
            }
            procedureType.description?.let { Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis) }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                val price = procedureType.standardPrice?.let { "${procedureType.currency} ${formatMoney(it)}" } ?: "—"
                val dur = procedureType.defaultDurationMinutes?.let { formatDuration(it) } ?: "—"
                Text("$dur · $price", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (procedureType.requiresToothSpecification) Text("🦷", style = AppTypography.Body)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                procedureType.category?.let {
                    SurfaceChip(it, displayColor)
                }
                if (!procedureType.isActive) SurfaceChip("Inactivo", MaterialTheme.colorScheme.error)
            }
            procedureType.notes?.let { Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.outline, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
    }
}

@Composable
private fun SurfaceChip(label: String, color: Color? = null) {
    val bgColor = (color ?: PatientsPremiumPalette.primary).copy(alpha = 0.12f)
    val fgColor = color ?: PatientsPremiumPalette.primary
    androidx.compose.material3.Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp), color = bgColor) {
        Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = AppTypography.Caption, fontWeight = FontWeight.SemiBold, color = fgColor)
    }
}

@Composable
private fun ProcedureTypeActionsDropdown(visible: Boolean, onDismiss: () -> Unit, onEdit: () -> Unit, onArchive: () -> Unit, onRestore: () -> Unit, onDelete: () -> Unit, isArchived: Boolean) {
    DropdownMenu(expanded = visible, onDismissRequest = onDismiss) {
        if (isArchived) { DropdownMenuItem(text = { Text("Restaurar") }, onClick = { onDismiss(); onRestore() }) }
        else { DropdownMenuItem(text = { Text("Editar") }, onClick = { onDismiss(); onEdit() }); DropdownMenuItem(text = { Text("Archivar") }, onClick = { onDismiss(); onArchive() }) }
        DropdownMenuItem(text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) }, onClick = { onDismiss(); onDelete() })
    }
}

@Composable
private fun ProcedureTypeDeleteDialog(procedureName: String, isSaving: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AppSurfaceDialog(onDismissRequest = onDismiss, modifier = Modifier.widthIn(max = 440.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text("Eliminar tratamiento", style = AppTypography.SectionTitle, color = MaterialTheme.colorScheme.error)
            Text("¿Eliminar permanentemente \"$procedureName\"? Esta acción no se puede deshacer.", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End)) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(text = if (isSaving) "Eliminando…" else "Eliminar", onClick = onConfirm, enabled = !isSaving)
            }
        }
    }
}

@Composable
private fun TreatmentStatusEditDialog(currentStatus: TreatmentStatus, isSaving: Boolean, onDismiss: () -> Unit, onConfirm: (TreatmentStatus) -> Unit) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    AppSurfaceDialog(onDismissRequest = onDismiss, modifier = Modifier.widthIn(max = 400.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text("Cambiar estado", style = AppTypography.SectionTitle)
            Text("Selecciona el nuevo estado clínico.", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            com.denticode.kt.ui.components.inputs.AppDropdownField(
                label = "Estado", options = com.denticode.kt.data.treatmentStatusOptions(),
                selected = selectedStatus, onSelected = { selectedStatus = it }, enabled = !isSaving,
                optionLabel = { it.labelEs }, placeholder = "Estado…",
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End)) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(text = if (isSaving) "Guardando…" else "Guardar", onClick = { onConfirm(selectedStatus) }, enabled = !isSaving)
            }
        }
    }
}

@Composable
private fun TreatmentDeleteDialog(treatmentLabel: String, patientName: String, isSaving: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AppSurfaceDialog(onDismissRequest = onDismiss, modifier = Modifier.widthIn(max = 440.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text("Eliminar tratamiento", style = AppTypography.SectionTitle, color = MaterialTheme.colorScheme.error)
            Text("¿Eliminar \"$treatmentLabel\" de $patientName? Se desvinculará de la cita.", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End)) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(text = if (isSaving) "Eliminando…" else "Eliminar", onClick = onConfirm, enabled = !isSaving)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProcedureTypeFormDialog(
    title: String, subtitle: String, submitLabel: String, isSaving: Boolean, errorMessage: String?, categories: List<TreatmentCategory>,
    initialName: String = "", initialDescription: String = "", initialDurationText: String = "", initialPriceText: String = "",
    initialCategoryText: String = "", initialCategoryId: Int? = null, initialCurrency: String = "BOB",
    initialColor: String? = null, initialIcon: String? = null, initialRequiresTooth: Boolean = false,
    initialIsActive: Boolean = true, initialNotes: String = "",
    onDismiss: () -> Unit, onSubmit: (ProcedureTypeUpdateRequest) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    var durationText by remember { mutableStateOf(initialDurationText) }
    var priceText by remember { mutableStateOf(initialPriceText) }
    var categoryText by remember { mutableStateOf(initialCategoryText) }
    var selectedCategoryId by remember { mutableStateOf(initialCategoryId) }
    var currency by remember { mutableStateOf(initialCurrency) }
    var color by remember { mutableStateOf(initialColor) }
    var icon by remember { mutableStateOf(initialIcon) }
    var requiresTooth by remember { mutableStateOf(initialRequiresTooth) }
    var isActive by remember { mutableStateOf(initialIsActive) }
    var notes by remember { mutableStateOf(initialNotes) }

    val nameError = Validators.required(name, "Nombre") ?: Validators.length(name, 255, "Nombre")
    val priceError = Validators.positiveNumber(priceText, "Precio")
    val durationError = if (durationText.isNotBlank()) Validators.numeric(durationText, "Duración") else null
    val canSubmit = nameError == null && priceError == null && durationError == null && !isSaving

    AppSurfaceDialog(onDismissRequest = onDismiss, modifier = Modifier.widthIn(max = 560.dp)) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            item {
                Text(title, style = AppTypography.SectionTitle, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                AppTextField(value = name, onValueChange = { name = it }, label = "Nombre *", placeholder = "Limpieza, endodoncia…", enabled = !isSaving)
                nameError?.let { if (name.isNotEmpty()) Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.error) }
            }
            item {
                AppTextArea(value = description, onValueChange = { description = it }, label = "Descripción (opcional)", enabled = !isSaving, minLines = 2, maxLines = 4)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    AppTextField(value = durationText, onValueChange = { durationText = it.filter { ch -> ch.isDigit() }.take(4) },
                        label = "Duración (min)", placeholder = "45", enabled = !isSaving,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    AppTextField(value = priceText, onValueChange = { priceText = it }, label = "Precio *", placeholder = "150,00", enabled = !isSaving,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                }
                priceError?.let { if (priceText.isNotBlank()) Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.error) }
                durationError?.let { if (durationText.isNotBlank()) Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.error) }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    AppTextField(value = currency, onValueChange = { currency = it.take(4) }, label = "Moneda", placeholder = "BOB", enabled = !isSaving, modifier = Modifier.weight(0.5f))
                    com.denticode.kt.ui.components.inputs.AppDropdownField(
                        label = "Categoría", options = categories, selected = categories.find { it.id == selectedCategoryId },
                        onSelected = { selectedCategoryId = it?.id; categoryText = it?.name.orEmpty() },
                        enabled = !isSaving, optionLabel = { it.name }, placeholder = "Categoría…", modifier = Modifier.weight(1.5f),
                    )
                }
            }
            item {
                Text("Color", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    PRESET_COLORS.forEach { c ->
                        val isSelected = color == c
                val bgColor = parseHexColor(c) ?: Color.Gray
                    Box(Modifier.size(28.dp).clip(CircleShape).background(bgColor)
                            .then(if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                            .clickable { color = if (isSelected) null else c })
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Checkbox(checked = requiresTooth, onCheckedChange = { requiresTooth = it }, enabled = !isSaving)
                    Text("Requiere diente", style = AppTypography.Body)
                    Spacer(Modifier.width(12.dp))
                    Checkbox(checked = isActive, onCheckedChange = { isActive = it }, enabled = !isSaving)
                    Text("Activo", style = AppTypography.Body)
                }
            }
            item {
                AppTextArea(value = notes, onValueChange = { notes = it }, label = "Notas (opcional)", enabled = !isSaving, minLines = 2, maxLines = 3)
            }
            item {
                errorMessage?.let { Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End)) {
                    AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                    AppButton(text = if (isSaving) "Guardando…" else submitLabel, onClick = {
                        onSubmit(ProcedureTypeUpdateRequest(
                            name = name.trim(), description = description.trim().takeIf { it.isNotEmpty() },
                            defaultDurationMinutes = durationText.trim().toIntOrNull(),
                            standardPrice = parseMoneyAmount(priceText),
                            requiresToothSpecification = requiresTooth,
                            category = categoryText.trim().takeIf { it.isNotEmpty() }, categoryId = selectedCategoryId,
                            currency = currency.trim().ifBlank { "BOB" }, color = color, icon = icon,
                            notes = notes.trim().takeIf { it.isNotEmpty() }, isActive = isActive,
                        ))
                    }, enabled = canSubmit)
                }
            }
        }
    }
}

// ── Category Management ─────────────────────────────────────────────────────

@Composable
private fun CategoryManagerDialog(repo: DentiRepository, onDismiss: () -> Unit) {
    var cats by remember { mutableStateOf<List<TreatmentCategory>>(emptyList()) }
    var showCreate by remember { mutableStateOf(false) }
    var createBusy by remember { mutableStateOf(false) }
    var createError by remember { mutableStateOf<String?>(null) }
    var editingCat by remember { mutableStateOf<TreatmentCategory?>(null) }
    var editBusy by remember { mutableStateOf(false) }
    var editError by remember { mutableStateOf<String?>(null) }
    var deletingCat by remember { mutableStateOf<TreatmentCategory?>(null) }
    var deleteBusy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val messenger = LocalAppMessenger.current

    LaunchedEffect(Unit) { withContext(Dispatchers.IO) { cats = repo.listAllCategoriesIncludingArchived() } }

    AppSurfaceDialog(onDismissRequest = onDismiss, modifier = Modifier.widthIn(max = 520.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Gestionar categorías", style = AppTypography.SectionTitle)
                AppButton(text = "Nueva", onClick = { createError = null; showCreate = true })
            }
            LazyColumn(Modifier.height(400.dp), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                items(cats, key = { it.id }) { cat ->
                    Row(Modifier.fillMaxWidth().padding(AppSpacing.sm), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            cat.color?.let { parseHexColor(it)?.let { c -> Box(Modifier.size(12.dp).clip(CircleShape).background(c)) } }
                            Spacer(Modifier.width(AppSpacing.sm))
                            Column {
                                Text(cat.name, style = AppTypography.Body)
                                cat.icon?.let { Text("Icon: $it", style = AppTypography.Caption, color = MaterialTheme.colorScheme.outline) }
                            }
                            if (cat.isArchived) { Spacer(Modifier.width(AppSpacing.sm)); SurfaceChip("Archivada", MaterialTheme.colorScheme.error) }
                        }
                        Row {
                            if (cat.isArchived) {
                                AppOutlinedButton(text = "Restaurar", onClick = {
                                    scope.launch { withContext(Dispatchers.IO) { repo.restoreCategory(cat.id) }; cats = repo.listAllCategoriesIncludingArchived() }
                                })
                            } else {
                                AppOutlinedButton(text = "Editar", onClick = { editError = null; editingCat = cat })
                            }
                            AppOutlinedButton(text = "Eliminar", onClick = { deletingCat = cat })
                        }
                    }
                    HorizontalDivider()
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                AppOutlinedButton(text = "Cerrar", onClick = onDismiss)
            }
        }
    }

    if (showCreate) {
        CategoryFormDialog("Nueva categoría", "Crear", createBusy, createError,
            onDismiss = { if (!createBusy) { showCreate = false; createError = null } },
            onSubmit = { req ->
                createBusy = true; createError = null
                scope.launch {
                    runCatching { withContext(Dispatchers.IO) { repo.findCategoryByName(req.name)?.let { throw IllegalArgumentException("Ya existe una categoría con el nombre \"${req.name}\".") }; repo.registerCategory(CategoryRegisterRequest(name = req.name, icon = req.icon, color = req.color, sortOrder = req.sortOrder, isActive = req.isActive)) } }
                        .onSuccess { showCreate = false; cats = repo.listAllCategoriesIncludingArchived(); messenger.showSuccess("Categoría creada.") }
                        .onFailure { createError = it.message ?: "Error al crear." }
                    createBusy = false
                }
            },
        )
    }

    editingCat?.let { cat ->
        CategoryFormDialog("Editar categoría", "Guardar", editBusy, editError,
            initialName = cat.name, initialIcon = cat.icon, initialColor = cat.color, initialSortOrder = cat.sortOrder, initialIsActive = cat.isActive,
            onDismiss = { if (!editBusy) { editingCat = null; editError = null } },
            onSubmit = { req ->
                editBusy = true; editError = null
                scope.launch {
                    runCatching { withContext(Dispatchers.IO) { repo.findCategoryByName(req.name, excludeId = cat.id)?.let { throw IllegalArgumentException("Ya existe otra categoría con el nombre \"${req.name}\".") }; repo.updateCategory(cat.id, req) } }
                        .onSuccess { editingCat = null; cats = repo.listAllCategoriesIncludingArchived(); messenger.showSuccess("Categoría actualizada.") }
                        .onFailure { editError = it.message ?: "Error al actualizar." }
                    editBusy = false
                }
            },
        )
    }

    deletingCat?.let { cat ->
        CategoryDeleteDialog(cat.name, deleteBusy,
            onDismiss = { if (!deleteBusy) deletingCat = null },
            onConfirm = {
                deleteBusy = true
                scope.launch {
                    runCatching { withContext(Dispatchers.IO) { repo.hardDeleteCategory(cat.id) } }
                        .onSuccess { deletingCat = null; cats = repo.listAllCategoriesIncludingArchived(); messenger.showSuccess("Categoría eliminada.") }
                        .onFailure { messenger.showError(it.message ?: "Error al eliminar."); deletingCat = null }
                    deleteBusy = false
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryFormDialog(
    title: String, submitLabel: String, isSaving: Boolean, errorMessage: String?,
    initialName: String = "", initialIcon: String? = null, initialColor: String? = null, initialSortOrder: Int = 0, initialIsActive: Boolean = true,
    onDismiss: () -> Unit, onSubmit: (CategoryUpdateRequest) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var icon by remember { mutableStateOf(initialIcon.orEmpty()) }
    var color by remember { mutableStateOf(initialColor) }
    var sortOrder by remember { mutableStateOf(initialSortOrder.toString()) }
    var isActive by remember { mutableStateOf(initialIsActive) }
    val nameError = Validators.required(name, "Nombre") ?: Validators.length(name, 255, "Nombre")
    val canSubmit = nameError == null && !isSaving

    AppSurfaceDialog(onDismissRequest = onDismiss, modifier = Modifier.widthIn(max = 480.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(title, style = AppTypography.SectionTitle)
            AppTextField(value = name, onValueChange = { name = it }, label = "Nombre *", placeholder = "Preventiva, Cirugía…", enabled = !isSaving)
            nameError?.let { if (name.isNotEmpty()) Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.error) }
            AppTextField(value = icon, onValueChange = { icon = it }, label = "Icono (opcional)", placeholder = "shield, build…", enabled = !isSaving)
            Text("Color", style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                PRESET_COLORS.forEach { c ->
                    val isSelected = color == c
                    val bgColor = parseHexColor(c) ?: Color.Gray
                    Box(Modifier.size(28.dp).clip(CircleShape).background(bgColor)
                        .then(if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                        .clickable { color = if (isSelected) null else c })
                }
            }
            AppTextField(value = sortOrder, onValueChange = { sortOrder = it.filter { ch -> ch.isDigit() }.take(3) }, label = "Orden", placeholder = "0", enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Checkbox(checked = isActive, onCheckedChange = { isActive = it }, enabled = !isSaving)
                Text("Activa", style = AppTypography.Body)
            }
            errorMessage?.let { Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End)) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(text = if (isSaving) "Guardando…" else submitLabel, onClick = {
                    onSubmit(CategoryUpdateRequest(name = name.trim(), icon = icon.trim().takeIf { it.isNotEmpty() }, color = color,
                        sortOrder = sortOrder.toIntOrNull() ?: 0, isActive = isActive))
                }, enabled = canSubmit)
            }
        }
    }
}

@Composable
private fun CategoryDeleteDialog(categoryName: String, isSaving: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AppSurfaceDialog(onDismissRequest = onDismiss, modifier = Modifier.widthIn(max = 440.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text("Eliminar categoría", style = AppTypography.SectionTitle, color = MaterialTheme.colorScheme.error)
            Text("¿Eliminar permanentemente \"$categoryName\"? Esta acción no se puede deshacer.", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End)) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(text = if (isSaving) "Eliminando…" else "Eliminar", onClick = onConfirm, enabled = !isSaving)
            }
        }
    }
}
