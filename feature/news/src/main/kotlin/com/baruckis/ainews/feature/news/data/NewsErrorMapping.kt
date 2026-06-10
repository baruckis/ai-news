package com.baruckis.ainews.feature.news.data

import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.exception.ApolloNetworkException
import com.baruckis.ainews.core.network.RequestResult
import com.baruckis.ainews.feature.news.domain.model.NewsError

/**
 * Classifies a failed request for the UI: transport-level causes (no connectivity,
 * unreachable or erroring server) become [NewsError.Network] so the user is asked to
 * retry; everything else maps to [NewsError.Unknown].
 */
fun RequestResult.Error.toNewsError(): NewsError =
    when (cause) {
        is ApolloNetworkException, is ApolloHttpException -> NewsError.Network
        else -> NewsError.Unknown
    }
