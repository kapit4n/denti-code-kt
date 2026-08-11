package com.denticode.kt.ui.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.Permission
import com.denticode.kt.data.RoleCatalog
import com.denticode.kt.data.UserRole
import com.denticode.kt.ui.components.dialogs.AppSurfaceDialog
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

@Composable
fun RolesDialog(
    onDismiss: () -> Unit,
) {
    val roles = RoleCatalog.roles()
    AppSurfaceDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 760.dp),
    ) {
        Column(
            modifier = Modifier.heightIn(max = 640.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text("Roles y permisos", style = AppTypography.SectionTitle, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Matriz de acceso por rol. La aplicación aún no impone estos permisos en las pantallas (llegará con el login y las sesiones).",
                    style = AppTypography.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            roles.forEach { role ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Text(
                        role.displayLabel,
                        style = AppTypography.Body,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.widthIn(min = 128.dp),
                    )
                    Text(
                        role.description,
                        style = AppTypography.BodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            PermissionMatrix(roles = roles)
        }
    }
}

@Composable
private fun PermissionMatrix(roles: List<UserRole>) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        MatrixHeaderRow(roles = roles)
        val grouped =
            Permission.entries.groupBy { it.moduleEs }
        grouped.forEach { (module, permissions) ->
            Text(
                text = module,
                style = AppTypography.Caption,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            )
            permissions.forEach { permission ->
                MatrixPermissionRow(permission = permission, roles = roles)
            }
        }
    }
}

@Composable
private fun MatrixHeaderRow(roles: List<UserRole>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Permiso",
            style = AppTypography.Caption,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(3f),
        )
        roles.forEach { role ->
            Text(
                role.displayLabel,
                style = AppTypography.Caption,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MatrixPermissionRow(permission: Permission, roles: List<UserRole>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            permission.labelEs,
            style = AppTypography.BodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(3f),
        )
        roles.forEach { role ->
            val allowed = role.permissions.contains(permission)
            val tint = if (allowed) Color(0xFF16A34A) else MaterialTheme.colorScheme.outlineVariant
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Check,
                contentDescription =
                    if (allowed) "Permitido para ${role.displayLabel}" else "No permitido para ${role.displayLabel}",
                tint = tint,
                modifier = Modifier.weight(1f).size(18.dp),
            )
        }
    }
}
