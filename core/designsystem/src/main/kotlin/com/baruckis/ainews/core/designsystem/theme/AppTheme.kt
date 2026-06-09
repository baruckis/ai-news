package com.baruckis.ainews.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Entry point for reading design-system tokens inside composables, e.g.
 * `AppTheme.colors.accent`. Backed by the [LocalColors], [LocalTypography], [LocalGrid]
 * and [LocalShapes] composition locals supplied by the [AppTheme] wrapper.
 */
object AppTheme {
    /** Active semantic color palette. */
    val colors: AppColors
        @Composable @ReadOnlyComposable
        get() = LocalColors.current

    /** Active type scale. */
    val typography: AppTypography
        @Composable @ReadOnlyComposable
        get() = LocalTypography.current

    /** Active spacing scale. */
    val grid: Grid
        @Composable @ReadOnlyComposable
        get() = LocalGrid.current

    /** Active corner-shape tokens. */
    val shapes: AppShapes
        @Composable @ReadOnlyComposable
        get() = LocalShapes.current
}

/**
 * Supplies the design-system tokens to [content]. Picks the light or dark palette from
 * [darkTheme] (the system setting by default) and leaves typography, grid and shapes shared.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalColors provides if (darkTheme) DarkColors else LightColors,
        LocalTypography provides DefaultTypography,
        LocalGrid provides DefaultGrid,
        LocalShapes provides DefaultShapes,
        content = content,
    )
}
