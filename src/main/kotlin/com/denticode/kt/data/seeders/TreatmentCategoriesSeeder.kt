package com.denticode.kt.data.seeders

import com.denticode.kt.data.TreatmentCategoriesTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

object TreatmentCategoriesSeeder {

    data class SeedCategory(
        val id: Int,
        val name: String,
        val icon: String?,
        val color: String?,
        val sortOrder: Int,
    )

    fun seed(config: DemoDataConfig): List<SeedCategory> {
        val existing =
            TreatmentCategoriesTable
                .selectAll()
                .map {
                    SeedCategory(
                        id = it[TreatmentCategoriesTable.id],
                        name = it[TreatmentCategoriesTable.name],
                        icon = it[TreatmentCategoriesTable.icon],
                        color = it[TreatmentCategoriesTable.color],
                        sortOrder = it[TreatmentCategoriesTable.sortOrder],
                    )
                }
        if (existing.isNotEmpty()) return existing

        val categories =
            listOf(
                SeedCategory(0, "Preventiva", "shield", "#4CAF50", 0),
                SeedCategory(0, "Diagnóstico", "search", "#2196F3", 1),
                SeedCategory(0, "Restaurativa", "build", "#FF9800", 2),
                SeedCategory(0, "Endodoncia", "local_hospital", "#F44336", 3),
                SeedCategory(0, "Cirugía", "content_cut", "#9C27B0", 4),
                SeedCategory(0, "Prótesis", "construction", "#795548", 5),
                SeedCategory(0, "Estética", "palette", "#E91E63", 6),
                SeedCategory(0, "Ortodoncia", "straighten", "#00BCD4", 7),
                SeedCategory(0, "Periodoncia", "spa", "#8BC34A", 8),
                SeedCategory(0, "Emergencia", "emergency", "#FF5722", 9),
                SeedCategory(0, "Seguimiento", "event_repeat", "#607D8B", 10),
            )

        return categories.map { c ->
            val catId =
                TreatmentCategoriesTable.insert {
                    it[name] = c.name
                    it[icon] = c.icon
                    it[color] = c.color
                    it[sortOrder] = c.sortOrder
                    it[isActive] = true
                } get TreatmentCategoriesTable.id

            c.copy(id = catId)
        }
    }
}
