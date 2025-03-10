package io.github.agrevster.pocketbaseKotlin.services


import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.models.Collection
import io.github.agrevster.pocketbaseKotlin.services.utils.CrudService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

public class CollectionService(client: PocketbaseClient) : CrudService<Collection>(client) {

    @Serializable
    private data class ImportRequestBody(val collections: List<Collection>, val deleteMissing: Boolean)

    override val baseCrudPath: String = "/api/collections"

    /**
     * Bulk imports the provided Collections configuration.
     *
     * @param collections List of collections to import (replace and create).
     * @param deleteMissing If true all existing collections and schema fields
     *    that are not present in the imported configuration will be deleted,
     *    including their related records data.
     */
    public suspend fun import(
        collections: List<Collection>,
        deleteMissing: Boolean = false,
    ): Boolean {
        val response = client.httpClient.put {
            url {
                path(baseCrudPath, "import")
            }
            contentType(ContentType.Application.Json)
            setBody(ImportRequestBody(collections, deleteMissing))
        }
        PocketbaseException.handle(response)
        return true
    }

    /**
     * Returns an object will all of the collection types and their default
     * fields (used primarily in the Dashboard UI).
     *
     * Only superusers can perform this action.
     */

    public suspend fun getScaffolds(): JsonObject {
        val response = client.httpClient.get {
            url {
                path(baseCrudPath, "meta", "scaffolds")
            }
            contentType(ContentType.Application.Json)
        }
        PocketbaseException.handle(response)
        return response.body()
    }


    /**
     * Deletes all the records of a single collection (including their related
     * files and cascade delete enabled relations).
     *
     * Only superusers can perform this action.
     *
     * @param collection ID or name of the collection to truncate.
     */
    public suspend fun truncate(
        collection: String
    ): Boolean {
        val response = client.httpClient.delete {
            url {
                path(baseCrudPath, collection, "truncate")
            }
            contentType(ContentType.Application.Json)
        }
        PocketbaseException.handle(response)
        return true
    }

}