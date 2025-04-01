package io.github.agrevster.pocketbaseKotlin.services


import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.dsl.query.ShowFields
import io.github.agrevster.pocketbaseKotlin.models.CronJob
import io.github.agrevster.pocketbaseKotlin.services.utils.BaseService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

public class CronsService(client: PocketbaseClient) : BaseService(client) {

    /** Returns list with all registered app level cron jobs. */
    public suspend fun list(fields: ShowFields = ShowFields()): List<CronJob> {
        val response = client.httpClient.get {
            url {
                path("api", "crons")
                fields.addTo(parameters)
            }
            contentType(ContentType.Application.Json)
        }
        PocketbaseException.handle(response)
        return response.body()
    }


    /**
     * Triggers a single cron job by its id.
     *
     * @param jobId The identifier of the cron job to run.
     */
    public suspend fun run(jobId: String): Unit {
        val response = client.httpClient.post {
            url {
                path("api", "crons", jobId)
            }
        }
        PocketbaseException.handle(response)
    }

}