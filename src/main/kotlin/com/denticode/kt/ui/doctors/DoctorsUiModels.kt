package com.denticode.kt.ui.doctors

import com.denticode.kt.data.DoctorDirectoryKpis
import com.denticode.kt.data.DoctorDirectoryRow
import com.denticode.kt.data.DoctorListStatus

enum class DoctorSortOrder(val label: String) {
    NAME_AZ("Nombre A-Z"),
    NAME_ZA("Nombre Z-A"),
    MOST_APPOINTMENTS("Más citas"),
    LEAST_APPOINTMENTS("Menos citas"),
    ;

    companion object {
        fun fromLabel(label: String): DoctorSortOrder =
            entries.firstOrNull { it.label == label } ?: NAME_AZ
    }
}

data class DoctorUiModel(
    val id: Int,
    val fullName: String,
    val specialty: String,
    val email: String,
    val phone: String,
    val licenseNumber: String?,
    val todayAppointmentsCount: Int,
    val totalAppointmentsCount: Int,
    val status: DoctorListStatus,
    val yearsExperience: Int,
    val experienceLabel: String,
    val address: String?,
)

data class DoctorsUiState(
    val doctors: List<DoctorUiModel>,
    val kpis: DoctorDirectoryKpis,
    val searchQuery: String,
    val selectedSpecialty: String?,
    val selectedStatus: DoctorListStatus?,
    val sortOrder: DoctorSortOrder,
    val selectedDoctorId: Int?,
)

fun DoctorDirectoryRow.toUiModel(): DoctorUiModel {
    val years = yearsExperience
    return DoctorUiModel(
        id = doctor.id,
        fullName = doctor.fullName,
        specialty = doctor.specialization?.trim()?.takeIf { it.isNotEmpty() } ?: "General",
        email = doctor.email,
        phone = doctor.contactPhone?.trim()?.takeIf { it.isNotEmpty() } ?: "—",
        licenseNumber = doctor.licenseNumber?.trim()?.takeIf { it.isNotEmpty() },
        todayAppointmentsCount = todayAppointmentsCount,
        totalAppointmentsCount = totalAppointmentsCount,
        status = status,
        yearsExperience = years,
        experienceLabel = if (years == 1) "1 año" else "$years años",
        address = doctor.address?.trim()?.takeIf { it.isNotEmpty() },
    )
}

fun buildDoctorsUiState(
    rows: List<DoctorDirectoryRow>,
    kpis: DoctorDirectoryKpis,
    searchQuery: String,
    selectedSpecialty: String?,
    selectedStatus: DoctorListStatus?,
    sortOrder: DoctorSortOrder,
    selectedDoctorId: Int?,
): DoctorsUiState {
    val q = searchQuery.trim().lowercase()
    val filtered =
        rows
            .map { it.toUiModel() }
            .filter { doctor ->
                val matchesSearch =
                    q.isEmpty() ||
                        doctor.fullName.lowercase().contains(q) ||
                        doctor.specialty.lowercase().contains(q) ||
                        doctor.email.lowercase().contains(q) ||
                        doctor.phone.lowercase().contains(q) ||
                        (doctor.licenseNumber?.lowercase()?.contains(q) == true)
                val matchesSpecialty =
                    selectedSpecialty == null || doctor.specialty == selectedSpecialty
                val matchesStatus = selectedStatus == null || doctor.status == selectedStatus
                matchesSearch && matchesSpecialty && matchesStatus
            }
            .let { list ->
                when (sortOrder) {
                    DoctorSortOrder.NAME_AZ -> list.sortedBy { it.fullName.lowercase() }
                    DoctorSortOrder.NAME_ZA -> list.sortedByDescending { it.fullName.lowercase() }
                    DoctorSortOrder.MOST_APPOINTMENTS ->
                        list.sortedByDescending { it.totalAppointmentsCount }
                    DoctorSortOrder.LEAST_APPOINTMENTS ->
                        list.sortedBy { it.totalAppointmentsCount }
                }
            }
    return DoctorsUiState(
        doctors = filtered,
        kpis = kpis,
        searchQuery = searchQuery,
        selectedSpecialty = selectedSpecialty,
        selectedStatus = selectedStatus,
        sortOrder = sortOrder,
        selectedDoctorId = selectedDoctorId,
    )
}
