package io.github.agrevster.pocketbaseKotlin.services


import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.dsl.BatchRequestBuilder
import io.github.agrevster.pocketbaseKotlin.dsl.BatchResponse
import io.github.agrevster.pocketbaseKotlin.services.utils.BaseService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

public class BatchService(client: PocketbaseClient) : BaseService(client) {


    /**
     * Batch and transactional create/update/upsert/delete of multiple records
     * in a single request.
     *
     * Use the DSL functions to create a batch request. See
     * [readme](https://github.com/agrevster/pocketbase-kotlin?#caveats)
     * for examples.
     */
    public suspend fun send(setup: BatchRequestBuilder.() -> Unit): List<BatchResponse> {
        val builder = BatchRequestBuilder()
        builder.setup()

        val response = client.httpClient.post {
            url {
                path("api", "batch")
            }
            setBody(MultiPartFormDataContent(builder.createBatchBody()))
        }
        PocketbaseException.handle(response)
        return response.body()
    }

}