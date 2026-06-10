package com.baruckis.ainews.core.network.di

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.apollographql.cache.normalized.normalizedCache
import com.apollographql.cache.normalized.sql.SqlNormalizedCacheFactory
import com.baruckis.ainews.core.network.BuildConfig
import com.baruckis.ainews.core.network.DefaultGqlApiLayer
import com.baruckis.ainews.core.network.GqlApiLayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt bindings for the GraphQL client and the [GqlApiLayer] it powers. */
@Module
@InstallIn(SingletonComponent::class)
object ApolloModule {
    /**
     * Single [ApolloClient] for the app: BFF endpoint from `BuildConfig.GRAPHQL_URL`
     * (sourced from `local.properties`, never hard-coded) plus a SQLite normalized cache
     * with cache-first reads, so an article already seen in the list opens instantly.
     */
    @Provides
    @Singleton
    fun provideApolloClient(
        @ApplicationContext context: Context,
    ): ApolloClient =
        ApolloClient
            .Builder()
            .serverUrl(BuildConfig.GRAPHQL_URL)
            .normalizedCache(
                normalizedCacheFactory = SqlNormalizedCacheFactory(context, "apollo.db"),
                // No custom policies: records are keyed by the default generator.
                typePolicies = emptyMap(),
                fieldPolicies = emptyMap(),
            ).fetchPolicy(FetchPolicy.CacheFirst)
            .build()

    /** Exposes the Apollo-backed [GqlApiLayer] to the data layer. */
    @Provides
    @Singleton
    fun provideGqlApiLayer(client: ApolloClient): GqlApiLayer = DefaultGqlApiLayer(client)
}
