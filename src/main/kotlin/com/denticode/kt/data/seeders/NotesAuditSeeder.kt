package com.denticode.kt.data.seeders

import com.denticode.kt.data.AppointmentAuditLogTable
import com.denticode.kt.data.AppointmentNotesTable
import com.denticode.kt.data.AppointmentStatus
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object NotesAuditSeeder {

    private val ISO_DT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    private val noteBodies =
        listOf(
            "Paciente refiere dolor desde hace 3 días en zona inferior izquierda.",
            "Se realiza radiografía periapical. Se observa caries profunda en pieza 36.",
            "Paciente alérgico a la penicilina. Se administra amoxicilina con precaución.",
            "Control post-operatorio: cicatrización correcta, sin signos de infección.",
            "Paciente solicita información sobre opciones de blanqueamiento.",
            "Se programa segunda fase de tratamiento periodontal.",
            "Paciente no asistió a la cita programada. Se reprograma para la próxima semana.",
            "Se colocan brackets en arcada superior. Instrucciones de higiene entregadas.",
            "Endodoncia completada en pieza 16. Se recomienda corona de porcelana.",
            "Extracción sin complicaciones. Indicaciones post-operatorias entregadas.",
            "Paciente presenta bruxismo. Se sugiere uso de férula de descarga.",
            "Limpieza y profilaxis completadas. Próximo control en 6 meses.",
            "Paciente confirma asistencia para la próxima cita.",
            "Se realiza aplicación de fluoruro en toda la arcada.",
            "Paciente manifiesta satisfacción con el resultado del blanqueamiento.",
        )

    private val auditActions =
        listOf(
            "Appointment created" to "Cita programada",
            "Appointment confirmed" to "Paciente confirmó asistencia",
            "Appointment completed" to "Cita completada",
            "Payment registered" to "Pago registrado",
            "Appointment cancelled" to "Cita cancelada por el paciente",
            "Appointment rescheduled" to "Cita reprogramada",
            "Treatment registered" to "Tratamiento registrado",
            "Notes added" to "Notas clínicas agregadas",
        )

    fun seed(
        config: DemoDataConfig,
        appointments: List<AppointmentsSeeder.SeedAppointment>,
    ) {
        if (AppointmentNotesTable.selectAll().count() > 0) return

        val rng = Random(33)

        for (appt in appointments) {
            if (rng.nextDouble() > 0.6) continue

            val apptTime = LocalDateTime.parse(appt.scheduledAt)
            val noteCount = rng.nextInt(1, 4)

            for (n in 0 until noteCount) {
                val noteTime = apptTime.minusDays(rng.nextLong(0, 3)).plusHours(rng.nextLong(0, 24))

                AppointmentNotesTable.insert {
                    it[appointmentId] = appt.id
                    it[body] = noteBodies[rng.nextInt(noteBodies.size)]
                    it[authorLabel] = if (rng.nextDouble() > 0.5) "Recepción" else "Dr. ${rng.nextInt(1, 6)}"
                    it[createdAtEpochMs] = noteTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                }
            }

            val auditCount = rng.nextInt(1, 5)
            for (a in 0 until auditCount) {
                val (action, _) = auditActions[rng.nextInt(auditActions.size)]
                val auditTime = apptTime.minusDays(rng.nextLong(0, 5)).plusHours(rng.nextLong(0, 48))

                AppointmentAuditLogTable.insert {
                    it[appointmentId] = appt.id
                    it[AppointmentAuditLogTable.action] = action
                    it[actorLabel] = if (rng.nextDouble() > 0.4) "Recepción" else "Sistema"
                    it[detail] = "Cita #${appt.id}: $action"
                    it[createdAtEpochMs] = auditTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                }
            }
        }
    }
}
