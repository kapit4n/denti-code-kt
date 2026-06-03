package com.denticode.kt.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.DoctorDirectoryKpis
import com.denticode.kt.data.DoctorDirectoryRow
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.doctors.ModernDoctorsContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DoctorsScreen(repo: DentiRepository) {
    val messenger = LocalAppMessenger.current
    var directoryRows by remember { mutableStateOf<List<DoctorDirectoryRow>>(emptyList()) }
    var kpis by remember {
        mutableStateOf(
            DoctorDirectoryKpis(
                totalDoctors = 0,
                activeDoctors = 0,
                todayAppointments = 0,
                specialtyCount = 0,
            ),
        )
    }

    LaunchedEffect(Unit) {
        val (loadedKpis, rows) =
            withContext(Dispatchers.IO) {
                repo.loadDoctorDirectory()
            }
        kpis = loadedKpis
        directoryRows = rows
    }

    ModernDoctorsContent(
        directoryRows = directoryRows,
        kpis = kpis,
        onNewDoctorClick = {
            messenger.showSuccess("Registro de doctores próximamente.")
        },
    )
}
