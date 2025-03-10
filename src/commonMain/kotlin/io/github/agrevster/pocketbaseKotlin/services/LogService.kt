package io.github.agrevster.pocketbaseKotlin.services

import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.dsl.query.Filter
import io.github.agrevster.pocketbaseKotlin.dsl.query.ShowFields
import io.github.agrevster.pocketbaseKotlin.dsl.query.SortFields
import io.github.agrevster.pocketbaseKotlin.models.Log
import io.github.agrevster.pocketbaseKotlin.models.utils.InstantPocketbase
import io.github.agrevster.pocketbaseKotlin.models.utils.ListResult
import io.github.agrevster.pocketbaseKotlin.services.utils.BaseService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

public class LogService(client: PocketbaseClient) : BaseService(client) {
    @Serializable
    public data class HourlyStats(val total: Int, val date: InstantPocketbase)

    /**
     * Returns a paginated log list.
     *
     * @param page The page (aka. offset) of the paginated list.
     * @param perPage The max returned request logs per page.
     */
    public suspend fun getList(
        page: Int = 1,
        perPage: Int = 30,
        sortBy: SortFields = SortFields(),
        filterBy: Filter = Filter(),
        fields: ShowFields = ShowFields()
    ): ListResult<Log> {
        val params = mapOf(
            "page" to page.toString(),
            "perPage" to perPage.toString(),
        )
        val response = client.httpClient.get {
            url {
                path("api", "logs")
                params.forEach { parameters.append(it.key, it.value) }
                filterBy.addTo(parameters)
                sortBy.addTo(parameters)
                fields.addTo(parameters)
            }
            contentType(ContentType.Application.Json)
        }
        PocketbaseException.handle(response)
        return response.body()
    }


    /**
     * Returns a single log by its ID.
     *
     * @param id The id of the long you wish to retrieve.
     */
    public suspend fun getOne(id: String, fields: ShowFields = ShowFields()): Log {
        val response = client.httpClient.get {
            url {
                path("api", "logs", id)
                fields.addTo(parameters)

            }
            contentType(ContentType.Application.Json)
        }
        PocketbaseException.handle(response)
        return response.body()
    }

    /** Returns hourly aggregated logs statistics. */
    public suspend fun getStats(
        filterBy: Filter = Filter(), fields: ShowFields = ShowFields()
    ): List<HourlyStats> {
        val response = client.httpClient.get {
            url {
                path("api", "logs", "stats")
                filterBy.addTo(parameters)
                fields.addTo(parameters)
            }
            contentType(ContentType.Application.Json)
        }
        PocketbaseException.handle(response)
        return response.body()
    }

}