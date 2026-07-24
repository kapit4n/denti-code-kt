package com.denticode.kt.data.seeders

import com.denticode.kt.data.ConsultoriesTable
import com.denticode.kt.data.InventoryMovementsTable
import com.denticode.kt.data.MaterialInventoryLinesTable
import com.denticode.kt.data.TreatmentFacilitiesTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import kotlin.random.Random

object MaterialInventorySeeder {

    fun seed(
        config: DemoDataConfig,
        consultories: List<ConsultoriesSeeder.SeedConsultory>,
        facilities: List<FacilitiesSeeder.SeedFacility>,
    ) {
        if (MaterialInventoryLinesTable.selectAll().count() > 0) return

        val rng = Random(77)
        val now = System.currentTimeMillis()

        for (consultory in consultories) {
            for (facility in facilities) {
                val baseQty =
                    when (facility.category) {
                        "EPP" -> rng.nextInt(40, 120)
                        "INYECCIÓN" -> rng.nextInt(15, 60)
                        "RESTAURACIÓN" -> rng.nextInt(20, 80)
                        "PRÓTESIS" -> rng.nextInt(5, 25)
                        "PREVENCIÓN" -> rng.nextInt(25, 70)
                        "PERIODONCIA" -> rng.nextInt(10, 40)
                        "CIRUGÍA" -> rng.nextInt(10, 35)
                        "GENERAL" -> rng.nextInt(30, 100)
                        else -> rng.nextInt(15, 50)
                    }

                val lineId =
                    MaterialInventoryLinesTable.insert {
                        it[consultoryId] = consultory.id
                        it[facilityId] = facility.id
                        it[quantity] = baseQty
                    } get MaterialInventoryLinesTable.id

                InventoryMovementsTable.insert {
                    it[consultoryId] = consultory.id
                    it[facilityId] = facility.id
                    it[quantityChange] = baseQty
                    it[type] = "RESTOCK"
                    it[note] = "Reposición inicial de inventario"
                    it[createdAtEpochMs] = now - rng.nextLong(0, 14L * 86_400_000)
                }

                val adjustments = rng.nextInt(0, 3)
                for (adj in 0 until adjustments) {
                    val change = -rng.nextInt(1, 8)
                    InventoryMovementsTable.insert {
                        it[consultoryId] = consultory.id
                        it[facilityId] = facility.id
                        it[quantityChange] = change
                        it[type] = "USAGE"
                        it[note] = "Uso en tratamiento"
                        it[createdAtEpochMs] = now - rng.nextLong(0, 7L * 86_400_000)
                    }
                }
            }
        }
    }
}
