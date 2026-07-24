package com.denticode.kt.data.seeders

import com.denticode.kt.data.AppointmentSource
import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.AppointmentsTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object AppointmentsSeeder {

    data class SeedAppointment(
        val id: Int,
        val patientId: Int,
        val doctorId: Int,
        val procedureId: Int?,
        val scheduledAt: String,
        val status: AppointmentStatus,
        val durationMinutes: Int,
        val purpose: String?,
        val source: AppointmentSource,
    )

    private val ISO_DT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    private val appointmentPurposes =
        listOf(
            "Revisión general", "Limpieza dental", "Dolor de muela", "Control post-operatorio",
            "Colocación de corona", "Extracción dental", "Endodoncia", "Blanqueamiento",
            "Ajuste de ortodoncia", "Consulta de emergencia", "Sellante dental",
            "Aplicación de fluoruro", "Valoración para implante", "Prótesis dental",
            "Tratamiento periodontal", "Odontopediatría", "Consulta preventiva",
        )

    fun seed(
        config: DemoDataConfig,
        patients: List<PatientsSeeder.SeedPatient>,
        doctors: List<DoctorsSeeder.SeedDoctor>,
        procedures: List<ProceduresSeeder.SeedProcedure>,
    ): List<SeedAppointment> {
        if (AppointmentsTable.selectAll().count() > 0) return emptyList()

        val rng = Random(123)
        val today = LocalDate.now()
        val startDate = today.minusDays(config.appointmentPastDays.toLong())
        val endDate = today.plusDays(config.appointmentFutureDays.toLong())
        val workStart = LocalTime.of(8, 30)
        val workEnd = LocalTime.of(17, 30)
        val slotMinutes = 30

        val appointments = mutableListOf<SeedAppointment>()
        var date = startDate

        while (date <= endDate && appointments.size < config.appointmentCount) {
            if (date.dayOfWeek == DayOfWeek.SUNDAY) {
                date = date.plusDays(1)
                continue
            }

            val slotsPerDay = if (date.dayOfWeek == DayOfWeek.SATURDAY) 8 else 14
            val doctorsToday =
                if (date.dayOfWeek == DayOfWeek.SATURDAY) {
                    doctors.filter { rng.nextDouble() > 0.4 }
                } else {
                    doctors
                }

            for (slot in 0 until slotsPerDay) {
                if (appointments.size >= config.appointmentCount) break

                val hour = workStart.hour + (slot * slotMinutes) / 60
                val minute = workStart.minute + (slot * slotMinutes) % 60
                if (hour >= workEnd.hour) break

                val doctor = doctorsToday[rng.nextInt(doctorsToday.size)]
                val patient = patients[rng.nextInt(patients.size)]
                val procedure = procedures[rng.nextInt(procedures.size)]
                val appointmentTime = LocalDateTime.of(date, LocalTime.of(hour, minute))

                val status = resolveStatus(date, today, rng)
                val duration = procedure.durationMinutes + rng.nextInt(-10, 15)
                val source = if (rng.nextDouble() > 0.85) AppointmentSource.ONLINE else AppointmentSource.MANUAL

                val appointmentId =
                    AppointmentsTable.insert {
                        it[patientId] = patient.id
                        it[primaryDoctorId] = doctor.id
                        it[scheduledAt] = appointmentTime.format(ISO_DT)
                        it[estimatedDurationMinutes] = duration.coerceAtLeast(15)
                        it[purpose] = appointmentPurposes[rng.nextInt(appointmentPurposes.size)]
                        it[procedureTypeId] = procedure.id
                        it[AppointmentsTable.status] = status.name
                        it[createdAtEpochMs] = appointmentTime.minusDays(rng.nextLong(1, 8)).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                        it[updatedAtEpochMs] = appointmentTime.minusHours(rng.nextLong(0, 24)).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                        it[appointmentSource] = source.name
                        it[cancellationReason] =
                            if (status == AppointmentStatus.CANCELLED) {
                                listOf("Paciente canceló", "Motivo personal", "Cambio de horario")[rng.nextInt(3)]
                            } else {
                                null
                            }
                    } get AppointmentsTable.id

                appointments.add(
                    SeedAppointment(
                        id = appointmentId,
                        patientId = patient.id,
                        doctorId = doctor.id,
                        procedureId = procedure.id,
                        scheduledAt = appointmentTime.format(ISO_DT),
                        status = status,
                        durationMinutes = duration.coerceAtLeast(15),
                        purpose = appointmentPurposes[rng.nextInt(appointmentPurposes.size)],
                        source = source,
                    ),
                )
            }
            date = date.plusDays(1)
        }

        return appointments
    }

    private fun resolveStatus(date: LocalDate, today: LocalDate, rng: Random): AppointmentStatus {
        val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(date, today)
        return when {
            daysDiff < -7 -> if (rng.nextDouble() > 0.15) AppointmentStatus.SCHEDULED else AppointmentStatus.CONFIRMED
            daysDiff in -7..-1 -> if (rng.nextDouble() > 0.3) AppointmentStatus.CONFIRMED else AppointmentStatus.SCHEDULED
            daysDiff == 0L ->
                when (rng.nextInt(10)) {
                    in 0..2 -> AppointmentStatus.COMPLETED
                    in 3..5 -> AppointmentStatus.IN_PROGRESS
                    in 6..7 -> AppointmentStatus.CONFIRMED
                    else -> AppointmentStatus.SCHEDULED
                }
            daysDiff in 1..3 ->
                when (rng.nextInt(10)) {
                    in 0..3 -> AppointmentStatus.SCHEDULED
                    in 4..6 -> AppointmentStatus.CONFIRMED
                    in 7..8 -> AppointmentStatus.RESCHEDULED
                    else -> AppointmentStatus.CANCELLED
                }
            daysDiff in 4..14 ->
                when (rng.nextInt(10)) {
                    in 0..4 -> AppointmentStatus.COMPLETED
                    in 5..6 -> AppointmentStatus.NO_SHOW
                    in 7..8 -> AppointmentStatus.CANCELLED
                    else -> AppointmentStatus.CONFIRMED
                }
            else ->
                when (rng.nextInt(10)) {
                    in 0..6 -> AppointmentStatus.COMPLETED
                    in 7..8 -> AppointmentStatus.CANCELLED
                    else -> AppointmentStatus.NO_SHOW
                }
        }
    }
}
