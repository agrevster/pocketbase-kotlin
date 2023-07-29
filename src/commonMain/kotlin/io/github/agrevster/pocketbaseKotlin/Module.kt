package io.github.agrevster.pocketbaseKotlin

import io.ktor.client.*

public expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient