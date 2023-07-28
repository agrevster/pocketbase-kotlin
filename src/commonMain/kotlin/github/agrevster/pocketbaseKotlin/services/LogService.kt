package github.agrevster.pocketbaseKotlin.services

import github.agrevster.pocketbaseKotlin.PocketbaseClient
import github.agrevster.pocketbaseKotlin.PocketbaseException
import github.agrevster.pocketbaseKotlin.dsl.query.Filter
import github.agrevster.pocketbaseKotlin.dsl.query.ShowFields
import github.agrevster.pocketbaseKotlin.dsl.query.SortFields
import github.agrevster.pocketbaseKotlin.models.LogRequest
import github.agrevster.pocketbaseKotlin.models.utils.ListResult
import github.agrevster.pocketbaseKotlin.services.utils.BaseService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

public class LogService(client: PocketbaseClient) : BaseService(client) {
    @Serializable
    public data class HourlyStats(val total: Int, @SerialName("date") val initialDate: String) {
        @Transient
        val date: Instant = initialDate.replace(" ", "T").toInstant()
    }

    /**
     * Returns a paginated request logs list.
     * @param [page] The page (aka. offset) of the paginated list.
     * @param [perPage] The max returned request logs per page.
     */
    public suspend fun getRequestsList(
        page: Int = 1,
        perPage: Int = 30,
        sortBy: SortFields = SortFields(),
        filterBy: Filter = Filter(),
        fields: ShowFields = ShowFields()
    ): ListResult<LogRequest> {
        val params = mapOf(
            "page" to page.toString(),
            "perPage" to perPage.toString(),
        )
        val response = client.httpClient.get {
            url {
                path("api", "logs", "requests")
                params.forEach { parameters.append(it.key, it.value) }
                filterBy.addTo(parameters)
                sortBy.addTo(parameters)
                fields.addTo(parameters)
            }
        }
        PocketbaseException.handle(response)
        return response.body()
    }

    /**
     * Returns a single request log by its ID.
     * @param [id]
     */
    public suspend fun getRequest(id: String,fields: ShowFields = ShowFields()): LogRequest {
        val response = client.httpClient.get {
            url {
                path("api", "logs", "requests", id)
                fields.addTo(parameters)

            }
        }
        PocketbaseException.handle(response)
        return response.body()
    }

    /**
     * Returns hourly aggregated request logs statistics.
     */
    public suspend fun getRequestsStats(filterBy: Filter = Filter(),fields: ShowFields = ShowFields()): List<HourlyStats> {
        val response = client.httpClient.get {
            url {
                path("api", "logs", "requests", "stats")
                filterBy.addTo(parameters)
                fields.addTo(parameters)
            }
        }
        PocketbaseException.handle(response)
        return response.body()
    }

}