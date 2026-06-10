package com.baruckis.ainews.core.network

/**
 * Outcome of a network request, hiding transport details from the layers above.
 *
 * Higher layers (repositories, use cases) branch on this instead of catching
 * client-specific exceptions.
 */
sealed interface RequestResult<out T> {
    /** The request succeeded and produced [data]. */
    data class Success<T>(
        val data: T,
    ) : RequestResult<T>

    /** The request failed; [cause] carries the underlying reason for logging or mapping. */
    data class Error(
        val cause: Throwable,
    ) : RequestResult<Nothing>
}
