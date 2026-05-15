package com.denticode.kt.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class SemanticColors(
    val success: Color,
    val warning: Color,
    val info: Color,
    val border: Color,
    val cardBackground: Color,
    val sidebarBackground: Color,
)

object AppColors {
    /** Paleta premium Denti-Code (modo claro). */
    private val primary = Color(0xFF6C63FF)
    private val onPrimary = Color(0xFFFFFFFF)
    private val primaryContainer = Color(0xFFE8E6FF)
    private val onPrimaryContainer = Color(0xFF1E1A4A)

    private val secondary = Color(0xFF8B80F9)
    private val onSecondary = Color(0xFFFFFFFF)
    private val secondaryContainer = Color(0xFFF0EEFF)
    private val onSecondaryContainer = Color(0xFF2A2540)

    private val backgroundLight = Color(0xFFF5F7FB)
    private val backgroundDark = Color(0xFF121318)

    private val surfaceLight = Color(0xFFFFFFFF)
    private val surfaceDark = Color(0xFF1C1D22)

    private val surfaceVariantLight = Color(0xFFE5E8EF)
    private val surfaceVariantDark = Color(0xFF3A3C45)

    private val successLight = Color(0xFF34C759)
    private val successDark = Color(0xFF5CDE7A)

    private val warningLight = Color(0xFFFFB020)
    private val warningDark = Color(0xFFFFD28A)

    private val errorLight = Color(0xFFFF5A5F)
    private val errorDark = Color(0xFFFF8A8E)

    private val infoLight = Color(0xFF4DA3FF)
    private val infoDark = Color(0xFF8FC4FF)

    private val textPrimaryLight = Color(0xFF1F2937)
    private val textSecondaryLight = Color(0xFF6B7280)

    private val borderLight = Color(0xFFE1E4EC)
    private val borderDark = Color(0xFF3F424C)

    private val cardLight = Color(0xFFFFFFFF)
    private val cardDark = Color(0xFF23252C)

    private val sidebarLight = Color(0xFFF0F2FA)
    private val sidebarDark = Color(0xFF1A1C24)

    fun lightSemantic(): SemanticColors =
        SemanticColors(
            success = successLight,
            warning = warningLight,
            info = infoLight,
            border = borderLight,
            cardBackground = cardLight,
            sidebarBackground = sidebarLight,
        )

    fun darkSemantic(): SemanticColors =
        SemanticColors(
            success = successDark,
            warning = warningDark,
            info = infoDark,
            border = borderDark,
            cardBackground = cardDark,
            sidebarBackground = sidebarDark,
        )

    fun lightColorScheme() =
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = infoLight,
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFDCEBFF),
            onTertiaryContainer = Color(0xFF0F2A44),
            error = errorLight,
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFE5E6),
            onErrorContainer = Color(0xFF5C0A0D),
            background = backgroundLight,
            onBackground = textPrimaryLight,
            surface = surfaceLight,
            onSurface = textPrimaryLight,
            surfaceVariant = surfaceVariantLight,
            onSurfaceVariant = textSecondaryLight,
            outline = borderLight,
            outlineVariant = Color(0xFFD1D5DB),
            scrim = Color(0xFF000000),
            inverseSurface = Color(0xFF2D2F36),
            inverseOnSurface = Color(0xFFF3F4F8),
            inversePrimary = secondary,
            surfaceTint = primary,
        )

    fun darkColorScheme() =
        darkColorScheme(
            primary = Color(0xFFB8B0FF),
            onPrimary = Color(0xFF1E1A4A),
            primaryContainer = Color(0xFF3D3480),
            onPrimaryContainer = Color(0xFFE8E6FF),
            secondary = Color(0xFFC9C2FF),
            onSecondary = Color(0xFF2A2540),
            secondaryContainer = Color(0xFF4A4370),
            onSecondaryContainer = Color(0xFFF0EEFF),
            tertiary = infoDark,
            onTertiary = Color(0xFF0F2A44),
            tertiaryContainer = Color(0xFF284A72),
            onTertiaryContainer = Color(0xFFDCEBFF),
            error = errorDark,
            onError = Color(0xFF5C0A0D),
            errorContainer = Color(0xFF8C1D18),
            onErrorContainer = Color(0xFFFFE5E6),
            background = backgroundDark,
            onBackground = Color(0xFFE6E8EF),
            surface = surfaceDark,
            onSurface = Color(0xFFE6E8EF),
            surfaceVariant = surfaceVariantDark,
            onSurfaceVariant = Color(0xFFB6BAC7),
            outline = borderDark,
            outlineVariant = Color(0xFF4B4F5A),
            scrim = Color(0xFF000000),
            inverseSurface = Color(0xFFE6E8EF),
            inverseOnSurface = Color(0xFF2D2F36),
            inversePrimary = primary,
            surfaceTint = Color(0xFFB8B0FF),
        )
}
