package com.denticode.kt.data

/** Transiciones de estado del flujo de citas. */
class AppointmentWorkflowService(
    private val actions: AppointmentActionsService,
) {
    fun confirmAppointment(appointmentId: Int) =
        transition(
            appointmentId = appointmentId,
            target = AppointmentStatus.CONFIRMED,
            auditAction = "Appointment confirmed",
            allowedFrom = setOf(AppointmentStatus.SCHEDULED, AppointmentStatus.RESCHEDULED),
        )

    fun startAppointment(appointmentId: Int) =
        transition(
            appointmentId = appointmentId,
            target = AppointmentStatus.IN_PROGRESS,
            auditAction = "Appointment started",
            allowedFrom = setOf(AppointmentStatus.CONFIRMED),
        )

    fun completeAppointment(appointmentId: Int) =
        transition(
            appointmentId = appointmentId,
            target = AppointmentStatus.COMPLETED,
            auditAction = "Appointment completed",
            allowedFrom = setOf(AppointmentStatus.IN_PROGRESS, AppointmentStatus.CONFIRMED),
        )

    private fun transition(
        appointmentId: Int,
        target: AppointmentStatus,
        auditAction: String,
        allowedFrom: Set<AppointmentStatus>,
    ) {
        val row =
            actions.findAppointment(appointmentId)
                ?: throw IllegalArgumentException("No se encontró la cita seleccionada.")
        if (row.status !in allowedFrom) {
            throw IllegalArgumentException(
                "No se puede cambiar de «${row.status.displayLabel}» a «${target.displayLabel}».",
            )
        }
        actions.updateStatus(appointmentId, target, auditAction = auditAction)
    }
}
