package com.denticode.kt.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.Doctor
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.components.feedback.LoadingIndicator
import com.denticode.kt.ui.doctors.DoctorEditDialog
import com.denticode.kt.ui.doctors.DoctorToggleActiveDialog
import com.denticode.kt.ui.doctordetail.DoctorDetailUiState
import com.denticode.kt.ui.doctordetail.ModernDoctorDetailContent
import com.denticode.kt.ui.doctordetail.buildDoctorDetailUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDetailWindow(
    repo: DentiRepository,
    doctor: Doctor,
    onClose: () -> Unit,
    onViewSchedule: (Doctor) -> Unit,
    onDoctorUpdated: () -> Unit = {},
) {
    val messenger = LocalAppMessenger.current
    val scope = rememberCoroutineScope()
    var uiState by remember { mutableStateOf<DoctorDetailUiState?>(null) }
    var refreshNonce by remember { mutableStateOf(0) }
    var showEdit by remember { mutableStateOf(false) }
    var showToggleActive by remember { mutableStateOf(false) }
    var saveBusy by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(doctor.id, refreshNonce) {
        withContext(Dispatchers.IO) {
            val (_, rows) = repo.loadDoctorDirectory()
            val row = rows.find { it.doctor.id == doctor.id }
            uiState =
                row?.let { directoryRow ->
                    buildDoctorDetailUiState(
                        directoryRow = directoryRow,
                        appointments = repo.listAppointmentsForDoctor(directoryRow.doctor.id),
                    )
                }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F7FB),
    ) {
        val state = uiState
        if (state == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            ModernDoctorDetailContent(
                uiState = state,
                onClose = onClose,
                onEdit = {
                    saveError = null
                    showEdit = true
                },
                onViewSchedule = { onViewSchedule(state.doctor) },
                onToggleActive = {
                    saveError = null
                    showToggleActive = true
                },
            )
        }
    }

    val state = uiState
    if (showEdit && state != null) {
        DoctorEditDialog(
            doctor = state.doctor,
            status = state.status,
            isSaving = saveBusy,
            errorMessage = saveError,
            onDismiss = {
                if (!saveBusy) {
                    showEdit = false
                    saveError = null
                }
            },
            onSubmit = { request ->
                saveBusy = true
                saveError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.updateDoctor(state.doctor.id, request)
                        }
                    }.onSuccess {
                        refreshNonce++
                        showEdit = false
                        onDoctorUpdated()
                        messenger.showSuccess("Doctor actualizado correctamente.")
                    }.onFailure { e ->
                        saveError = e.message ?: "No se pudo actualizar el doctor."
                    }
                    saveBusy = false
                }
            },
        )
    }

    if (showToggleActive && state != null) {
        DoctorToggleActiveDialog(
            doctorName = state.doctor.fullName,
            currentlyActive = state.doctor.isActive,
            isSaving = saveBusy,
            errorMessage = saveError,
            onDismiss = {
                if (!saveBusy) {
                    showToggleActive = false
                    saveError = null
                }
            },
            onConfirm = {
                saveBusy = true
                saveError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.setDoctorActive(state.doctor.id, active = !state.doctor.isActive)
                        }
                    }.onSuccess {
                        refreshNonce++
                        showToggleActive = false
                        onDoctorUpdated()
                        messenger.showSuccess(
                            if (state.doctor.isActive) {
                                "Doctor desactivado."
                            } else {
                                "Doctor reactivado."
                            },
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
