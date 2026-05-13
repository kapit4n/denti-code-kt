package com.denticode.kt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.denticode.kt.data.AppointmentRow
import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.ui.components.cards.AppCard
import com.denticode.kt.ui.components.feedback.StatusChip
import com.denticode.kt.ui.models.StatusKind
import com.denticode.kt.ui.navigation.PageHeader
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppointmentsScreen(repo: DentiRepository) {
    var rows by remember { mutableStateOf<List<AppointmentRow>>(emptyList()) }
    LaunchedEffect(Unit) {
        rows = withContext(Dispatchers.IO) { repo.listAppointments(200) }
    }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        PageHeader(
            title = "Citas",
            subtitle = "Citas con paciente y doctor principal (modelo Appointment de Denti-Code).",
            modifier = Modifier.fillMaxWidth(),
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(rows, key = { it.id }) { a ->
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        Text(
                            a.scheduledAt,
                            style = AppTypography.CardTitle,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "${a.patientName} · ${a.doctorName}",
                            style = AppTypography.Body,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        a.purpose?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        val kind =
                            when (a.status) {
                                AppointmentStatus.COMPLETED -> StatusKind.Success
                                AppointmentStatus.CANCELLED,
                                AppointmentStatus.NO_SHOW,
                                -> StatusKind.Error
                                AppointmentStatus.IN_PROGRESS -> StatusKind.Info
                                else -> StatusKind.Neutral
                            }
                        StatusChip(text = a.status.displayLabel, kind = kind)
                    }
                }
            }
        }
    }
}
