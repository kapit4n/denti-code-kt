package com.denticode.kt.data.seeders

import com.denticode.kt.data.UserRolesTable
import com.denticode.kt.data.UsersTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

object UsersSeeder {

    data class SeedUser(
        val id: Int,
        val email: String,
        val displayName: String,
        val role: String,
    )

    fun seed(config: DemoDataConfig): List<SeedUser> {
        val existing =
            UsersTable
                .selectAll()
                .map { row ->
                    val userId = row[UsersTable.id]
                    val role =
                        UserRolesTable
                            .selectAll()
                            .where { UserRolesTable.userId eq userId }
                            .firstOrNull()
                            ?.get(UserRolesTable.role)
                            ?: "USER"
                    SeedUser(
                        id = userId,
                        email = row[UsersTable.email],
                        displayName = row[UsersTable.displayName] ?: "",
                        role = role,
                    )
                }
        if (existing.isNotEmpty()) return existing

        val now = System.currentTimeMillis()
        val users =
            listOf(
                SeedUser(0, "admin@sonrisa-clinica.bo", "María Elena Vargas", "ADMIN"),
                SeedUser(0, "recepcion@sonrisa-clinica.bo", "Lucía Fernández", "RECEPTIONIST"),
            )

        return users.map { u ->
            val userId =
                UsersTable.insert {
                    it[email] = u.email
                    it[passwordHash] = "\$2a\$10\$demoHashNotForProduction"
                    it[displayName] = u.displayName
                    it[preferredLocale] = "es"
                    it[isActive] = true
                    it[createdAtEpochMs] = now
                } get UsersTable.id

            UserRolesTable.insert {
                it[UserRolesTable.userId] = userId
                it[UserRolesTable.role] = u.role
            }

            u.copy(id = userId)
        }
    }
}
