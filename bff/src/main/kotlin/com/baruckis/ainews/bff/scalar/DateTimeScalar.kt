package com.baruckis.ainews.bff.scalar

import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.StringValue
import graphql.language.Value
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import graphql.schema.GraphQLType
import java.time.Instant
import java.util.Locale
import kotlin.reflect.KType

/** Coerces between the GraphQL `DateTime` scalar and a Java [Instant] using ISO-8601 strings. */
object InstantCoercing : Coercing<Instant, String> {
    override fun serialize(
        dataFetcherResult: Any,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): String =
        (dataFetcherResult as? Instant)?.toString()
            ?: throw CoercingSerializeException(
                "Expected a java.time.Instant but was ${dataFetcherResult::class.simpleName}.",
            )

    override fun parseValue(
        input: Any,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): Instant =
        runCatching { Instant.parse(input.toString()) }
            .getOrElse { throw CoercingParseValueException("Invalid DateTime value: '$input'.", it) }

    override fun parseLiteral(
        input: Value<*>,
        variables: CoercedVariables,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): Instant {
        val literal =
            (input as? StringValue)
                ?: throw CoercingParseLiteralException(
                    "Expected a string literal but was ${input::class.simpleName}.",
                )
        return runCatching { Instant.parse(literal.value) }
            .getOrElse { throw CoercingParseLiteralException("Invalid DateTime literal: '${literal.value}'.", it) }
    }
}

/** The custom `DateTime` GraphQL scalar backed by [InstantCoercing]. */
val dateTimeScalar: GraphQLScalarType =
    GraphQLScalarType
        .newScalar()
        .name("DateTime")
        .description("An ISO-8601 instant, e.g. 2026-06-01T12:00:00Z.")
        .coercing(InstantCoercing)
        .build()

/** Schema hooks that expose Kotlin [Instant] properties as the `DateTime` scalar. */
class DateTimeHooks : SchemaGeneratorHooks {
    override fun willGenerateGraphQLType(type: KType): GraphQLType? =
        when (type.classifier) {
            Instant::class -> dateTimeScalar
            else -> null
        }
}
