package github.agrevster.pocketbaseKotlin.services


import github.agrevster.pocketbaseKotlin.PocketbaseClient
import github.agrevster.pocketbaseKotlin.PocketbaseException
import github.agrevster.pocketbaseKotlin.services.utils.BaseService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

public class HealthService(client: PocketbaseClient) : BaseService(client) {

    @Serializable
    public data class HealthResponses(val code: Int, val message: String)

    /**
     * Returns the health status of the server.
     */
    public suspend fun healthCheck(): HealthResponses {
        val response = client.httpClient.get {
            url {
                path("api", "health")
            }
        }
        PocketbaseException.handle(response)
        return response.body()
    }

}