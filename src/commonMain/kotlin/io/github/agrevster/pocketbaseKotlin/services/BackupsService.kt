package io.github.agrevster.pocketbaseKotlin.services


import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.dsl.query.ShowFields
import io.github.agrevster.pocketbaseKotlin.services.utils.BaseService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

public class BackupsService(client: io.github.agrevster.pocketbaseKotlin.PocketbaseClient) : BaseService(client) {

    @Serializable
    public data class Backup(val key: String, val modified: String, val size: Long)

    /**
     * Returns a list of all available back up files
     */
    public suspend fun getFullList(fields: ShowFields = ShowFields()): List<Backup> {
        val response = client.httpClient.get {
            url {
                path("api", "backups")
                fields.addTo(parameters)
            }
            contentType(ContentType.Application.Json)
        }
        PocketbaseException.handle(response)
        return response.body()
    }

    /**
     * Creates a new app data backup.
     * This action will return an error if the backup to delete is still being generated or part of a restore operation.
     * @param [name] The name of the backup you wish to create. It must be in the format [a-z0-9_-].zip.
     * If not set, it will be autogenerated.
     */
    public suspend fun create(name: String? = null): Boolean {
        val body = mapOf(
            "name" to JsonPrimitive(name)
        )
        val response = client.httpClient.post {
            url {
                path("api", "backups")
            }
            contentType(ContentType.Application.Json)
            if (name != null) setBody(body)
        }
        PocketbaseException.handle(response)
        return true
    }

    /**
     * Deletes a single backup by name
     * This action will return an error if the backup to delete is still being generated or part of a restore operation.
     * @param [key] the key of the backup you wish to delete
     */
    public suspend fun delete(key: String): Boolean {
        val response = client.httpClient.delete {
            url {
                path("api", "backups", key)
            }
        }
        PocketbaseException.handle(response)
        return true
    }

    /**
     * Restore a single backup by its name and restarts the current running PocketBase process.
     * This action will return an error if the backup to delete is still being generated or part of a restore operation.
     * @param [key] the key of the backup you wish to restore to
     */
    public suspend fun restore(key: String): Boolean {
        val response = client.httpClient.post {
            url {
                path("api", "backups", key, "restore")
            }
        }
        PocketbaseException.handle(response)
        return true
    }


    /**
     * Gets the url to download a single backup file
     * @param [key] the key of the backup you wish to download
     * @param [token] the file token for granting access to the backup file (must be logged in as admin)
     */
    public fun getBackupUrl(key: String, token: String): String {
        val url = URLBuilder()
        this.client.baseUrl(url)
        return "$url/api/backups/${key}?token=${token}"
    }
}