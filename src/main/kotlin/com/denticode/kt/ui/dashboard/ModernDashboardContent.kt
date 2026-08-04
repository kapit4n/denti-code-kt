package com.denticode.kt.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.AppointmentRow
import com.denticode.kt.data.AppointmentStatus
import com.denticode.kt.data.ClinicOverview
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.StockStatus
import com.denticode.kt.ui.components.feedback.LoadingIndicator
import com.denticode.kt.ui.navigation.ScreenRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    var activityItems by remember { mutableStateOf<List<ActivityFeedItem>>(emptyList()) }
    var weekValues by remember { mutableStateOf<List<Double>>(emptyList()) }
    var weekTotal by remember { mutableStateOf(0.0) }
    var pendingPayments by remember { mutableStateOf(0) }
    var stockAlerts by remember { mutableStateOf<List<StockAlertUi>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            overview = repo.clinicOverview()
            today = repo.listTodayAppointments(5).map { it.toTodayUi() }
            revenueToday = repo.sumPaymentsToday()
            lowStock = repo.countLowStockLines(5)
            donut = buildDonutSlices(repo.listAppointments(300))
            val (daily, total) = repo.weeklyRevenue()
            weekValues = daily
            weekTotal = total
            val rawActivity = repo.recentActivity(5)
            activityItems = rawActivity.mapIndexed { i, (label, time) ->
                val accent = when {
                    label.contains("Pago", ignoreCase = true) -> Color(0xFF4DA3FF)
                    label.contains("Tratamiento", ignoreCase = true) -> Color(0xFFFFB020)
                    label.contains("cancel", ignoreCase = true) -> Color(0xFFFF5A5F)
                    label.contains("paciente", ignoreCase = true) -> Color(0xFF6C63FF)
                    else -> Color(0xFF34C759)
                }
                ActivityFeedItem(title = label, subtitle = "", timeLabel = time, accent = accent)
            }
            pendingPayments = repo.countPendingPayments()
            stockAlerts =
                repo.listInventoryLines()
                    .mapNotNull { it.toStockAlertUi() }
                    .sortedWith(
                        compareBy<StockAlertUi>(
                            { if (it.status == StockStatus.OUT) 0 else 1 },
                            { it.productName },
                        ),
                    )
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DashboardWelcomeHeader(Modifier.fillMaxWidth())

        if (overview == null) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        } else {
            DashboardKpiRow(
                overview = overview!!,
                todayCount = today.size,
                revenueToday = revenueToday,
                lowStock = lowStock,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        val weekTotalLabel = "Bs. ${"%.1f".format(weekTotal / 1000)}k"
        val weekDeltaLabel = if (weekValues.size >= 2) {
            val last = weekValues.last()
            val prev = weekValues[weekValues.size - 2]
            val pct = if (prev > 0) ((last - prev) / prev * 100).toInt() else 0
            if (pct >= 0) "+$pct%" else "$pct%"
        } else "+0%"
        val weekFloats = weekValues.map { it.toFloat() }

        ResponsiveDashboardGrid(
            todayRows = today,
            activityItems = activityItems,
            revenueValues = weekFloats,
            revenueTotalLabel = weekTotalLabel,
            revenueDeltaLabel = weekDeltaLabel,
            donutSlices = donut,
            alerts = buildAlerts(stockAlerts.size, pendingPayments, overview),
            stockAlerts = stockAlerts,
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

private fun buildAlerts(lowStock: Int, pendingPayments: Int, overview: ClinicOverview?): List<DashboardAlertUi> {
    val pending = overview?.upcomingAppointmentCount ?: 0
    return listOf(
        DashboardAlertUi(
            title = "Stock bajo",
            subtitle = if (lowStock > 0) "$lowStock líneas por debajo del mínimo" else "Sin alertas de stock",
            tint = Color(0xFFFFB020),
        ),
        DashboardAlertUi(
            title = "Pagos pendientes",
            subtitle = if (pendingPayments > 0) "$pendingPayments tratamientos con saldo pendiente" else "Todos los pagos al día",
            tint = Color(0xFF4DA3FF),
        ),
        DashboardAlertUi(
            title = "Citas sin confirmar",
            subtitle = "$pending citas en estados abiertos",
            tint = Color(0xFF6C63FF),
        ),
    )
}
