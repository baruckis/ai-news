package com.baruckis.ainews.feature.news.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImage
import com.baruckis.ainews.core.designsystem.components.AppButton
import com.baruckis.ainews.core.designsystem.components.AppText
import com.baruckis.ainews.core.designsystem.components.AppTopBar
import com.baruckis.ainews.core.designsystem.components.ErrorView
import com.baruckis.ainews.core.designsystem.components.NewsCardSkeleton
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import com.baruckis.ainews.core.model.Article
import com.baruckis.ainews.feature.news.R
import com.baruckis.ainews.feature.news.domain.model.NewsError
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val IMAGE_ASPECT_RATIO = 16f / 9f

/** Test tag of the placeholder shown while the article load is in flight. */
const val ARTICLE_DETAIL_LOADING_TAG = "article_detail_loading"

/**
 * Stateless article detail screen: renders [state] and reports every user action through
 * [onIntent], except back navigation which goes straight to the navigation layer via
 * [onBack]. Business logic lives in the ViewModel, never in this composable.
 */
@Composable
fun ArticleDetailScreen(
    state: ArticleDetailState,
    onIntent: (ArticleDetailIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppTheme.colors.surfacePrimary),
    ) {
        AppTopBar(
            title = stringResource(R.string.article_detail_title),
            navigationIcon = { BackButton(onBack) },
        )
        when {
            state.isLoading -> LoadingPlaceholder()
            state.error != null && state.article == null ->
                ErrorContent(
                    error = state.error,
                    onRetry = { state.articleId?.let { onIntent(ArticleDetailIntent.Load(it)) } },
                )
            state.article != null ->
                ArticleContent(
                    article = state.article,
                    onReadAtSource = { onIntent(ArticleDetailIntent.OpenSource) },
                )
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.article_detail_back),
            tint = AppTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun LoadingPlaceholder() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(AppTheme.grid.m)
                .testTag(ARTICLE_DETAIL_LOADING_TAG),
    ) {
        NewsCardSkeleton()
    }
}

@Composable
private fun ErrorContent(
    error: NewsError,
    onRetry: () -> Unit,
) {
    ErrorView(
        message =
            when (error) {
                NewsError.Network -> stringResource(R.string.article_detail_error_network)
                NewsError.Unknown -> stringResource(R.string.article_detail_error_unknown)
            },
        onRetry = onRetry,
    )
}

@Composable
private fun ArticleContent(
    article: Article,
    onReadAtSource: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        if (article.imageUrl != null) {
            AsyncImage(
                model = article.imageUrl,
                // Decorative: the headline below carries the article's meaning.
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(IMAGE_ASPECT_RATIO)
                        .background(AppTheme.colors.surfaceSecondary),
            )
        }
        Column(modifier = Modifier.padding(AppTheme.grid.m)) {
            AppText(
                text = article.title,
                style = AppTheme.typography.titleLarge,
            )
            AppText(
                text = "${article.sourceName} · ${rememberFormattedDate(article.publishedAt)}",
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary,
                modifier = Modifier.padding(top = AppTheme.grid.s),
            )
            val body = article.content ?: article.description
            if (body != null) {
                AppText(
                    text = body,
                    style = AppTheme.typography.body,
                    modifier = Modifier.padding(top = AppTheme.grid.m),
                )
            }
            AppButton(
                text = stringResource(R.string.article_detail_read_at_source),
                onClick = onReadAtSource,
                modifier = Modifier.padding(top = AppTheme.grid.l),
            )
        }
    }
}

/** Formats [publishedAt] like "Jun 1, 2026" in the user's locale and time zone. */
@Composable
private fun rememberFormattedDate(publishedAt: Instant): String =
    remember(publishedAt) {
        DateTimeFormatter
            .ofPattern("MMM d, yyyy", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
            .format(publishedAt)
    }
