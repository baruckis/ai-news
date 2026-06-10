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
     * @param forceRefresh when true, bypasses the normalized cache and fetches from the
     *   network (e.g. pull-to-refresh); default reads honor the client's cache-first policy
     * @param transform maps the raw operation data into the caller's model; if it throws,
     *   the failure is returned as [RequestResult.Error] rather than escaping
     * @return [RequestResult.Success] with the transformed data, or [RequestResult.Error]
     *   on transport failures, GraphQL errors, missing data or a throwing [transform]
     */
    suspend fun <D : Query.Data, R> query(
        query: Query<D>,
        forceRefresh: Boolean = false,
        transform: (D) -> R,
    ): RequestResult<R>
}
