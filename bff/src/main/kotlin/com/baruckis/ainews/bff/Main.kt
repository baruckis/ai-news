package com.baruckis.ainews.bff

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.serialization.json.Json

private const val DEFAULT_PORT = 8080

/** Process entry point: starts the Netty server hosting the GraphQL BFF. */
fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: DEFAULT_PORT
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::bffModule).start(wait = true)
}

/**
 * Builds the production [NewsDataClient] from environment variables (`NEWSDATA_KEY`,
 * `GNEWS_KEY`, `NEWS_TIMEFRAME`). This is the default wiring used by [bffModule]; tests
 * inject their own [NewsService] instead.
 */
fun newsDataClientFromEnvironment(): NewsDataClient {
    val apiKey =
        System.getenv("NEWSDATA_KEY")
            ?: error("NEWSDATA_KEY environment variable is required")
    val httpClient =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    return NewsDataClient(
        httpClient = httpClient,
        newsDataApiKey = apiKey,
        gnewsApiKey = System.getenv("GNEWS_KEY"),
        timeframe = System.getenv("NEWS_TIMEFRAME"),
    )
}
