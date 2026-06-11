package com.baruckis.ainews.feature.news.list.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import com.baruckis.ainews.core.model.ArticleSummary
import java.time.Instant

// Light and dark previews for NewsCard plus the sample article shared with the screen
// previews, kept in their own file so the component file stays focused on runtime code.

internal val previewArticle =
    ArticleSummary(
        id = "1",
        title = "AI breakthrough promises faster on-device inference for everyone",
        description =
            "Researchers unveiled a compact model architecture that runs " +
                "state-of-the-art inference on ordinary phones.",
        imageUrl = null,
        sourceName = "TechWire",
        publishedAt = Instant.parse("2026-06-01T10:00:00Z"),
    )

@Preview(name = "NewsCard light")
@Composable
private fun NewsCardLightPreview() {
    AppTheme(darkTheme = false) {
        Surface(color = AppTheme.colors.surfacePrimary) {
            NewsCard(article = previewArticle, onClick = {}, modifier = Modifier.padding(AppTheme.grid.m))
        }
    }
}

@Preview(name = "NewsCard dark")
@Composable
private fun NewsCardDarkPreview() {
    AppTheme(darkTheme = true) {
        Surface(color = AppTheme.colors.surfacePrimary) {
            NewsCard(article = previewArticle, onClick = {}, modifier = Modifier.padding(AppTheme.grid.m))
        }
    }
}
