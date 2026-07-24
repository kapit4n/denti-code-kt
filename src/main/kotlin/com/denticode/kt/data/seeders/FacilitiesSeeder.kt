package com.denticode.kt.data.seeders

import com.denticode.kt.data.TreatmentFacilitiesTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

object FacilitiesSeeder {

    data class SeedFacility(
        val id: Int,
        val code: String,
        val category: String,
        val displayName: String,
    )

    fun seed(config: DemoDataConfig): List<SeedFacility> {
        if (TreatmentFacilitiesTable.selectAll().count() > 0) return emptyList()

        val facilities =
            listOf(
                SeedFacility(0, "GLOVES-S", "EPP", "Guantes de examen (S)"),
                SeedFacility(0, "GLOVES-M", "EPP", "Guantes de examen (M)"),
                SeedFacility(0, "GLOVES-L", "EPP", "Guantes de examen (L)"),
                SeedFacility(0, "MASK-S", "EPP", "Mascarilla quirúrgica"),
                SeedFacility(0, "MASK-N95", "EPP", "Mascarilla N95"),
                SeedFacility(0, "CAP", "EPP", "Gorrito desechable"),
                SeedFacility(0, "GOWN", "EPP", "Bata desechable"),
                SeedFacility(0, "ANEST-LIDO", "INYECCIÓN", "Lidocaína 2% con epinefrina"),
                SeedFacility(0, "ANEST-ARTI", "INYECCIÓN", "Articaina 4%"),
                SeedFacility(0, "NEEDLE-27", "INYECCIÓN", "Aguja 27G"),
                SeedFacility(0, "NEEDLE-30", "INYECCIÓN", "Aguja 30G"),
                SeedFacility(0, "CARPULE", "INYECCIÓN", "Cárpula de anestesia"),
                SeedFacility(0, "COMPOSITE-A1", "RESTAURACIÓN", "Composite resina A1"),
                SeedFacility(0, "COMPOSITE-A2", "RESTAURACIÓN", "Composite resina A2"),
                SeedFacility(0, "COMPOSITE-B1", "RESTAURACIÓN", "Composite resina B1"),
                SeedFacility(0, "COMPOSITE-B2", "RESTAURACIÓN", "Composite resina B2"),
                SeedFacility(0, "AMALGAM", "RESTAURACIÓN", "Amalgama dental"),
                SeedFacility(0, "ETCH-GEL", "RESTAURACIÓN", "Ácido grabador"),
                SeedFacility(0, "BOND", "RESTAURACIÓN", "Adhesivo dental"),
                SeedFacility(0, "CEMENT-EUG", "PRÓTESIS", "Cemento de eugenol"),
                SeedFacility(0, "CEMENT-ZOE", "PRÓTESIS", "Cemento zinc oxidoeugenol"),
                SeedFacility(0, "CIMENT-RES", "PRÓTESIS", "Cemento resinoso"),
                SeedFacility(0, "IMPRESS-ALG", "PRÓTESIS", "Alginato de impresión"),
                SeedFacility(0, "IMPRESS-SIL", "PRÓTESIS", "Silicona de impresión"),
                SeedFacility(0, "FLUOR-VARN", "PREVENCIÓN", "Barniz de fluoruro"),
                SeedFacility(0, "SEALANT", "PREVENCIÓN", "Sellador de fisuras"),
                SeedFacility(0, "SCALER-TIP", "PERIODONCIA", "Punta de ultrasonido"),
                SeedFacility(0, "SRP-FILE", "PERIODONCIA", "Instrumento de raspado"),
                SeedFacility(0, "SUTURE-0", "CIRUGÍA", "Hilo de sutura 3-0"),
                SeedFacility(0, "SUTURE-40", "CIRUGÍA", "Hilo de sutura 4-0"),
                SeedFacility(0, "SCALPEL-15", "CIRUGÍA", "Hoja de bisturí #15"),
                SeedFacility(0, "GAUZE", "GENERAL", "Gasas estériles"),
                SeedFacility(0, "SALINE", "GENERAL", "Suero fisiológico"),
                SeedFacility(0, "COTTON-ROLL", "GENERAL", "Rollos de algodón"),
                SeedFacility(0, "MOUTH-RINSE", "GENERAL", "Enjuague bucal preoperatorio"),
            )

        return facilities.take(config.facilityCount).map { f ->
            val facilityId =
                TreatmentFacilitiesTable.insert {
                    it[facilityCode] = f.code
                    it[categoryKey] = f.category
                    it[displayName] = f.displayName
                    it[sortOrder] = facilities.indexOf(f) * 10
                    it[isActive] = true
                } get TreatmentFacilitiesTable.id

            f.copy(id = facilityId)
        }
    }
}
