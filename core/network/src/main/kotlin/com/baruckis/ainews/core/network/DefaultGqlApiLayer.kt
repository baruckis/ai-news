package com.baruckis.ainews.core.network

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Query
import javax.inject.Inject

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
            transform: (D) -> R,
        ): RequestResult<R> {
            val response = client.query(query).execute()
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
                else -> RequestResult.Success(transform(data))
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
