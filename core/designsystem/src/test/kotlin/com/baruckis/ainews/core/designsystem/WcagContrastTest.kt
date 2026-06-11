package com.baruckis.ainews.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.baruckis.ainews.core.designsystem.theme.AppColors
import com.baruckis.ainews.core.designsystem.theme.DarkColors
import com.baruckis.ainews.core.designsystem.theme.LightColors
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * WCAG 2.1 colour-contrast gate for the design-system tokens: every text-on-surface pair
 * the app actually renders must reach AA for normal text (4.5:1). Runs in both palettes,
 * so a future token tweak that breaks readability fails the build instead of shipping.
 */
class WcagContrastTest {
    /** WCAG AA minimum contrast for normal-size text. */
    private val aaNormalText = 4.5

    /** Contrast ratio per WCAG: (L_lighter + 0.05) / (L_darker + 0.05). */
    private fun contrastRatio(
        foreground: Color,
        background: Color,
    ): Double {
        val lighter = maxOf(foreground.luminance(), background.luminance()).toDouble()
        val darker = minOf(foreground.luminance(), background.luminance()).toDouble()
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun assertReadable(
        palette: String,
        pair: String,
        foreground: Color,
        background: Color,
    ) {
        val ratio = contrastRatio(foreground, background)
        assertTrue(
            "$palette $pair contrast is ${"%.2f".format(ratio)}:1, below the AA minimum of $aaNormalText:1",
            ratio >= aaNormalText,
        )
    }

    private fun assertPaletteReadable(
        name: String,
        colors: AppColors,
    ) {
        assertReadable(name, "textPrimary on surfacePrimary", colors.textPrimary, colors.surfacePrimary)
        assertReadable(name, "textPrimary on surfaceSecondary", colors.textPrimary, colors.surfaceSecondary)
        assertReadable(name, "textSecondary on surfacePrimary", colors.textSecondary, colors.surfacePrimary)
        assertReadable(name, "textSecondary on surfaceSecondary", colors.textSecondary, colors.surfaceSecondary)
        assertReadable(name, "onAccent on accent", colors.onAccent, colors.accent)
        assertReadable(name, "error on surfacePrimary", colors.error, colors.surfacePrimary)
    }

    @Test
    fun `light palette meets WCAG AA for normal text`() = assertPaletteReadable("light", LightColors)

    @Test
    fun `dark palette meets WCAG AA for normal text`() = assertPaletteReadable("dark", DarkColors)
}
