@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.doctors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.Doctor
import com.denticode.kt.data.DoctorListStatus
import com.denticode.kt.data.DoctorRegistrationRequest
import com.denticode.kt.data.DoctorUpdateRequest
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

@Composable
fun DoctorRegistrationDialog(
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (DoctorRegistrationRequest) -> Unit,
) {
    DoctorFormDialog(
        title = "Nuevo doctor",
        subtitle = "Registra un profesional en el directorio clínico.",
        submitLabel = "Registrar",
        isSaving = isSaving,
        errorMessage = errorMessage,
        onDismiss = onDismiss,
        onSubmit = { form ->
            onSubmit(
                DoctorRegistrationRequest(
                    firstName = form.firstName,
                    lastName = form.lastName,
                    email = form.email,
                    contactPhone = form.contactPhone,
                    specialization = form.specialization,
                    licenseNumber = form.licenseNumber,
                    officeRoom = form.officeRoom,
                    isActive = form.isActive,
                ),
            )
        },
    )
}

@Composable
fun DoctorEditDialog(
    doctor: Doctor,
    status: DoctorListStatus,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (DoctorUpdateRequest) -> Unit,
) {
    DoctorFormDialog(
        title = "Editar doctor",
        subtitle = doctor.fullName,
        submitLabel = "Guardar",
        isSaving = isSaving,
        errorMessage = errorMessage,
        onDismiss = onDismiss,
        initialFirstName = doctor.firstName,
        initialLastName = doctor.lastName,
        initialEmail = doctor.email,
        initialPhone = doctor.contactPhone.orEmpty(),
        initialSpecialization = doctor.specialization.orEmpty(),
        initialLicense = doctor.licenseNumber.orEmpty(),
        initialOfficeRoom = doctor.officeRoom?.takeIf { !it.equals("VACATION", ignoreCase = true) }.orEmpty(),
        initialActive = doctor.isActive,
        initialVacation = status == DoctorListStatus.VACATION,
        onSubmit = { form ->
            val officeRoom =
                when {
                    form.onVacation -> "VACATION"
                    !form.officeRoom.isNullOrBlank() -> form.officeRoom
                    else -> null
                }
            onSubmit(
                DoctorUpdateRequest(
                    firstName = form.firstName,
                    lastName = form.lastName,
                    email = form.email,
                    contactPhone = form.contactPhone,
                    specialization = form.specialization,
                    licenseNumber = form.licenseNumber,
                    officeRoom = officeRoom,
                    isActive = form.isActive,
                ),
            )
        },
    )
}

@Composable
fun DoctorToggleActiveDialog(
    doctorName: String,
    currentlyActive: Boolean,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 440.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                if (currentlyActive) "Desactivar doctor" else "Reactivar doctor",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                if (currentlyActive) {
                    "¿Desactivar a $doctorName? No podrá asignarse en nuevas citas."
                } else {
                    "¿Reactivar a $doctorName en el directorio clínico?"
                },
                style = AppTypography.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            errorMessage?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Guardando..." else if (currentlyActive) "Desactivar" else "Reactivar",
                    onClick = onConfirm,
                    enabled = !isSaving,
                )
            }
        }
    }
}

private data class DoctorFormValues(
    val firstName: String,
    val lastName: String,
    val email: String,
    val contactPhone: String?,
    val specialization: String?,
    val licenseNumber: String?,
    val officeRoom: String?,
    val isActive: Boolean,
    val onVacation: Boolean = false,
)

@Composable
private fun DoctorFormDialog(
    title: String,
    subtitle: String,
    submitLabel: String,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (DoctorFormValues) -> Unit,
    initialFirstName: String = "",
    initialLastName: String = "",
    initialEmail: String = "",
    initialPhone: String = "",
    initialSpecialization: String = "",
    initialLicense: String = "",
    initialOfficeRoom: String = "",
    initialActive: Boolean = true,
    initialVacation: Boolean = false,
) {
    var firstName by remember(initialFirstName) { mutableStateOf(initialFirstName) }
    var lastName by remember(initialLastName) { mutableStateOf(initialLastName) }
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }
    var phone by remember(initialPhone) { mutableStateOf(initialPhone) }
    var specialization by remember(initialSpecialization) { mutableStateOf(initialSpecialization) }
    var license by remember(initialLicense) { mutableStateOf(initialLicense) }
    var officeRoom by remember(initialOfficeRoom) { mutableStateOf(initialOfficeRoom) }
    var isActive by remember(initialActive) { mutableStateOf(initialActive) }
    var onVacation by remember(initialVacation) { mutableStateOf(initialVacation) }

    val canSubmit =
        firstName.trim().isNotEmpty() &&
            lastName.trim().isNotEmpty() &&
            email.trim().isNotEmpty() &&
            !isSaving

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 560.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(title, style = AppTypography.SectionTitle, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                AppTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "Nombre",
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                )
                AppTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "Apellidos",
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                )
            }
            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = "Correo",
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            AppTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Teléfono (opcional)",
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
            AppTextField(
                value = specialization,
                onValueChange = { specialization = it },
                label = "Especialidad (opcional)",
                enabled = !isSaving,
            )
            AppTextField(
                value = license,
                onValueChange = { license = it },
                label = "Nº colegiado (opcional)",
                enabled = !isSaving,
            )
            AppTextField(
                value = officeRoom,
                onValueChange = { officeRoom = it },
                label = "Consultorio (opcional)",
                enabled = !isSaving && !onVacation,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isActive, onCheckedChange = { isActive = it }, enabled = !isSaving)
                Text("Activo en clínica", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurface)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = onVacation, onCheckedChange = { onVacation = it }, enabled = !isSaving)
                Text("En vacaciones", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurface)
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
                    text = if (isSaving) "Guardando..." else submitLabel,
                    onClick = {
                        onSubmit(
                            DoctorFormValues(
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                email = email.trim(),
                                contactPhone = phone.trim().takeIf { it.isNotEmpty() },
                                specialization = specialization.trim().takeIf { it.isNotEmpty() },
                                licenseNumber = license.trim().takeIf { it.isNotEmpty() },
                                officeRoom = officeRoom.trim().takeIf { it.isNotEmpty() },
                                isActive = isActive,
                                onVacation = onVacation,
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}
