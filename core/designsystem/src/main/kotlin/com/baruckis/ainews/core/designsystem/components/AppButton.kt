package com.baruckis.ainews.core.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.baruckis.ainews.core.designsystem.theme.AppTheme

/**
 * Primary action button of the design system. Uses the accent color, the button shape
 * token and the label typography so every call site stays visually consistent.
 *
 * @param onClick invoked when the enabled button is tapped.
 * @param enabled whether the button reacts to input.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = AppTheme.shapes.button,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.accent,
                contentColor = AppTheme.colors.onAccent,
            ),
        contentPadding =
            PaddingValues(
                horizontal = AppTheme.grid.l,
                vertical = AppTheme.grid.s,
            ),
    ) {
        AppText(
            text = text,
            style = AppTheme.typography.label,
            color = AppTheme.colors.onAccent,
        )
    }
}

@Preview(name = "AppButton light")
@Composable
private fun AppButtonLightPreview() {
    AppTheme(darkTheme = false) {
        ThemedPreviewSurface {
            AppButton(text = "Try again", onClick = {})
        }
    }
}

@Preview(name = "AppButton dark")
@Composable
private fun AppButtonDarkPreview() {
    AppTheme(darkTheme = true) {
        ThemedPreviewSurface {
            AppButton(text = "Try again", onClick = {})
        }
    }
}
