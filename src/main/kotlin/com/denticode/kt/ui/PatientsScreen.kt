@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
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
import com.denticode.kt.data.Patient
import com.denticode.kt.data.PatientDirectoryKpis
import com.denticode.kt.data.PatientDirectoryRow
import com.denticode.kt.data.PatientRegistrationRequest
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.feedback.LoadingIndicator
import com.denticode.kt.ui.components.inputs.AppDatePickerField
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.components.inputs.rememberPastOrTodaySelectableDates
import com.denticode.kt.ui.patients.ModernPatientsContent
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PatientsScreen(
    repo: DentiRepository,
    onOpenPatientDetail: (Patient) -> Unit = {},
) {
    var directoryRows by remember { mutableStateOf<List<PatientDirectoryRow>>(emptyList()) }
    var kpis by remember {
        mutableStateOf(
            PatientDirectoryKpis(
                totalPatients = 0,
                activePatients = 0,
                newThisMonth = 0,
                scheduledAppointments = 0,
                pendingDebt = 0.0,
            ),
        )
    }
    var showRegistrationForm by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun reloadDirectory() {
        val (k, rows) = withContext(Dispatchers.IO) { repo.loadPatientDirectory() }
        kpis = k
        directoryRows = rows
        loaded = true
    }

    LaunchedEffect(Unit) {
        reloadDirectory()
    }

    if (!loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator()
        }
    } else {
        ModernPatientsContent(
        directoryRows = directoryRows,
        kpis = kpis,
        onRegisterClick = {
            saveError = null
            showRegistrationForm = true
        },
        onOpenPatientDetail = onOpenPatientDetail,
        )
    }

    if (showRegistrationForm) {
        ClientRegistrationDialog(
            isSaving = isSaving,
            errorMessage = saveError,
            onDismiss = {
                if (!isSaving) {
                    showRegistrationForm = false
                    saveError = null
                }
            },
            onSubmit = { request ->
                isSaving = true
                saveError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            val existing = repo.findPatientByFullName(request.firstName, request.lastName)
                            if (existing != null) {
                                throw IllegalArgumentException("Ya existe un paciente con ese nombre.")
                            }
                            repo.registerPatient(request)
                        }
                        reloadDirectory()
                    }.onSuccess {
                        showRegistrationForm = false
                    }.onFailure { error ->
                        saveError = error.message ?: "No se pudo registrar el cliente."
                    }
                    isSaving = false
                }
            },
        )
    }
}

@Composable
private fun ClientRegistrationDialog(
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (PatientRegistrationRequest) -> Unit,
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf(LocalDate.now().minusYears(25)) }
    val birthSelectableDates = rememberPastOrTodaySelectableDates()
    var documentNumber by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var medicalHistorySummary by remember { mutableStateOf("") }
    val canSubmit =
        firstName.isNotBlank() &&
            lastName.isNotBlank() &&
            contactPhone.isNotBlank() &&
            !isSaving

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 560.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = "Registrar cliente",
                    style = AppTypography.SectionTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Captura los datos básicos del paciente para agregarlo al directorio.",
                    style = AppTypography.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AppTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = "Nombre",
                placeholder = "Ana",
                enabled = !isSaving,
            )
            AppTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = "Apellido",
                placeholder = "López",
                enabled = !isSaving,
            )
            AppTextField(
                value = documentNumber,
                onValueChange = { documentNumber = it },
                label = "Documento de identidad",
                placeholder = "1234567",
                enabled = !isSaving,
            )
            AppDatePickerField(
                label = "Fecha de nacimiento",
                value = birthDate,
                onValueChange = { birthDate = it },
                enabled = !isSaving,
                selectableDates = birthSelectableDates,
                shortcuts = null,
            )
            AppTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it },
                label = "Teléfono",
                placeholder = "+34 600 000 000",
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "cliente@correo.com",
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            AppTextArea(
                value = medicalHistorySummary,
                onValueChange = { medicalHistorySummary = it },
                label = "Resumen médico",
                placeholder = "Alergias, condiciones relevantes o notas iniciales",
                enabled = !isSaving,
                minLines = 3,
                maxLines = 5,
            )
            errorMessage?.let {
                Text(
                    text = it,
                    style = AppTypography.BodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, alignment = Alignment.End),
            ) {
                AppOutlinedButton(
                    text = "Cancelar",
                    onClick = onDismiss,
                    enabled = !isSaving,
                )
                AppButton(
                    text = if (isSaving) "Guardando..." else "Guardar cliente",
                    onClick = {
                        onSubmit(
                            PatientRegistrationRequest(
                                firstName = firstName,
                                lastName = lastName,
                                dateOfBirth = birthDate.toString(),
                                documentNumber = documentNumber,
                                contactPhone = contactPhone,
                                email = email,
                                medicalHistorySummary = medicalHistorySummary,
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}
