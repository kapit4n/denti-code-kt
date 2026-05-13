package com.denticode.kt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.data.MaterialStockRow
import com.denticode.kt.ui.components.cards.AppCard
import com.denticode.kt.ui.navigation.PageHeader
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun InventoryStockScreen(repo: DentiRepository) {
    var rows by remember { mutableStateOf<List<MaterialStockRow>>(emptyList()) }
    LaunchedEffect(Unit) {
        rows = withContext(Dispatchers.IO) { repo.listMaterialStock() }
    }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        PageHeader(
            title = "Stock por consultorio",
            subtitle = "MaterialInventoryLine + TreatmentFacility (mismo esquema que denti-code-desktop).",
            modifier = Modifier.fillMaxWidth(),
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            items(rows, key = { "${it.consultoryName}-${it.facilityCode}" }) { r ->
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        Text(
                            "${r.consultoryName}${r.consultoryShortCode?.let { " ($it)" } ?: ""}",
                            style = AppTypography.CardTitle,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "${r.facilityDisplayName} (${r.facilityCode})",
                            style = AppTypography.Body,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text("Cantidad: ${r.quantity}", style = AppTypography.MetricMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
