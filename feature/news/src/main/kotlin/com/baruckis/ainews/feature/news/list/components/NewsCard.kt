package com.baruckis.ainews.feature.news.list.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.baruckis.ainews.core.designsystem.components.AppText
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import com.baruckis.ainews.core.designsystem.theme.HairlineBorderWidth
import com.baruckis.ainews.core.model.ArticleSummary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.baruckis.ainews.core.designsystem.R as DesignSystemR

private const val IMAGE_ASPECT_RATIO = 16f / 9f
private const val TITLE_MAX_LINES = 2
private const val DESCRIPTION_MAX_LINES = 2

/**
 * List item for one [ArticleSummary]: lead image (when available), headline, optional
 * description and a source + date caption. Tapping the card invokes [onClick].
 */
@Composable
fun NewsCard(
    article: ArticleSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = AppTheme.colors.surfacePrimary,
        shape = AppTheme.shapes.card,
        border = BorderStroke(HairlineBorderWidth, AppTheme.colors.border),
    ) {
        Column {
            ArticleImage(imageUrl = article.imageUrl)
            Column(modifier = Modifier.padding(AppTheme.grid.m)) {
                AppText(
                    text = article.title,
                    style = AppTheme.typography.titleMedium,
                    maxLines = TITLE_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
                article.description?.let { description ->
                    AppText(
                        text = description,
                        style = AppTheme.typography.body,
                        color = AppTheme.colors.textSecondary,
                        maxLines = DESCRIPTION_MAX_LINES,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = AppTheme.grid.xs),
                    )
                }
                AppText(
                    text = "${article.sourceName} · ${rememberFormattedDate(article.publishedAt)}",
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = AppTheme.grid.s),
                )
            }
        }
    }
}

/**
 * Lead image slot. Always rendered: a missing or failed image falls back to the branded
 * placeholder instead of collapsing the slot.
 */
@Composable
private fun ArticleImage(imageUrl: String?) {
    val placeholder = painterResource(DesignSystemR.drawable.img_article_placeholder)
    val context = LocalPlatformContext.current
    AsyncImage(
        // The fillMaxWidth + aspectRatio constraints below give the request an exact
        // target size, so Coil decodes a downsampled bitmap instead of the original.
        // Crossfade comes from the app-wide ImageLoader default (AiNewsApplication).
        model =
            remember(context, imageUrl) {
                ImageRequest
                    .Builder(context)
                    .data(imageUrl)
                    .build()
            },
        // Decorative (placeholder included): the headline next to it carries the
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
