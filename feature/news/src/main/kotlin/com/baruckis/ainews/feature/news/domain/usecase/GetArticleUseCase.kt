package com.baruckis.ainews.feature.news.domain.usecase

import com.baruckis.ainews.core.model.Article
import com.baruckis.ainews.core.network.RequestResult
import com.baruckis.ainews.feature.news.domain.repository.NewsRepository
import javax.inject.Inject

/**
 * Loads the full detail of one article for the detail screen, delegating to [NewsRepository].
 */
class GetArticleUseCase
    @Inject
    constructor(
        private val repository: NewsRepository,
    ) {
        /** Fetches the article with the given [id]. */
        suspend operator fun invoke(id: String): RequestResult<Article> = repository.getArticle(id)
    }
