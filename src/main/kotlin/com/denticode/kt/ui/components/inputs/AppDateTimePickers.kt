@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.components.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denticode.kt.ui.localDateToMaterialDatePickerMillis
import com.denticode.kt.ui.materialDatePickerMillisToLocalDate
import com.denticode.kt.ui.components.dialogs.AppBasicDialog
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val PickerDateDisplayFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale("es", "ES"))

private val PickerTimeDisplayFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm", Locale("es", "ES"))

/** Atajo mostrado como chip en el diálogo de fecha (p. ej. «Hoy»). */
data class DatePickerShortcut(
    val label: String,
    val date: LocalDate,
)

/** Minutos alineados a pasos de 5 (citas y pagos). */
val FormMinuteSteps: List<Int> = (0..55 step 5).toList()

fun snapFormMinuteToStep5(minute: Int): Int =
    FormMinuteSteps.minByOrNull { abs(it - minute) } ?: 0

fun formatPickerDisplayDate(date: LocalDate): String {
    val raw = date.format(PickerDateDisplayFmt)
    return raw.replaceFirstChar { ch ->
        if (ch.isLowerCase()) ch.titlecase(Locale("es", "ES")) else ch.toString()
    }
}

fun formatPickerDisplayTime(hour: Int, minute: Int): String =
    LocalTime.of(hour, minute).format(PickerTimeDisplayFmt)

fun defaultAppointmentDateShortcuts(): List<DatePickerShortcut> {
    val today = LocalDate.now()
    return listOf(
        DatePickerShortcut("Hoy", today),
        DatePickerShortcut("Mañana", today.plusDays(1)),
        DatePickerShortcut("En 1 semana", today.plusDays(7)),
    )
}

fun defaultPaymentDateShortcuts(): List<DatePickerShortcut> {
    val today = LocalDate.now()
    return listOf(
        DatePickerShortcut("Hoy", today),
        DatePickerShortcut("Ayer", today.minusDays(1)),
    )
}

/** Franjas horarias típicas de clínica (08:00–18:00 cada 15 min). */
fun defaultClinicTimeSlots(): List<LocalTime> {
    val out = ArrayList<LocalTime>(48)
    var t = LocalTime.of(8, 0)
    val end = LocalTime.of(18, 0)
    while (!t.isAfter(end)) {
        out.add(t)
        t = t.plusMinutes(15)
    }
    return out
}

/** Fechas de hoy o anteriores (p. ej. fecha de nacimiento). */
@Composable
fun rememberPastOrTodaySelectableDates(zoneId: ZoneId = ZoneId.systemDefault()): SelectableDates =
    remember(zoneId) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val d = materialDatePickerMillisToLocalDate(utcTimeMillis)
                return !d.isAfter(LocalDate.now(zoneId))
            }

            override fun isSelectableYear(year: Int): Boolean = year <= LocalDate.now(zoneId).year
        }
    }

@Composable
fun AppDatePickerField(
    label: String,
    value: LocalDate,
    onValueChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
    shortcuts: List<DatePickerShortcut>? = null,
) {
    var showDialog by remember { mutableStateOf(false) }
    val fieldInteraction = remember { MutableInteractionSource() }

    AppTextField(
        value = formatPickerDisplayDate(value),
        onValueChange = {},
        modifier = modifier.fillMaxWidth(),
        textFieldModifier =
            Modifier.clickable(enabled = enabled, interactionSource = fieldInteraction, indication = null) {
                if (enabled) showDialog = true
            },
        label = label,
        readOnly = true,
        enabled = enabled,
        trailingIcon = {
            IconButton(onClick = { if (enabled) showDialog = true }, enabled = enabled) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Elegir fecha")
            }
        },
    )

    if (showDialog) {
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = localDateToMaterialDatePickerMillis(value),
                selectableDates = selectableDates,
            )
        AppBasicDialog(
            title = label,
            onDismissRequest = { showDialog = false },
            confirmText = "Aceptar",
            onConfirm = {
                datePickerState.selectedDateMillis?.let { ms ->
                    onValueChange(materialDatePickerMillisToLocalDate(ms))
                }
                showDialog = false
            },
            dismissText = "Cancelar",
            onDismiss = { showDialog = false },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                shortcuts?.takeIf { it.isNotEmpty() }?.let { items ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        items.forEach { shortcut ->
                            FilterChip(
                                selected = value == shortcut.date,
                                onClick = {
                                    onValueChange(shortcut.date)
                                    showDialog = false
                                },
                                label = { Text(shortcut.label) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.padding(start = 4.dp),
                                    )
                                },
                            )
                        }
                    }
                }
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
fun AppTimePickerField(
    label: String,
    hour: Int,
    minute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    is24Hour: Boolean = true,
    timeSlots: List<LocalTime> = defaultClinicTimeSlots(),
    snapMinute: (Int) -> Int = ::snapFormMinuteToStep5,
) {
    var showDialog by remember { mutableStateOf(false) }
    val fieldInteraction = remember { MutableInteractionSource() }
    val display = remember(hour, minute) { formatPickerDisplayTime(hour, minute) }

    AppTextField(
        value = display,
        onValueChange = {},
        modifier = modifier.fillMaxWidth(),
        textFieldModifier =
            Modifier.clickable(enabled = enabled, interactionSource = fieldInteraction, indication = null) {
                if (enabled) showDialog = true
            },
        label = label,
        readOnly = true,
        enabled = enabled,
        trailingIcon = {
            IconButton(onClick = { if (enabled) showDialog = true }, enabled = enabled) {
                Icon(Icons.Default.Schedule, contentDescription = "Elegir hora")
            }
        },
    )

    if (showDialog) {
        val timePickerState =
            rememberTimePickerState(
                initialHour = hour,
                initialMinute = minute,
                is24Hour = is24Hour,
            )
        AppBasicDialog(
            title = label,
            onDismissRequest = { showDialog = false },
            confirmText = "Aceptar",
            onConfirm = {
                onTimeChange(
                    timePickerState.hour,
                    snapMinute(timePickerState.minute),
                )
                showDialog = false
            },
            dismissText = "Cancelar",
            onDismiss = { showDialog = false },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Text(
                    "Horarios habituales",
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    timeSlots.forEach { slot ->
                        val selected = hour == slot.hour && minute == slot.minute
                        FilterChip(
                            selected = selected,
                            onClick = {
                                onTimeChange(slot.hour, slot.minute)
                                showDialog = false
                            },
                            label = { Text(slot.format(PickerTimeDisplayFmt)) },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                        )
                    }
                }
                Text(
                    "O ajuste con el selector",
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TimePicker(state = timePickerState)
            }
        }
    }
}

/** Fecha y hora en formularios de cita / pago (sustituye dos desplegables de hora y minuto). */
@Composable
fun AppVisitDateTimeFields(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    hour: Int,
    minute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    dateLabel: String = "Fecha",
    timeLabel: String = "Hora",
    dateShortcuts: List<DatePickerShortcut>? = defaultAppointmentDateShortcuts(),
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        AppDatePickerField(
            label = dateLabel,
            value = date,
            onValueChange = onDateChange,
            enabled = enabled,
            selectableDates = selectableDates,
            shortcuts = dateShortcuts,
            modifier = Modifier.weight(1.35f),
        )
        AppTimePickerField(
            label = timeLabel,
            hour = hour,
            minute = minute,
            onTimeChange = onTimeChange,
            enabled = enabled,
            modifier = Modifier.weight(0.85f),
        )
    }
}
