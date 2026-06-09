package com.baruckis.ainews.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.baruckis.ainews.core.designsystem.theme.AppShapes
import com.baruckis.ainews.core.designsystem.theme.DarkColors
import com.baruckis.ainews.core.designsystem.theme.DefaultTypography
import com.baruckis.ainews.core.designsystem.theme.Grid
import com.baruckis.ainews.core.designsystem.theme.LightColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** Verifies the brand decisions encoded in the design tokens. */
class ThemeTokensTest {
    private val brandAccent = Color(0xFF6C5CE7)

    @Test
    fun `brand accent is the same in light and dark themes`() {
        assertEquals(brandAccent, LightColors.accent)
        assertEquals(brandAccent, DarkColors.accent)
    }

    @Test
    fun `light and dark palettes differ`() {
        assertNotEquals(LightColors, DarkColors)
        assertNotEquals(LightColors.surfacePrimary, DarkColors.surfacePrimary)
        assertNotEquals(LightColors.textPrimary, DarkColors.textPrimary)
    }

    @Test
    fun `palette copy overrides a single token`() {
        val recolored = LightColors.copy(accent = DarkColors.error)
        assertEquals(DarkColors.error, recolored.accent)
        assertEquals(LightColors.surfacePrimary, recolored.surfacePrimary)
    }

    @Test
    fun `spacing scale increases monotonically`() {
        val grid = Grid()
        assertEquals(4.dp, grid.xs)
        assertEquals(8.dp, grid.s)
        assertEquals(16.dp, grid.m)
        assertEquals(24.dp, grid.l)
        assertEquals(32.dp, grid.xl)
    }

    @Test
    fun `default typography and shapes are populated`() {
        assertNotEquals(DefaultTypography.titleLarge, DefaultTypography.body)
        assertNotEquals(AppShapes().card, AppShapes().button)
    }
}
