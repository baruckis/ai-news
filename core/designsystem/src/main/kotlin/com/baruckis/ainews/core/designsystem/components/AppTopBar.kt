package com.baruckis.ainews.core.designsystem.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.baruckis.ainews.core.designsystem.theme.AppTheme

/**
 * App-wide top bar showing a [title] and an optional [navigationIcon] slot (e.g. a back
 * button). Colors come from the surface and text tokens so it matches the active theme.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
) {
    TopAppBar(
        title = {
            AppText(text = title, style = AppTheme.typography.titleMedium)
        },
        modifier = modifier,
        navigationIcon = navigationIcon,
        colors = appTopBarColors(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun appTopBarColors(): TopAppBarColors =
    TopAppBarDefaults.topAppBarColors(
        containerColor = AppTheme.colors.surfacePrimary,
        titleContentColor = AppTheme.colors.textPrimary,
        navigationIconContentColor = AppTheme.colors.textPrimary,
    )

@Preview(name = "AppTopBar light")
@Composable
private fun AppTopBarLightPreview() {
    AppTheme(darkTheme = false) {
        ThemedPreviewSurface {
            AppTopBar(title = "AI News")
        }
    }
}

@Preview(name = "AppTopBar dark")
@Composable
private fun AppTopBarDarkPreview() {
    AppTheme(darkTheme = true) {
        ThemedPreviewSurface {
            AppTopBar(title = "AI News")
        }
    }
}
