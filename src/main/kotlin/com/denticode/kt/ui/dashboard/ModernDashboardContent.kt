package com.denticode.kt.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
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
import com.denticode.kt.ui.theme.AppSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
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
            today = repo.listTodayAppointments(8).map { it.toTodayUi() }
            revenueToday = repo.sumPaymentsToday()
            lowStock = repo.countLowStockLines(5)
            donut = buildDonutSlices(repo.listAppointments(300))
        }
    }

    val scroll = rememberScrollState()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(bottom = AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        DashboardWelcomeHeader(Modifier.fillMaxWidth())

        if (overview == null) {
            Text("Cargando panel…", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            val o = overview!!
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                modifier = Modifier.fillMaxWidth(),
            ) {
                val minW = 200.dp
                val maxW = 320.dp
                DashboardMetricPill(
                    title = "Pacientes",
                    value = o.patientCount.toString(),
                    subtitle = "Directorio activo",
                    icon = Icons.Default.People,
                    gradient = listOf(Color(0xFF4DA3FF), Color(0xFF6C63FF)),
                    modifier = Modifier.widthIn(min = minW, max = maxW),
                )
                DashboardMetricPill(
                    title = "Citas hoy",
                    value = today.size.toString(),
                    subtitle = "Agendadas para hoy",
                    icon = Icons.Default.CalendarMonth,
                    gradient = listOf(Color(0xFF8B80F9), Color(0xFF6C63FF)),
                    modifier = Modifier.widthIn(min = minW, max = maxW),
                )
                DashboardMetricPill(
                    title = "Ingresos hoy",
                    value = "€ %.0f".format(revenueToday),
                    subtitle = "Pagos registrados hoy",
                    icon = Icons.Default.Payments,
                    gradient = listOf(Color(0xFF34C759), Color(0xFF2FA34A)),
                    modifier = Modifier.widthIn(min = minW, max = maxW),
                )
                DashboardMetricPill(
                    title = "Tratamientos pendientes",
                    value = o.upcomingAppointmentCount.toString(),
                    subtitle = "Programada / confirmada / en curso",
                    icon = Icons.Default.MedicalServices,
                    gradient = listOf(Color(0xFFFFB020), Color(0xFFFF8A3D)),
                    modifier = Modifier.widthIn(min = minW, max = maxW),
                )
                DashboardMetricPill(
                    title = "Stock bajo",
                    value = lowStock.toString(),
                    subtitle = "Líneas con menos de 5 uds.",
                    icon = Icons.Default.Inventory2,
                    gradient = listOf(Color(0xFFFF5A5F), Color(0xFFFF8A8E)),
                    modifier = Modifier.widthIn(min = minW, max = maxW),
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val wide = maxWidth > 980.dp
            if (wide) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TodayAppointmentsPanel(
                        rows = today,
                        onViewCalendar = { onNavigate(ScreenRoute.Appointments) },
                        onViewAll = { onNavigate(ScreenRoute.Appointments) },
                        modifier = Modifier.weight(1.6f),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        ActivityFeedPanel(buildMockActivity())
                        RevenueLineChartCard()
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    TodayAppointmentsPanel(
                        rows = today,
                        onViewCalendar = { onNavigate(ScreenRoute.Appointments) },
                        onViewAll = { onNavigate(ScreenRoute.Appointments) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ActivityFeedPanel(buildMockActivity())
                    RevenueLineChartCard()
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            AppointmentDonutSection(
                slices = donut.ifEmpty { defaultDonut() },
                modifier = Modifier.weight(1f),
            )
            AlertsPanel(
                alerts = buildAlerts(lowStock, overview),
                modifier = Modifier.weight(1f),
            )
        }

        QuickActionsFooter(onNavigate = onNavigate, modifier = Modifier.fillMaxWidth())
    }
}

private fun buildDonutSlices(rows: List<AppointmentRow>): List<DonutSlice> {
    if (rows.isEmpty()) return defaultDonut()
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
    if (confirmed + pending + progress + cancelled == 0f) return defaultDonut()
    return listOf(
        DonutSlice("Confirmadas", confirmed, Color(0xFF34C759)),
        DonutSlice("Pendientes", pending, Color(0xFFFFB020)),
        DonutSlice("En proceso", progress, Color(0xFF4DA3FF)),
        DonutSlice("Canceladas", cancelled, Color(0xFFFF5A5F)),
    )
}

private fun defaultDonut(): List<DonutSlice> =
    listOf(
        DonutSlice("Confirmadas", 4f, Color(0xFF34C759)),
        DonutSlice("Pendientes", 3f, Color(0xFFFFB020)),
        DonutSlice("En proceso", 2f, Color(0xFF4DA3FF)),
        DonutSlice("Canceladas", 1f, Color(0xFFFF5A5F)),
    )

private fun buildMockActivity(): List<ActivityFeedItem> =
    listOf(
        ActivityFeedItem("Nuevo paciente", "Alta en directorio", "Hace 32 min", Color(0xFF6C63FF)),
        ActivityFeedItem("Cita confirmada", "Consulta programada", "Hace 1 h", Color(0xFF34C759)),
        ActivityFeedItem("Pago recibido", "Tarjeta · €120", "Hace 2 h", Color(0xFF4DA3FF)),
        ActivityFeedItem("Tratamiento completado", "Limpieza", "Hace 3 h", Color(0xFFFFB020)),
        ActivityFeedItem("Cita cancelada", "Reagendar pendiente", "Hace 5 h", Color(0xFFFF5A5F)),
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
            title = "Pagos pendientes de conciliar",
            subtitle = "Revisa cobros sin nota en pagos",
            tint = Color(0xFF4DA3FF),
        ),
        DashboardAlertUi(
            title = "Citas sin confirmar",
            subtitle = "$pending citas en estados abiertos",
            tint = Color(0xFF6C63FF),
        ),
    )
}
