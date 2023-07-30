package io.github.agrevster.pocketbaseKotlin

import io.ktor.client.*
import io.ktor.client.engine.winhttp.*


public actual fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(WinHttp) { config(this) }
}