package com.denticode.kt.ui.treatments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.PatientTreatmentRow
import com.denticode.kt.data.TreatmentStatus
import com.denticode.kt.ui.formatMoney
import com.denticode.kt.ui.patients.PatientsPremiumPalette
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import com.denticode.kt.ui.parseAppointmentScheduledAt
import java.time.format.DateTimeFormatter
import java.util.Locale

private val treatmentDateFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale("es", "ES"))

@Composable
fun TreatmentStatusBadge(
    status: TreatmentStatus,
    modifier: Modifier = Modifier,
) {
    val (bg, fg) =
        when (status) {
            TreatmentStatus.PLANNED -> PatientsPremiumPalette.primary.copy(alpha = 0.12f) to PatientsPremiumPalette.primary
            TreatmentStatus.IN_PROGRESS -> PatientsPremiumPalette.info.copy(alpha = 0.14f) to PatientsPremiumPalette.info
            TreatmentStatus.COMPLETED -> PatientsPremiumPalette.success.copy(alpha = 0.14f) to PatientsPremiumPalette.success
            TreatmentStatus.CANCELLED -> PatientsPremiumPalette.error.copy(alpha = 0.14f) to PatientsPremiumPalette.error
        }
    Surface(modifier = modifier, shape = RoundedCornerShape(999.dp), color = bg) {
        Text(
            status.labelEs,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = AppTypography.Caption,
            fontWeight = FontWeight.SemiBold,
            color = fg,
        )
    }
}

@Composable
fun TreatmentsTableHeader(showPatientColumn: Boolean, showActionsColumn: Boolean = false, modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(PatientsPremiumPalette.background)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showPatientColumn) TreatmentHeaderCell("PACIENTE", Modifier.weight(1.1f))
        TreatmentHeaderCell("TRATAMIENTO", Modifier.weight(1.2f))
        TreatmentHeaderCell("DOCTOR", Modifier.weight(1f))
        TreatmentHeaderCell("PRECIO", Modifier.weight(0.75f))
        TreatmentHeaderCell("ESTADO", Modifier.width(110.dp))
        TreatmentHeaderCell("FECHA", Modifier.weight(0.9f))
        if (showActionsColumn) TreatmentHeaderCell("", Modifier.width(60.dp))
    }
}

@Composable
private fun TreatmentHeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        style = AppTypography.Caption,
        fontWeight = FontWeight.SemiBold,
        color = PatientsPremiumPalette.textSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun TreatmentTableRow(
    treatment: PatientTreatmentRow,
    showPatientColumn: Boolean,
    showActionsColumn: Boolean = false,
    onStatusChange: ((PatientTreatmentRow) -> Unit)? = null,
    onDelete: ((PatientTreatmentRow) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val dateLabel =
        runCatching {
            parseAppointmentScheduledAt(treatment.actionAt).format(treatmentDateFmt)
        }.getOrElse { treatment.actionAt.take(16) }
    val priceLabel =
        buildString {
            append("€ ${formatMoney(treatment.totalPrice)}")
            treatment.standardPrice?.let { std ->
                if (kotlin.math.abs(std - treatment.unitPrice) > 0.009) {
                    append(" (cat. € ${formatMoney(std)})")
                }
            }
        }
    var showMenu by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showPatientColumn) {
                Column(Modifier.weight(1.1f)) {
                    Text(
                        treatment.patientName,
                        style = AppTypography.BodySmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "ID ${treatment.patientId}",
                        style = AppTypography.Caption,
                        color = PatientsPremiumPalette.textSecondary,
                    )
                }
            }
            Text(
                treatment.procedureTypeName,
                Modifier.weight(1.2f),
                style = AppTypography.BodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                treatment.doctorName,
                Modifier.weight(1f),
                style = AppTypography.BodySmall,
                color = PatientsPremiumPalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                priceLabel,
                Modifier.weight(0.75f),
                style = AppTypography.BodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
            )
            Box(Modifier.width(110.dp)) {
                TreatmentStatusBadge(treatment.status)
            }
            Text(
                dateLabel,
                Modifier.weight(0.9f),
                style = AppTypography.Caption,
                color = PatientsPremiumPalette.textSecondary,
                maxLines = 2,
            )
            if (showActionsColumn) {
                Box(Modifier.width(60.dp)) {
                    if (onStatusChange != null || onDelete != null) {
                        Box {
                            Text(
                                "⋯",
                                style = AppTypography.Body,
                                color = PatientsPremiumPalette.textSecondary,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .then(
                                        Modifier.padding(0.dp)
                                    ),
                            )
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                            ) {
                                onStatusChange?.let { handler ->
                                    DropdownMenuItem(
                                        text = { Text("Cambiar estado") },
                                        onClick = {
                                            showMenu = false
                                            handler(treatment)
                                        },
                                    )
                                }
                                onDelete?.let { handler ->
                                    DropdownMenuItem(
                                        text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showMenu = false
                                            handler(treatment)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    }
}

@Composable
fun TreatmentsTable(
    treatments: List<PatientTreatmentRow>,
    showPatientColumn: Boolean,
    modifier: Modifier = Modifier,
    emptyMessage: String = "No hay tratamientos registrados.",
    /** Use when the table sits inside another vertical scroll (e.g. patient detail). */
    embeddedInScroll: Boolean = false,
    onStatusChange: ((PatientTreatmentRow) -> Unit)? = null,
    onDelete: ((PatientTreatmentRow) -> Unit)? = null,
) {
    val showActions = onStatusChange != null || onDelete != null
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.medium,
        color = Color.White,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
    ) {
        if (treatments.isEmpty()) {
            Text(
                emptyMessage,
                modifier = Modifier.padding(AppSpacing.lg),
                style = AppTypography.BodySmall,
                color = PatientsPremiumPalette.textSecondary,
            )
        } else if (embeddedInScroll) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TreatmentsTableHeader(showPatientColumn = showPatientColumn, showActionsColumn = showActions)
                treatments.forEach { t ->
                    TreatmentTableRow(
                        treatment = t,
                        showPatientColumn = showPatientColumn,
                        showActionsColumn = showActions,
                        onStatusChange = onStatusChange,
                        onDelete = onDelete,
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item(key = "header") {
                    TreatmentsTableHeader(showPatientColumn = showPatientColumn, showActionsColumn = showActions)
                }
                items(treatments, key = { it.id }) { t ->
                    TreatmentTableRow(
                        treatment = t,
                        showPatientColumn = showPatientColumn,
                        showActionsColumn = showActions,
                        onStatusChange = onStatusChange,
                        onDelete = onDelete,
                    )
                }
            }
        }
    }
}
