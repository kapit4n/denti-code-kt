package com.denticode.kt.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.denticode.kt.ui.navigation.ScreenRoute
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppTypography

@Composable
fun PlaceholderScreen(route: ScreenRoute, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(route.title, style = AppTypography.PageTitle, color = MaterialTheme.colorScheme.onSurface)
        Text(
            "Esta sección está en preparación. Próximamente: ${route.subtitle ?: route.title}.",
            style = AppTypography.Body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
