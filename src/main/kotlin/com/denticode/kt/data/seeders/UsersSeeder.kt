package com.denticode.kt.data.seeders

import com.denticode.kt.data.UserRolesTable
import com.denticode.kt.data.UsersTable
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
        if (UsersTable.selectAll().count() > 0) return emptyList()

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
