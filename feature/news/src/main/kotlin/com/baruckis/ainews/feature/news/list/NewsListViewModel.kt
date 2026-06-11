package com.baruckis.ainews.feature.news.list

import androidx.lifecycle.viewModelScope
import com.baruckis.ainews.core.mvi.MviViewModel
import com.baruckis.ainews.core.network.RequestResult
import com.baruckis.ainews.feature.news.data.toNewsError
import com.baruckis.ainews.feature.news.domain.model.NewsError
import com.baruckis.ainews.feature.news.domain.usecase.GetAiNewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Orchestrates the news list screen: turns [NewsListIntent]s into state transitions via
 * [NewsListReducer] and one-time [NewsListEffect]s. Navigation is emitted as an effect —
 * the ViewModel never calls into navigation directly.
 */
@HiltViewModel
class NewsListViewModel
    @Inject
    constructor(
        private val getAiNews: GetAiNewsUseCase,
    ) : MviViewModel<NewsListState, NewsListIntent, NewsListEffect>(NewsListState()) {
        init {
            onIntent(NewsListIntent.Load)
        }

        override fun onIntent(intent: NewsListIntent) {
            when (intent) {
                NewsListIntent.Load, NewsListIntent.Retry -> load(forceRefresh = false)
                NewsListIntent.Refresh -> load(forceRefresh = true)
                is NewsListIntent.ArticleClicked ->
                    sendEffect(NewsListEffect.NavigateToDetail(intent.id))
            }
        }

        private fun load(forceRefresh: Boolean) {
            viewModelScope.launch {
                setState {
                    if (forceRefresh) NewsListReducer.refreshing(this) else NewsListReducer.loading(this)
                }
                when (val result = getAiNews(forceRefresh)) {
                    is RequestResult.Success -> setState { NewsListReducer.success(this, result.data) }
                    is RequestResult.Error -> {
                        val error = result.toNewsError()
                        setState { NewsListReducer.failure(this, error) }
                        // Connectivity problems are also surfaced as a snackbar so the user
                        // is informed even when stale articles remain visible on screen.
                        if (error == NewsError.Network) {
                            sendEffect(NewsListEffect.ShowOfflineSnackbar)
                        }
                    }
                }
            }
        }
    }
