package com.baruckis.ainews.feature.news.domain.usecase

import com.baruckis.ainews.core.model.ArticleSummary
import com.baruckis.ainews.core.network.RequestResult
import com.baruckis.ainews.feature.news.data.NewsRepository
import javax.inject.Inject

/**
 * Loads the AI news feed for the list screen; the single entry point ViewModels use,
 * delegating to [NewsRepository].
 */
class GetAiNewsUseCase
    @Inject
    constructor(
        private val repository: NewsRepository,
    ) {
        /**
         * Fetches the feed; pass [forceRefresh] = true to bypass the cache (pull-to-refresh).
         */
        suspend operator fun invoke(forceRefresh: Boolean = false): RequestResult<List<ArticleSummary>> =
            repository.getAiNews(forceRefresh)
    }
