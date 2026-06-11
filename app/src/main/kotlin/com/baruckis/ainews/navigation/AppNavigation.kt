package com.baruckis.ainews.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.baruckis.ainews.feature.news.detail.ArticleDetailEffect
import com.baruckis.ainews.feature.news.detail.ArticleDetailIntent
import com.baruckis.ainews.feature.news.detail.ArticleDetailScreen
import com.baruckis.ainews.feature.news.detail.ArticleDetailViewModel
import com.baruckis.ainews.feature.news.list.NewsListEffect
import com.baruckis.ainews.feature.news.list.NewsListScreen
import com.baruckis.ainews.feature.news.list.NewsListViewModel

/**
 * Navigation 3 host of the app: the back stack is a plain observable list of [NavKey]s
 * owned here — screens never navigate directly, they emit effects that are collected at
 * this layer and turned into back-stack mutations.
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
        entryProvider =
            entryProvider {
                entry<NewsList> {
                    val viewModel: NewsListViewModel = hiltViewModel()
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    LaunchedEffect(viewModel) {
                        viewModel.effects.collect { effect ->
                            when (effect) {
                                is NewsListEffect.NavigateToDetail ->
                                    backStack.add(ArticleDetail(effect.id))
                                // Connectivity failures already surface through the list's
                                // error state; a snackbar on top is Stage 9 polish.
                                NewsListEffect.ShowOfflineSnackbar -> Unit
                            }
                        }
                    }
                    NewsListScreen(state = state, onIntent = viewModel::onIntent)
                }
                entry<ArticleDetail> { key ->
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
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
            },
    )
}
