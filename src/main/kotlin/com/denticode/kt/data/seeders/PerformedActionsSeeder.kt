package com.denticode.kt.data.seeders

import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.PerformedActionsTable
import com.denticode.kt.data.TreatmentStatus
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import kotlin.random.Random

object PerformedActionsSeeder {

    private val toothNumbers =
        listOf(
            "11", "12", "13", "14", "15", "16", "17", "18",
            "21", "22", "23", "24", "25", "26", "27", "28",
            "31", "32", "33", "34", "35", "36", "37", "38",
            "41", "42", "43", "44", "45", "46", "47", "48",
        )

    private val treatmentNotes =
        listOf(
            "Tratamiento completado sin complicaciones.",
            "Paciente cooperó durante el procedimiento.",
            "Se observa buena evolución post-operatoria.",
            "Control en 2 semanas.",
            "Se recomienda seguimiento en 1 mes.",
            "Dolor leve post-operatorio, indicado ibuprofeno.",
            "Paciente referirá dolor por 48 horas, normal.",
            "Material de restauración bien adaptado.",
            "Conductos limpiados y obturados.",
            "Extracción sin incidencias.",
        )

    fun seed(
        config: DemoDataConfig,
        appointments: List<AppointmentsSeeder.SeedAppointment>,
        patients: List<PatientsSeeder.SeedPatient>,
        doctors: List<DoctorsSeeder.SeedDoctor>,
        procedures: List<ProceduresSeeder.SeedProcedure>,
    ) {
        if (PerformedActionsTable.selectAll().count() > 0) return

        val rng = Random(99)
        val completedAppts = appointments.filter { it.status == AppointmentStatus.COMPLETED }

        for (appt in completedAppts) {
            val procedure = procedures.firstOrNull { it.id == appt.procedureId } ?: continue
            val status = TreatmentStatus.COMPLETED

            val priceVariation = 0.85 + rng.nextDouble() * 0.30
            val unitPrice = procedure.priceBs * priceVariation

            PerformedActionsTable.insert {
                it[patientId] = appt.patientId
                it[appointmentId] = appt.id
                it[procedureTypeId] = appt.procedureId!!
                it[performingDoctorId] = appt.doctorId
                it[actionAt] = appt.scheduledAt
                it[PerformedActionsTable.status] = status.name
                it[standardPrice] = procedure.priceBs
                it[PerformedActionsTable.unitPrice] = unitPrice
                it[totalPrice] = unitPrice
                it[toothInvolved] =
                    if (procedure.requiresTooth) {
                        toothNumbers[rng.nextInt(toothNumbers.size)]
                    } else {
                        null
                    }
                it[anesthesiaUsed] =
                    if (procedure.category in listOf("Cirugía", "Endodoncia", "Restaurativa")) {
                        if (rng.nextDouble() > 0.3) "Lidocaína 2%" else null
                    } else {
                        null
                    }
                it[descriptionNotes] = treatmentNotes[rng.nextInt(treatmentNotes.size)]
                it[quantity] = 1
            }
        }

        val inProgressAppts = appointments.filter { it.status == AppointmentStatus.IN_PROGRESS }
        for (appt in inProgressAppts) {
            val procedure = procedures.firstOrNull { it.id == appt.procedureId } ?: continue

            PerformedActionsTable.insert {
                it[patientId] = appt.patientId
                it[appointmentId] = appt.id
                it[procedureTypeId] = appt.procedureId!!
                it[performingDoctorId] = appt.doctorId
                it[actionAt] = appt.scheduledAt
                it[PerformedActionsTable.status] = TreatmentStatus.IN_PROGRESS.name
                it[standardPrice] = procedure.priceBs
                it[unitPrice] = procedure.priceBs
                it[totalPrice] = procedure.priceBs
                it[descriptionNotes] = "Tratamiento en curso"
                it[quantity] = 1
            }
        }
    }
}
