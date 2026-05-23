package com.denticode.kt.ui.patients

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.Patient
import com.denticode.kt.data.PatientDirectoryKpis
import com.denticode.kt.data.PatientDirectoryRow
import com.denticode.kt.data.PatientListStatus
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.formatMoney
import com.denticode.kt.ui.theme.AppSpacing

@Composable
fun ModernPatientsContent(
    directoryRows: List<PatientDirectoryRow>,
    kpis: PatientDirectoryKpis,
    onRegisterClick: () -> Unit,
    onOpenPatientDetail: (Patient) -> Unit,
    modifier: Modifier = Modifier,
) {
    val messenger = LocalAppMessenger.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedDoctor by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<PatientListStatus?>(null) }
    var selectedAgeRange by remember { mutableStateOf(PatientAgeRangeFilter.ALL) }
    var currentPage by remember { mutableIntStateOf(1) }
    var pageSize by remember { mutableIntStateOf(10) }
    var selectedPatientId by remember { mutableStateOf<Int?>(null) }

    val doctorOptions =
        remember(directoryRows) {
            listOf("Todos los doctores") +
                directoryRows
                    .mapNotNull { it.primaryDoctorName }
                    .distinct()
                    .sorted()
        }
    val statusOptions =
        remember {
            listOf("Todos los estados") + PatientListStatus.entries.map { it.labelEs }
        }
    val ageOptions = remember { PatientAgeRangeFilter.entries.map { it.label } }

    val uiState =
        remember(
            directoryRows,
            kpis,
            searchQuery,
            selectedDoctor,
            selectedStatus,
            selectedAgeRange,
            currentPage,
            pageSize,
        ) {
            buildPatientsUiState(
                rows = directoryRows,
                kpis = kpis,
                searchQuery = searchQuery,
                selectedDoctor = selectedDoctor,
                selectedStatus = selectedStatus,
                selectedAgeRange = selectedAgeRange,
                currentPage = currentPage,
                pageSize = pageSize,
            )
        }

    val pagePatients = remember(uiState) { paginatedPatients(uiState) }
    val pages = remember(uiState) { totalPages(uiState) }
    val rangeStart =
        if (uiState.totalPatients == 0) {
            0
        } else {
            (uiState.currentPage - 1) * uiState.pageSize + 1
        }
    val rangeEnd = (uiState.currentPage * uiState.pageSize).coerceAtMost(uiState.totalPatients)

    val statusLabel = selectedStatus?.labelEs ?: "Todos los estados"
    val doctorLabel = selectedDoctor ?: "Todos los doctores"
    val ageLabel = selectedAgeRange.label

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxSize()
                .background(PatientsPremiumPalette.background)
                .padding(AppSpacing.md),
    ) {
        val compact = maxWidth < 1100.dp
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            PatientsPageHeader(onRegisterClick = onRegisterClick)

            if (compact) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    PatientMetricCard(
                        title = "Total pacientes",
                        value = uiState.kpis.totalPatients.toString(),
                        trendText = "Directorio completo",
                        icon = Icons.Default.People,
                        iconBackground = PatientsPremiumPalette.primary.copy(alpha = 0.12f),
                        iconTint = PatientsPremiumPalette.primary,
                        modifier = Modifier.width(220.dp),
                    )
                    PatientMetricCard(
                        title = "Pacientes activos",
                        value = uiState.kpis.activePatients.toString(),
                        trendText = "Con actividad reciente",
                        icon = Icons.Default.Person,
                        iconBackground = PatientsPremiumPalette.success.copy(alpha = 0.12f),
                        iconTint = PatientsPremiumPalette.success,
                        modifier = Modifier.width(220.dp),
                    )
                    PatientMetricCard(
                        title = "Nuevos este mes",
                        value = uiState.kpis.newThisMonth.toString(),
                        trendText = "Altas del mes actual",
                        icon = Icons.Default.Add,
                        iconBackground = PatientsPremiumPalette.info.copy(alpha = 0.12f),
                        iconTint = PatientsPremiumPalette.info,
                        modifier = Modifier.width(220.dp),
                    )
                    PatientMetricCard(
                        title = "Citas programadas",
                        value = uiState.kpis.scheduledAppointments.toString(),
                        trendText = "Próximas en agenda",
                        icon = Icons.Default.CalendarMonth,
                        iconBackground = PatientsPremiumPalette.warning.copy(alpha = 0.14f),
                        iconTint = PatientsPremiumPalette.warning,
                        modifier = Modifier.width(220.dp),
                    )
                    PatientMetricCard(
                        title = "Deuda pendiente",
                        value = formatMoney(uiState.kpis.pendingDebt),
                        trendText = "Saldo estimado",
                        icon = Icons.Default.Payments,
                        iconBackground = Color(0xFF6366F1).copy(alpha = 0.12f),
                        iconTint = Color(0xFF6366F1),
                        modifier = Modifier.width(220.dp),
                    )
                }
            } else {
                PatientsKpiRow(
                    total = uiState.kpis.totalPatients,
                    active = uiState.kpis.activePatients,
                    newMonth = uiState.kpis.newThisMonth,
                    scheduled = uiState.kpis.scheduledAppointments,
                    pendingDebt = uiState.kpis.pendingDebt,
                )
            }

            PatientsFilterToolbar(
                searchQuery = searchQuery,
                onSearchChange = {
                    searchQuery = it
                    currentPage = 1
                },
                statusLabel = statusLabel,
                statusOptions = statusOptions,
                onStatusSelect = { label ->
                    selectedStatus =
                        if (label == "Todos los estados") {
                            null
                        } else {
                            PatientListStatus.entries.first { it.labelEs == label }
                        }
                    currentPage = 1
                },
                doctorLabel = doctorLabel,
                doctorOptions = doctorOptions,
                onDoctorSelect = { label ->
                    selectedDoctor = if (label == "Todos los doctores") null else label
                    currentPage = 1
                },
                ageLabel = ageLabel,
                ageOptions = ageOptions,
                onAgeSelect = { label ->
                    selectedAgeRange = PatientAgeRangeFilter.entries.first { it.label == label }
                    currentPage = 1
                },
                moreFiltersCount = uiState.activeFilterCount,
                onMoreFilters = {
                    searchQuery = ""
                    selectedDoctor = null
                    selectedStatus = null
                    selectedAgeRange = PatientAgeRangeFilter.ALL
                    currentPage = 1
                    messenger.showSuccess("Filtros restablecidos.")
                },
                onExport = {
                    messenger.showSuccess("Exportación de pacientes preparada (simulación).")
                },
            )

            if (pagePatients.isEmpty()) {
                EmptyPatientsState(hasFilters = uiState.activeFilterCount > 0 || searchQuery.isNotBlank())
            } else {
                PatientTable(
                    patients = pagePatients,
                    selectedId = selectedPatientId,
                    onSelect = { selectedPatientId = it.id },
                    onViewDetail = { onOpenPatientDetail(it.patient) },
                    modifier = Modifier.weight(1f),
                )
            }

            PaginationControls(
                currentPage = uiState.currentPage,
                totalPages = pages,
                pageSize = uiState.pageSize,
                totalItems = uiState.totalPatients,
                rangeStart = rangeStart,
                rangeEnd = rangeEnd,
                pageSizeOptions = listOf(5, 10, 25, 50),
                onPageChange = { currentPage = it },
                onPageSizeChange = {
                    pageSize = it
                    currentPage = 1
                },
            )
        }
    }
}
