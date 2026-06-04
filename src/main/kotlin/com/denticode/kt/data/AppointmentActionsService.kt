package com.denticode.kt.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/** Reprogramación: nueva fecha/hora; doctor y tratamiento se mantienen. */
data class AppointmentRescheduleRequest(
    val newDate: LocalDate,
    val newHour: Int,
    val newMinute: Int,
    val note: String? = null,
)

/**
 * Operaciones de negocio para acciones rápidas sobre citas (editar, reprogramar, cancelar, pagos).
 * La UI no accede a la base de datos directamente.
 */
class AppointmentActionsService(
    private val repo: DentiRepository,
) {
    fun findAppointment(appointmentId: Int): AppointmentRow? =
        repo.listAppointments(10_000).find { it.id == appointmentId }

    fun findTreatmentForAppointment(appointmentId: Int): PatientTreatmentRow? =
        repo.listAllTreatments(5_000).find { it.appointmentId == appointmentId }

    fun updateAppointment(appointmentId: Int, request: AppointmentEditRequest) {
        repo.updateAppointment(appointmentId, request)
    }

    fun updateStatus(
        appointmentId: Int,
        status: AppointmentStatus,
        auditAction: String,
        auditDetail: String? = null,
        cancellationReason: String? = null,
    ) {
        val row =
            findAppointment(appointmentId)
                ?: throw IllegalArgumentException("No se encontró la cita seleccionada.")
        val treatment = findTreatmentForAppointment(appointmentId)
        val ldt = parseScheduledAtLocal(row.scheduledAt)
        repo.updateAppointment(
            appointmentId,
            buildEditRequest(
                row = row,
                visitDate = ldt.toLocalDate(),
                visitHour = ldt.hour,
                visitMinute = ldt.minute,
                notes = row.notes,
                status = status,
                treatment = treatment?.let { syncTreatmentStatus(it, status) },
                cancellationReason = cancellationReason,
            ),
            auditAction = auditAction,
            auditDetail = auditDetail,
        )
    }

    fun rescheduleAppointment(appointmentId: Int, request: AppointmentRescheduleRequest) {
        val row =
            findAppointment(appointmentId)
                ?: throw IllegalArgumentException("No se encontró la cita seleccionada.")
        validateFutureDateTime(request.newDate, request.newHour, request.newMinute)
        val start = LocalDateTime.of(request.newDate, LocalTime.of(request.newHour, request.newMinute))
        val duration = row.estimatedDurationMinutes ?: 30
        if (
            repo.hasDoctorScheduleConflict(
                doctorId = row.primaryDoctorId,
                start = start,
                durationMinutes = duration,
                excludeAppointmentId = appointmentId,
            )
        ) {
            throw IllegalArgumentException("El doctor ya tiene otra cita en ese horario.")
        }
        val treatment = findTreatmentForAppointment(appointmentId)
        val mergedNotes = mergeRescheduleNote(row.notes, request.note)
        val newStatus =
            when (row.status) {
                AppointmentStatus.CANCELLED,
                AppointmentStatus.NO_SHOW,
                AppointmentStatus.COMPLETED,
                -> throw IllegalArgumentException("No se puede reprogramar una cita ${row.status.displayLabel.lowercase()}.")
                else -> AppointmentStatus.RESCHEDULED
            }
        repo.updateAppointment(
            appointmentId,
            buildEditRequest(
                row = row,
                visitDate = request.newDate,
                visitHour = request.newHour,
                visitMinute = request.newMinute,
                notes = mergedNotes,
                status = newStatus,
                treatment = treatment,
            ),
            auditAction = "Appointment rescheduled",
            auditDetail = start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        )
    }

    fun cancelAppointment(appointmentId: Int, reason: String? = null) {
        val row =
            findAppointment(appointmentId)
                ?: throw IllegalArgumentException("No se encontró la cita seleccionada.")
        if (row.status == AppointmentStatus.CANCELLED || row.status == AppointmentStatus.NO_SHOW) {
            throw IllegalArgumentException("La cita ya está cancelada.")
        }
        if (row.status == AppointmentStatus.COMPLETED) {
            throw IllegalArgumentException("No se puede cancelar una cita completada.")
        }
        val treatment = findTreatmentForAppointment(appointmentId)?.copy(status = TreatmentStatus.CANCELLED)
        val ldt = parseScheduledAtLocal(row.scheduledAt)
        repo.updateAppointment(
            appointmentId,
            buildEditRequest(
                row = row,
                visitDate = ldt.toLocalDate(),
                visitHour = ldt.hour,
                visitMinute = ldt.minute,
                notes = row.notes,
                status = AppointmentStatus.CANCELLED,
                treatment = treatment,
                cancellationReason = reason,
            ),
            auditAction = "Appointment cancelled",
            auditDetail = reason?.trim()?.takeIf { it.isNotEmpty() },
        )
    }

    fun registerPaymentForAppointment(
        appointmentId: Int,
        request: PatientPaymentRegisterRequest,
    ) {
        val row =
            findAppointment(appointmentId)
                ?: throw IllegalArgumentException("No se encontró la cita seleccionada.")
        repo.registerPaymentForPatient(
            row.patientId,
            request.copy(
                appointmentId = request.appointmentId ?: appointmentId,
            ),
        )
    }

    fun buildPaymentPrefill(appointmentId: Int): AppointmentPaymentPrefill {
        val row =
            findAppointment(appointmentId)
                ?: throw IllegalArgumentException("No se encontró la cita seleccionada.")
        val treatment = findTreatmentForAppointment(appointmentId)
        return AppointmentPaymentPrefill(
            patientId = row.patientId,
            appointmentId = appointmentId,
            procedureTypeId = row.procedureTypeId ?: treatment?.procedureTypeId,
            performedActionId = treatment?.id,
            suggestedAmount = treatment?.totalPrice,
            treatmentLabel = treatment?.procedureTypeName ?: row.procedureTypeName,
        )
    }

    private fun syncTreatmentStatus(
        treatment: PatientTreatmentRow,
        status: AppointmentStatus,
    ): PatientTreatmentRow =
        when (status) {
            AppointmentStatus.COMPLETED -> treatment.copy(status = TreatmentStatus.COMPLETED)
            AppointmentStatus.CANCELLED,
            AppointmentStatus.NO_SHOW,
            -> treatment.copy(status = TreatmentStatus.CANCELLED)
            AppointmentStatus.IN_PROGRESS -> treatment.copy(status = TreatmentStatus.IN_PROGRESS)
            else -> treatment
        }

    private fun buildEditRequest(
        row: AppointmentRow,
        visitDate: LocalDate,
        visitHour: Int,
        visitMinute: Int,
        notes: String?,
        status: AppointmentStatus,
        treatment: PatientTreatmentRow?,
        cancellationReason: String? = null,
    ): AppointmentEditRequest =
        AppointmentEditRequest(
            patientId = row.patientId,
            primaryDoctorId = row.primaryDoctorId,
            visitDate = visitDate,
            visitHour = visitHour,
            visitMinute = visitMinute,
            estimatedDurationMinutes = row.estimatedDurationMinutes,
            purpose = row.purpose,
            notes = notes,
            procedureTypeId = row.procedureTypeId,
            status = status,
            treatmentStatus = treatment?.status,
            treatmentUnitPrice = treatment?.unitPrice,
            cancellationReason = cancellationReason,
        )

    fun registerTreatmentForAppointment(
        appointmentId: Int,
        request: PatientTreatmentRegisterRequest,
    ) {
        val row =
            findAppointment(appointmentId)
                ?: throw IllegalArgumentException("No se encontró la cita seleccionada.")
        if (row.status == AppointmentStatus.CANCELLED || row.status == AppointmentStatus.NO_SHOW) {
            throw IllegalArgumentException("No se puede registrar tratamiento en una cita cancelada.")
        }
        repo.registerTreatmentForPatient(
            request.copy(
                patientId = row.patientId,
                primaryDoctorId = request.primaryDoctorId.takeIf { it > 0 } ?: row.primaryDoctorId,
                appointmentId = appointmentId,
                createAppointmentIfMissing = false,
            ),
        )
    }

    fun buildTreatmentPrefill(appointmentId: Int): TreatmentRegisterPrefill {
        val row =
            findAppointment(appointmentId)
                ?: throw IllegalArgumentException("No se encontró la cita seleccionada.")
        val treatment = findTreatmentForAppointment(appointmentId)
        val ldt = parseScheduledAtLocal(row.scheduledAt)
        return TreatmentRegisterPrefill(
            patientId = row.patientId,
            appointmentId = appointmentId,
            primaryDoctorId = row.primaryDoctorId,
            procedureTypeId = row.procedureTypeId ?: treatment?.procedureTypeId,
            unitPrice = treatment?.unitPrice ?: treatment?.standardPrice,
            treatmentStatus = treatment?.status ?: TreatmentStatus.PLANNED,
            actionDate = ldt.toLocalDate(),
            actionHour = ldt.hour,
            actionMinute = ldt.minute,
            descriptionNotes = treatment?.descriptionNotes ?: row.notes,
            existingTreatmentId = treatment?.id,
        )
    }

    private fun validateFutureDateTime(date: LocalDate, hour: Int, minute: Int) {
        val dt = LocalDateTime.of(date, LocalTime.of(hour, minute, 0))
        if (dt.isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw IllegalArgumentException("La nueva fecha y hora deben ser futuras.")
        }
    }

    private fun mergeRescheduleNote(existing: String?, rescheduleNote: String?): String? {
        val extra = rescheduleNote?.trim()?.takeIf { it.isNotEmpty() } ?: return existing?.trim()?.takeIf { it.isNotEmpty() }
        val base = existing?.trim()?.takeIf { it.isNotEmpty() }
        return if (base == null) {
            "Reprogramada: $extra"
        } else {
            "$base\nReprogramada: $extra"
        }
    }

    private fun parseScheduledAtLocal(value: String): LocalDateTime {
        val t = value.trim()
        return try {
            LocalDateTime.parse(t, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (_: DateTimeParseException) {
            try {
                LocalDate.parse(t, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay()
            } catch (_: DateTimeParseException) {
                LocalDateTime.now()
            }
        }
    }
}

data class AppointmentPaymentPrefill(
    val patientId: Int,
    val appointmentId: Int,
    val procedureTypeId: Int?,
    val performedActionId: Int?,
    val suggestedAmount: Double?,
    val treatmentLabel: String?,
)

data class TreatmentRegisterPrefill(
    val patientId: Int,
    val appointmentId: Int,
    val primaryDoctorId: Int,
    val procedureTypeId: Int?,
    val unitPrice: Double?,
    val treatmentStatus: TreatmentStatus,
    val actionDate: LocalDate,
    val actionHour: Int,
    val actionMinute: Int,
    val descriptionNotes: String?,
    val existingTreatmentId: Int?,
)
