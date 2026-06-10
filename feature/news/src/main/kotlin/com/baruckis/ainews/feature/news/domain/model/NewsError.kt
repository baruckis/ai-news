package com.baruckis.ainews.feature.news.domain.model

/**
 * User-facing classification of a news loading failure, kept coarse on purpose: the UI
 * only decides between a connectivity message and a generic one. "No error" is modelled
 * as a nullable `NewsError?` in UI state rather than a sentinel value here.
 */
sealed interface NewsError {
    /** The backend could not be reached or answered with a transport-level failure. */
    data object Network : NewsError

    /** Any other failure (GraphQL errors, mapping issues); generic message with retry. */
    data object Unknown : NewsError
}
