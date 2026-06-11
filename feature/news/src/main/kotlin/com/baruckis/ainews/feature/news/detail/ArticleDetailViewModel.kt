package com.baruckis.ainews.feature.news.detail

import androidx.lifecycle.viewModelScope
import com.baruckis.ainews.core.mvi.MviViewModel
import com.baruckis.ainews.core.network.RequestResult
import com.baruckis.ainews.feature.news.data.toNewsError
import com.baruckis.ainews.feature.news.domain.usecase.GetArticleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Orchestrates the article detail screen: turns [ArticleDetailIntent]s into state
 * transitions via [ArticleDetailReducer] and one-time [ArticleDetailEffect]s. Opening the
 * source URL is emitted as an effect — the ViewModel never opens the URL itself.
 */
@HiltViewModel
class ArticleDetailViewModel
    @Inject
    constructor(
        private val getArticle: GetArticleUseCase,
    ) : MviViewModel<ArticleDetailState, ArticleDetailIntent, ArticleDetailEffect>(ArticleDetailState()) {
        private var loadJob: Job? = null

        override fun onIntent(intent: ArticleDetailIntent) {
            when (intent) {
                is ArticleDetailIntent.Load -> load(intent.id)
                ArticleDetailIntent.OpenSource ->
                    currentState.article?.let { sendEffect(ArticleDetailEffect.OpenUrl(it.url)) }
            }
        }

        private fun load(id: String) {
            // The navigation layer re-sends Load on every (re)composition of the entry;
            // skip the round trip when the requested article is already on screen.
            val alreadyShown =
                currentState.articleId == id && currentState.article != null && currentState.error == null
            if (alreadyShown) return
            // Single-flight: a newer Load cancels an in-flight one, so a slow stale
            // response can never overwrite the newer request's result.
            loadJob?.cancel()
            loadJob =
                viewModelScope.launch {
                    setState { ArticleDetailReducer.loading(this, id) }
                    // CacheFirst in the api layer: an article already seen in the list is
                    // served from the normalized cache and opens instantly.
                    when (val result = getArticle(id)) {
                        is RequestResult.Success -> setState { ArticleDetailReducer.success(this, result.data) }
                        is RequestResult.Error ->
                            setState { ArticleDetailReducer.failure(this, result.toNewsError()) }
                    }
                }
        }
    }
