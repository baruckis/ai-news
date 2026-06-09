package com.baruckis.ainews.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.baruckis.ainews.core.designsystem.theme.AppTheme

/**
 * Wraps preview content in a themed [Surface] with standard padding so each `@Preview`
 * renders on the correct background for the active theme.
 */
@Composable
internal fun ThemedPreviewSurface(content: @Composable () -> Unit) {
    Surface(color = AppTheme.colors.surfacePrimary) {
        Box(modifier = Modifier.padding(AppTheme.grid.m)) {
            content()
        }
    }
}
