package com.denticode.kt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.ClinicOverview
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.components.cards.MetricCard
import com.denticode.kt.ui.layout.SectionContainer
import com.denticode.kt.ui.navigation.PageHeader
import com.denticode.kt.ui.navigation.ScreenRoute
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DashboardScreen(
    repo: DentiRepository,
    onNavigate: (ScreenRoute) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var overview by remember { mutableStateOf<ClinicOverview?>(null) }
    LaunchedEffect(Unit) {
        overview = withContext(Dispatchers.IO) { repo.clinicOverview() }
    }
    Column(
        modifier = modifier.fillMaxSize().padding(bottom = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        PageHeader(
            title = "Panel clínico",
            subtitle =
                "Resumen operativo basado en los mismos modelos de dominio que denti-code-desktop " +
                    "(pacientes, doctores, citas, catálogo de procedimientos, stock por consultorio y pagos).",
            modifier = Modifier.fillMaxWidth(),
        )
        SectionContainer(verticalPadding = AppSpacing.sm) {
            Text(
                text = "Métricas rápidas",
                style = AppTypography.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = AppSpacing.sm),
            )
            overview?.let { o ->
                @OptIn(ExperimentalLayoutApi::class)
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val minCard = 200.dp
                    val maxCard = 320.dp
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        MetricCard(
                            label = "Pacientes",
                            value = o.patientCount.toString(),
                            modifier = Modifier.widthIn(min = minCard, max = maxCard),
                        )
                        MetricCard(
                            label = "Doctores activos",
                            value = o.doctorCount.toString(),
                            modifier = Modifier.widthIn(min = minCard, max = maxCard),
                        )
                        MetricCard(
                            label = "Citas registradas",
                            value = o.appointmentCount.toString(),
                            modifier = Modifier.widthIn(min = minCard, max = maxCard),
                        )
                        MetricCard(
                            label = "Citas abiertas",
                            value = o.upcomingAppointmentCount.toString(),
                            secondaryLabel = "Programada / confirmada / en curso",
                            modifier = Modifier.widthIn(min = minCard, max = maxCard),
                        )
                        MetricCard(
                            label = "Total cobrado (histórico)",
                            value = "%.2f".format(o.paymentTotalRecent),
                            secondaryLabel = "Suma de pagos en base local",
                            modifier = Modifier.widthIn(min = minCard, max = maxCard),
                        )
                    }
                }
            } ?: Text("Cargando…", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            AppOutlinedButton(
                text = "Ir a citas",
                onClick = { onNavigate(ScreenRoute.Appointments) },
                modifier = Modifier.padding(top = AppSpacing.md),
            )
        }
    }
}
