package io.github.agrevster.pocketbaseKotlin

import io.ktor.client.*
import io.ktor.client.engine.cio.*

internal actual fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(CIO) { config(this) }
}