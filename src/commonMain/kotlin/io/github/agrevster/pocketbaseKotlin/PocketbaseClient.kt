package io.github.agrevster.pocketbaseKotlin


import io.github.agrevster.pocketbaseKotlin.services.*
import io.github.agrevster.pocketbaseKotlin.stores.BaseAuthStore
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * A multiplatform Kotlin SDK for [Pocketbase](https://pocketbase.io)
 *
 * @param baseUrl the URL of the Pocketbase server
 * @param lang the language of the Pocketbase server
 * @param store the authentication store used to store Pocketbase
 *    authentication data
 */
public class PocketbaseClient(baseUrl: URLBuilder.() -> Unit, lang: String = "en-US", store: BaseAuthStore = BaseAuthStore(null)) {

    public val baseUrl: URLBuilder.() -> Unit = baseUrl
    public val lang: String = lang

    public var authStore: BaseAuthStore = store

    /**
     * The API for Pocketbase
     * [settings](https://pocketbase.io/docs/api-settings/)
     */
    public val settings: SettingsService = SettingsService(this)


    /**
     * The API for Pocketbase
     * [collections](https://pocketbase.io/docs/api-collections/)
     */
    public val collections: CollectionService = CollectionService(this)

    /** The API for Pocketbase [logs](https://pocketbase.io/docs/api-logs/) */
    public val logs: LogService = LogService(this)

    /**
     * The API for Pocketbase
     * [records](https://pocketbase.io/docs/api-records/)
     *
     * This includes both CRUD actions and collection auth methods.
     */
    public val records: RecordsService = RecordsService(this)

    /** The API for Pocketbase [health](https://pocketbase.io/docs/api-health/) */
    public val health: HealthService = HealthService(this)

    /** The API for Pocketbase [files](https://pocketbase.io/docs/api-files/) */
    public val files: FilesService = FilesService(this)

    /**
     * The API for Pocketbase
     * [realtime](https://pocketbase.io/docs/api-realtime/)
     *
     * Adapted for
     * [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
     */
    public val realtime: RealtimeService = RealtimeService(this)

    /**
     * The API for Pocketbase
     * [backups](https://pocketbase.io/docs/api-backups/)
     */
    public val backups: BackupsService = BackupsService(this)

    /** The API for Pocketbase [crons](https://pocketbase.io/docs/api-crons/) */
    public val crons: CronsService = CronsService(this)

    /**
     * The API for Pocketbase
     * [batch](https://pocketbase.io/docs/api-records/#batch-createupdateupsertdelete-records)
     */
    public val batch: BatchService = BatchService(this)

    private val json: Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * The Ktor [HttpClient] used to connect to the
     * [Pocketbase](https://pocketbase.io) API.
     *
     * This automatically adds the current authorization token from the
     * client's [authStore].
     */
    public val httpClient: HttpClient = httpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout)
        install(SSE)
        defaultRequest {
            url(baseUrl)
            header("Authorization", authStore.token)
        }
    }
}