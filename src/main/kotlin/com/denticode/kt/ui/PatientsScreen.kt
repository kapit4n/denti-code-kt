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
import com.denticode.kt.data.PatientRegistrationRequest
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.cards.AppCard
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.inputs.AppDatePickerField
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.components.inputs.rememberPastOrTodaySelectableDates
import com.denticode.kt.ui.navigation.PageHeader
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
    var rows by remember { mutableStateOf<List<Patient>>(emptyList()) }
    var showRegistrationForm by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        rows = withContext(Dispatchers.IO) { repo.listPatients() }
    }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        PageHeader(
            title = "Pacientes",
            subtitle = "Entidad Patient (denti-code-desktop) — datos demográficos y contacto.",
            modifier = Modifier.fillMaxWidth(),
            actions = {
                AppButton(
                    text = "Registrar cliente",
                    onClick = {
                        saveError = null
                        showRegistrationForm = true
                    },
                )
            },
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            items(rows, key = { it.id }) { p ->
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        Text(p.fullName, style = AppTypography.CardTitle, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "Nac. ${p.dateOfBirth} · ${p.contactPhone}",
                            style = AppTypography.BodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        p.email?.let {
                            Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        AppOutlinedButton(
                            text = "Ver citas y pagos",
                            onClick = { onOpenPatientDetail(p) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
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
                            repo.registerPatient(request)
                            repo.listPatients()
                        }
                    }.onSuccess { refreshedRows ->
                        rows = refreshedRows
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
            AppDatePickerField(
                label = "Fecha de nacimiento",
                value = birthDate,
                onValueChange = { birthDate = it },
                enabled = !isSaving,
                selectableDates = birthSelectableDates,
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
