package io.lb.lbmealsnew.core.network.factory

import io.ktor.client.HttpClient

/**
 * Creates the platform-specific [HttpClient]: each platform picks its own
 * engine, while the plugins configured on it stay identical.
 */
expect class HttpClientFactory() {
    fun create(): HttpClient
}
