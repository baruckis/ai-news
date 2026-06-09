package com.baruckis.ainews.core.designsystem.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import com.baruckis.ainews.core.designsystem.theme.HairlineBorderWidth

private const val SHIMMER_DURATION_MS = 1200
private const val SHIMMER_TRAVEL = 1000f
private const val SHIMMER_BAND = 300f
private const val IMAGE_ASPECT_RATIO = 16f / 9f
private const val TITLE_WIDTH_FRACTION = 0.6f
private const val SOURCE_WIDTH_FRACTION = 0.3f

/**
 * Loading placeholder for a news card. Renders a card-shaped skeleton with an animated
 * shimmer sweep so the list communicates progress while real content is being fetched.
 */
@Composable
fun NewsCardSkeleton(modifier: Modifier = Modifier) {
    val shimmer = rememberShimmerBrush()
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppTheme.colors.surfacePrimary,
        shape = AppTheme.shapes.card,
        border = BorderStroke(HairlineBorderWidth, AppTheme.colors.border),
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.grid.m),
            verticalArrangement = Arrangement.spacedBy(AppTheme.grid.s),
        ) {
            ShimmerBlock(
                brush = shimmer,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(IMAGE_ASPECT_RATIO),
            )
            ShimmerBlock(
                brush = shimmer,
                modifier = Modifier.fillMaxWidth().height(AppTheme.grid.m),
            )
            ShimmerBlock(
                brush = shimmer,
                modifier = Modifier.fillMaxWidth(TITLE_WIDTH_FRACTION).height(AppTheme.grid.m),
            )
            ShimmerBlock(
                brush = shimmer,
                modifier = Modifier.fillMaxWidth(SOURCE_WIDTH_FRACTION).height(AppTheme.grid.s),
            )
        }
    }
}

@Composable
private fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = SHIMMER_TRAVEL,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = SHIMMER_DURATION_MS),
                repeatMode = RepeatMode.Restart,
            ),
        label = "shimmerTranslate",
    )
    return Brush.linearGradient(
        colors =
            listOf(
                AppTheme.colors.surfaceSecondary,
                AppTheme.colors.surfacePrimary,
                AppTheme.colors.surfaceSecondary,
            ),
        start = Offset(translate - SHIMMER_BAND, 0f),
        end = Offset(translate, 0f),
    )
}

@Composable
private fun ShimmerBlock(
    brush: Brush,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(AppTheme.grid.xs))
                .background(brush),
    )
}

@Preview(name = "NewsCardSkeleton light")
@Composable
private fun NewsCardSkeletonLightPreview() {
    AppTheme(darkTheme = false) {
        ThemedPreviewSurface {
            NewsCardSkeleton()
        }
    }
}

@Preview(name = "NewsCardSkeleton dark")
@Composable
private fun NewsCardSkeletonDarkPreview() {
    AppTheme(darkTheme = true) {
        ThemedPreviewSurface {
            NewsCardSkeleton()
        }
    }
}
