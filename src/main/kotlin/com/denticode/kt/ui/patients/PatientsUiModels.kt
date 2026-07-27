package com.denticode.kt.ui.patients

import com.denticode.kt.data.Patient
import com.denticode.kt.data.PatientDirectoryKpis
import com.denticode.kt.data.PatientDirectoryRow
import com.denticode.kt.data.PatientListStatus
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

enum class PatientAgeRangeFilter(val label: String) {
    ALL("Todas las edades"),
    CHILD("0–18 años"),
    YOUNG("19–35 años"),
    MID("36–55 años"),
    SENIOR("56+ años"),
    ;

    fun matches(age: Int?): Boolean {
        if (this == ALL) return true
        if (age == null) return false
        return when (this) {
            CHILD -> age in 0..18
            YOUNG -> age in 19..35
            MID -> age in 36..55
            SENIOR -> age >= 56
            ALL -> true
        }
    }
}

data class PatientUiModel(
    val id: Int,
    val fullName: String,
    val birthDateLabel: String,
    val ageYears: Int?,
    val phone: String,
    val email: String?,
    val primaryDoctorName: String,
    val lastAppointmentDate: String?,
    val lastAppointmentTreatment: String?,
    val nextAppointmentDate: String?,
    val nextAppointmentTime: String?,
    val status: PatientListStatus,
    val pendingBalance: Double,
    val patient: Patient,
)

data class PatientsUiState(
    val patients: List<PatientUiModel>,
    val kpis: PatientDirectoryKpis,
    val searchQuery: String,
    val selectedDoctor: String?,
    val selectedStatus: PatientListStatus?,
    val selectedAgeRange: PatientAgeRangeFilter,
    val currentPage: Int,
    val pageSize: Int,
    val totalPatients: Int,
    val activeFilterCount: Int,
)

private val birthDisplayFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES"))

fun patientAgeFromDob(dateOfBirth: String): Int? =
    try {
        val dob = LocalDate.parse(dateOfBirth.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
        Period.between(dob, LocalDate.now()).years
    } catch (_: DateTimeParseException) {
        null
    }

fun formatBirthDateLabel(dateOfBirth: String): String =
    try {
        LocalDate.parse(dateOfBirth.trim(), DateTimeFormatter.ISO_LOCAL_DATE).format(birthDisplayFmt)
    } catch (_: DateTimeParseException) {
        dateOfBirth
    }

fun PatientDirectoryRow.toUiModel(): PatientUiModel {
    val age = patientAgeFromDob(patient.dateOfBirth)
    val birthLabel = formatBirthDateLabel(patient.dateOfBirth)
    val ageSuffix = age?.let { " · $it años" } ?: ""
    return PatientUiModel(
        id = patient.id,
        fullName = patient.fullName,
        birthDateLabel = birthLabel + ageSuffix,
        ageYears = age,
        phone = patient.contactPhone,
        email = patient.email,
        primaryDoctorName = primaryDoctorName ?: "Sin asignar",
        lastAppointmentDate = lastAppointmentAt,
        lastAppointmentTreatment = lastAppointmentTreatment,
        nextAppointmentDate = nextAppointmentAt,
        nextAppointmentTime = nextAppointmentTimeLabel,
        status = status,
        pendingBalance = pendingBalance,
        patient = patient,
    )
}

fun buildPatientsUiState(
    rows: List<PatientDirectoryRow>,
    kpis: PatientDirectoryKpis,
    searchQuery: String,
    selectedDoctor: String?,
    selectedStatus: PatientListStatus?,
    selectedAgeRange: PatientAgeRangeFilter,
    currentPage: Int,
    pageSize: Int,
): PatientsUiState {
    val allUi = rows.map { it.toUiModel() }
    val q = searchQuery.trim().lowercase()
    val filtered =
        allUi.filter { p ->
            val matchesSearch =
                q.isEmpty() ||
                    p.fullName.lowercase().contains(q) ||
                    p.phone.lowercase().contains(q) ||
                    (p.email?.lowercase()?.contains(q) == true) ||
                    (p.patient.documentNumber?.lowercase()?.contains(q) == true)
            val matchesDoctor =
                selectedDoctor == null || p.primaryDoctorName == selectedDoctor
            val matchesStatus = selectedStatus == null || p.status == selectedStatus
            val matchesAge = selectedAgeRange.matches(p.ageYears)
            matchesSearch && matchesDoctor && matchesStatus && matchesAge
        }
    val activeFilters =
        (if (searchQuery.isNotBlank()) 1 else 0) +
            (if (selectedDoctor != null) 1 else 0) +
            (if (selectedStatus != null) 1 else 0) +
            (if (selectedAgeRange != PatientAgeRangeFilter.ALL) 1 else 0)
    val maxPage =
        if (filtered.isEmpty()) {
            1
        } else {
            (filtered.size + pageSize - 1) / pageSize
        }
    val page = currentPage.coerceIn(1, maxPage)
    return PatientsUiState(
        patients = filtered,
        kpis = kpis,
        searchQuery = searchQuery,
        selectedDoctor = selectedDoctor,
        selectedStatus = selectedStatus,
        selectedAgeRange = selectedAgeRange,
        currentPage = page,
        pageSize = pageSize,
        totalPatients = filtered.size,
        activeFilterCount = activeFilters,
    )
}

fun paginatedPatients(state: PatientsUiState): List<PatientUiModel> {
    if (state.patients.isEmpty()) return emptyList()
    val start = (state.currentPage - 1) * state.pageSize
    val end = (start + state.pageSize).coerceAtMost(state.patients.size)
    if (start >= state.patients.size) return emptyList()
    return state.patients.subList(start, end)
}

fun totalPages(state: PatientsUiState): Int =
    if (state.totalPatients == 0) {
        1
    } else {
        (state.totalPatients + state.pageSize - 1) / state.pageSize
    }
