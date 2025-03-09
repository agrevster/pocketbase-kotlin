package io.github.agrevster.pocketbaseKotlin.services

import io.github.agrevster.pocketbaseKotlin.AuthResponse
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.Untested
import io.github.agrevster.pocketbaseKotlin.dsl.query.ExpandRelations
import io.github.agrevster.pocketbaseKotlin.dsl.query.ShowFields
import io.github.agrevster.pocketbaseKotlin.models.AuthRecord
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
import kotlin.time.Duration

public class RecordsService(client: io.github.agrevster.pocketbaseKotlin.PocketbaseClient) :
    SubCrudService<Record>(client), AuthService {
    public fun basePath(collectionId: String): String = "api/collections/$collectionId"
    override fun baseCrudPath(collectionId: String): String = "${basePath(collectionId)}/records"

    /**
     * Authenticate a single auth record by their username/email and password.
     *
     * @param collection the ID or name of the auth collection
     * @param email the auth record username, email address or identityField
     * @param password the auth record password
     * @param identityField A specific identity field to use (by default
     *    fallbacks to the first matching one).
     */
    public suspend inline fun <reified T : AuthRecord> authWithPassword(
        collection: String,
        email: String,
        password: String,
        identityField: String? = null,
        expandRelations: ExpandRelations = ExpandRelations(),
        fields: ShowFields = ShowFields()
    ): AuthResponse<T> {
        val params = buildMap<String, JsonPrimitive> {
            put("identity", JsonPrimitive(email))
            put("password", JsonPrimitive(password))
            if (identityField != null) put("identityField", JsonPrimitive(identityField))
        }

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


    @Untested("Requires oauth2")
//  @TODO handle createData body param
    /**
     * Authenticate with an OAuth2 provider and returns a new auth token and
     * record data. This action usually should be called right after the
     * provider login page redirect. You could also check the
     * [OAuth2 web integration example](https://pocketbase.io/docs/authentication#web-oauth2-integration).
     *
     * @param collection ID or name of the auth collection
     * @param provider The name of the OAuth2 client provider (eg. "google")
     * @param code The authorization code returned from the initial request.
     * @param codeVerifier The code verifier sent with the initial request as
     *    part of the code_challenge.
     * @param redirectUrl The redirect url sent with the initial request.
     */
    public suspend inline fun <reified T : AuthRecord> authWithOauth2(
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
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(params))
        }
        PocketbaseException.handle(response)
        return response.body()
    }


    /**
     * Requests an OTP email be sent
     * Authenticate a single auth record with an one-time password (OTP).
     *
     * Note that when requesting an OTP we return an otpId even if a user
     * with the provided email doesn't exist as a very basic enumeration
     * protection.
     *
     * @param collection the ID or name of the auth collection
     * @param email the auth record's email.
     * @return returns the OTP ID
     */
    public suspend inline fun <reified T : AuthRecord> requestOTP(
        collection: String,
        email: String,
    ): String {
        val params = mapOf("email" to JsonPrimitive(email))

        val response = client.httpClient.post {
            url {
                path(basePath(collection), "request-otp")
            }
            header("Authorization", "")
            contentType(ContentType.Application.Json)
            setBody(params)
        }
        PocketbaseException.handle(response)
        return response.body<Map<String, JsonPrimitive>>()["otpId"]!!.content
    }


    /**
     * Use the OTP password from the OTP email to log in
     * Authenticate a single auth record with an one-time password (OTP).
     *
     * Note that when requesting an OTP we return an otpId even if a user
     * with the provided email doesn't exist as a very basic enumeration
     * protection.
     *
     * @param collection the ID or name of the auth collection
     * @param otpID the auth record's email.
     * @param password the auth record's email.
     */
    public suspend inline fun <reified T : AuthRecord> authWithOTP(
        collection: String,
        otpID: String,
        password: String,
        expandRelations: ExpandRelations = ExpandRelations(),
        fields: ShowFields = ShowFields()
    ): AuthResponse<T> {
        val params = mapOf("otpId" to JsonPrimitive(otpID), "password" to JsonPrimitive(password))

        val response = client.httpClient.post {
            url {
                path(basePath(collection), "auth-with-otp")
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
     * Returns a new auth response (token and user data) for already
     * authenticated auth record. This method is usually called by users
     * on page/screen reload to ensure that the previously stored data in
     * pb.authStore is still valid and up-to-date.
     *
     * @param collection ID or name of the auth collection.
     */
    public suspend inline fun <reified T : BaseModel> refresh(
        collection: String, expandRelations: ExpandRelations = ExpandRelations(), fields: ShowFields = ShowFields()
    ): AuthResponse<T> {
        val response = client.httpClient.post {
            url {
                path(basePath(collection), "auth-refresh")
                expandRelations.addTo(parameters)
                fields.addTo(parameters)
            }
            contentType(ContentType.Application.Json)
        }
        PocketbaseException.handle(response)
        return response.body()
    }


    /**
     * Impersonate allows you to authenticate as a different user by generating
     * a non-refreshable auth token.
     *
     * Only superusers can perform this action.
     *
     * @param collection the ID or name of the auth collection
     * @param id the ID of the user you with to impersonate
     * @param duration Optional custom JWT duration for the exp claim (in
     *    seconds). If not set or 0, it fallbacks to the default collection
     *    auth token duration option.
     */
    public suspend inline fun <reified T : AuthRecord> impersonate(
        collection: String,
        id: String,
        duration: Duration? = null,
        expandRelations: ExpandRelations = ExpandRelations(),
        fields: ShowFields = ShowFields()
    ): AuthResponse<T> {
        val response = client.httpClient.post {
            url {
                path(basePath(collection), "impersonate", id)
                expandRelations.addTo(parameters)
                fields.addTo(parameters)
            }

            if (duration != null) {
                setBody(mapOf("duration" to JsonPrimitive(duration.inWholeSeconds)))
            }
            contentType(ContentType.Application.Json)
        }
        PocketbaseException.handle(response)
        return response.body()
    }
}