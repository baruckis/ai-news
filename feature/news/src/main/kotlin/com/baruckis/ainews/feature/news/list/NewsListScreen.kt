package com.baruckis.ainews.feature.news.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.baruckis.ainews.core.designsystem.components.AppTopBar
import com.baruckis.ainews.core.designsystem.components.EmptyView
import com.baruckis.ainews.core.designsystem.components.ErrorView
import com.baruckis.ainews.core.designsystem.components.NewsCardSkeleton
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import com.baruckis.ainews.core.model.ArticleSummary
import com.baruckis.ainews.feature.news.R
import com.baruckis.ainews.feature.news.domain.model.NewsError
import com.baruckis.ainews.feature.news.list.components.NewsCard

private const val SKELETON_COUNT = 6

/** Test tag of the skeleton list shown while the initial load is in flight. */
const val NEWS_LIST_LOADING_TAG = "news_list_loading"

/** Test tag of the article list, used to drive scrolling from UI tests. */
const val NEWS_LIST_TAG = "news_list"

/**
 * Stateless news list screen: renders [state] and reports every user action through
 * [onIntent]. All four states (loading, content, error, empty) are handled here; business
 * logic lives in the ViewModel, never in this composable.
 *
 * @param snackbarHostState host for transient messages (e.g. the offline notice); owned
 * by the caller so effects collected at the navigation layer can show snackbars here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsListScreen(
    state: NewsListState,
    onIntent: (NewsListIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppTheme.colors.surfacePrimary),
    ) {
        AppTopBar(title = stringResource(R.string.news_list_title))
        Box(modifier = Modifier.fillMaxSize()) {
            RefreshableContent(state = state, onIntent = onIntent)
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefreshableContent(
    state: NewsListState,
    onIntent: (NewsListIntent) -> Unit,
) {
    val pullState = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onIntent(NewsListIntent.Refresh) },
        state = pullState,
        modifier = Modifier.fillMaxSize(),
        indicator = {
            // Themed indicator: the design-system accent on a secondary surface instead
            // of the default Material colors.
            PullToRefreshDefaults.Indicator(
                state = pullState,
                isRefreshing = state.isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = AppTheme.colors.surfaceSecondary,
                color = AppTheme.colors.accent,
            )
        },
    ) {
        when {
            state.isLoading -> LoadingSkeletons()
            state.error != null && state.articles.isEmpty() ->
                ErrorContent(error = state.error, onRetry = { onIntent(NewsListIntent.Retry) })
            state.isEmpty -> EmptyContent()
            else -> ArticleList(articles = state.articles, onIntent = onIntent)
        }
    }
}

@Composable
private fun LoadingSkeletons() {
    val loadingDescription = stringResource(R.string.news_list_loading_a11y)
    // Scrollable so the pull-to-refresh gesture also works while skeletons are shown.
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppTheme.grid.m)
                .testTag(NEWS_LIST_LOADING_TAG)
                // The shimmer blocks are meaningless to screen readers; announce a single
                // loading message for the whole skeleton list instead.
                .semantics { contentDescription = loadingDescription },
        verticalArrangement = Arrangement.spacedBy(AppTheme.grid.m),
    ) {
        repeat(SKELETON_COUNT) {
            NewsCardSkeleton()
        }
    }
}

@Composable
private fun ErrorContent(
    error: NewsError,
    onRetry: () -> Unit,
) {
    // Scrollable so the pull-to-refresh gesture also works from the error state.
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
    ) {
        ErrorView(
            message =
                when (error) {
                    NewsError.Network -> stringResource(R.string.news_list_error_network)
                    NewsError.Unknown -> stringResource(R.string.news_list_error_unknown)
                },
            onRetry = onRetry,
        )
    }
}

@Composable
private fun EmptyContent() {
    // Scrollable so the pull-to-refresh gesture also works from the empty state.
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
    ) {
        EmptyView(
            title = stringResource(R.string.news_list_empty_title),
            message = stringResource(R.string.news_list_empty_message),
        )
    }
}

@Composable
private fun ArticleList(
    articles: List<ArticleSummary>,
    onIntent: (NewsListIntent) -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .testTag(NEWS_LIST_TAG),
        contentPadding = PaddingValues(AppTheme.grid.m),
        verticalArrangement = Arrangement.spacedBy(AppTheme.grid.m),
    ) {
        items(items = articles, key = { it.id }) { article ->
            NewsCard(
                article = article,
                onClick = { onIntent(NewsListIntent.ArticleClicked(article.id)) },
                // Fade newly appearing cards in and animate reorders after a refresh.
                modifier = Modifier.animateItem(),
            )
        }
    }
}
