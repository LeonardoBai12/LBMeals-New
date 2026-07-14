package io.lb.lbmealsnew.core.common

/**
 * A value on its way from a data source to the screen: still being fetched,
 * arrived, or failed to arrive.
 *
 * The screens render exactly one branch per state, which keeps "nothing to
 * show yet" (Loading) distinct from "nothing to show at all" (Success with
 * empty content) — an absent value is never assumed to mean either.
 */
sealed interface Resource<out T> {
    /**
     * The value is still being fetched — the screen should keep waiting.
     */
    data object Loading : Resource<Nothing>

    /**
     * The value arrived. [data] may still be empty content (e.g. an empty
     * list), which is a legitimate final state, not a loading one.
     */
    data class Success<T>(val data: T) : Resource<T>

    /**
     * The value failed to arrive and no retry is in flight.
     *
     * @property message Optional cause, for logging or debugging; screens
     * show their own user-facing copy.
     */
    data class Error(val message: String? = null) : Resource<Nothing>
}
