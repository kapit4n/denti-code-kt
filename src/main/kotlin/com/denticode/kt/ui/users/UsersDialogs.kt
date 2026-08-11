package com.denticode.kt.ui.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.UserRegistrationRequest
import com.denticode.kt.data.UserRole
import com.denticode.kt.data.UserUpdateRequest
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.inputs.AppDropdownField
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

@Composable
fun UserRegistrationDialog(
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (UserRegistrationRequest) -> Unit,
) {
    UserFormDialog(
        title = "Nuevo usuario",
        subtitle = "Da acceso al sistema a un miembro de la clínica.",
        submitLabel = "Registrar",
        isSaving = isSaving,
        errorMessage = errorMessage,
        onDismiss = onDismiss,
        onSubmit = { form ->
            onSubmit(
                UserRegistrationRequest(
                    email = form.email,
                    displayName = form.displayName,
                    password = form.password,
                    role = form.role,
                    isActive = form.isActive,
                ),
            )
        },
    )
}

@Composable
fun UserEditDialog(
    user: UserUiModel,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (UserUpdateRequest) -> Unit,
) {
    UserFormDialog(
        title = "Editar usuario",
        subtitle = user.displayName,
        submitLabel = "Guardar cambios",
        isSaving = isSaving,
        errorMessage = errorMessage,
        onDismiss = onDismiss,
        initialEmail = user.email,
        initialDisplayName = user.displayName,
        initialRole = user.role,
        initialActive = user.isActive,
        requirePassword = false,
        onSubmit = { form ->
            onSubmit(
                UserUpdateRequest(
                    email = form.email,
                    displayName = form.displayName,
                    role = form.role,
                    isActive = form.isActive,
                    newPassword = form.password.takeIf { it.isNotBlank() },
                ),
            )
        },
    )
}

@Composable
fun UserToggleActiveDialog(
    userName: String,
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
                if (currentlyActive) "Desactivar usuario" else "Reactivar usuario",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                if (currentlyActive) {
                    "¿Desactivar a $userName? Perderá el acceso al sistema hasta ser reactivado."
                } else {
                    "¿Reactivar a $userName para que vuelva a acceder al sistema?"
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

@Composable
fun UserDeleteDialog(
    userName: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 440.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = "Eliminar usuario",
                    style = AppTypography.SectionTitle,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = "¿Estás seguro de que deseas eliminar permanentemente a $userName? Se revocarán todos sus accesos. Esta acción no se puede deshacer.",
                    style = AppTypography.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Eliminando..." else "Eliminar permanentemente",
                    onClick = onConfirm,
                    enabled = !isSaving,
                )
            }
        }
    }
}

private data class UserFormValues(
    val email: String,
    val displayName: String,
    val password: String,
    val role: UserRole,
    val isActive: Boolean,
)

@Composable
private fun UserFormDialog(
    title: String,
    subtitle: String,
    submitLabel: String,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (UserFormValues) -> Unit,
    initialEmail: String = "",
    initialDisplayName: String = "",
    initialRole: UserRole = UserRole.USER,
    initialActive: Boolean = true,
    requirePassword: Boolean = true,
) {
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }
    var displayName by remember(initialDisplayName) { mutableStateOf(initialDisplayName) }
    var password by remember { mutableStateOf("") }
    var role by remember(initialRole) { mutableStateOf(initialRole) }
    var isActive by remember(initialActive) { mutableStateOf(initialActive) }

    val canSubmit =
        email.trim().isNotEmpty() &&
            displayName.trim().isNotEmpty() &&
            (!requirePassword || password.length >= 6) &&
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
            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = "Correo",
                enabled = !isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            AppTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = "Nombre",
                enabled = !isSaving,
            )
            AppDropdownField(
                label = "Rol",
                options = UserRole.entries,
                selected = role,
                onSelected = { role = it },
                enabled = !isSaving,
                optionLabel = { it.displayLabel },
            )
            AppTextField(
                value = password,
                onValueChange = { password = it },
                label = if (requirePassword) "Contraseña" else "Nueva contraseña (opcional)",
                enabled = !isSaving,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            Text(
                text = if (requirePassword) "Mínimo 6 caracteres." else "Déjalo vacío para no cambiar la contraseña.",
                style = AppTypography.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isActive, onCheckedChange = { isActive = it }, enabled = !isSaving)
                Text("Acceso activo", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurface)
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
                            UserFormValues(
                                email = email.trim(),
                                displayName = displayName.trim(),
                                password = password,
                                role = role,
                                isActive = isActive,
                            ),
                        )
                    },
                    enabled = canSubmit,
                )
            }
        }
    }
}
