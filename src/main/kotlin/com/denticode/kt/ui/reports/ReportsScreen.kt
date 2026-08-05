package com.denticode.kt.ui.reports

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.ReportsOverview
import com.denticode.kt.export.ExportService
import com.denticode.kt.export.renderReportsCsv
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.components.feedback.LoadingIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@Composable
fun ReportsScreen(repo: DentiRepository) {
    val messenger = LocalAppMessenger.current
    val scope = rememberCoroutineScope()
    val today = LocalDate.now()
    var startDate by remember { mutableStateOf(today.minusDays(29)) }
    var endDate by remember { mutableStateOf(today) }
    var presets by remember { mutableStateOf<List<SavedReportRange>>(emptyList()) }
    var settingsLoaded by remember { mutableStateOf(false) }
    var overview by remember { mutableStateOf<ReportsOverview?>(null) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val loadedSettings = withContext(Dispatchers.IO) {
            ReportSettingsStore.load(today.minusDays(29), today)
        }
        startDate = loadedSettings.startDate
        endDate = loadedSettings.endDate
        presets = loadedSettings.presets
        settingsLoaded = true
    }

    LaunchedEffect(startDate, endDate, presets) {
        if (settingsLoaded) {
            withContext(Dispatchers.IO) {
                ReportSettingsStore.save(ReportSettings(startDate, endDate, presets))
            }
        }
    }

    LaunchedEffect(startDate, endDate) {
        loaded = false
        val result = withContext(Dispatchers.IO) { repo.reportsOverview(startDate, endDate) }
        overview = result
        loaded = true
    }

    fun savePreset(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            messenger.showError("El nombre del rango no puede estar vacío.")
            return
        }
        presets = (presets.filterNot { it.name.equals(trimmed, ignoreCase = true) } + SavedReportRange(trimmed, startDate, endDate))
            .take(12)
        messenger.showSuccess("Rango guardado como «$trimmed».")
    }

    fun removePreset(name: String) {
        presets = presets.filterNot { it.name.equals(name, ignoreCase = true) }
        messenger.showSuccess("Rango «$name» eliminado.")
    }

    fun exportReport() {
        val current = overview ?: return
        scope.launch {
            val file =
                ExportService.pickSaveFile("reporte-${LocalDate.now()}.csv")
                    ?: return@launch
            ExportService.writeTextFile(file, renderReportsCsv(current))
            messenger.showSuccess("Reporte exportado a ${file.name}.")
        }
    }

    if (!loaded || overview == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator()
        }
        return
    }

    ReportsContent(
        overview = overview!!,
        startDate = startDate,
        endDate = endDate,
        onStartDateChange = { startDate = it },
        onEndDateChange = { endDate = it },
        presets = presets,
        onSavePreset = ::savePreset,
        onRemovePreset = ::removePreset,
        onExportClick = ::exportReport,
        modifier = Modifier.fillMaxSize(),
    )
}
