package com.denticode.kt.data.seeders

import com.denticode.kt.data.TreatmentPlanPhasesTable
import com.denticode.kt.data.TreatmentPlansTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import kotlin.random.Random

/** Seed para planes de tratamiento: planes por paciente con fases y costo estimado. */
object TreatmentPlansSeeder {

    private val planTitles =
        listOf(
            "Rehabilitación oral completa",
            "Tratamiento de ortodoncia",
            "Plan de implantes dentales",
            "Tratamiento periodontal",
            "Reconstrucción de piezas posteriores",
            "Blanqueamiento y estética",
            "Plan de prótesis parcial",
            "Rehabilitación del sector anterior",
        )

    private val phaseNames =
        listOf(
            "Diagnóstico y valoración",
            "Higiene y profilaxis",
            "Obturación de caries",
            "Endodoncia",
            "Colocación de corona",
            "Toma de impresión",
            "Colocación de prótesis",
            "Férula de descarga",
            "Control y ajuste final",
            "Seguimiento post-tratamiento",
        )

    fun seed(
        config: DemoDataConfig,
        patients: List<PatientsSeeder.SeedPatient>,
    ) {
        if (TreatmentPlansTable.selectAll().count() > 0) return

        val rng = Random(4242)
        val todayMs = System.currentTimeMillis()

        val patientIds = patients.map { it.id }

        patientIds.forEach { pid ->
            if (rng.nextDouble() > 0.5) return@forEach
            val planCount = rng.nextInt(1, 3)
            repeat(planCount) {
                val title = planTitles[rng.nextInt(planTitles.size)]
                val planStatus = listOf("DRAFT", "ACTIVE", "ACTIVE", "COMPLETED")[rng.nextInt(4)]
                val createdAt = todayMs - rng.nextLong(0, 300L * 86_400_000)
                val planId =
                    TreatmentPlansTable.insert {
                        it[patientId] = pid
                        it[TreatmentPlansTable.title] = title
                        it[description] =
                            if (rng.nextDouble() > 0.4) {
                                "Plan de tratamiento propuesto por el odontólogo, con fases y presupuesto estimado."
                            } else {
                                null
                            }
                        it[status] = planStatus
                        it[estimatedCost] = 0.0
                        it[createdAtEpochMs] = createdAt
                    } get TreatmentPlansTable.id

                var estimated = 0.0
                val phaseCount = rng.nextInt(2, 5)
                val baseCost = 120.0 + rng.nextInt(0, 4) * 60.0
                repeat(phaseCount) { idx ->
                    val cost = baseCost * rng.nextDouble(0.8, 1.6)
                    val phaseStatus =
                        when {
                            planStatus == "COMPLETED" -> "COMPLETED"
                            rng.nextDouble() > 0.6 -> "COMPLETED"
                            rng.nextDouble() > 0.5 -> "IN_PROGRESS"
                            else -> "PENDING"
                        }
                    estimated += cost
                    TreatmentPlanPhasesTable.insert {
                        it[TreatmentPlanPhasesTable.planId] = planId
                        it[name] = phaseNames[rng.nextInt(phaseNames.size)]
                        it[description] = null
                        it[estimatedCost] = cost
                        it[status] = phaseStatus
                        it[sortOrder] = idx
                        it[createdAtEpochMs] = createdAt + idx * 86_400_000L
                    }
                }
                TreatmentPlansTable.update({ TreatmentPlansTable.id eq planId }) {
                    it[TreatmentPlansTable.estimatedCost] = estimated
                }
            }
        }
    }
}
