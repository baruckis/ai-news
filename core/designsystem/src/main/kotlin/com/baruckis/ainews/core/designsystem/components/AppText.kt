package com.baruckis.ainews.core.designsystem.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.baruckis.ainews.core.designsystem.theme.AppTheme

/**
 * Text primitive of the design system. Renders [text] with a typography token and a
 * semantic color so callers never reach for raw [TextStyle] values or hex colors.
 *
 * @param style typography token; defaults to the body style.
 * @param color semantic text color; defaults to the primary text color.
 */
@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = AppTheme.typography.body,
    color: Color = AppTheme.colors.textPrimary,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Preview(name = "AppText light")
@Composable
private fun AppTextLightPreview() {
    AppTheme(darkTheme = false) {
        ThemedPreviewSurface {
            AppText(text = "The quick brown fox", style = AppTheme.typography.titleMedium)
        }
    }
}

@Preview(name = "AppText dark")
@Composable
private fun AppTextDarkPreview() {
    AppTheme(darkTheme = true) {
        ThemedPreviewSurface {
            AppText(text = "The quick brown fox", style = AppTheme.typography.titleMedium)
        }
    }
}
