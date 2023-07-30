package io.github.agrevster.pocketbaseKotlin.services.utils

import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive


/**
 * THIS IS NOT MY CODE IT COMES FROM
 * https://github.com/JurajBegovac/ktor-kmm-sse
 **/


private typealias EventType = String
private typealias EventData = String
private typealias EventId = String

internal data class SseEvent(
    val id: EventId? = null,
    val event: EventType? = null,
    val data: EventData = ""
)

internal fun HttpClient.readSse(
    url: String,
): Flow<SseEvent> {
    val client = this
    return flow {
        coroutineContext.ensureActive()
        while (isActive) {
            client.prepareGet {
                url {
                    path(url)
                }
                headers {
                    append(HttpHeaders.Accept, "text/event-stream")
                    append(HttpHeaders.CacheControl, "no-cache")
                    append(HttpHeaders.Connection, "keep-alive")
                }
                timeout {
                    requestTimeoutMillis = Long.MAX_VALUE
                }
            }.execute { response ->
                try {
                    PocketbaseException.handle(response)
                } catch (e: PocketbaseException) {
                    if (!e.message!!.contains("client id")) {
                        throw PocketbaseException(e.reason)
                    }
                }
                response.bodyAsChannel()
                    .readSse(
                        onSseEvent = { sseEvent ->
                            emit(sseEvent)
                        })
            }
        }
    }
}


internal suspend inline fun ByteReadChannel.readSse(
    onSseEvent: (SseEvent) -> (Unit),
) {
    var id: EventId? = null
    var event: EventType? = null
    var data: EventData? = null

    while (!isClosedForRead) {
        parseSseLine(
            line = readUTF8Line(),
            onSseRawEvent = { sseRawEvent ->
                when (sseRawEvent) {
                    SseRawEvent.End -> {
                        if (data != null) {
                            onSseEvent(SseEvent(id, event, data!!))
                            id = null
                            event = null
                            data = null
                        } else {
                            // do nothing
                        }
                    }

                    is SseRawEvent.Id -> id = sseRawEvent.value
                    is SseRawEvent.Event -> event = sseRawEvent.value
                    is SseRawEvent.Data -> data = sseRawEvent.value
                }
            }
        )
    }
}

internal inline fun parseSseLine(
    line: String?,
    onSseRawEvent: (SseRawEvent) -> (Unit)
) {
    val parts = line.takeIf { !it.isNullOrBlank() }?.split(":", limit = 2)
    val field = parts?.getOrNull(0)?.trim()
    val value = parts?.getOrNull(1)?.trim().orEmpty()
    onSseRawEvent(
        when (field) {
            null -> SseRawEvent.End
            "id" -> SseRawEvent.Id(value)
            "data" -> SseRawEvent.Data(value)
            "event" -> SseRawEvent.Event(value)
            else -> throw PocketbaseException("Invalid SSE Field")
        }
    )
}

internal sealed interface SseRawEvent {
    data class Id(val value: EventId) : SseRawEvent
    data class Event(val value: EventType) : SseRawEvent
    data class Data(val value: EventData) : SseRawEvent
    object End : SseRawEvent
}