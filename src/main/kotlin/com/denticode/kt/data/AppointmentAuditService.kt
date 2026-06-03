package com.denticode.kt.data

/** Registro de auditoría para acciones sobre citas. */
class AppointmentAuditService(
    private val repo: DentiRepository,
) {
    fun log(
        appointmentId: Int,
        action: String,
        detail: String? = null,
        actorLabel: String = "Recepción",
    ) {
        repo.logAppointmentAudit(appointmentId, action, detail, actorLabel)
    }
}
