package com.baruckis.ainews.bff.scalar

import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.IntValue
import graphql.language.StringValue
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.time.Instant
import java.util.Locale

class DateTimeScalarTest {
    private val context: GraphQLContext = GraphQLContext.getDefault()
    private val locale: Locale = Locale.ENGLISH
    private val instant = Instant.parse("2026-06-01T12:30:00Z")

    @Test
    fun `serialize renders an Instant as an ISO-8601 string`() {
        assertEquals("2026-06-01T12:30:00Z", InstantCoercing.serialize(instant, context, locale))
    }

    @Test
    fun `serialize rejects a non-Instant value`() {
        assertThrows(CoercingSerializeException::class.java) {
            InstantCoercing.serialize("not an instant", context, locale)
        }
    }

    @Test
    fun `parseValue parses an ISO-8601 string`() {
        assertEquals(instant, InstantCoercing.parseValue("2026-06-01T12:30:00Z", context, locale))
    }

    @Test
    fun `parseValue rejects an invalid value`() {
        assertThrows(CoercingParseValueException::class.java) {
            InstantCoercing.parseValue("not-a-date", context, locale)
        }
    }

    @Test
    fun `parseLiteral parses a string literal`() {
        val literal = StringValue.newStringValue("2026-06-01T12:30:00Z").build()
        assertEquals(instant, InstantCoercing.parseLiteral(literal, CoercedVariables.emptyVariables(), context, locale))
    }

    @Test
    fun `parseLiteral rejects a non-string literal`() {
        val literal = IntValue.newIntValue(BigInteger.ONE).build()
        assertThrows(CoercingParseLiteralException::class.java) {
            InstantCoercing.parseLiteral(literal, CoercedVariables.emptyVariables(), context, locale)
        }
    }

    @Test
    fun `parseLiteral rejects an invalid string literal`() {
        val literal = StringValue.newStringValue("not-a-date").build()
        assertThrows(CoercingParseLiteralException::class.java) {
            InstantCoercing.parseLiteral(literal, CoercedVariables.emptyVariables(), context, locale)
        }
    }
}
