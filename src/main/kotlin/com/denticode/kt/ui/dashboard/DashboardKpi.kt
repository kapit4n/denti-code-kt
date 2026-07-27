package com.denticode.kt.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.ClinicOverview
import com.denticode.kt.ui.theme.AppElevations
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val KpiRowHeight = 110.dp

@Composable
fun DashboardWelcomeHeader(modifier: Modifier = Modifier) {
    val dateFmt = remember { DateTimeFormatter.ofPattern("EEEE d MMM yyyy", Locale("es", "ES")) }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.Default.MedicalServices,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Hola, equipo clínico",
                    style = AppTypography.CardTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "Centro operativo · Denti-Code",
                    style = AppTypography.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            LocalDate.now().format(dateFmt),
            style = AppTypography.Caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun DashboardKpiRow(
    overview: ClinicOverview,
    todayCount: Int,
    revenueToday: Double,
    lowStock: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(KpiRowHeight),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DashboardKpiCard(
            title = "Pacientes",
            value = overview.patientCount.toString(),
            subtitle = "Directorio activo",
            icon = Icons.Default.People,
            gradient = listOf(Color(0xFF4DA3FF), Color(0xFF6C63FF)),
            modifier = Modifier.weight(1f),
        )
        DashboardKpiCard(
            title = "Citas hoy",
            value = todayCount.toString(),
            subtitle = "Agendadas hoy",
            icon = Icons.Default.CalendarMonth,
            gradient = listOf(Color(0xFF8B80F9), Color(0xFF6C63FF)),
            modifier = Modifier.weight(1f),
        )
        DashboardKpiCard(
            title = "Ingresos hoy",
            value = "Bs. %.0f".format(revenueToday),
            subtitle = "Pagos registrados",
            icon = Icons.Default.Payments,
            gradient = listOf(Color(0xFF34C759), Color(0xFF2FA34A)),
            modifier = Modifier.weight(1f),
        )
        DashboardKpiCard(
            title = "Tratamientos",
            value = overview.upcomingAppointmentCount.toString(),
            subtitle = "Pendientes / en curso",
            icon = Icons.Default.MedicalServices,
            gradient = listOf(Color(0xFFFFB020), Color(0xFFFF8A3D)),
            modifier = Modifier.weight(1f),
        )
        DashboardKpiCard(
            title = "Stock",
            value = lowStock.toString(),
            subtitle = "Líneas bajo mínimo",
            icon = Icons.Default.Inventory2,
            gradient = listOf(Color(0xFFFF5A5F), Color(0xFFFF8A8E)),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DashboardKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val scale by animateFloatAsState(if (hovered) 1.02f else 1f, tween(180), label = "kpi")
    Surface(
        modifier =
            modifier
                .fillMaxHeight()
                .scale(scale)
                .hoverable(interaction),
        shape = AppShapes.medium,
        shadowElevation = if (hovered) AppElevations.cardRaised else AppElevations.low,
        tonalElevation = 0.dp,
        color = Color.Transparent,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(gradient))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = AppTypography.Caption, color = Color.White.copy(alpha = 0.9f), maxLines = 1)
                Text(value, style = AppTypography.MetricMedium, color = Color.White, maxLines = 1)
                Text(subtitle, style = AppTypography.Caption, color = Color.White.copy(alpha = 0.85f), maxLines = 1)
            }
        }
    }
}
