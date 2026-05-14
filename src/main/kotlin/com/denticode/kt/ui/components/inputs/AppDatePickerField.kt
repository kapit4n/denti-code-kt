@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.components.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.theme.AppSpacing
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DisplayDateFormat: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

/** Dates on or before today (typical for date of birth). */
@Composable
fun rememberPastOrTodaySelectableDates(zoneId: ZoneId = ZoneId.systemDefault()): SelectableDates =
    remember(zoneId) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val d = Instant.ofEpochMilli(utcTimeMillis).atZone(zoneId).toLocalDate()
                return !d.isAfter(LocalDate.now(zoneId))
            }

            override fun isSelectableYear(year: Int): Boolean = year <= LocalDate.now(zoneId).year
        }
    }

private fun LocalDate.toStartOfDayMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long =
    atStartOfDay(zoneId).toInstant().toEpochMilli()

private fun millisToLocalDate(millis: Long, zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()

@Composable
fun AppDatePickerField(
    label: String,
    value: LocalDate,
    onValueChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
    chooseButtonText: String = "Elegir",
) {
    var showDialog by remember { mutableStateOf(false) }
    val zone = ZoneId.systemDefault()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        AppTextField(
            value = value.format(DisplayDateFormat),
            onValueChange = {},
            modifier = Modifier.weight(1f),
            label = label,
            readOnly = true,
            enabled = enabled,
        )
        AppOutlinedButton(
            text = chooseButtonText,
            onClick = { showDialog = true },
            enabled = enabled,
        )
    }

    if (showDialog) {
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = value.toStartOfDayMillis(zone),
                selectableDates = selectableDates,
            )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { ms ->
                            onValueChange(millisToLocalDate(ms, zone))
                        }
                        showDialog = false
                    },
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
