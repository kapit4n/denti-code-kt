package com.denticode.kt.data

import java.time.LocalDate

/** Carga de detalle enriquecido, notas estructuradas y conteos de calendario. */
class AppointmentDetailsService(
    private val repo: DentiRepository,
) {
    fun loadDetail(appointmentId: Int): AppointmentDetailSnapshot? =
        repo.loadAppointmentDetail(appointmentId)

    fun appointmentCountByDate(): Map<LocalDate, Int> =
        repo.appointmentCountByDate()

    fun addNote(
        appointmentId: Int,
        body: String,
        authorLabel: String = "Recepción",
    ) {
        repo.addAppointmentNote(appointmentId, body, authorLabel)
    }

    fun updateNote(
        noteId: Int,
        appointmentId: Int,
        body: String,
    ) {
        repo.updateAppointmentNote(noteId, appointmentId, body)
    }

    fun deleteNote(
        noteId: Int,
        appointmentId: Int,
    ) {
        repo.deleteAppointmentNote(noteId, appointmentId)
    }
}
