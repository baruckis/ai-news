package com.baruckis.ainews.bff

import com.baruckis.ainews.bff.scalar.DateTimeHooks
import com.expediagroup.graphql.server.ktor.GraphQL
import com.expediagroup.graphql.server.ktor.graphQLPostRoute
import com.expediagroup.graphql.server.ktor.graphQLSDLRoute
import com.expediagroup.graphql.server.ktor.graphiQLRoute
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.routing.routing

/**
 * Installs the graphql-kotlin engine (with the `DateTime` scalar) and the GraphQL HTTP/SDL/GraphiQL
 * routes. [newsService] is injectable so tests can supply a fake; production builds the real
 * NewsData.io-backed client from the environment via [newsDataClientFromEnvironment].
 */
fun Application.bffModule(newsService: NewsService = newsDataClientFromEnvironment()) {
    install(GraphQL) {
        schema {
            packages = listOf("com.baruckis.ainews.bff")
            queries = listOf(NewsQuery(newsService))
            hooks = DateTimeHooks()
        }
    }
    routing {
        graphQLPostRoute()
        graphQLSDLRoute()
        graphiQLRoute()
    }
    // Release the HTTP client (connection pool + dispatcher) when the server stops.
    monitor.subscribe(ApplicationStopped) { newsService.close() }
}
