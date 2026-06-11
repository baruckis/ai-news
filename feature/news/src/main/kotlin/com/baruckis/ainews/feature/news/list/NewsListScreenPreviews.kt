package com.baruckis.ainews.feature.news.list

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import com.baruckis.ainews.feature.news.domain.model.NewsError
import com.baruckis.ainews.feature.news.list.components.previewArticle

// Light and dark previews for every NewsListScreen state, kept in their own file so the
// screen file stays focused on runtime composables.

private val previewArticles =
    listOf(
        previewArticle,
        previewArticle.copy(id = "2", title = "Open models close the gap in coding benchmarks"),
        previewArticle.copy(id = "3", title = "EU agrees on updated AI act guidance", description = null),
    )

@Composable
private fun NewsListScreenPreview(
    state: NewsListState,
    darkTheme: Boolean,
) {
    AppTheme(darkTheme = darkTheme) {
        Surface(color = AppTheme.colors.surfacePrimary) {
            NewsListScreen(state = state, onIntent = {})
        }
    }
}

@Preview(name = "NewsListScreen content light")
@Composable
private fun NewsListScreenContentLightPreview() {
    NewsListScreenPreview(
        state = NewsListState(articles = previewArticles, isLoading = false),
        darkTheme = false,
    )
}

@Preview(name = "NewsListScreen content dark")
@Composable
private fun NewsListScreenContentDarkPreview() {
    NewsListScreenPreview(
        state = NewsListState(articles = previewArticles, isLoading = false),
        darkTheme = true,
    )
}

@Preview(name = "NewsListScreen loading light")
@Composable
private fun NewsListScreenLoadingLightPreview() {
    NewsListScreenPreview(state = NewsListState(isLoading = true), darkTheme = false)
}

@Preview(name = "NewsListScreen loading dark")
@Composable
private fun NewsListScreenLoadingDarkPreview() {
    NewsListScreenPreview(state = NewsListState(isLoading = true), darkTheme = true)
}

@Preview(name = "NewsListScreen error light")
@Composable
private fun NewsListScreenErrorLightPreview() {
    NewsListScreenPreview(
        state = NewsListState(isLoading = false, error = NewsError.Network),
        darkTheme = false,
    )
}

@Preview(name = "NewsListScreen error dark")
@Composable
private fun NewsListScreenErrorDarkPreview() {
    NewsListScreenPreview(
        state = NewsListState(isLoading = false, error = NewsError.Network),
        darkTheme = true,
    )
}

@Preview(name = "NewsListScreen empty light")
@Composable
private fun NewsListScreenEmptyLightPreview() {
    NewsListScreenPreview(state = NewsListState(isLoading = false), darkTheme = false)
}

@Preview(name = "NewsListScreen empty dark")
@Composable
private fun NewsListScreenEmptyDarkPreview() {
    NewsListScreenPreview(state = NewsListState(isLoading = false), darkTheme = true)
}
