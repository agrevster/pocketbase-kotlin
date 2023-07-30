package io.github.agrevster.pocketbaseKotlin.services


import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.dsl.query.ShowFields
import io.github.agrevster.pocketbaseKotlin.services.utils.BaseService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

public class HealthService(client: io.github.agrevster.pocketbaseKotlin.PocketbaseClient) : BaseService(client) {

    @Serializable
    public data class HealthResponses(val code: Int, val message: String)

    /**
     * Returns the health status of the server.
     */
    public suspend fun healthCheck(fields: ShowFields = ShowFields()): HealthResponses {
        val response = client.httpClient.get {
            url {
                path("api", "health")
                fields.addTo(parameters)
            }
        }
        PocketbaseException.handle(response)
        return response.body()
    }

}