@file:OptIn(ExperimentalFoundationApi::class)

package com.denticode.kt.ui.patientdetail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppSpacing

@Composable
fun ModernPatientDetailContent(
    uiState: PatientDetailUiState,
    totalAppointmentsUnfiltered: Int,
    focusSection: PatientDetailFocusSection = PatientDetailFocusSection.OVERVIEW,
    onClose: () -> Unit,
    onRegisterAppointment: () -> Unit,
    onRegisterTreatment: () -> Unit,
    onRegisterPayment: () -> Unit,
    registerAppointmentEnabled: Boolean,
    registerTreatmentEnabled: Boolean = true,
    onEditPatient: () -> Unit,
    onArchivePatient: () -> Unit = {},
    onDeletePatient: () -> Unit = {},
    isArchived: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val messenger = LocalAppMessenger.current
    val clinicalHistoryRequester = remember { BringIntoViewRequester() }
    var appointmentFilter by remember { mutableStateOf<AppointmentStatus?>(null) }

    LaunchedEffect(focusSection) {
        if (focusSection == PatientDetailFocusSection.CLINICAL_HISTORY) {
            clinicalHistoryRequester.bringIntoView()
        }
    }

    val displayState =
        remember(uiState, appointmentFilter) {
            if (appointmentFilter == null) {
                uiState
            } else {
                uiState.copy(
                    appointments =
                        uiState.appointments.filter { it.status == appointmentFilter },
                    selectedAppointmentFilter = appointmentFilter,
                )
            }
        }

    val statusFilterLabel =
        appointmentFilter?.let { appointmentStatusDetailLabel(it) } ?: "Todos los estados"
    val statusOptions =
        remember {
            listOf("Todos los estados") +
                listOf(
                    AppointmentStatus.CONFIRMED,
                    AppointmentStatus.SCHEDULED,
                    AppointmentStatus.COMPLETED,
                    AppointmentStatus.CANCELLED,
                    AppointmentStatus.IN_PROGRESS,
                ).map { appointmentStatusDetailLabel(it) }
        }

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxSize()
                .background(PatientsPremiumPalette.background),
    ) {
        val stacked = maxWidth < 960.dp
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            PatientHeaderCard(
                patient = displayState.patient,
                kpis = displayState.kpis,
                onViewFullProfile = { messenger.showSuccess("Perfil completo (próximamente).") },
                onClose = onClose,
            )

            TreatmentsPanel(
                treatments = displayState.treatments,
                onRegisterTreatment = onRegisterTreatment,
                registerTreatmentEnabled = registerTreatmentEnabled,
                modifier = Modifier.bringIntoViewRequester(clinicalHistoryRequester),
            )

            if (stacked) {
                AppointmentsPanel(
                    appointments = displayState.appointments,
                    totalCount = totalAppointmentsUnfiltered,
                    statusFilterLabel = statusFilterLabel,
                    statusOptions = statusOptions,
                    onStatusSelect = { label ->
                        appointmentFilter =
                            if (label == "Todos los estados") {
                                null
                            } else {
                                AppointmentStatus.entries.first { appointmentStatusDetailLabel(it) == label }
                            }
                    },
                    onRegisterAppointment = onRegisterAppointment,
                    onViewAll = { messenger.showSuccess("Abrir módulo Citas.") },
                    registerEnabled = registerAppointmentEnabled,
                )
                PaymentsPanel(
                    payments = displayState.payments,
                    summary = displayState.paymentSummary,
                    onRegisterPayment = onRegisterPayment,
                    onViewHistory = { messenger.showSuccess("Historial de pagos (próximamente).") },
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    AppointmentsPanel(
                        appointments = displayState.appointments,
                        totalCount = totalAppointmentsUnfiltered,
                        statusFilterLabel = statusFilterLabel,
                        statusOptions = statusOptions,
                        onStatusSelect = { label ->
                            appointmentFilter =
                                if (label == "Todos los estados") {
                                    null
                                } else {
                                    AppointmentStatus.entries.first { appointmentStatusDetailLabel(it) == label }
                                }
                        },
                        onRegisterAppointment = onRegisterAppointment,
                        onViewAll = { messenger.showSuccess("Abrir módulo Citas.") },
                        registerEnabled = registerAppointmentEnabled,
                        modifier = Modifier.weight(1f),
                    )
                    PaymentsPanel(
                        payments = displayState.payments,
                        summary = displayState.paymentSummary,
                        onRegisterPayment = onRegisterPayment,
                        onViewHistory = { messenger.showSuccess("Historial de pagos (próximamente).") },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            QuickActionsFooter(
                onEditPatient = onEditPatient,
                onNewAppointment = onRegisterAppointment,
                onRegisterTreatment = onRegisterTreatment,
                onRegisterPayment = onRegisterPayment,
                onClinicalHistory = { messenger.showSuccess("Historial clínico (próximamente).") },
                onSendReminder = { messenger.showSuccess("Recordatorio preparado (simulación).") },
                onMoreActions = { messenger.showSuccess("Más acciones.") },
                onArchivePatient = onArchivePatient,
                onDeletePatient = onDeletePatient,
                isArchived = isArchived,
            )
        }
    }
}
