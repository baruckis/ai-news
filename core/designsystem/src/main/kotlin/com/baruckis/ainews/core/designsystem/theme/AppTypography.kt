package com.baruckis.ainews.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Semantic typography tokens. Each style is referenced by role (title, body, caption…)
 * so screens never hardcode font sizes or weights.
 */
@Immutable
data class AppTypography(
    /** Largest title, e.g. a screen or article headline. */
    val titleLarge: TextStyle,
    /** Section and card titles. */
    val titleMedium: TextStyle,
    /** Default running text. */
    val body: TextStyle,
    /** Small supporting text such as source and date metadata. */
    val caption: TextStyle,
    /** Emphasis text for buttons and chips. */
    val label: TextStyle,
)

/** Default type scale shared by both themes. */
val DefaultTypography =
    AppTypography(
        titleLarge = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
        titleMedium = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
        body = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
        caption = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
        label = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    )

/** Provides [AppTypography] down the composition. */
val LocalTypography = staticCompositionLocalOf { DefaultTypography }
