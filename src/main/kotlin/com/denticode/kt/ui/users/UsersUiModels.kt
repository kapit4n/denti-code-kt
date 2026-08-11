package com.denticode.kt.ui.users

import com.denticode.kt.data.AppUser
import com.denticode.kt.data.UserDirectoryKpis
import com.denticode.kt.data.UserRole

enum class UserSortOrder(val label: String) {
    NAME_AZ("Nombre A-Z"),
    NAME_ZA("Nombre Z-A"),
    NEWEST("Más recientes"),
    OLDEST("Más antiguos"),
    ;

    companion object {
        fun fromLabel(label: String): UserSortOrder =
            entries.firstOrNull { it.label == label } ?: NAME_AZ
    }
}

data class UserUiModel(
    val id: Int,
    val email: String,
    val displayName: String,
    val role: UserRole,
    val isActive: Boolean,
    val createdAtEpochMs: Long,
)

data class UsersUiState(
    val users: List<UserUiModel>,
    val kpis: UserDirectoryKpis,
    val searchQuery: String,
    val selectedRole: UserRole?,
    val selectedStatus: Boolean?,
    val sortOrder: UserSortOrder,
)

fun AppUser.toUiModel(): UserUiModel =
    UserUiModel(
        id = id,
        email = email,
        displayName = displayName?.trim()?.takeIf { it.isNotEmpty() } ?: email.substringBefore("@"),
        role = role,
        isActive = isActive,
        createdAtEpochMs = createdAtEpochMs,
    )

fun buildUsersUiState(
    rows: List<AppUser>,
    kpis: UserDirectoryKpis,
    searchQuery: String,
    selectedRole: UserRole?,
    selectedStatus: Boolean?,
    sortOrder: UserSortOrder,
    selectedUserId: Int?,
): UsersUiState {
    val q = searchQuery.trim().lowercase()
    val filtered =
        rows
            .map { it.toUiModel() }
            .filter { user ->
                val matchesSearch =
                    q.isEmpty() ||
                        user.displayName.lowercase().contains(q) ||
                        user.email.lowercase().contains(q) ||
                        user.role.displayLabel.lowercase().contains(q)
                val matchesRole = selectedRole == null || user.role == selectedRole
                val matchesStatus = selectedStatus == null || user.isActive == selectedStatus
                matchesSearch && matchesRole && matchesStatus
            }
            .let { list ->
                when (sortOrder) {
                    UserSortOrder.NAME_AZ -> list.sortedBy { it.displayName.lowercase() }
                    UserSortOrder.NAME_ZA -> list.sortedByDescending { it.displayName.lowercase() }
                    UserSortOrder.NEWEST -> list.sortedByDescending { it.createdAtEpochMs }
                    UserSortOrder.OLDEST -> list.sortedBy { it.createdAtEpochMs }
                }
            }
    return UsersUiState(
        users = filtered,
        kpis = kpis,
        searchQuery = searchQuery,
        selectedRole = selectedRole,
        selectedStatus = selectedStatus,
        sortOrder = sortOrder,
    )
}
