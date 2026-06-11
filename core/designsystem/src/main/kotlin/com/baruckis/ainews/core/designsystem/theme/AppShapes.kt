package com.baruckis.ainews.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Corner-shape tokens for the app's surfaces, named by the component they shape.
 */
@Immutable
data class AppShapes(
    /** Shape for cards and other large content containers. */
    val card: Shape = RoundedCornerShape(16.dp),
    /** Shape for buttons and other interactive controls. */
    val button: Shape = RoundedCornerShape(12.dp),
)

/** Stroke width for hairline borders and dividers (1dp, independent of the spacing scale). */
val HairlineBorderWidth: Dp = 1.dp

/** Minimum touch-target size for interactive components (WCAG / Material accessibility). */
val MinTouchTargetSize: Dp = 48.dp

/** Shared default shape set; a single stable instance avoids per-recomposition allocation. */
val DefaultShapes = AppShapes()

/** Provides [AppShapes] down the composition. */
val LocalShapes = staticCompositionLocalOf { DefaultShapes }
