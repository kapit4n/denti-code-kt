package com.denticode.kt.ui.appointments

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denticode.kt.data.AppointmentDetailSnapshot
import com.denticode.kt.data.AppointmentNoteRow
import com.denticode.kt.data.AppointmentPaymentStatus
import com.denticode.kt.ui.components.buttons.AppButton
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.inputs.AppTextArea
import com.denticode.kt.ui.formatEpochMs
import com.denticode.kt.ui.formatMoney
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

@Composable
fun AppointmentDetailPanel(
    appointment: AppointmentUiModel?,
    detail: AppointmentDetailSnapshot?,
    reminderPreview: String,
    onReminderPreviewChange: (String) -> Unit,
    onCopyReminder: () -> Unit,
    onOpenWhatsApp: () -> Unit,
    onClose: () -> Unit,
    onEditAppointment: () -> Unit,
    onReschedule: () -> Unit,
    onCancelAppointment: () -> Unit,
    onConfirmAppointment: () -> Unit,
    onStartAppointment: () -> Unit,
    onCompleteAppointment: () -> Unit,
    onViewPatient: () -> Unit,
    onClinicalHistory: () -> Unit,
    onRegisterPayment: () -> Unit,
    onRegisterTreatment: () -> Unit,
    onViewPayments: () -> Unit,
    onAddNote: (String) -> Unit,
    onEditNote: (Int, String) -> Unit,
    onDeleteNote: (Int) -> Unit,
    modifier: Modifier = Modifier,
    linkedToSelection: Boolean = appointment != null,
    busyAction: AppointmentQuickActionKind? = null,
    detailLoading: Boolean = false,
) {
    val linked = linkedToSelection && appointment != null
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = AppShapes.medium,
        color =
            if (linked) {
                AppointmentPremiumPalette.primary.copy(alpha = 0.03f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        shadowElevation = if (linked) 2.dp else 0.dp,
        tonalElevation = 0.dp,
        border =
            if (linked) {
                BorderStroke(1.dp, AppointmentPremiumPalette.primary.copy(alpha = 0.35f))
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            },
    ) {
        AnimatedContent(
            targetState = appointment?.id,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(180))
            },
            label = "appointmentDetail",
        ) { targetId ->
            val current = if (targetId == appointment?.id) appointment else null
            if (current == null) {
                Column(
                    Modifier.padding(AppSpacing.xl).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        "Seleccione una cita para ver sus detalles.",
                        style = AppTypography.Body,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                AppointmentDetailBody(
                    appointment = current,
                    detail = if (detail?.appointment?.id == current.id) detail else null,
                    reminderPreview = reminderPreview,
                    onReminderPreviewChange = onReminderPreviewChange,
                    onCopyReminder = onCopyReminder,
                    onOpenWhatsApp = onOpenWhatsApp,
                    onClose = onClose,
                    onEditAppointment = onEditAppointment,
                    onReschedule = onReschedule,
                    onCancelAppointment = onCancelAppointment,
                    onConfirmAppointment = onConfirmAppointment,
                    onStartAppointment = onStartAppointment,
                    onCompleteAppointment = onCompleteAppointment,
                    onViewPatient = onViewPatient,
                    onClinicalHistory = onClinicalHistory,
                    onRegisterPayment = onRegisterPayment,
                    onRegisterTreatment = onRegisterTreatment,
                    onViewPayments = onViewPayments,
                    onAddNote = onAddNote,
                    onEditNote = onEditNote,
                    onDeleteNote = onDeleteNote,
                    busyAction = busyAction,
                    detailLoading = detailLoading,
                )
            }
        }
    }
}

@Composable
private fun AppointmentDetailBody(
    appointment: AppointmentUiModel,
    detail: AppointmentDetailSnapshot?,
    reminderPreview: String,
    onReminderPreviewChange: (String) -> Unit,
    onCopyReminder: () -> Unit,
    onOpenWhatsApp: () -> Unit,
    onClose: () -> Unit,
    onEditAppointment: () -> Unit,
    onReschedule: () -> Unit,
    onCancelAppointment: () -> Unit,
    onConfirmAppointment: () -> Unit,
    onStartAppointment: () -> Unit,
    onCompleteAppointment: () -> Unit,
    onViewPatient: () -> Unit,
    onClinicalHistory: () -> Unit,
    onRegisterPayment: () -> Unit,
    onRegisterTreatment: () -> Unit,
    onViewPayments: () -> Unit,
    onAddNote: (String) -> Unit,
    onEditNote: (Int, String) -> Unit,
    onDeleteNote: (Int) -> Unit,
    busyAction: AppointmentQuickActionKind? = null,
    detailLoading: Boolean = false,
) {
    val appt = detail?.appointment
    Column(
        Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.lg),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = AppointmentPremiumPalette.primary,
                    )
                    Text(
                        "Cita seleccionada",
                        style = AppTypography.Caption,
                        fontWeight = FontWeight.SemiBold,
                        color = AppointmentPremiumPalette.primary,
                    )
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PatientAvatar(appointment.patientName, size = 44.dp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            appointment.patientName,
                            style = AppTypography.SectionTitle,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            appointment.treatmentName,
                            style = AppTypography.BodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        AppointmentStatusBadge(
                            appointment.displayStatusLabel,
                            appointment.statusAccentColor,
                        )
                    }
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.padding(end = 4.dp, top = 4.dp).size(36.dp),
            ) {
                Icon(Icons.Default.Close, "Cerrar panel", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(AppSpacing.sm))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(AppSpacing.md))

        Text(
            "Información del paciente",
            style = AppTypography.CardTitle,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(AppSpacing.xs))

        DetailRow(Icons.Outlined.Person, "Paciente", appointment.patientName)
        DetailRow(Icons.Outlined.Phone, "Teléfono", nullDisplay(detail?.patientPhone ?: appointment.patientPhone))
        DetailRow(Icons.Outlined.Person, "Doctor", appointment.doctorName)
        DetailRow(Icons.Outlined.MedicalServices, "Tratamiento", appointment.treatmentName)
        DetailRow(Icons.AutoMirrored.Outlined.EventNote, "Motivo", nullDisplay(appointment.purpose))
        DetailRow(Icons.Outlined.Schedule, "Hora", "${appointment.timeLabel} · ${appointment.durationMinutes} min")
        DetailRow(Icons.Outlined.CalendarMonth, "Estado", appointment.displayStatusLabel)

        var technicalDetailsExpanded by remember { mutableStateOf(false) }

        Spacer(Modifier.height(AppSpacing.sm))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(AppSpacing.sm))

        Surface(
            modifier = Modifier.fillMaxWidth().clickable { technicalDetailsExpanded = !technicalDetailsExpanded },
            shape = AppShapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Detalles técnicos",
                    style = AppTypography.CardTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                    if (technicalDetailsExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (technicalDetailsExpanded) {
            Spacer(Modifier.height(AppSpacing.xs))
            DetailRow(Icons.Outlined.CalendarMonth, "ID cita", "#${appointment.id}")
            DetailRow(Icons.Outlined.CalendarMonth, "Fecha", appointment.dateLabel)
            DetailRow(Icons.AutoMirrored.Outlined.EventNote, "Notas internas", nullDisplay(appointment.notes))
            DetailRow(
                Icons.Outlined.Schedule,
                "Creada",
                appt?.createdAtEpochMs?.let { formatEpochMs(it) } ?: appointment.createdDisplay,
            )
            DetailRow(
                Icons.Outlined.Schedule,
                "Última actualización",
                appt?.updatedAtEpochMs?.let { formatEpochMs(it) } ?: "No registrado",
            )
            DetailRow(Icons.Outlined.CalendarMonth, "Origen", appt?.source?.labelEs ?: "No registrado")
            DetailRow(
                Icons.Outlined.CalendarMonth,
                "Cita de seguimiento",
                appt?.followUpAppointmentId?.let { "#$it" } ?: "No registrado",
            )
            if (appt?.cancellationReason != null) {
                DetailRow(Icons.AutoMirrored.Outlined.EventNote, "Motivo cancelación", appt.cancellationReason)
            }
        }

        Spacer(Modifier.height(AppSpacing.md))
        PaymentStatusSection(
            detail = detail,
            detailLoading = detailLoading,
            onRegisterPayment = onRegisterPayment,
            onViewPayments = onViewPayments,
            canRegisterPayment = appointment.status.isQuickActionEnabled(AppointmentQuickActionKind.REGISTER_PAYMENT),
            busyPayment = busyAction == AppointmentQuickActionKind.REGISTER_PAYMENT,
        )

        Spacer(Modifier.height(AppSpacing.md))
        StructuredNotesSection(
            notes = detail?.structuredNotes.orEmpty(),
            detailLoading = detailLoading,
            onAddNote = onAddNote,
            onEditNote = onEditNote,
            onDeleteNote = onDeleteNote,
        )

        if (!detail?.auditLog.isNullOrEmpty()) {
            Spacer(Modifier.height(AppSpacing.md))
            AuditLogSection(detail!!.auditLog.take(8))
        }

        Spacer(Modifier.height(AppSpacing.md))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(AppSpacing.md))
        Text(
            "Acciones rápidas",
            style = AppTypography.CardTitle,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(AppSpacing.sm))

        val workflowRows =
            buildList {
                if (appointment.status.isQuickActionEnabled(AppointmentQuickActionKind.CONFIRM)) {
                    add(Triple(AppointmentQuickActionKind.CONFIRM, "Confirmar", onConfirmAppointment))
                }
                if (appointment.status.isQuickActionEnabled(AppointmentQuickActionKind.START)) {
                    add(Triple(AppointmentQuickActionKind.START, "Iniciar cita", onStartAppointment))
                }
                if (appointment.status.isQuickActionEnabled(AppointmentQuickActionKind.COMPLETE)) {
                    add(Triple(AppointmentQuickActionKind.COMPLETE, "Completar", onCompleteAppointment))
                }
            }
        workflowRows.chunked(2).forEach { row ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                row.forEach { (kind, label, onClick) ->
                    QuickActionButton(
                        text = label,
                        icon = Icons.Outlined.Schedule,
                        onClick = onClick,
                        modifier = Modifier.weight(1f),
                        enabled = true,
                        loading = busyAction == kind,
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        val actionRows =
            listOf(
                listOf(
                    Triple(AppointmentQuickActionKind.EDIT, "Editar cita", Icons.Outlined.Edit to onEditAppointment),
                    Triple(AppointmentQuickActionKind.RESCHEDULE, "Reprogramar", Icons.Outlined.Schedule to onReschedule),
                ),
                listOf(
                    Triple(AppointmentQuickActionKind.CANCEL, "Cancelar cita", Icons.AutoMirrored.Outlined.EventNote to onCancelAppointment),
                    Triple(AppointmentQuickActionKind.VIEW_PATIENT, "Ver paciente", Icons.Outlined.Person to onViewPatient),
                ),
                listOf(
                    Triple(AppointmentQuickActionKind.CLINICAL_HISTORY, "Historial clínico", Icons.Outlined.MedicalServices to onClinicalHistory),
                    Triple(AppointmentQuickActionKind.REGISTER_TREATMENT, "Registrar tratamiento", Icons.Outlined.MedicalServices to onRegisterTreatment),
                ),
                listOf(
                    Triple(AppointmentQuickActionKind.REGISTER_PAYMENT, "Registrar pago", Icons.Outlined.Payments to onRegisterPayment),
                ),
            )
        actionRows.forEach { pair ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                pair.forEach { (kind, label, iconAction) ->
                    val (icon, onClick) = iconAction
                    QuickActionButton(
                        text = label,
                        icon = icon,
                        onClick = onClick,
                        modifier = Modifier.weight(1f),
                        enabled = appointment.status.isQuickActionEnabled(kind),
                        loading = busyAction == kind,
                    )
                }
            }
        }
        Spacer(Modifier.height(AppSpacing.lg))
        ReminderCard(
            previewText = reminderPreview,
            onPreviewChange = onReminderPreviewChange,
            onCopy = onCopyReminder,
            onOpenWhatsApp = onOpenWhatsApp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PaymentStatusSection(
    detail: AppointmentDetailSnapshot?,
    detailLoading: Boolean,
    onRegisterPayment: () -> Unit,
    onViewPayments: () -> Unit,
    canRegisterPayment: Boolean,
    busyPayment: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(
            "Estado de pago",
            style = AppTypography.CardTitle,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (detailLoading && detail == null) {
            Text("Cargando pagos…", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@Column
        }
        val summary = detail?.paymentSummary
        if (summary == null) {
            Text("No registrado", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@Column
        }
        DetailRow(Icons.Outlined.Payments, "Estado", summary.status.labelEs)
        summary.treatmentCost?.let { cost ->
            DetailRow(Icons.Outlined.Payments, "Coste tratamiento", formatMoney(cost))
        }
        DetailRow(Icons.Outlined.Payments, "Importe pagado", formatMoney(summary.amountPaid))
        if (summary.remainingBalance > 0.0) {
            DetailRow(Icons.Outlined.Payments, "Saldo pendiente", formatMoney(summary.remainingBalance))
        }
        if (summary.status == AppointmentPaymentStatus.NONE && summary.amountPaid <= 0.0) {
            Text(
                "No existen pagos registrados para esta cita.",
                style = AppTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            if (summary.paymentIds.isNotEmpty()) {
                AppOutlinedButton(text = "Ver pago", onClick = onViewPayments, minHeight = 36.dp)
            }
            if (canRegisterPayment) {
                AppButton(
                    text = if (busyPayment) "Cargando..." else "Registrar pago",
                    onClick = onRegisterPayment,
                    enabled = !busyPayment,
                    minHeight = 36.dp,
                )
            }
        }
    }
}

@Composable
private fun StructuredNotesSection(
    notes: List<AppointmentNoteRow>,
    detailLoading: Boolean,
    onAddNote: (String) -> Unit,
    onEditNote: (Int, String) -> Unit,
    onDeleteNote: (Int) -> Unit,
) {
    var newNote by remember { mutableStateOf("") }
    var editingNoteId by remember { mutableStateOf<Int?>(null) }
    var editingText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(
            "Notas",
            style = AppTypography.CardTitle,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (detailLoading && notes.isEmpty()) {
            Text("Cargando notas…", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        notes.forEach { note ->
            Surface(
                shape = AppShapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (editingNoteId == note.id) {
                        AppTextArea(
                            value = editingText,
                            onValueChange = { editingText = it },
                            label = "Editar nota",
                            minLines = 2,
                            maxLines = 4,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            AppButton(
                                text = "Guardar",
                                onClick = {
                                    onEditNote(note.id, editingText)
                                    editingNoteId = null
                                },
                                minHeight = 32.dp,
                            )
                            AppOutlinedButton(
                                text = "Cancelar",
                                onClick = { editingNoteId = null },
                                minHeight = 32.dp,
                            )
                        }
                    } else {
                        Text(note.body, style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "Creado por ${note.authorLabel}",
                            style = AppTypography.Caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            formatEpochMs(note.createdAtEpochMs),
                            style = AppTypography.Caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            AppOutlinedButton(
                                text = "Editar",
                                onClick = {
                                    editingNoteId = note.id
                                    editingText = note.body
                                },
                                minHeight = 32.dp,
                            )
                            AppOutlinedButton(
                                text = "Eliminar",
                                onClick = { onDeleteNote(note.id) },
                                minHeight = 32.dp,
                            )
                        }
                    }
                }
            }
        }
        AppTextArea(
            value = newNote,
            onValueChange = { newNote = it },
            label = "Nueva nota",
            placeholder = "El paciente prefiere horario de tarde…",
            minLines = 2,
            maxLines = 4,
        )
        AppButton(
            text = "Añadir nota",
            onClick = {
                if (newNote.trim().isNotEmpty()) {
                    onAddNote(newNote.trim())
                    newNote = ""
                }
            },
            enabled = newNote.trim().isNotEmpty(),
            minHeight = 36.dp,
        )
    }
}

@Composable
private fun AuditLogSection(entries: List<com.denticode.kt.data.AppointmentAuditEntry>) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(
            "Actividad reciente",
            style = AppTypography.CardTitle,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        entries.forEach { entry ->
            Text(
                "${entry.action} · ${formatEpochMs(entry.createdAtEpochMs)}",
                style = AppTypography.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            entry.detail?.let {
                Text(it, style = AppTypography.Caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(icon, null, Modifier.size(18.dp).padding(top = 1.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                label.uppercase(),
                style = AppTypography.Caption.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(value, style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
