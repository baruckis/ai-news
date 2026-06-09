package com.baruckis.ainews.bff

import com.baruckis.ainews.bff.scalar.DateTimeHooks
import com.expediagroup.graphql.server.ktor.GraphQL
import com.expediagroup.graphql.server.ktor.graphQLPostRoute
import com.expediagroup.graphql.server.ktor.graphQLSDLRoute
import com.expediagroup.graphql.server.ktor.graphiQLRoute
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing

private const val DEFAULT_PORT = 8080

/** Process entry point: starts the Netty server hosting the GraphQL BFF. */
fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: DEFAULT_PORT
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::bffModule).start(wait = true)
}

/**
 * Installs the graphql-kotlin engine (with the `DateTime` scalar) and the GraphQL HTTP/SDL/GraphiQL
 * routes. [newsService] is injectable so tests can supply a fake; production builds the real
 * NewsData.io-backed client from the environment.
 */
fun Application.bffModule(newsService: NewsService = NewsDataClient.fromEnvironment()) {
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
}
