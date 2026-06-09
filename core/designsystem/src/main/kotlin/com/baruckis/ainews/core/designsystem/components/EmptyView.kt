package com.baruckis.ainews.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.baruckis.ainews.core.designsystem.theme.AppTheme

/**
 * Full-width empty state shown when a screen loaded successfully but has no content,
 * presenting a [title] and an explanatory [message].
 */
@Composable
fun EmptyView(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(AppTheme.grid.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.grid.s, Alignment.CenterVertically),
    ) {
        AppText(text = title, style = AppTheme.typography.titleMedium)
        AppText(
            text = message,
            style = AppTheme.typography.body,
            color = AppTheme.colors.textSecondary,
        )
    }
}

@Preview(name = "EmptyView light")
@Composable
private fun EmptyViewLightPreview() {
    AppTheme(darkTheme = false) {
        ThemedPreviewSurface {
            EmptyView(title = "No news yet", message = "Check back later for fresh AI stories.")
        }
    }
}

@Preview(name = "EmptyView dark")
@Composable
private fun EmptyViewDarkPreview() {
    AppTheme(darkTheme = true) {
        ThemedPreviewSurface {
            EmptyView(title = "No news yet", message = "Check back later for fresh AI stories.")
        }
    }
}
