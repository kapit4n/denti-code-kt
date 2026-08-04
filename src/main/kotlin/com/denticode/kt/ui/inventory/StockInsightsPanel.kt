@file:OptIn(ExperimentalMaterial3Api::class)

package com.denticode.kt.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.denticode.kt.data.StockStatus
import com.denticode.kt.ui.charts.BarChartEntry
import com.denticode.kt.ui.charts.EnterpriseBarChart
import com.denticode.kt.ui.theme.AppElevations
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

private val StockSuccess = Color(0xFF22C55E)
private val StockDanger = Color(0xFFEF4444)
private val StockInfo = Color(0xFF2563EB)
private val StockWarning = Color(0xFFF59E0B)

@Composable
fun StockInsightsRow(
    insights: StockInsights,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StockSummaryCard(
            title = "Entradas (30 días)",
            value = "+${insights.last30dEntries}",
            icon = Icons.Default.ArrowUpward,
            iconBackground = StockSuccess.copy(alpha = 0.12f),
            iconTint = StockSuccess,
            modifier = Modifier.weight(1f),
        )
        StockSummaryCard(
            title = "Salidas (30 días)",
            value = "-${insights.last30dExits}",
            icon = Icons.Default.ArrowDownward,
            iconBackground = StockDanger.copy(alpha = 0.12f),
            iconTint = StockDanger,
            modifier = Modifier.weight(1f),
        )
        StockSummaryCard(
            title = "Movimientos (30 días)",
            value = insights.last30dMovementsCount.toString(),
            icon = Icons.Default.CompareArrows,
            iconBackground = StockInfo.copy(alpha = 0.12f),
            iconTint = StockInfo,
            modifier = Modifier.weight(1f),
        )
        StockSummaryCard(
            title = "Necesitan reposición",
            value = insights.reorderCount.toString(),
            icon = Icons.Default.ShoppingCart,
            iconBackground = StockWarning.copy(alpha = 0.14f),
            iconTint = StockWarning,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun StockInsightsPanel(
    insights: StockInsights,
    categoryStats: List<StockCategoryStat>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxHeight().widthIn(min = 320.dp, max = 380.dp),
        shape = AppShapes.medium,
        color = Color.White,
        shadowElevation = AppElevations.cardRest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            item {
                Text(
                    "Análisis de stock",
                    style = AppTypography.SectionTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    val entries =
                        categoryStats.map { stat ->
                            val color = categoryColors(stat.categoryKey).second
                            BarChartEntry(
                                label = stat.label,
                                value = stat.units.toFloat(),
                                color = color,
                            )
                        }
                    if (entries.isEmpty()) {
                        Text(
                            "Sin datos de categorías.",
                            style = AppTypography.BodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        EnterpriseBarChart(
                            title = "Unidades por categoría",
                            entries = entries,
                            chartHeight = 180.dp,
                        )
                        Text(
                            "Entradas: +${insights.last30dEntries} · Salidas: -${insights.last30dExits} (últimos 30 días)",
                            style = AppTypography.Caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text(
                        "Reposición sugerida",
                        style = AppTypography.CardTitle,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    )
                    Text(
                        if (insights.reorderCount == 0) "Todo en orden" else "${insights.reorderCount} líneas",
                        style = AppTypography.Caption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (insights.reorderLines.isEmpty()) {
                item {
                    Text(
                        "Ningún insumo requiere reposición.",
                        style = AppTypography.BodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(insights.reorderLines, key = { it.productCode + it.consultoryLabel }) { s ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                s.productName,
                                style = AppTypography.BodySmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            )
                            Text(
                                "Pedir ${s.suggestedOrder}",
                                style = AppTypography.BodySmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                color = if (s.status == StockStatus.OUT) StockDanger else StockWarning,
                            )
                        }
                        Text(
                            "${s.consultoryLabel} · Stock ${s.quantity}/${s.minQuantity} mín. · ${s.categoryLabel}",
                            style = AppTypography.Caption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
