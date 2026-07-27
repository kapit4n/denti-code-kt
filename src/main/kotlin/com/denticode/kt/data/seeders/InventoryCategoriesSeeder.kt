package com.denticode.kt.data.seeders

import com.denticode.kt.data.InventoryCategoriesTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

object InventoryCategoriesSeeder {

    data class SeedInventoryCategory(
        val id: Int,
        val name: String,
        val description: String?,
        val icon: String?,
        val color: String?,
        val sortOrder: Int,
    )

    fun seed(config: DemoDataConfig): List<SeedInventoryCategory> {
        val existing = InventoryCategoriesTable.selectAll().map {
            SeedInventoryCategory(
                id = it[InventoryCategoriesTable.id],
                name = it[InventoryCategoriesTable.name],
                description = it[InventoryCategoriesTable.description],
                icon = it[InventoryCategoriesTable.icon],
                color = it[InventoryCategoriesTable.color],
                sortOrder = it[InventoryCategoriesTable.sortOrder],
            )
        }
        if (existing.isNotEmpty()) return existing

        val categories = listOf(
            SeedInventoryCategory(0, "Material restaurativo", "Resinas, composite, amalgama", "build", "#FF9800", 0),
            SeedInventoryCategory(0, "Material preventivo", "Flúor, sellantes, profilaxis", "shield", "#4CAF50", 1),
            SeedInventoryCategory(0, "Anestesia", "Jeringas, agujas, anestésicos", "vaccines", "#F44336", 2),
            SeedInventoryCategory(0, "Material de impresión", "Alginato, silicona, cera", "print", "#9C27B0", 3),
            SeedInventoryCategory(0, "Instrumental", "Instrumentos estériles y reutilizables", "build", "#607D8B", 4),
            SeedInventoryCategory(0, "Material de sutura", "Hilos, agujas de sutura", "content_cut", "#E91E63", 5),
            SeedInventoryCategory(0, "EPP", "Guantes, mascarillas, gorros", "health_and_safety", "#2196F3", 6),
            SeedInventoryCategory(0, "Limpieza y desinfección", "Desinfectantes, esterilización", "cleaning_services", "#00BCD4", 7),
            SeedInventoryCategory(0, "Endodoncia", "Conos de gutapercha, limas, selladores", "local_hospital", "#FF5722", 8),
            SeedInventoryCategory(0, "Ortodoncia", "Brackets, alambres, ligaduras", "straighten", "#795548", 9),
            SeedInventoryCategory(0, "Prótesis", "Materiales para prótesis dental", "construction", "#3F51B5", 10),
            SeedInventoryCategory(0, "General", "Suministros varios", "inventory_2", "#9E9E9E", 11),
        )

        return categories.map { c ->
            val catId = InventoryCategoriesTable.insert {
                it[name] = c.name
                it[description] = c.description
                it[icon] = c.icon
                it[color] = c.color
                it[sortOrder] = c.sortOrder
                it[isActive] = true
            } get InventoryCategoriesTable.id
            c.copy(id = catId)
        }
    }
}
