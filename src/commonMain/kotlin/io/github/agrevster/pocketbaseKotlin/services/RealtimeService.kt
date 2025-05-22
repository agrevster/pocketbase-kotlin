package io.github.agrevster.pocketbaseKotlin.services

import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.services.utils.BaseService
import io.github.agrevster.pocketbaseKotlin.toJsonPrimitive
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

public class RealtimeService(client: PocketbaseClient) : BaseService(client) {

    private val unknownKeysJson = Json {
        ignoreUnknownKeys = true
    }

    @Serializable
    /** The data sent when a realtime event is emitted */
    public data class MessageData(val action: RealtimeActionType, val record: JsonElement?) {
        /**
         * Serializes the record emitted to type [T] Used to get the record from an
         * event
         * @param [json] An optional custom Json parser
         */
        public inline fun <reified T> parseRecord(json: Json = Json): T {
            if (action == RealtimeActionType.CONNECT) throw PocketbaseException("Connect event cannot be parsed!")
            return json.decodeFromJsonElement(record!!)
        }
    }

    /** The type of realtime event emitted */
    public enum class RealtimeActionType {
        CONNECT,

        CREATE,

        UPDATE,

        DELETE;

        /**
         * Returns weather or not the event type is capable of containing a body or
         * record
         */
        public fun isBodyEvent(): Boolean {
            return when (this) {
                CONNECT -> false
                else -> {
                    true
                }
            }
        }

    }

    private var clientId: String? = null
    private var connected: Boolean = false
    private var connection = MutableSharedFlow<MessageData>()
    private val subscriptions: MutableSet<String> = mutableSetOf()
    private val sseCoroutines: MutableSet<Job> = mutableSetOf()

    private suspend fun sendSubscribeRequest(): Boolean {
        val body = mapOf(
            "clientId" to clientId!!.toJsonPrimitive(),
            "subscriptions" to JsonArray(subscriptions.map { it.toJsonPrimitive() })
        )
        val response = client.httpClient.post {
            url {
                path("/api/realtime")
            }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        PocketbaseException.handle(response)
        return true
    }

    /**
     * Creates a realtime connection NOTE: There can only be one realtime
     * connection going at once
     *
     * @sample runBlocking { launch{ realtime.connect() } }
     */
    public suspend fun connect() {
        coroutineScope {
            val job = launch {
                if (connected) throw PocketbaseException("You are already connected to the realtime service!")
                connected = true
                while (isActive) {
                    client.httpClient.sse(path = "/api/realtime") {
                        incoming.collectLatest { event ->
                            if (clientId == null || event.id != clientId) {
                                clientId = event.id
                                sendSubscribeRequest()
                            }
                            //If there is data emit it, else, assume it is a new connection attempt
                            try {
                                val jsonData = unknownKeysJson.decodeFromString<JsonObject>(event.data!!)
                                connection.emit(
                                    MessageData(
                                        RealtimeActionType.valueOf(
                                            jsonData["action"].toString().removeSurrounding("\"").uppercase()
                                        ), jsonData["record"]
                                    )
                                )
                            } catch (e: Exception) {
                                connection.emit(MessageData(RealtimeActionType.CONNECT, null))
                            }
                        }
                    }
                }
            }
            sseCoroutines.add(job)
        }
    }

    /**
     * Listens for subscribed realtime events asynchronously, when one is
     * received [callback] is run
     *
     * @sample runBlocking { launch{ service.listen { ... } } }
     */
    public suspend fun listen(callback: MessageData.() -> Unit) {
        if (!connected) throw PocketbaseException("You must connect to the SSE client first!")
        coroutineScope {
            val job = launch {
                connection.collectLatest { event ->
                    callback(event)
                    coroutineContext.ensureActive()
                }
            }
            sseCoroutines.add(job)
        }
    }

    /**
     * Creates a subscription to a record or collection allowing [listen]() to
     * received events from the desired target.
     *
     * @param subscription The record or collection to subscribe to (*) for
     *    wildcard
     * @param delay The delay waited before sending the request if the client
     *    is not yet connected
     */
    public suspend fun subscribe(subscription: String, delay: Long = 2000) {
        if (!connected || clientId == null) delay(delay)
        if (!connected) throw PocketbaseException("You must connect to the SSE client first!")
        subscriptions.add(subscription)
        sendSubscribeRequest()
    }

    /**
     * Creates a subscription to a record or collection allowing [listen]() to
     * received events from the desired target.
     *
     * @param delay The delay waited before sending the request if the client
     *    is not yet connected
     * @param subscriptionList The records or collections to subscribe to (*)
     *    for wildcard
     */
    public suspend fun subscribe(delay: Long = 2000, vararg subscriptionList: String) {
        if (!connected || clientId == null) delay(delay)
        if (!connected) throw PocketbaseException("You must connect to the SSE client first!")
        subscriptionList.forEach { subscription -> subscriptions.add(subscription) }
        sendSubscribeRequest()
    }

    /**
     * Removes a subscription to a record or collection allowing [listen]() to
     * no longer receive events from the desired target.
     *
     * @param subscription The record or collection to unsubscribe to (*) for
     *    wildcard
     * @param delay The delay waited before sending the request if the client
     *    is not yet connected
     */
    public suspend fun unsubscribe(subscription: String, delay: Long = 2000) {
        if (!connected || clientId == null) delay(delay)
        if (!connected) throw PocketbaseException("You must connect to the SSE client first!")
        subscriptions.remove(subscription)
        sendSubscribeRequest()
    }

    /**
     * Unsubscribes from every subscription, meaning the listen no longer
     * receives events from any collection or record until one is subscribed to
     *
     * @param delay The delay waited before sending the request if the client
     *    is not yet connected
     */
    public suspend fun unsubscribeAll(delay: Long = 2000) {
        if (!connected || clientId == null) delay(delay)
        if (!connected) throw PocketbaseException("You must connect to the SSE client first!")
        subscriptions.clear()
        sendSubscribeRequest()
    }

    /**
     * Removes a subscriptions to a record or collection allowing [listen]() to
     * no longer receive events from the desired target.
     *
     * @param delay The delay waited before sending the request if the client
     *    is not yet connected
     * @param subscriptionList The records or collections to unsubscribe to (*)
     *    for wildcard
     */
    public suspend fun unsubscribe(delay: Long = 2000, vararg subscriptionList: String) {
        if (!connected || clientId == null) delay(delay)
        if (!connected) throw PocketbaseException("You must connect to the SSE client first!")
        subscriptionList.forEach { subscription -> subscriptions.remove(subscription) }
        sendSubscribeRequest()
    }

    /**
     * Disconnects from the current realtime session,closes all active [listen]
     * coroutines and unsubscribes from all records.
     */
    public suspend fun disconnect() {
        subscriptions.clear()
        if (clientId != null) sendSubscribeRequest()
        clientId = null
        connected = false
        sseCoroutines.forEach { job ->
            job.cancelChildren()
            job.cancelAndJoin()
        }
        sseCoroutines.clear()
    }

}
