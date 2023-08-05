package io.github.agrevster.pocketbaseKotlin.services

import io.github.agrevster.pocketbaseKotlin.AuthResponse
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.Untested
import io.github.agrevster.pocketbaseKotlin.dsl.query.ExpandRelations
import io.github.agrevster.pocketbaseKotlin.dsl.query.ShowFields
import io.github.agrevster.pocketbaseKotlin.models.Record
import io.github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import io.github.agrevster.pocketbaseKotlin.services.utils.AuthService
import io.github.agrevster.pocketbaseKotlin.services.utils.SubCrudService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

public class RecordsService(client: io.github.agrevster.pocketbaseKotlin.PocketbaseClient) :
    SubCrudService<Record>(client), AuthService {
    public fun basePath(collectionId: String): String = "api/collections/$collectionId"
    override fun baseCrudPath(collectionId: String): String = "${basePath(collectionId)}/records"

    @Deprecated(
        "Please use the newly created files service for file related tasks",
        ReplaceWith("client.files.getFileURL"), DeprecationLevel.ERROR
    )
    public fun getFileURL(record: Record, filename: String, thumbFormat: FilesService.ThumbFormat? = null): String {
        return client.files.getFileURL(record, filename, thumbFormat)
    }

    /**
     * Authenticate a single auth record by their username/email and password.
     * @param [collection] the ID or name of the auth collection
     * @param [email] the auth record username or email address
     * @param [password] the auth record password
     */
    public suspend inline fun <reified T : BaseModel> authWithPassword(
        collection: String,
        email: String,
        password: String,
        expandRelations: ExpandRelations = ExpandRelations(),
        fields: ShowFields = ShowFields()
    ): AuthResponse<T> {
        val params = mapOf(
            "identity" to JsonPrimitive(email),
            "password" to JsonPrimitive(password)
        )
        val response = client.httpClient.post {
            url {
                path(basePath(collection), "auth-with-password")
                expandRelations.addTo(parameters)
                fields.addTo(parameters)
            }
            header("Authorization", "")
            contentType(ContentType.Application.Json)
            setBody(params)
        }
        PocketbaseException.handle(response)
        return response.body()
    }

    /**
     * Authenticate a single auth record by their username/email and password.
     * @param [collection] the ID or name of the auth collection
     * @param [username] the auth record username or email address
     * @param [password] the auth record password
     */
    public suspend inline fun <reified T : BaseModel> authWithUsername(
        collection: String,
        username: String,
        password: String,
        expandRelations: ExpandRelations = ExpandRelations(),
        fields: ShowFields = ShowFields()
    ): AuthResponse<T> = authWithPassword(collection, username, password, expandRelations, fields)


    @Untested("Requires oauth2")
//  @TODO handle createData body param
    /**
     * Authenticate with an OAuth2 provider and returns a new auth token and record data.
     * This action usually should be called right after the provider login page redirect.
     * You could also check the [OAuth2 web integration example](https://pocketbase.io/docs/authentication#web-oauth2-integration).
     * @param [collection] ID or name of the auth collection
     * @param [provider] The name of the OAuth2 client provider (eg. "google")
     * @param [code] The authorization code returned from the initial request.
     * @param [codeVerifier] The code verifier sent with the initial request as part of the code_challenge.
     * @param [redirectUrl] The redirect url sent with the initial request.
     */
    public suspend inline fun <reified T : BaseModel> authWithOauth2(
        collection: String,
        provider: String,
        code: String,
        codeVerifier: String,
        redirectUrl: String,
        expandRelations: ExpandRelations = ExpandRelations(),
        fields: ShowFields = ShowFields()
    ): AuthResponse<T> {
        val params = mapOf(
            "provider" to JsonPrimitive(provider),
            "code" to JsonPrimitive(code),
            "codeVerifier" to JsonPrimitive(codeVerifier),
            "redirectUrl" to JsonPrimitive(redirectUrl),
        )
        val response = client.httpClient.post {
            url {
                path(basePath(collection), "auth-with-oauth2")
                contentType(ContentType.Application.Json)
                header("Authorization", "")
                expandRelations.addTo(parameters)
                fields.addTo(parameters)
            }
            setBody(Json.encodeToString(params))
        }
        PocketbaseException.handle(response)
        return response.body()
    }

    /**
     * Returns a new auth response (token and user data) for already authenticated auth record.
     * This method is usually called by users on page/screen reload to ensure that the previously stored data in pb.authStore is still valid and up-to-date.
     *
     * @param [collection] ID or name of the auth collection.
     */
    public suspend inline fun <reified T : BaseModel> refresh(
        collection: String,
        expandRelations: ExpandRelations = ExpandRelations(),
        fields: ShowFields = ShowFields()
    ): AuthResponse<T> {
        val response = client.httpClient.post {
            url {
                path(basePath(collection), "auth-refresh")
                expandRelations.addTo(parameters)
                fields.addTo(parameters)
            }
        }
        PocketbaseException.handle(response)
        return response.body()
    }
}