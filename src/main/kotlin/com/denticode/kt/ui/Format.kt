package com.denticode.kt.ui

import com.denticode.kt.data.ProcedureTypeOption
import com.denticode.kt.data.ProcedureTypeRow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val DATE_TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
private val ZONE: ZoneId = ZoneId.systemDefault()

/**
 * Material3 [androidx.compose.material3.DatePicker] stores the selected day as midnight **UTC**.
 * Do not convert picker millis with [ZoneId.systemDefault] or the calendar day shifts (e.g. 25 → 24).
 */
fun localDateToMaterialDatePickerMillis(date: LocalDate): Long =
    date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

fun materialDatePickerMillisToLocalDate(utcMillis: Long): LocalDate =
    Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()

private val APPOINTMENT_SCHEDULED_AT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

/** Texto `appointments.scheduled_at` (ISO fecha-hora o solo fecha) → `LocalDateTime` en zona local. */
fun parseAppointmentScheduledAt(value: String): LocalDateTime {
    val t = value.trim()
    return try {
        LocalDateTime.parse(t, APPOINTMENT_SCHEDULED_AT)
    } catch (_: DateTimeParseException) {
        try {
            LocalDate.parse(t, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay()
        } catch (_: DateTimeParseException) {
            LocalDateTime.now(ZONE)
        }
    }
}

/** Epoch ms → "yyyy-MM-dd HH:mm" en la zona local. */
fun formatEpochMs(epochMs: Long): String =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs), ZONE).format(DATE_TIME_FMT)

/** "yyyy-MM-dd HH:mm" → epoch ms; null si no parsea. */
fun parseDateTime(input: String): Long? {
    return try {
        LocalDateTime.parse(input.trim(), DATE_TIME_FMT)
            .atZone(ZONE)
            .toInstant()
            .toEpochMilli()
    } catch (_: DateTimeParseException) {
        null
    }
}

/** Muestra "120" como "120" y "1.25" como "1.25". */
fun formatQty(v: Double): String = if (v % 1.0 == 0.0) v.toInt().toString() else "%.2f".format(v)

fun formatMoney(v: Double): String = "%.2f".format(v)

/**
 * Monto desde el campo de texto (acepta "1234.56", "1234,56", "1.234,56", "1,234.56").
 */
fun parseMoneyAmount(input: String): Double? {
    var t = input.trim().replace('\u00A0', ' ').replace(" ", "")
    if (t.isEmpty()) return null
    val lastComma = t.lastIndexOf(',')
    val lastDot = t.lastIndexOf('.')
    t =
        when {
            lastComma >= 0 && lastDot >= 0 ->
                if (lastComma > lastDot) {
                    t.replace(".", "").replace(',', '.')
                } else {
                    t.replace(",", "")
                }
            lastComma >= 0 -> t.replace(',', '.')
            else -> t
        }
    return t.toDoubleOrNull()
}

/** % de ganancia desde el campo de texto (acepta "20", "20%", "20,5"). */
fun parseMarginPercent(input: String): Double? {
    val t = input.trim().removeSuffix("%").trim().replace(',', '.')
    if (t.isEmpty()) return null
    return t.toDoubleOrNull()
}

private val ISO_DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

/** Fecha local → yyyy-MM-dd */
fun formatIsoDate(d: LocalDate): String = d.format(ISO_DATE)

/** Texto yyyy-MM-dd → fecha; null si vacío o inválido. */
fun parseIsoDate(input: String): LocalDate? {
    val t = input.trim()
    if (t.isEmpty()) return null
    return try {
        LocalDate.parse(t, ISO_DATE)
    } catch (_: DateTimeParseException) {
        null
    }
}

fun formatIsoDateOrDash(d: LocalDate?): String =
    if (d == null) "—" else formatIsoDate(d)

/** Opciones para desplegable de tratamiento (ninguno + tipos activos por nombre). */
fun procedureTypeDropdownOptions(rows: List<ProcedureTypeRow>): List<ProcedureTypeOption> =
    buildList {
        add(ProcedureTypeOption.none())
        rows.filter { it.isActive }.sortedBy { it.name }.forEach { add(ProcedureTypeOption.from(it)) }
    }

fun formatDuration(minutes: Int): String {
    if (minutes < 60) return "${minutes} min"
    val h = minutes / 60
    val m = minutes % 60
    return if (m == 0) "${h} h" else "${h} h ${m} min"
}

fun parseHexColor(hex: String?): androidx.compose.ui.graphics.Color? {
    if (hex.isNullOrBlank()) return null
    return runCatching {
        val cleaned = hex.trim().removePrefix("#")
        val colorLong = cleaned.toLong(16)
        when (cleaned.length) {
            6 -> androidx.compose.ui.graphics.Color(0xFF000000 or colorLong)
            8 -> androidx.compose.ui.graphics.Color(colorLong)
            else -> null
        }
    }.getOrNull()
}
