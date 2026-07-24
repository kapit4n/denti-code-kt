package com.denticode.kt.data.seeders

import com.denticode.kt.data.ProcedureTypesTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

object ProceduresSeeder {

    data class SeedProcedure(
        val id: Int,
        val name: String,
        val description: String,
        val durationMinutes: Int,
        val priceBs: Double,
        val category: String,
        val requiresTooth: Boolean,
    )

    fun seed(config: DemoDataConfig): List<SeedProcedure> {
        if (ProcedureTypesTable.selectAll().count() > 0) return emptyList()

        val procedures =
            listOf(
                SeedProcedure(0, "Limpieza dental", "Profilaxis y pulido profesional", 30, 150.0, "Preventiva", false),
                SeedProcedure(0, "Revisión general", "Evaluación oral completa", 20, 100.0, "Diagnóstico", false),
                SeedProcedure(0, "Radiografía panorámica", "Radiografía de las arcadas dentarias", 15, 80.0, "Diagnóstico", false),
                SeedProcedure(0, "Radiografía periapical", "Radiografía de una zona específica", 10, 50.0, "Diagnóstico", true),
                SeedProcedure(0, "Obturación composite", "Resina estética dental", 45, 250.0, "Restaurativa", true),
                SeedProcedure(0, "Obturación amalgama", "Resmetalla dental posterior", 40, 180.0, "Restaurativa", true),
                SeedProcedure(0, "Endodoncia molar", "Tratamiento de conducto molar", 90, 1200.0, "Endodoncia", true),
                SeedProcedure(0, "Endodoncia premolar", "Tratamiento de conducto premolar", 70, 900.0, "Endodoncia", true),
                SeedProcedure(0, "Extracción simple", "Extracción dental sin cirugía", 30, 350.0, "Cirugía", true),
                SeedProcedure(0, "Extracción quirúrgica", "Extracción con incisión", 60, 700.0, "Cirugía", true),
                SeedProcedure(0, "Corona de porcelana", "Corona unitaria cerámica", 120, 1800.0, "Prótesis", true),
                SeedProcedure(0, "Corona metálica", "Corona unitaria en metal", 90, 1200.0, "Prótesis", true),
                SeedProcedure(0, "Puente dental", "Puente fijo de 3 unidades", 180, 3500.0, "Prótesis", true),
                SeedProcedure(0, "Prótesis removible superior", "Placa removible superior", 60, 2200.0, "Prótesis", false),
                SeedProcedure(0, "Prótesis removible inferior", "Placa removible inferior", 60, 2000.0, "Prótesis", false),
                SeedProcedure(0, "Blanqueamiento dental", "Blanqueamiento en consultorio", 60, 800.0, "Estética", false),
                SeedProcedure(0, "Carillas de porcelana", "Laminados estéticos", 90, 2500.0, "Estética", true),
                SeedProcedure(0, "Ajuste de ortodoncia", "Activación de brackets", 30, 200.0, "Ortodoncia", false),
                SeedProcedure(0, "Colocación de brackets", "Instalación de aparatología fija", 120, 3500.0, "Ortodoncia", false),
                SeedProcedure(0, "Retención ortodóncica", "Control de retainer", 20, 100.0, "Ortodoncia", false),
                SeedProcedure(0, "Sellante dental", "Sellador de fisuras", 20, 80.0, "Preventiva", true),
                SeedProcedure(0, "Aplicación de fluoruro", "Barniz fluorado profesional", 15, 60.0, "Preventiva", false),
                SeedProcedure(0, "Raspado y alisado", "Scaling y root planing por cuadrante", 60, 400.0, "Periodoncia", false),
                SeedProcedure(0, "Cirugía periodontal", "Cirugía de tejidos blandos", 90, 1500.0, "Periodoncia", false),
                SeedProcedure(0, "Implante dental", "Colocación de implante osteointegrado", 120, 4500.0, "Cirugía", true),
                SeedProcedure(0, "Consulta de emergencia", "Atención de dolor agudo", 30, 200.0, "Emergencia", false),
                SeedProcedure(0, "Control post-operatorio", "Seguimiento post cirugía", 20, 80.0, "Seguimiento", false),
                SeedProcedure(0, "Protección dental deportiva", "Moldeado de protector bucal", 30, 150.0, "Preventiva", false),
            )

        return procedures.take(config.procedureTypeCount).map { p ->
            val procId =
                ProcedureTypesTable.insert {
                    it[name] = p.name
                    it[description] = p.description
                    it[defaultDurationMinutes] = p.durationMinutes
                    it[standardPrice] = p.priceBs
                    it[requiresToothSpecification] = p.requiresTooth
                    it[category] = p.category
                    it[isActive] = true
                } get ProcedureTypesTable.id

            p.copy(id = procId)
        }
    }
}
