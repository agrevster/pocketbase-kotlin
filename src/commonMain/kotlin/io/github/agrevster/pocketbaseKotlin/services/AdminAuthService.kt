package io.github.agrevster.pocketbaseKotlin.services


import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.Untested
import io.github.agrevster.pocketbaseKotlin.dsl.query.ShowFields
import io.github.agrevster.pocketbaseKotlin.models.Admin
import io.github.agrevster.pocketbaseKotlin.services.utils.CrudService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive


public class AdminAuthService(client: io.github.agrevster.pocketbaseKotlin.PocketbaseClient) :
    CrudService<Admin>(client) {

    @Serializable
    public data class AdminAuthResponse(@SerialName("admin") val record: Admin, val token: String)

    override val baseCrudPath: String = "/api/admins"

    /**
     * Authenticate a single auth record by their username/email and password.
     * @param [email] Auth record email address.
     * @param [password] Auth record password.
     */
    public suspend fun authWithPassword(
        email: String,
        password: String,
        fields: ShowFields = ShowFields()
    ): AdminAuthResponse {
        val params = mapOf(
            "identity" to JsonPrimitive(email),
            "password" to JsonPrimitive(password)
        )
        val response = client.httpClient.post {
            url {
                path(baseCrudPath, "auth-with-password")
                contentType(ContentType.Application.Json)
                header("Authorization", "")
                fields.addTo(parameters)
            }
            setBody(Json.encodeToString(params))
        }
        PocketbaseException.handle(response)
        return response.body()
    }

    /**
     * Returns a new auth response (token and user data) for already authenticated auth record.
     */
    public suspend fun authRefresh(fields: ShowFields = ShowFields()): AdminAuthResponse {
        val response = client.httpClient.post {
            url {
                path(baseCrudPath, "auth-refresh")
                fields.addTo(parameters)
            }
        }
        PocketbaseException.handle(response)
        return response.body()
    }

    @Untested("Requires SMTP server")
    /**
     * Sends a password reset email to a specified auth record email.
     * @param [email] The email address to send the password reset request (if registered).
     */
    public suspend fun requestPasswordReset(
        email: String
    ): Boolean {
        val params = mapOf(
            "email" to JsonPrimitive(email)
        )
        val response = client.httpClient.post {
            url {
                path(baseCrudPath, "request-password-reset")
                contentType(ContentType.Application.Json)
            }
            setBody(Json.encodeToString(params))
        }
        PocketbaseException.handle(response)
        return true
    }

    @Untested("Requires SMTP server")
    /**
     * Confirms a password reset request and sets a new auth record password.
     * @param [passwordResetToken] The token from the password reset request email.
     * @param [password] The new auth record password to set.
     * @param [passwordConfirm] New auth record password confirmation.
     */
    public suspend fun confirmPasswordReset(
        passwordResetToken: String,
        password: String,
        passwordConfirm: String,
        fields: ShowFields = ShowFields()
    ): AdminAuthResponse {
        val params = mapOf(
            "token" to JsonPrimitive(passwordResetToken),
            "password" to JsonPrimitive(password),
            "passwordConfirm" to JsonPrimitive(passwordConfirm)
        )
        val response = client.httpClient.post {
            url {
                path(baseCrudPath, "confirm-password-reset")
                contentType(ContentType.Application.Json)
                fields.addTo(parameters)
            }
            setBody(Json.encodeToString(params))
        }
        PocketbaseException.handle(response)
        return response.body()
    }
}