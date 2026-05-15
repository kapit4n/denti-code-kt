package com.denticode.kt.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.ui.dashboard.ModernDashboardContent
import com.denticode.kt.ui.navigation.ScreenRoute

@Composable
fun DashboardScreen(
    repo: DentiRepository,
    onNavigate: (ScreenRoute) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    ModernDashboardContent(repo = repo, onNavigate = onNavigate, modifier = modifier)
}
