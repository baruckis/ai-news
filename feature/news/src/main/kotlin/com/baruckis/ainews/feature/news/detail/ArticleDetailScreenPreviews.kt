package com.baruckis.ainews.feature.news.detail

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import com.baruckis.ainews.core.model.Article
import com.baruckis.ainews.feature.news.domain.model.NewsError
import java.time.Instant

// Light and dark previews for every ArticleDetailScreen state, kept in their own file so
// the screen file stays focused on runtime composables.

private val previewDetailArticle =
    Article(
        id = "1",
        title = "AI breakthrough promises faster on-device inference for everyone",
        description = "Researchers unveiled a compact model architecture.",
        content =
            "Researchers unveiled a compact model architecture that runs state-of-the-art " +
                "inference on ordinary phones, cutting latency and energy use while keeping " +
                "accuracy within a percent of much larger models.",
        imageUrl = null,
        sourceName = "TechWire",
        author = "Jane Doe",
        url = "https://example.com/article",
        publishedAt = Instant.parse("2026-06-01T10:00:00Z"),
    )

@Composable
private fun ArticleDetailScreenPreview(
    state: ArticleDetailState,
    darkTheme: Boolean,
) {
    AppTheme(darkTheme = darkTheme) {
        Surface(color = AppTheme.colors.surfacePrimary) {
            ArticleDetailScreen(state = state, onIntent = {}, onBack = {})
        }
    }
}

@Preview(name = "ArticleDetailScreen content light")
@Composable
private fun ArticleDetailScreenContentLightPreview() {
    ArticleDetailScreenPreview(
        state = ArticleDetailState(article = previewDetailArticle, isLoading = false),
        darkTheme = false,
    )
}

@Preview(name = "ArticleDetailScreen content dark")
@Composable
private fun ArticleDetailScreenContentDarkPreview() {
    ArticleDetailScreenPreview(
        state = ArticleDetailState(article = previewDetailArticle, isLoading = false),
        darkTheme = true,
    )
}

@Preview(name = "ArticleDetailScreen loading light")
@Composable
private fun ArticleDetailScreenLoadingLightPreview() {
    ArticleDetailScreenPreview(state = ArticleDetailState(isLoading = true), darkTheme = false)
}

@Preview(name = "ArticleDetailScreen loading dark")
@Composable
private fun ArticleDetailScreenLoadingDarkPreview() {
    ArticleDetailScreenPreview(state = ArticleDetailState(isLoading = true), darkTheme = true)
}

@Preview(name = "ArticleDetailScreen error light")
@Composable
private fun ArticleDetailScreenErrorLightPreview() {
    ArticleDetailScreenPreview(
        state = ArticleDetailState(isLoading = false, error = NewsError.Network),
        darkTheme = false,
    )
}

@Preview(name = "ArticleDetailScreen error dark")
@Composable
private fun ArticleDetailScreenErrorDarkPreview() {
    ArticleDetailScreenPreview(
        state = ArticleDetailState(isLoading = false, error = NewsError.Network),
        darkTheme = true,
    )
}
