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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
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
import com.baruckis.ainews.core.designsystem.R as DesignSystemR

private const val IMAGE_ASPECT_RATIO = 16f / 9f

/** Test tag of the placeholder shown while the article load is in flight. */
const val ARTICLE_DETAIL_LOADING_TAG = "article_detail_loading"

/**
 * Stateless article detail screen: renders [state] and reports every user action through
 * [onIntent], except back navigation which goes straight to the navigation layer via
 * [onBack]. Business logic lives in the ViewModel, never in this composable.
 *
 * @param onBack invoked by the top-bar back button; pass null to hide the button (the
 * two-pane layout keeps the list on screen, so the detail pane needs no back action).
 */
@Composable
fun ArticleDetailScreen(
    state: ArticleDetailState,
    onIntent: (ArticleDetailIntent) -> Unit,
    onBack: (() -> Unit)?,
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
            navigationIcon = { onBack?.let { BackButton(it) } },
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
    val loadingDescription = stringResource(R.string.article_detail_loading_a11y)
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(AppTheme.grid.m)
                .testTag(ARTICLE_DETAIL_LOADING_TAG)
                // The shimmer placeholder is meaningless to screen readers; announce a
                // single loading message instead.
                .semantics { contentDescription = loadingDescription },
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
        // Always rendered: a missing or failed hero image falls back to the branded
        // placeholder instead of collapsing the slot.
        val placeholder = painterResource(DesignSystemR.drawable.img_article_placeholder)
        val context = LocalPlatformContext.current
        AsyncImage(
            // The fillMaxWidth + aspectRatio constraints below give the request an exact
            // target size, so Coil decodes a downsampled bitmap instead of the original.
            model =
                remember(context, article.imageUrl) {
                    ImageRequest
                        .Builder(context)
                        .data(article.imageUrl)
                        .crossfade(true)
                        .build()
                },
            // Decorative (placeholder included): the headline below carries the
            // article's meaning.
            contentDescription = null,
            placeholder = placeholder,
            error = placeholder,
            fallback = placeholder,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(IMAGE_ASPECT_RATIO)
                    .background(AppTheme.colors.surfaceSecondary),
        )
        Column(modifier = Modifier.padding(AppTheme.grid.m)) {
            AppText(
                text = article.title,
                style = AppTheme.typography.titleLarge,
                // TalkBack treats the title as a heading, so users navigating by
                // headings can jump straight to the article.
                modifier = Modifier.semantics { heading() },
            )
            AppText(
                text = "${article.sourceName} · ${rememberFormattedDate(article.publishedAt)}",
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary,
                modifier = Modifier.padding(top = AppTheme.grid.s),
            )
            // The BFF normalizes provider sentinels to null, but blank-guard anyway so a
            // contentless article (free-tier feeds) falls back to its description.
            val body =
                article.content?.takeIf { it.isNotBlank() }
                    ?: article.description?.takeIf { it.isNotBlank() }
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
private fun rememberFormattedDate(publishedAt: Instant): String {
    // Read the locale reactively (and key the cache on it) so a runtime language change
    // reformats the date instead of showing a stale value.
    val locale = LocalConfiguration.current.locales[0]
    return remember(publishedAt, locale) {
        DateTimeFormatter
            .ofPattern("MMM d, yyyy", locale)
            .withZone(ZoneId.systemDefault())
            .format(publishedAt)
    }
}
