package com.baruckis.ainews.core.network

import com.apollographql.apollo.api.Query

/**
 * Thin gateway to the GraphQL backend: executes a query and maps its data in one step.
 *
 * Callers hand in a generated operation plus a [transform] into their own model, so Apollo
 * response types never travel upwards — errors come back as [RequestResult.Error] instead
 * of client-specific exceptions.
 */
interface GqlApiLayer {
    /**
     * Executes [query] and converts its data with [transform].
     *
     * @param query the generated GraphQL operation to execute
     * @param transform maps the raw operation data into the caller's model
     * @return [RequestResult.Success] with the transformed data, or [RequestResult.Error]
     *   on transport failures, GraphQL errors or missing data
     */
    suspend fun <D : Query.Data, R> query(
        query: Query<D>,
        transform: (D) -> R,
    ): RequestResult<R>
}
