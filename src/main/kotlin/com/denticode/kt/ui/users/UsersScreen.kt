package com.denticode.kt.ui.users

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.denticode.kt.data.AppUser
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.UserDirectoryKpis
import com.denticode.kt.data.UserRegistrationRequest
import com.denticode.kt.data.UserUpdateRequest
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.components.feedback.LoadingIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun UsersScreen(repo: DentiRepository) {
    val messenger = LocalAppMessenger.current
    val scope = rememberCoroutineScope()
    var users by remember { mutableStateOf<List<AppUser>>(emptyList()) }
    var kpis by remember { mutableStateOf(UserDirectoryKpis(0, 0, 0)) }
    var loaded by remember { mutableStateOf(false) }
    var showRegister by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<UserUiModel?>(null) }
    var togglingUser by remember { mutableStateOf<UserUiModel?>(null) }
    var deletingUser by remember { mutableStateOf<UserUiModel?>(null) }
    var saveBusy by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    suspend fun reloadUsers() {
        val (loadedKpis, rows) = withContext(Dispatchers.IO) { repo.listUsers() }
        kpis = loadedKpis
        users = rows
        loaded = true
    }

    LaunchedEffect(Unit) {
        reloadUsers()
    }

    if (!loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator()
        }
    } else {
        fun findRow(user: UserUiModel): AppUser? = users.find { it.id == user.id }

        UsersContent(
            users = users,
            kpis = kpis,
            onNewUserClick = {
                saveError = null
                showRegister = true
            },
            onEdit = { user ->
                saveError = null
                editingUser = findRow(user)?.toUiModel()
            },
            onToggleActive = { user ->
                saveError = null
                togglingUser = findRow(user)?.toUiModel()
            },
            onDelete = { user ->
                deletingUser = findRow(user)?.toUiModel()
            },
            modifier = Modifier.fillMaxSize(),
        )
    }

    if (showRegister) {
        UserRegistrationDialog(
            isSaving = saveBusy,
            errorMessage = saveError,
            onDismiss = {
                if (!saveBusy) {
                    showRegister = false
                    saveError = null
                }
            },
            onSubmit = { request: UserRegistrationRequest ->
                saveBusy = true
                saveError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.registerUser(request)
                        }
                        reloadUsers()
                    }.onSuccess {
                        showRegister = false
                        messenger.showSuccess("Usuario registrado correctamente.")
                    }.onFailure { e ->
                        saveError = e.message ?: "No se pudo registrar el usuario."
                    }
                    saveBusy = false
                }
            },
        )
    }

    editingUser?.let { user ->
        UserEditDialog(
            user = user,
            isSaving = saveBusy,
            errorMessage = saveError,
            onDismiss = {
                if (!saveBusy) {
                    editingUser = null
                    saveError = null
                }
            },
            onSubmit = { request: UserUpdateRequest ->
                saveBusy = true
                saveError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.updateUser(user.id, request)
                        }
                        reloadUsers()
                    }.onSuccess {
                        editingUser = null
                        messenger.showSuccess("Usuario actualizado correctamente.")
                    }.onFailure { e ->
                        saveError = e.message ?: "No se pudo actualizar el usuario."
                    }
                    saveBusy = false
                }
            },
        )
    }

    togglingUser?.let { user ->
        UserToggleActiveDialog(
            userName = user.displayName,
            currentlyActive = user.isActive,
            isSaving = saveBusy,
            errorMessage = saveError,
            onDismiss = {
                if (!saveBusy) {
                    togglingUser = null
                    saveError = null
                }
            },
            onConfirm = {
                saveBusy = true
                saveError = null
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.setUserActive(user.id, active = !user.isActive)
                        }
                        reloadUsers()
                    }.onSuccess {
                        togglingUser = null
                        messenger.showSuccess(if (user.isActive) "Usuario desactivado." else "Usuario reactivado.")
                    }.onFailure { e ->
                        saveError = e.message ?: "No se pudo cambiar el estado del usuario."
                    }
                    saveBusy = false
                }
            },
        )
    }

    deletingUser?.let { user ->
        UserDeleteDialog(
            userName = user.displayName,
            isSaving = saveBusy,
            onDismiss = {
                if (!saveBusy) {
                    deletingUser = null
                }
            },
            onConfirm = {
                saveBusy = true
                scope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            repo.deleteUser(user.id)
                        }
                        reloadUsers()
                    }.onSuccess {
                        deletingUser = null
                        messenger.showSuccess("Usuario eliminado permanentemente.")
                    }.onFailure { e ->
                        messenger.showError(e.message ?: "Error al eliminar usuario.")
                    }
                    saveBusy = false
                }
            },
        )
    }
}
