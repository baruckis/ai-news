package com.baruckis.ainews.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import com.baruckis.ainews.feature.news.R
import com.baruckis.ainews.feature.news.detail.ArticleDetailEffect
import com.baruckis.ainews.feature.news.detail.ArticleDetailIntent
import com.baruckis.ainews.feature.news.detail.ArticleDetailScreen
import com.baruckis.ainews.feature.news.detail.ArticleDetailViewModel
import com.baruckis.ainews.feature.news.list.NewsListEffect
import com.baruckis.ainews.feature.news.list.NewsListScreen
import com.baruckis.ainews.feature.news.list.NewsListViewModel

private const val ENTER_DURATION_MS = 220
private const val EXIT_DURATION_MS = 90
private const val SLIDE_DISTANCE_FRACTION = 4

/**
 * Navigation 3 host of the app: the back stack is a plain observable list of [NavKey]s
 * owned here — screens never navigate directly, they emit effects that are collected at
 * this layer and turned into back-stack mutations.
 *
 * The layout adapts to the window width: expanded windows (tablets, unfolded foldables)
 * render the list and the detail side by side through [TwoPaneSceneStrategy], while
 * compact windows keep the single-pane list → detail flow.
 *
 * @param onOpenUrl invoked when a screen asks to open an external URL; the Activity layer
 * decides how (a Chrome Custom Tab), keeping ViewModels free of Android navigation.
 * @param backStack the navigation back stack; injectable so tests can observe it.
 */
@Composable
fun AppNavigation(
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey> = rememberNavBackStack(NewsList),
) {
    val isTwoPane =
        currentWindowAdaptiveInfo()
            .windowSizeClass
            .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators =
            listOf(
                // Default decorator first so entry state saving keeps working, then the
                // ViewModel decorator so each entry gets (and clears) its own ViewModel.
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        // NavDisplay falls back to its single-pane strategy whenever this one returns
        // null, which keeps the phone flow untouched on compact widths.
        sceneStrategies = remember(isTwoPane) { listOf<SceneStrategy<NavKey>>(TwoPaneSceneStrategy(isTwoPane)) },
        transitionSpec = { forwardTransition() },
        popTransitionSpec = { backwardTransition() },
        predictivePopTransitionSpec = { backwardTransition() },
        entryProvider =
            entryProvider {
                entry<NewsList>(metadata = TwoPaneScene.listPane()) {
                    NewsListEntry(backStack = backStack)
                }
                entry<ArticleDetail>(metadata = TwoPaneScene.detailPane()) { key ->
                    ArticleDetailEntry(
                        key = key,
                        // In the two-pane layout the list stays on screen, so the detail
                        // pane has no back button of its own.
                        onBack = if (isTwoPane) null else ({ backStack.removeLastOrNull() }),
                        onOpenUrl = onOpenUrl,
                    )
                }
            },
    )
}

/** News list entry: collects the list effects and renders [NewsListScreen]. */
@Composable
private fun NewsListEntry(backStack: NavBackStack<NavKey>) {
    val viewModel: NewsListViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val offlineMessage = stringResource(R.string.news_list_offline)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NewsListEffect.NavigateToDetail -> backStack.showDetail(effect.id)
                NewsListEffect.ShowOfflineSnackbar ->
                    snackbarHostState.showSnackbar(offlineMessage)
            }
        }
    }
    NewsListScreen(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
    )
}

/** Article detail entry: loads the article for [key] and renders [ArticleDetailScreen]. */
@Composable
private fun ArticleDetailEntry(
    key: ArticleDetail,
    onBack: (() -> Unit)?,
    onOpenUrl: (String) -> Unit,
) {
    val viewModel: ArticleDetailViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key.id) {
        viewModel.onIntent(ArticleDetailIntent.Load(key.id))
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ArticleDetailEffect.OpenUrl -> onOpenUrl(effect.url)
            }
        }
    }
    ArticleDetailScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
    )
}

/**
 * Shows the article with [id]: replaces the top detail entry when one is already visible
 * (selecting another article in the two-pane layout) and pushes a new entry otherwise,
 * so back always returns to the list instead of walking through every viewed article.
 */
private fun NavBackStack<NavKey>.showDetail(id: String) {
    if (lastOrNull() is ArticleDetail) {
        this[lastIndex] = ArticleDetail(id)
    } else {
        add(ArticleDetail(id))
    }
}

/** Forward navigation: the new screen slides in over a quick fade-out of the old one. */
private fun AnimatedContentTransitionScope<Scene<NavKey>>.forwardTransition(): ContentTransform =
    (
        slideInHorizontally(
            animationSpec = tween(ENTER_DURATION_MS),
            initialOffsetX = { it / SLIDE_DISTANCE_FRACTION },
        ) + fadeIn(animationSpec = tween(ENTER_DURATION_MS))
    ) togetherWith fadeOut(animationSpec = tween(EXIT_DURATION_MS))

/** Back navigation: the leaving screen slides back out the way it came in. */
private fun AnimatedContentTransitionScope<Scene<NavKey>>.backwardTransition(): ContentTransform =
    fadeIn(animationSpec = tween(ENTER_DURATION_MS)) togetherWith
        slideOutHorizontally(
            animationSpec = tween(EXIT_DURATION_MS),
            targetOffsetX = { it / SLIDE_DISTANCE_FRACTION },
        ) + fadeOut(animationSpec = tween(EXIT_DURATION_MS))
