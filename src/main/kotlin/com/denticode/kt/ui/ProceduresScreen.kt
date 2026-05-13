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
import com.denticode.kt.data.ProcedureTypeRow
import com.denticode.kt.ui.components.cards.AppCard
import com.denticode.kt.ui.navigation.PageHeader
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProceduresScreen(repo: DentiRepository) {
    var rows by remember { mutableStateOf<List<ProcedureTypeRow>>(emptyList()) }
    LaunchedEffect(Unit) {
        rows = withContext(Dispatchers.IO) { repo.listProcedureTypes() }
    }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        PageHeader(
            title = "Catálogo clínico",
            subtitle = "ProcedureType — precio estándar y duración por defecto (denti-code).",
            modifier = Modifier.fillMaxWidth(),
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            items(rows, key = { it.id }) { pr ->
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        Text(pr.name, style = AppTypography.CardTitle, color = MaterialTheme.colorScheme.onSurface)
                        pr.description?.let {
                            Text(it, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        val price = pr.standardPrice?.let { formatMoney(it) } ?: "—"
                        val dur = pr.defaultDurationMinutes?.let { formatDuration(it) } ?: "—"
                        Text(
                            "$dur · $price · diente: ${if (pr.requiresToothSpecification) "sí" else "no"}",
                            style = AppTypography.Body,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        pr.category?.let {
                            Text("Categoría: $it", style = AppTypography.Caption, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}
