package io.github.agrevster.pocketbaseKotlin

import io.ktor.client.*
import io.ktor.client.engine.darwin.*

internal actual fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(Darwin) { config(this) }
}
