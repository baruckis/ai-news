package com.baruckis.ainews.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.baruckis.ainews.R
import com.baruckis.ainews.core.designsystem.components.AppText
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import com.baruckis.ainews.core.designsystem.theme.HairlineBorderWidth

private const val LIST_PANE_WEIGHT = 0.4f
private const val DETAIL_PANE_WEIGHT = 0.6f

/**
 * A [Scene] that lays the news list and the article detail out side by side, used on
 * window widths where a single pane would waste space (tablets, unfolded foldables).
 *
 * The list pane is always present; the detail pane shows either the top [detailEntry] of
 * the back stack or a placeholder prompting the user to select an article.
 */
class TwoPaneScene<T : Any>(
    /** Entry rendered in the left (list) pane. */
    private val listEntry: NavEntry<T>,
    /** Entry rendered in the right (detail) pane, or null to show the placeholder. */
    private val detailEntry: NavEntry<T>?,
    override val previousEntries: List<NavEntry<T>>,
) : Scene<T> {
    // A constant key keeps NavDisplay on the same scene while the selection changes, so
    // picking another article swaps only the detail pane instead of animating the whole
    // two-pane layout through a navigation transition.
    override val key: Any = SCENE_KEY

    override val entries: List<NavEntry<T>> = listOfNotNull(listEntry, detailEntry)

    override val content: @Composable () -> Unit = {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(LIST_PANE_WEIGHT)) {
                listEntry.Content()
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(HairlineBorderWidth)
                        .background(AppTheme.colors.border),
            )
            Box(modifier = Modifier.weight(DETAIL_PANE_WEIGHT)) {
                detailEntry?.Content() ?: NoArticleSelectedPane()
            }
        }
    }

    /** Metadata markers used by [TwoPaneSceneStrategy] to assign entries to panes. */
    companion object {
        private const val SCENE_KEY = "TwoPaneScene"
        private const val LIST_PANE_KEY = "TwoPaneScene.list"
        private const val DETAIL_PANE_KEY = "TwoPaneScene.detail"

        /** Metadata marking an entry as the list (left) pane of the two-pane layout. */
        fun listPane(): Map<String, Any> = mapOf(LIST_PANE_KEY to true)

        /** Metadata marking an entry as the detail (right) pane of the two-pane layout. */
        fun detailPane(): Map<String, Any> = mapOf(DETAIL_PANE_KEY to true)

        internal fun isListPane(entry: NavEntry<*>): Boolean = entry.metadata.containsKey(LIST_PANE_KEY)

        internal fun isDetailPane(entry: NavEntry<*>): Boolean = entry.metadata.containsKey(DETAIL_PANE_KEY)
    }
}

/** Placeholder for the detail pane while no article is selected. */
@Composable
private fun NoArticleSelectedPane() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(AppTheme.colors.surfacePrimary)
                .padding(AppTheme.grid.l),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppText(
            text = stringResource(R.string.two_pane_no_selection_title),
            style = AppTheme.typography.titleMedium,
        )
        AppText(
            text = stringResource(R.string.two_pane_no_selection_message),
            style = AppTheme.typography.body,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.padding(top = AppTheme.grid.s),
        )
    }
}

/**
 * [SceneStrategy] that maps the back stack to a [TwoPaneScene] when [isTwoPane] is true
 * (expanded window widths): the bottom list entry stays visible on the left while the
 * topmost detail entry — if any — fills the right pane.
 *
 * Returns null on compact widths so NavDisplay falls back to the single-pane strategy,
 * keeping the phone behaviour untouched.
 */
class TwoPaneSceneStrategy<T : Any>(
    /** Whether the current window is wide enough for the side-by-side layout. */
    private val isTwoPane: Boolean,
) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (!isTwoPane) return null
        val listEntry = entries.firstOrNull() ?: return null
        if (!TwoPaneScene.isListPane(listEntry)) return null
        val detailEntry = entries.lastOrNull()?.takeIf { TwoPaneScene.isDetailPane(it) }
        return TwoPaneScene(
            listEntry = listEntry,
            detailEntry = detailEntry,
            // Popping the detail keeps the list pane on screen; popping the bare list
            // leaves the app, hence no previous entries in that case.
            previousEntries = if (detailEntry != null) entries.dropLast(1) else emptyList(),
        )
    }
}
