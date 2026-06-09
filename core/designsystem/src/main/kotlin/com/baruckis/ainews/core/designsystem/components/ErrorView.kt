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
 * Full-width error state showing a [message] and a retry action. Used whenever a screen
 * fails to load and the user can try again.
 *
 * @param onRetry invoked when the retry button is tapped.
 * @param retryText label for the retry button.
 */
@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryText: String = "Try again",
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(AppTheme.grid.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.grid.m, Alignment.CenterVertically),
    ) {
        AppText(
            text = message,
            style = AppTheme.typography.body,
            color = AppTheme.colors.error,
            modifier = Modifier.fillMaxWidth(),
        )
        AppButton(text = retryText, onClick = onRetry)
    }
}

@Preview(name = "ErrorView light")
@Composable
private fun ErrorViewLightPreview() {
    AppTheme(darkTheme = false) {
        ThemedPreviewSurface {
            ErrorView(message = "Something went wrong while loading the news.", onRetry = {})
        }
    }
}

@Preview(name = "ErrorView dark")
@Composable
private fun ErrorViewDarkPreview() {
    AppTheme(darkTheme = true) {
        ThemedPreviewSurface {
            ErrorView(message = "Something went wrong while loading the news.", onRetry = {})
        }
    }
}
