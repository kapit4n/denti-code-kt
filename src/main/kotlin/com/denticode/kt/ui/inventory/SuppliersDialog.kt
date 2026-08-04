@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.Supplier
import com.denticode.kt.data.SupplierRegisterRequest
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.dialogs.AppBasicDialog
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.components.inputs.AppTextField
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

@Composable
fun SuppliersDialog(
    suppliers: List<Supplier>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onRegister: (SupplierRegisterRequest) -> Unit,
    onUpdate: (id: Int, request: SupplierRegisterRequest) -> Unit,
    onDelete: (Int) -> Unit,
) {
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Supplier?>(null) }
    var deleting by remember { mutableStateOf<Supplier?>(null) }

    if (showForm) {
        SupplierFormDialog(
            title = "Nuevo proveedor",
            submitLabel = "Crear",
            isSaving = isSaving,
            errorMessage = errorMessage,
            onDismiss = { showForm = false },
            onSubmit = { req ->
                onRegister(req)
                showForm = false
            },
        )
        return
    }

    editing?.let { supplier ->
        SupplierFormDialog(
            title = "Editar proveedor",
            submitLabel = "Guardar",
            isSaving = isSaving,
            errorMessage = errorMessage,
            initialName = supplier.name,
            initialContactName = supplier.contactName.orEmpty(),
            initialPhone = supplier.phone.orEmpty(),
            initialEmail = supplier.email.orEmpty(),
            initialAddress = supplier.address.orEmpty(),
            initialNotes = supplier.notes.orEmpty(),
            onDismiss = { editing = null },
            onSubmit = { req ->
                onUpdate(supplier.id, req)
                editing = null
            },
        )
        return
    }

    deleting?.let { supplier ->
        AppBasicDialog(
            title = "Eliminar proveedor",
            message = "¿Eliminar \"${supplier.name}\"?",
            onDismissRequest = { deleting = null },
            confirmText = "Eliminar",
            onConfirm = {
                onDelete(supplier.id)
                deleting = null
            },
            dismissText = "Cancelar",
            onDismiss = { deleting = null },
        )
        return
    }

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 720.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Proveedores",
                        style = AppTypography.SectionTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Proveedores de insumos para pedidos de reposición.",
                        style = AppTypography.Caption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                AppButton(
                    text = "Nuevo proveedor",
                    onClick = { showForm = true },
                    leadingIcon = { Icon(Icons.Default.Add, null, Modifier.size(18.dp)) },
                )
            }

            if (suppliers.isEmpty()) {
                Text(
                    "No hay proveedores registrados.",
                    style = AppTypography.BodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    suppliers.forEach { supplier ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    supplier.name,
                                    style = AppTypography.Body,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                val detail =
                                    listOfNotNull(
                                        supplier.contactName,
                                        supplier.phone,
                                        supplier.email,
                                    ).joinToString(" · ")
                                Text(
                                    detail.ifBlank { "Sin datos de contacto" },
                                    style = AppTypography.Caption,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            IconButton(onClick = { editing = supplier }) {
                                Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { deleting = supplier }) {
                                Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cerrar", onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun SupplierFormDialog(
    title: String,
    submitLabel: String,
    isSaving: Boolean,
    errorMessage: String?,
    initialName: String = "",
    initialContactName: String = "",
    initialPhone: String = "",
    initialEmail: String = "",
    initialAddress: String = "",
    initialNotes: String = "",
    onDismiss: () -> Unit,
    onSubmit: (SupplierRegisterRequest) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var contactName by remember { mutableStateOf(initialContactName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var email by remember { mutableStateOf(initialEmail) }
    var address by remember { mutableStateOf(initialAddress) }
    var notes by remember { mutableStateOf(initialNotes) }

    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 560.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(title, style = AppTypography.SectionTitle, color = MaterialTheme.colorScheme.onSurface)
            AppTextField(value = name, onValueChange = { name = it }, label = "Nombre *", placeholder = "Ej. Dental Supply Bolivia", enabled = !isSaving)
            AppTextField(value = contactName, onValueChange = { contactName = it }, label = "Contacto", placeholder = "Nombre de contacto", enabled = !isSaving)
            AppTextField(value = phone, onValueChange = { phone = it }, label = "Teléfono", placeholder = "Ej. +591 2 2345678", enabled = !isSaving)
            AppTextField(value = email, onValueChange = { email = it }, label = "Correo", placeholder = "ventas@proveedor.com", enabled = !isSaving)
            AppTextField(value = address, onValueChange = { address = it }, label = "Dirección", placeholder = "Dirección del proveedor", enabled = !isSaving)
            AppTextArea(value = notes, onValueChange = { notes = it }, label = "Notas", placeholder = "Observaciones…", enabled = !isSaving)
            errorMessage?.let {
                Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.error)
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            ) {
                AppOutlinedButton(text = "Cancelar", onClick = onDismiss, enabled = !isSaving)
                AppButton(
                    text = if (isSaving) "Guardando..." else submitLabel,
                    onClick = {
                        val trimmed = name.trim()
                        if (trimmed.isNotEmpty()) {
                            onSubmit(
                                SupplierRegisterRequest(
                                    name = trimmed,
                                    contactName = contactName.trim().ifBlank { null },
                                    phone = phone.trim().ifBlank { null },
                                    email = email.trim().ifBlank { null },
                                    address = address.trim().ifBlank { null },
                                    notes = notes.trim().ifBlank { null },
                                ),
                            )
                        }
                    },
                    enabled = !isSaving && name.trim().isNotEmpty(),
                )
            }
        }
    }
}
