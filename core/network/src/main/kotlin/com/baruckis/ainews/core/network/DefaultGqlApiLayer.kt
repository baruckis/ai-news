package com.baruckis.ainews.core.network

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Query
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * [GqlApiLayer] backed by an [ApolloClient].
 *
 * Apollo Kotlin 5 never throws from `execute()`: transport failures arrive in
 * `response.exception` and resolver failures in `response.errors`, so each branch is
 * folded into a [RequestResult] here.
 */
class DefaultGqlApiLayer
    @Inject
    constructor(
        private val client: ApolloClient,
    ) : GqlApiLayer {
        override suspend fun <D : Query.Data, R> query(
            query: Query<D>,
            forceRefresh: Boolean,
            transform: (D) -> R,
        ): RequestResult<R> {
            val call = client.query(query)
            val response = (if (forceRefresh) call.fetchPolicy(FetchPolicy.NetworkOnly) else call).execute()
            val exception = response.exception
            val data = response.data
            return when {
                exception != null -> RequestResult.Error(exception)
                response.hasErrors() || data == null ->
                    RequestResult.Error(
                        GraphQlOperationException(
                            response.errors?.joinToString { it.message } ?: "No data received",
                        ),
                    )
                else ->
                    try {
                        RequestResult.Success(transform(data))
                    } catch (e: CancellationException) {
                        // Never swallow coroutine cancellation.
                        throw e
                    } catch (
                        // The contract is that a throwing transform becomes an Error,
                        // whatever the mapper throws.
                        @Suppress("TooGenericExceptionCaught") e: Exception,
                    ) {
                        RequestResult.Error(e)
                    }
            }
        }
    }

/**
 * Signals that the server answered but the operation failed: the response carried
 * GraphQL errors or no data.
 */
class GraphQlOperationException(
    message: String,
) : Exception(message)
