package com.baruckis.ainews.feature.news.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.baruckis.ainews.core.mvi.MviViewModel
import com.baruckis.ainews.core.network.RequestResult
import com.baruckis.ainews.feature.news.data.toNewsError
import com.baruckis.ainews.feature.news.domain.usecase.GetArticleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/** [SavedStateHandle] key under which the requested article id survives process death. */
private const val SAVED_ARTICLE_ID_KEY = "articleId"

/**
 * Orchestrates the article detail screen: turns [ArticleDetailIntent]s into state
 * transitions via [ArticleDetailReducer] and one-time [ArticleDetailEffect]s. Opening the
 * source URL is emitted as an effect — the ViewModel never opens the URL itself.
 *
 * The requested article id is persisted in [SavedStateHandle], so after process death the
 * recreated ViewModel reloads the same article without waiting for the UI to ask again.
 */
@HiltViewModel
class ArticleDetailViewModel
    @Inject
    constructor(
        private val getArticle: GetArticleUseCase,
        private val savedStateHandle: SavedStateHandle,
    ) : MviViewModel<ArticleDetailState, ArticleDetailIntent, ArticleDetailEffect>(ArticleDetailState()) {
        private var loadJob: Job? = null

        init {
            // Restore after process death: the saved id is only present when a previous
            // instance had already started loading an article.
            savedStateHandle.get<String>(SAVED_ARTICLE_ID_KEY)?.let { restoredId ->
                onIntent(ArticleDetailIntent.Load(restoredId))
            }
        }

        override fun onIntent(intent: ArticleDetailIntent) {
            when (intent) {
                is ArticleDetailIntent.Load -> load(intent.id)
                ArticleDetailIntent.OpenSource ->
                    currentState.article?.let { sendEffect(ArticleDetailEffect.OpenUrl(it.url)) }
            }
        }

        private fun load(id: String) {
            savedStateHandle[SAVED_ARTICLE_ID_KEY] = id
            // Guard against duplicate Load intents: skip the round trip when the requested
            // article is already on screen, or when a load for the same id is in flight —
            // e.g. the UI's LaunchedEffect re-sending Load for the id this ViewModel just
            // started restoring in init, which would otherwise fetch the article twice.
            val alreadyHandled =
                currentState.articleId == id &&
                    currentState.error == null &&
                    (currentState.article != null || currentState.isLoading)
            if (alreadyHandled) return
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
