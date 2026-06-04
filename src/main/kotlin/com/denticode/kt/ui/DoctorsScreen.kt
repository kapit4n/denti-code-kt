package com.denticode.kt.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.Doctor
import com.denticode.kt.data.DoctorDirectoryKpis
import com.denticode.kt.data.DoctorDirectoryRow
import com.denticode.kt.data.DoctorRegistrationRequest
import com.denticode.kt.data.DoctorUpdateRequest
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.doctors.DoctorEditDialog
import com.denticode.kt.ui.doctors.DoctorRegistrationDialog
import com.denticode.kt.ui.doctors.DoctorToggleActiveDialog
import com.denticode.kt.ui.doctors.DoctorUiModel
import com.denticode.kt.ui.doctors.ModernDoctorsContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DoctorsScreen(
    repo: DentiRepository,
    onOpenDoctorDetail: (Doctor) -> Unit = {},
    onViewDoctorSchedule: (Doctor) -> Unit = {},
) {
    val messenger = LocalAppMessenger.current
    val scope = rememberCoroutineScope()
    var directoryRows by remember { mutableStateOf<List<DoctorDirectoryRow>>(emptyList()) }
    var kpis by remember {
        mutableStateOf(
            DoctorDirectoryKpis(
                totalDoctors = 0,
                activeDoctors = 0,
                todayAppointments = 0,
                specialtyCount = 0,
            ),
        )
    }
    var showRegister by remember { mutableStateOf(false) }
    var editingDoctor by remember { mutableStateOf<DoctorDirectoryRow?>(null) }
    var togglingDoctor by remember { mutableStateOf<DoctorDirectoryRow?>(null) }
    var saveBusy by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    suspend fun reloadDirectory() {
        val (loadedKpis, rows) = withContext(Dispatchers.IO) { repo.loadDoctorDirectory() }
        kpis = loadedKpis
        directoryRows = rows
    }

    LaunchedEffect(Unit) {
        reloadDirectory()
    }

    fun findRow(doctor: DoctorUiModel): DoctorDirectoryRow? =
        directoryRows.find { it.doctor.id == doctor.id }

    ModernDoctorsContent(
        directoryRows = directoryRows,
        kpis = kpis,
        onNewDoctorClick = {
            saveError = null
            showRegister = true
        },
        onViewProfile = { doctor ->
            findRow(doctor)?.doctor?.let(onOpenDoctorDetail)
                ?: messenger.showError("No se encontró el doctor seleccionado.")
        },
        onEdit = { doctor ->
            saveError = null
            editingDoctor = findRow(doctor)
        },
        onViewSchedule = { doctor ->
            findRow(doctor)?.doctor?.let(onViewDoctorSchedule)
                ?: messenger.showError("No se encontró el doctor seleccionado.")
        },
        onToggleActive = { doctor ->
            saveError = null
            togglingDoctor = findRow(doctor)
        },
    )

    if (showRegister) {
        DoctorRegistrationDialog(
            isSaving = saveBusy,
            errorMessage = saveError,
            onDismiss = {
                if (!saveBusy) {
                    showRegister = false
                    saveError = null
                }
            },
            onSubmit = { request ->
                saveBusy = true
                saveError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.registerDoctor(request)
                        }
                        reloadDirectory()
                    }.onSuccess {
                        showRegister = false
                        messenger.showSuccess("Doctor registrado correctamente.")
                    }.onFailure { e ->
                        saveError = e.message ?: "No se pudo registrar el doctor."
                    }
                    saveBusy = false
                }
            },
        )
    }

    editingDoctor?.let { row ->
        DoctorEditDialog(
            doctor = row.doctor,
            status = row.status,
            isSaving = saveBusy,
            errorMessage = saveError,
            onDismiss = {
                if (!saveBusy) {
                    editingDoctor = null
                    saveError = null
                }
            },
            onSubmit = { request: DoctorUpdateRequest ->
                saveBusy = true
                saveError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.updateDoctor(row.doctor.id, request)
                        }
                        reloadDirectory()
                    }.onSuccess {
                        editingDoctor = null
                        messenger.showSuccess("Doctor actualizado correctamente.")
                    }.onFailure { e ->
                        saveError = e.message ?: "No se pudo actualizar el doctor."
                    }
                    saveBusy = false
                }
            },
        )
    }

    togglingDoctor?.let { row ->
        DoctorToggleActiveDialog(
            doctorName = row.doctor.fullName,
            currentlyActive = row.doctor.isActive,
            isSaving = saveBusy,
            errorMessage = saveError,
            onDismiss = {
                if (!saveBusy) {
                    togglingDoctor = null
                    saveError = null
                }
            },
            onConfirm = {
                saveBusy = true
                saveError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.setDoctorActive(row.doctor.id, active = !row.doctor.isActive)
                        }
                        reloadDirectory()
                    }.onSuccess {
                        togglingDoctor = null
                        messenger.showSuccess(
                            if (row.doctor.isActive) "Doctor desactivado." else "Doctor reactivado.",
                        )
                    }.onFailure { e ->
                        saveError = e.message ?: "No se pudo cambiar el estado del doctor."
                    }
                    saveBusy = false
                }
            },
        )
    }
}
