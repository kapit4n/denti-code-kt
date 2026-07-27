package com.denticode.kt.ui.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.denticode.kt.ui.app.LocalAppMessenger
import com.denticode.kt.ui.components.buttons.AppOutlinedButton
import com.denticode.kt.ui.modifiers.smoothClickable
import com.denticode.kt.ui.theme.AppShapes
import com.denticode.kt.ui.theme.AppSpacing
import com.denticode.kt.ui.theme.AppThemeState
import com.denticode.kt.ui.theme.AppTypography

object SidebarSpec {
    val expandedWidth: Dp = 260.dp
    val collapsedWidth: Dp = 88.dp
}

@Composable
fun AppSidebar(
    navigationState: NavigationState,
    modifier: Modifier = Modifier,
) {
    val targetWidth = if (navigationState.sidebarCollapsed) SidebarSpec.collapsedWidth else SidebarSpec.expandedWidth
    val animatedWidth by
        animateDpAsState(
            targetValue = targetWidth,
            animationSpec = tween(240),
            label = "sidebarWidth",
        )
    val messenger = LocalAppMessenger.current
    Surface(
        modifier =
            modifier
                .width(animatedWidth)
                .fillMaxHeight()
                .border(
                    width = 1.dp,
                    color = AppThemeState.semantic.border.copy(alpha = 0.35f),
                ),
        color = AppThemeState.semantic.sidebarBackground,
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .padding(vertical = AppSpacing.md, horizontal = AppSpacing.sm),
        ) {
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xs, vertical = AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Icon(
                        Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp),
                    )
                    if (!navigationState.sidebarCollapsed) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Denti-Code",
                                style = AppTypography.SectionTitle,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                "Clínica Dental",
                                style = AppTypography.Caption,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                sidebarSections().forEach { section ->
                    if (!navigationState.sidebarCollapsed) {
                        Text(
                            section.title,
                            style = AppTypography.Caption,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = AppSpacing.xs, top = AppSpacing.md, bottom = AppSpacing.xs),
                        )
                    }
                    section.items.forEach { item ->
                        val selected = navigationState.currentRoute == item.route
                        SidebarWideItem(
                            icon = item.icon,
                            label = item.label,
                            selected = selected,
                            collapsed = navigationState.sidebarCollapsed,
                            onClick = { navigationState.navigateTo(item.route) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            if (!navigationState.sidebarCollapsed) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm),
                    shape = AppShapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    shadowElevation = 1.dp,
                ) {
                    Column(Modifier.padding(AppSpacing.md)) {
                        Text("Clínica Demo", style = AppTypography.Body, color = MaterialTheme.colorScheme.onSurface)
                        Text("Sede principal", style = AppTypography.Caption, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            AppOutlinedButton(
                text = if (navigationState.sidebarCollapsed) "…" else "Cerrar sesión",
                onClick = { messenger.showSuccess("Sesión demo — sin cierre real.") },
                modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.xs),
            )
            IconButton(
                onClick = { navigationState.toggleSidebarCollapsed() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector =
                        if (navigationState.sidebarCollapsed) {
                            Icons.AutoMirrored.Outlined.KeyboardArrowRight
                        } else {
                            Icons.AutoMirrored.Outlined.KeyboardArrowLeft
                        },
                    contentDescription = if (navigationState.sidebarCollapsed) "Expandir" else "Contraer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SidebarWideItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    collapsed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg =
        when {
            selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            else -> androidx.compose.ui.graphics.Color.Transparent
        }
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier =
            modifier
                .clip(shape)
                .background(bg)
                .smoothClickable(onClick = onClick)
                .padding(horizontal = AppSpacing.sm, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint =
                if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            modifier = Modifier.size(22.dp),
        )
        if (!collapsed) {
            Text(
                label,
                style = AppTypography.Body,
                color =
                    if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
