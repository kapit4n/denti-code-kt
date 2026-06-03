package com.denticode.kt.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.AppointmentRow
import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.ClinicOverview
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.ui.navigation.ScreenRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val defaultWeekRevenue = listOf(1200f, 1450f, 1320f, 1680f, 1890f, 2100f, 1980f)

@Composable
fun ModernDashboardContent(
    repo: DentiRepository,
    onNavigate: (ScreenRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    var overview by remember { mutableStateOf<ClinicOverview?>(null) }
    var today by remember { mutableStateOf<List<TodayAppointmentUi>>(emptyList()) }
    var donut by remember { mutableStateOf<List<DonutSlice>>(emptyList()) }
    var revenueToday by remember { mutableStateOf(0.0) }
    var lowStock by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            overview = repo.clinicOverview()
            today = repo.listTodayAppointments(5).map { it.toTodayUi() }
            revenueToday = repo.sumPaymentsToday()
            lowStock = repo.countLowStockLines(5)
            donut = buildDonutSlices(repo.listAppointments(300))
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DashboardWelcomeHeader(Modifier.fillMaxWidth())

        if (overview == null) {
            Text(
                "Cargando panel…",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            DashboardKpiRow(
                overview = overview!!,
                todayCount = today.size,
                revenueToday = revenueToday,
                lowStock = lowStock,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        ResponsiveDashboardGrid(
            todayRows = today,
            activityItems = buildMockActivity(),
            revenueValues = defaultWeekRevenue,
            revenueTotalLabel = "€ 12.4k",
            revenueDeltaLabel = "+12%",
            donutSlices = donut,
            alerts = buildAlerts(lowStock, overview),
            onNavigate = onNavigate,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )
    }
}

private fun buildDonutSlices(rows: List<AppointmentRow>): List<DonutSlice> {
    if (rows.isEmpty()) return emptyList()
    val confirmed =
        rows.count {
            it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.COMPLETED
        }.toFloat()
    val pending =
        rows.count {
            it.status == AppointmentStatus.SCHEDULED || it.status == AppointmentStatus.RESCHEDULED
        }.toFloat()
    val progress = rows.count { it.status == AppointmentStatus.IN_PROGRESS }.toFloat()
    val cancelled =
        rows.count {
            it.status == AppointmentStatus.CANCELLED || it.status == AppointmentStatus.NO_SHOW
        }.toFloat()
    if (confirmed + pending + progress + cancelled == 0f) return emptyList()
    return listOf(
        DonutSlice("Confirmadas", confirmed, Color(0xFF34C759)),
        DonutSlice("Pendientes", pending, Color(0xFFFFB020)),
        DonutSlice("En proceso", progress, Color(0xFF4DA3FF)),
        DonutSlice("Canceladas", cancelled, Color(0xFFFF5A5F)),
    )
}

private fun buildMockActivity(): List<ActivityFeedItem> =
    listOf(
        ActivityFeedItem("Nuevo paciente", "", "32m", Color(0xFF6C63FF)),
        ActivityFeedItem("Cita confirmada", "", "1h", Color(0xFF34C759)),
        ActivityFeedItem("Pago recibido", "", "2h", Color(0xFF4DA3FF)),
        ActivityFeedItem("Tratamiento completado", "", "3h", Color(0xFFFFB020)),
        ActivityFeedItem("Cita cancelada", "", "5h", Color(0xFFFF5A5F)),
    )

private fun buildAlerts(lowStock: Int, overview: ClinicOverview?): List<DashboardAlertUi> {
    val pending = overview?.upcomingAppointmentCount ?: 0
    return listOf(
        DashboardAlertUi(
            title = "Stock bajo",
            subtitle = if (lowStock > 0) "$lowStock líneas por debajo del mínimo" else "Sin alertas de stock",
            tint = Color(0xFFFFB020),
        ),
        DashboardAlertUi(
            title = "Pagos pendientes",
            subtitle = "Revisa cobros sin conciliar",
            tint = Color(0xFF4DA3FF),
        ),
        DashboardAlertUi(
            title = "Citas sin confirmar",
            subtitle = "$pending citas en estados abiertos",
            tint = Color(0xFF6C63FF),
        ),
    )
}
