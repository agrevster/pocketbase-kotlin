package io.github.agrevster.pocketbaseKotlin.services

import io.github.agrevster.pocketbaseKotlin.AuthResponse
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.Untested
import io.github.agrevster.pocketbaseKotlin.dsl.query.ExpandRelations
import io.github.agrevster.pocketbaseKotlin.dsl.query.ShowFields
import io.github.agrevster.pocketbaseKotlin.models.User
import io.github.agrevster.pocketbaseKotlin.services.utils.AuthService
import io.github.agrevster.pocketbaseKotlin.services.utils.CrudService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

public class UserAuthService(client: io.github.agrevster.pocketbaseKotlin.PocketbaseClient) : CrudService<User>(client),
    AuthService {

    private val basePath = "/api/collections/users"

    override val baseCrudPath: String = "$basePath/records"

    /**
     * Authenticate a single auth record by their username/email and password.
     * @param [email] the auth record username or email address
     * @param [password] the auth record password
     */
    public suspend fun authWithPassword(
        email: String,
        password: String,
        expandRelations: ExpandRelations = ExpandRelations(),
        fields: ShowFields = ShowFields()
    ): AuthResponse<User> {
        val params = mapOf(
            "identity" to JsonPrimitive(email),
            "password" to JsonPrimitive(password)
        )
        val response = client.httpClient.post {
            url {
                path(basePath, "auth-with-password")
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
     * @param [username] the auth record username or email address
     * @param [password] the auth record password
     */
    public suspend fun authWithUsername(
        username: String,
        password: String,
        expandRelations: ExpandRelations = ExpandRelations(),
        fields: ShowFields = ShowFields()
    ): AuthResponse<User> {
        return authWithPassword(username, password, expandRelations, fields)
    }

    @Untested("Requires oauth2")
//  @TODO handle createData body param
    /**
     * Authenticate with an OAuth2 provider and returns a new auth token and record data.
     * This action usually should be called right after the provider login page redirect.
     * You could also check the [OAuth2 web integration example](https://pocketbase.io/docs/authentication#web-oauth2-integration).
     * @param [provider] The name of the OAuth2 client provider (e.g. "google")
     * @param [code] The authorization code returned from the initial request.
     * @param [codeVerifier] The code verifier sent with the initial request as part of the code_challenge.
     * @param [redirectUrl] The redirect url sent with the initial request.
     */
    public suspend fun authWithOauth2(
        provider: String,
        code: String,
        codeVerifier: String,
        redirectUrl: String,
        expandRelations: ExpandRelations = ExpandRelations(),
        fields: ShowFields = ShowFields()
    ): AuthResponse<User> {
        val params = mapOf(
            "provider" to JsonPrimitive(provider),
            "code" to JsonPrimitive(code),
            "codeVerifier" to JsonPrimitive(codeVerifier),
            "redirectUrl" to JsonPrimitive(redirectUrl),
        )
        val response = client.httpClient.post {
            url {
                path(basePath, "auth-with-oauth2")
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
     */
    public suspend fun refresh(
        expandRelations: ExpandRelations = ExpandRelations(),
        fields: ShowFields = ShowFields()
    ): AuthResponse<User> {
        val response = client.httpClient.post {
            url {
                path(basePath, "auth-refresh")
                contentType(ContentType.Application.Json)
                expandRelations.addTo(parameters)
                fields.addTo(parameters)
            }
        }
        PocketbaseException.handle(response)
        return response.body()
    }
}