package com.baruckis.ainews.feature.news.data

import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.exception.ApolloNetworkException
import com.baruckis.ainews.core.network.GraphQlOperationException
import com.baruckis.ainews.core.network.RequestResult
import com.baruckis.ainews.feature.news.domain.model.NewsError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Checks the Network vs Unknown classification in [toNewsError]. */
class NewsErrorMappingTest {
    @Test
    fun `connectivity failure maps to Network`() {
        val error = RequestResult.Error(ApolloNetworkException("no route to host"))

        assertEquals(NewsError.Network, error.toNewsError())
    }

    @Test
    fun `http failure maps to Network`() {
        val error =
            RequestResult.Error(
                ApolloHttpException(
                    statusCode = 500,
                    headers = emptyList(),
                    body = null,
                    message = "Internal server error",
                ),
            )

        assertEquals(NewsError.Network, error.toNewsError())
    }

    @Test
    fun `GraphQL operation failure maps to Unknown`() {
        val error = RequestResult.Error(GraphQlOperationException("backend exploded"))

        assertEquals(NewsError.Unknown, error.toNewsError())
    }

    @Test
    fun `unexpected exception maps to Unknown`() {
        val error = RequestResult.Error(IllegalStateException("mapper exploded"))

        assertEquals(NewsError.Unknown, error.toNewsError())
    }
}
