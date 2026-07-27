package com.denticode.kt.ui.doctors

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.denticode.kt.data.DoctorDirectoryKpis
import com.denticode.kt.data.DoctorDirectoryRow
import com.denticode.kt.data.DoctorListStatus
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppSpacing

@Composable
fun ModernDoctorsContent(
    directoryRows: List<DoctorDirectoryRow>,
    kpis: DoctorDirectoryKpis,
    onNewDoctorClick: () -> Unit,
    onViewProfile: (DoctorUiModel) -> Unit,
    onEdit: (DoctorUiModel) -> Unit,
    onViewSchedule: (DoctorUiModel) -> Unit,
    onToggleActive: (DoctorUiModel) -> Unit,
    onArchive: (DoctorUiModel) -> Unit = {},
    onDelete: (DoctorUiModel) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedSpecialty by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedStatus by rememberSaveable { mutableStateOf<DoctorListStatus?>(null) }
    var sortOrder by rememberSaveable { mutableStateOf(DoctorSortOrder.NAME_AZ) }
    var selectedDoctorId by rememberSaveable { mutableStateOf<Int?>(null) }

    val specialtyOptions =
        remember(directoryRows) {
            listOf("Todas") +
                directoryRows
                    .mapNotNull { row ->
                        row.doctor.specialization?.trim()?.takeIf { it.isNotEmpty() }
                    }
                    .distinct()
                    .sorted()
        }
    val statusOptions =
        remember {
            listOf("Todos") + DoctorListStatus.entries.map { it.labelEs }
        }
    val sortOptions = remember { DoctorSortOrder.entries.map { it.label } }

    val uiState =
        remember(directoryRows, kpis, searchQuery, selectedSpecialty, selectedStatus, sortOrder, selectedDoctorId) {
            buildDoctorsUiState(
                rows = directoryRows,
                kpis = kpis,
                searchQuery = searchQuery,
                selectedSpecialty = selectedSpecialty,
                selectedStatus = selectedStatus,
                sortOrder = sortOrder,
                selectedDoctorId = selectedDoctorId,
            )
        }

    val hasFilters =
        searchQuery.isNotBlank() ||
            selectedSpecialty != null ||
            selectedStatus != null

    val specialtyLabel = selectedSpecialty ?: "Todas"
    val statusLabel = selectedStatus?.labelEs ?: "Todos"
    val sortLabel = sortOrder.label

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(PatientsPremiumPalette.background)
                .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        DoctorsPageHeader(onNewDoctorClick = onNewDoctorClick)
        DoctorsStatsRow(kpis = uiState.kpis)
        DoctorSearchFilters(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            specialtyLabel = specialtyLabel,
            specialtyOptions = specialtyOptions,
            onSpecialtySelect = { label ->
                selectedSpecialty = if (label == "Todas") null else label
            },
            statusLabel = statusLabel,
            statusOptions = statusOptions,
            onStatusSelect = { label ->
                selectedStatus =
                    if (label == "Todos") {
                        null
                    } else {
                        DoctorListStatus.entries.firstOrNull { it.labelEs == label }
                    }
            },
            sortLabel = sortLabel,
            sortOptions = sortOptions,
            onSortSelect = { label ->
                sortOrder = DoctorSortOrder.fromLabel(label)
            },
        )

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = uiState.doctors.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize(),
            ) {
                EmptyDoctorsState(
                    hasFilters = hasFilters,
                    onNewDoctorClick = onNewDoctorClick,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = uiState.doctors.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize(),
            ) {
                DoctorsTable(
                    doctors = uiState.doctors,
                    selectedDoctorId = selectedDoctorId,
                    onSelectDoctor = { selectedDoctorId = it.id },
                    onViewProfile = onViewProfile,
                    onEdit = onEdit,
                    onViewSchedule = onViewSchedule,
                    onToggleActive = onToggleActive,
                    onArchive = onArchive,
                    onDelete = onDelete,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
