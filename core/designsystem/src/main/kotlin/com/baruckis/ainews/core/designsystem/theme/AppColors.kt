package com.baruckis.ainews.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic color tokens for the app, named by purpose rather than hue so the same
 * component code works in both light and dark themes.
 */
@Immutable
data class AppColors(
    /** Background of the main content areas (screens, cards in resting state). */
    val surfacePrimary: Color,
    /** Background for raised or grouped surfaces and placeholders. */
    val surfaceSecondary: Color,
    /** Default, high-emphasis text and icon color. */
    val textPrimary: Color,
    /** Lower-emphasis text such as captions and metadata. */
    val textSecondary: Color,
    /** Brand accent used for primary actions and highlights. */
    val accent: Color,
    /** Content color rendered on top of [accent] surfaces. */
    val onAccent: Color,
    /** Hairline borders and dividers. */
    val border: Color,
    /** Error text and error-state emphasis. */
    val error: Color,
)

// Brand accent (#6C5CE7) is shared by both themes so the identity stays constant.
private val BrandAccent = Color(0xFF6C5CE7)

/** Light-theme palette built around the brand accent. */
val LightColors =
    AppColors(
        surfacePrimary = Color(0xFFFFFFFF),
        surfaceSecondary = Color(0xFFF2F2F7),
        textPrimary = Color(0xFF1A1A1A),
        textSecondary = Color(0xFF6B6B70),
        accent = BrandAccent,
        onAccent = Color(0xFFFFFFFF),
        border = Color(0xFFE2E2E8),
        error = Color(0xFFD32F2F),
    )

/** Dark-theme palette built around the brand accent. */
val DarkColors =
    AppColors(
        surfacePrimary = Color(0xFF121212),
        surfaceSecondary = Color(0xFF1E1E20),
        textPrimary = Color(0xFFECECEC),
        textSecondary = Color(0xFFA0A0A6),
        accent = BrandAccent,
        onAccent = Color(0xFFFFFFFF),
        border = Color(0xFF2C2C30),
        error = Color(0xFFEF5350),
    )

/** Provides [AppColors] down the composition; defaults to the light palette. */
val LocalColors = staticCompositionLocalOf { LightColors }
