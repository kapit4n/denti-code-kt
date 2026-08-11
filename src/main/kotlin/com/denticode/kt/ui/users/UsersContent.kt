package com.denticode.kt.ui.users

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.AppUser
import com.denticode.kt.data.UserDirectoryKpis
import com.denticode.kt.data.UserRole
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppIconButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.cards.AppCard
import com.denticode.kt.ui.components.cards.MetricCard
import com.denticode.kt.ui.components.feedback.EmptyState
import com.denticode.kt.ui.components.feedback.StatusChip
import com.denticode.kt.ui.components.inputs.AppDropdownField
import com.denticode.kt.ui.components.inputs.AppSearchField
import com.denticode.kt.ui.models.StatusKind
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val userDateFmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES"))

@Composable
fun UsersContent(
    users: List<AppUser>,
    kpis: UserDirectoryKpis,
    onNewUserClick: () -> Unit,
    onRolesClick: () -> Unit,
    onEdit: (UserUiModel) -> Unit,
    onToggleActive: (UserUiModel) -> Unit,
    onDelete: (UserUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedRole by rememberSaveable { mutableStateOf<UserRole?>(null) }
    var selectedStatus by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var sortOrder by rememberSaveable { mutableStateOf(UserSortOrder.NAME_AZ) }
    var selectedUserId by rememberSaveable { mutableStateOf<Int?>(null) }

    val roleOptions = remember { UserRole.entries }
    val sortOptions = remember { UserSortOrder.entries.map { it.label } }

    val uiState =
        remember(users, kpis, searchQuery, selectedRole, selectedStatus, sortOrder, selectedUserId) {
            buildUsersUiState(
                rows = users,
                kpis = kpis,
                searchQuery = searchQuery,
                selectedRole = selectedRole,
                selectedStatus = selectedStatus,
                sortOrder = sortOrder,
                selectedUserId = selectedUserId,
            )
        }

    val hasFilters =
        searchQuery.isNotBlank() || selectedRole != null || selectedStatus != null

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(PatientsPremiumPalette.background)
                .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        UsersPageHeader(onNewUserClick = onNewUserClick, onRolesClick = onRolesClick)
        UsersStatsRow(kpis = uiState.kpis)
        UserSearchFilters(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            roleLabel = selectedRole?.displayLabel,
            roleOptions = roleOptions,
            onRoleSelect = { role -> selectedRole = role },
            statusLabel =
                when (selectedStatus) {
                    null -> "Todos"
                    true -> "Activos"
                    false -> "Inactivos"
                },
            onStatusSelect = {
                selectedStatus =
                    when (it) {
                        "Todos" -> null
                        "Activos" -> true
                        else -> false
                    }
            },
            sortLabel = sortOrder.label,
            sortOptions = sortOptions,
            onSortSelect = { label -> sortOrder = UserSortOrder.fromLabel(label) },
        )

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = uiState.users.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize(),
            ) {
                EmptyState(
                    title = if (hasFilters) "Sin resultados" else "Aún no hay usuarios",
                    message =
                        if (hasFilters) {
                            "Ningún usuario coincide con la búsqueda y filtros actuales."
                        } else {
                            "Registra el primer usuario para dar acceso al sistema."
                        },
                    icon = {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    action = {
                        if (!hasFilters) {
                            AppButton(text = "Nuevo usuario", onClick = onNewUserClick)
                        }
                    },
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = uiState.users.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize(),
            ) {
                UsersTable(
                    users = uiState.users,
                    selectedUserId = selectedUserId,
                    onSelectUser = { selectedUserId = it.id },
                    onEdit = onEdit,
                    onToggleActive = onToggleActive,
                    onDelete = onDelete,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun UsersPageHeader(onNewUserClick: () -> Unit, onRolesClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(
                "Usuarios",
                style = AppTypography.PageTitle,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Accesos y roles del sistema.",
                style = AppTypography.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppOutlinedButton(
                text = "Roles y permisos",
                onClick = onRolesClick,
                leadingIcon = {
                    androidx.compose.material3.Icon(Icons.Default.Groups, null, Modifier.size(18.dp))
                },
            )
            AppButton(
                text = "Nuevo usuario",
                onClick = onNewUserClick,
                leadingIcon = {
                    androidx.compose.material3.Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                },
            )
        }
    }
}

@Composable
private fun UsersStatsRow(kpis: UserDirectoryKpis) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        MetricCard(
            label = "Usuarios",
            value = kpis.totalUsers.toString(),
            modifier = Modifier.weight(1f),
        )
        MetricCard(
            label = "Activos",
            value = kpis.activeUsers.toString(),
            modifier = Modifier.weight(1f),
        )
        MetricCard(
            label = "Administradores",
            value = kpis.adminCount.toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun UserSearchFilters(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    roleLabel: String?,
    roleOptions: List<UserRole>,
    onRoleSelect: (UserRole) -> Unit,
    statusLabel: String,
    onStatusSelect: (String) -> Unit,
    sortLabel: String,
    sortOptions: List<String>,
    onSortSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppSearchField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = null,
            placeholder = "Buscar por nombre, correo o rol…",
            keyboardShortcutHint = null,
            modifier = Modifier.weight(1.6f),
        )
        AppDropdownField(
            label = "Rol",
            options = roleOptions,
            selected = roleOptions.firstOrNull { it.displayLabel == roleLabel },
            onSelected = onRoleSelect,
            optionLabel = { it.displayLabel },
            placeholder = roleLabel ?: "Todos",
            modifier = Modifier.weight(1f),
        )
        AppDropdownField(
            label = "Estado",
            options = listOf("Todos", "Activos", "Inactivos"),
            selected = statusLabel,
            onSelected = onStatusSelect,
            optionLabel = { it },
            placeholder = statusLabel,
            modifier = Modifier.weight(1f),
        )
        AppDropdownField(
            label = "Orden",
            options = sortOptions,
            selected = sortLabel,
            onSelected = onSortSelect,
            optionLabel = { it },
            placeholder = sortLabel,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun UsersTable(
    users: List<UserUiModel>,
    selectedUserId: Int?,
    onSelectUser: (UserUiModel) -> Unit,
    onEdit: (UserUiModel) -> Unit,
    onToggleActive: (UserUiModel) -> Unit,
    onDelete: (UserUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppSpacing.md),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            ) {
                Text("Nombre", style = AppTypography.Caption, modifier = Modifier.weight(2f))
                Text("Correo", style = AppTypography.Caption, modifier = Modifier.weight(2.2f))
                Text("Rol", style = AppTypography.Caption, modifier = Modifier.weight(1.2f))
                Text("Estado", style = AppTypography.Caption, modifier = Modifier.weight(1f))
                Text("Creado", style = AppTypography.Caption, modifier = Modifier.weight(1.2f))
                Text("Acciones", style = AppTypography.Caption, modifier = Modifier.widthIn(min = 132.dp))
            }
            users.forEach { user ->
                UsersTableRow(
                    user = user,
                    selected = user.id == selectedUserId,
                    onClick = { onSelectUser(user) },
                    onEdit = { onEdit(user) },
                    onToggleActive = { onToggleActive(user) },
                    onDelete = { onDelete(user) },
                )
            }
        }
    }
}

@Composable
private fun UsersTableRow(
    user: UserUiModel,
    selected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
) {
    val container =
        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(container)
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(user.displayName, style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(2f))
        Text(user.email, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(2.2f))
        Text(user.role.displayLabel, style = AppTypography.BodySmall, modifier = Modifier.weight(1.2f))
        StatusChip(
            text = if (user.isActive) "Activo" else "Inactivo",
            kind = if (user.isActive) StatusKind.Success else StatusKind.Neutral,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = Instant.ofEpochMilli(user.createdAtEpochMs).atZone(ZoneId.systemDefault()).format(userDateFmt),
            style = AppTypography.BodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.2f),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier.widthIn(min = 132.dp),
        ) {
            AppIconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                IconCompat(Icons.Default.Edit, "Editar")
            }
            AppIconButton(onClick = onToggleActive, modifier = Modifier.size(36.dp)) {
                IconCompat(if (user.isActive) Icons.Default.PersonOff else Icons.Default.PowerSettingsNew, "Cambiar estado")
            }
            AppIconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                IconCompat(Icons.Default.Delete, "Eliminar")
            }
        }
    }
}

@Composable
private fun IconCompat(imageVector: androidx.compose.ui.graphics.vector.ImageVector, description: String) {
    androidx.compose.material3.Icon(imageVector, description, Modifier.size(16.dp))
}
