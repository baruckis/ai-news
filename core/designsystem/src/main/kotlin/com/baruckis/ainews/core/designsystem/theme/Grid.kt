package com.baruckis.ainews.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale used for padding, margins and gaps. Components reference these tokens
 * instead of hardcoding `dp` values so spacing stays consistent across the app.
 */
@Immutable
data class Grid(
    /** Extra-small spacing (4dp) for tight gaps and hairlines. */
    val xs: Dp = 4.dp,
    /** Small spacing (8dp) between closely related elements. */
    val s: Dp = 8.dp,
    /** Medium spacing (16dp), the default content padding. */
    val m: Dp = 16.dp,
    /** Large spacing (24dp) between distinct groups. */
    val l: Dp = 24.dp,
    /** Extra-large spacing (32dp) for section separation. */
    val xl: Dp = 32.dp,
)

/** Shared default spacing scale; a single stable instance avoids per-recomposition allocation. */
val DefaultGrid = Grid()

/** Provides [Grid] down the composition. */
val LocalGrid = staticCompositionLocalOf { DefaultGrid }
