package com.denticode.kt.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denticode.kt.ui.navigation.ScreenRoute
import com.denticode.kt.ui.theme.AppTypography

private val DashboardSectionSpacing = 12.dp
private val MainRowMaxHeight = 320.dp
private val LowerRowMaxHeight = 240.dp

@Composable
fun ResponsiveDashboardGrid(
    todayRows: List<TodayAppointmentUi>,
    activityItems: List<ActivityFeedItem>,
    revenueValues: List<Float>,
    revenueTotalLabel: String,
    revenueDeltaLabel: String,
    donutSlices: List<DonutSlice>,
    alerts: List<DashboardAlertUi>,
    onNavigate: (ScreenRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val layout = when {
            maxWidth >= 1200.dp -> DashboardGridLayout.ThreeColumn
            maxWidth >= 800.dp -> DashboardGridLayout.TwoColumn
            else -> DashboardGridLayout.SingleColumn
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(DashboardSectionSpacing),
        ) {
            when (layout) {
                DashboardGridLayout.ThreeColumn ->
                    Row(
                        modifier = Modifier.weight(0.58f).fillMaxWidth().heightIn(max = MainRowMaxHeight),
                        horizontalArrangement = Arrangement.spacedBy(DashboardSectionSpacing),
                    ) {
                        DashboardAppointmentsCard(
                            rows = todayRows,
                            onViewCalendar = { onNavigate(ScreenRoute.Appointments) },
                            onViewAll = { onNavigate(ScreenRoute.Appointments) },
                            modifier = Modifier.weight(45f),
                        )
                        DashboardActivityCard(
                            items = activityItems,
                            modifier = Modifier.weight(25f),
                        )
                        DashboardRevenueCard(
                            weekTotalLabel = revenueTotalLabel,
                            deltaLabel = revenueDeltaLabel,
                            values = revenueValues,
                            modifier = Modifier.weight(30f),
                        )
                    }
                DashboardGridLayout.TwoColumn ->
                    Column(
                        modifier = Modifier.weight(0.58f).fillMaxWidth().heightIn(max = MainRowMaxHeight + 12.dp + MainRowMaxHeight),
                        verticalArrangement = Arrangement.spacedBy(DashboardSectionSpacing),
                    ) {
                        DashboardAppointmentsCard(
                            rows = todayRows,
                            onViewCalendar = { onNavigate(ScreenRoute.Appointments) },
                            onViewAll = { onNavigate(ScreenRoute.Appointments) },
                            modifier = Modifier.fillMaxWidth().heightIn(max = MainRowMaxHeight),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(max = MainRowMaxHeight),
                            horizontalArrangement = Arrangement.spacedBy(DashboardSectionSpacing),
                        ) {
                            DashboardActivityCard(items = activityItems, modifier = Modifier.weight(1f))
                            DashboardRevenueCard(
                                weekTotalLabel = revenueTotalLabel,
                                deltaLabel = revenueDeltaLabel,
                                values = revenueValues,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                DashboardGridLayout.SingleColumn ->
                    Column(
                        modifier = Modifier.weight(0.58f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(DashboardSectionSpacing),
                    ) {
                        DashboardAppointmentsCard(
                            rows = todayRows,
                            onViewCalendar = { onNavigate(ScreenRoute.Appointments) },
                            onViewAll = { onNavigate(ScreenRoute.Appointments) },
                            modifier = Modifier.fillMaxWidth().heightIn(max = MainRowMaxHeight),
                        )
                        DashboardActivityCard(
                            items = activityItems,
                            modifier = Modifier.fillMaxWidth().heightIn(max = MainRowMaxHeight),
                        )
                        DashboardRevenueCard(
                            weekTotalLabel = revenueTotalLabel,
                            deltaLabel = revenueDeltaLabel,
                            values = revenueValues,
                            modifier = Modifier.fillMaxWidth().heightIn(max = MainRowMaxHeight),
                        )
                    }
            }
            Row(
                modifier = Modifier.weight(0.42f).fillMaxWidth().heightIn(max = LowerRowMaxHeight),
                horizontalArrangement = Arrangement.spacedBy(DashboardSectionSpacing),
            ) {
                DashboardStatusCard(slices = donutSlices, modifier = Modifier.weight(1f))
                DashboardAlertsCard(alerts = alerts, modifier = Modifier.weight(1f))
            }
        }
    }
}

private enum class DashboardGridLayout {
    ThreeColumn,
    TwoColumn,
    SingleColumn,
}

@Composable
fun DashboardStatusBadge(status: AppointmentDisplayStatus, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = status.color.copy(alpha = 0.14f),
    ) {
        Text(
            status.labelEs,
            style = AppTypography.Caption,
            color = status.color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}
