package com.denticode.kt.data.seeders

import com.denticode.kt.data.ConsultoriesTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

object ConsultoriesSeeder {

    data class SeedConsultory(
        val id: Int,
        val name: String,
        val shortCode: String,
    )

    fun seed(config: DemoDataConfig): List<SeedConsultory> {
        if (ConsultoriesTable.selectAll().count() > 0) return emptyList()

        val consultories =
            listOf(
                SeedConsultory(0, "Consultorio 1 — Dr. Mamani", "C1"),
                SeedConsultory(0, "Consultorio 2 — Dra. Torres", "C2"),
                SeedConsultory(0, "Consultorio 3 — Dr. Gutiérrez", "C3"),
            )

        return consultories.take(config.consultoryCount).map { c ->
            val consultoryId =
                ConsultoriesTable.insert {
                    it[name] = c.name
                    it[shortCode] = c.shortCode
                    it[sortOrder] = consultories.indexOf(c) * 10
                    it[isActive] = true
                } get ConsultoriesTable.id

            c.copy(id = consultoryId)
        }
    }
}
