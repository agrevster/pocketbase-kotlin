package io.github.agrevster.pocketbaseKotlin.services.utils


import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.Untested
import io.github.agrevster.pocketbaseKotlin.models.ExternalAuth
import io.github.agrevster.pocketbaseKotlin.recordAuthFrom
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

/**
 * A group of shared of methods used by authentication services
 */
public interface AuthService {

    public val client: io.github.agrevster.pocketbaseKotlin.PocketbaseClient

    @Serializable
    public data class AuthProviderInfo(
        val name: String,
        val state: String,
        val codeVerifier: String,
        val codeChallenge: String,
        val codeChallengeMethod: String,
        val authUrl: String
    )

    @Serializable
    public data class AuthMethodsList(
        val emailPassword: Boolean,
        val usernamePassword: Boolean,
        val authProviders: List<AuthProviderInfo>
    )

    /**
     * returns a public list with the allowed collection authentication methods
     * @param [collection] ID or name of the auth collection.
     */
    public suspend fun listAuthMethods(collection: String): AuthMethodsList {
        val response = client.httpClient.get {
            url {
                path(recordAuthFrom(collection), "auth-methods")
            }
        }
        PocketbaseException.handle(response)
        return response.body()
    }

    @Untested("Requires oauth2")
    /**
     * Return a list with all external auth providers linked to a single record.
     *
     * Only admins and the account owner can access this action.
     * @param [collection] ID or name of the auth collection.
     * @param [id] ID of the auth record.
     */
    public suspend fun listExternalAuths(collection: String, id: String): List<ExternalAuth> {
        val response = client.httpClient.get {
            url {
                path(recordAuthFrom(collection), id, "external-auths")
            }
        }
        PocketbaseException.handle(response)
        return response.body()
    }

    @Untested("Requires oauth2")
    /**
     * Unlink a single external OAuth2 provider from an auth record.
     *
     * Only admins and the account owner can access this action.
     *
     * @param [collection] ID or name of the auth collection.
     * @param [id] ID of the auth record.
     * @param [provider] The name of the auth provider to unlink, eg. google, twitter, github, etc.
     */
    public suspend fun unlinkExternalAuth(collection: String, id: String, provider: String): Boolean {
        val response = client.httpClient.delete {
            url {
                path(recordAuthFrom(collection), id, "external-auths", provider)
            }
        }
        PocketbaseException.handle(response)
        return true
    }


    @Untested("Requires SMTP server")
    /**
     * Sends auth record verification email request.
     * @param [collection] ID or name of the auth collection.
     * @param [email] the email address to send the password reset request (if registered).
     */
    public suspend fun requestVerification(
        collection: String,
        email: String
    ): Boolean {
        val params = mapOf(
            "email" to JsonPrimitive(email)
        )
        val response = client.httpClient.post {
            url {
                path(recordAuthFrom(collection), "request-verification")
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(params))
            }
        }
        PocketbaseException.handle(response)
        return true
    }

    @Untested("Requires SMTP server")
    /**
     * Confirms an email address verification request.
     * @param [collection] ID or name of the auth collection.
     * @param [verificationToken] The token from the verification request email.
     */
    public suspend fun confirmVerification(
        collection: String,
        verificationToken: String
    ): Boolean {
        val params = mapOf(
            "token" to JsonPrimitive(verificationToken)
        )
        val response = client.httpClient.post {
            url {
                path(recordAuthFrom(collection), "confirm-verification")
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(params))
            }
        }
        PocketbaseException.handle(response)
        return true
    }


    @Untested("Requires SMTP server")
    /**
     * Sends a password reset email to a specified auth record email.
     * @param [collection] ID or name of the auth collection.
     * @param [email] The email address to send the password reset request (if registered).
     */
    public suspend fun requestPasswordReset(
        collection: String,
        email: String
    ): Boolean {
        val params = mapOf(
            "email" to JsonPrimitive(email)
        )
        val response = client.httpClient.post {
            url {
                path(recordAuthFrom(collection), "request-password-reset")
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(params))
            }
        }
        PocketbaseException.handle(response)
        return true
    }

    @Untested("Requires SMTP server")
    /**
     * Confirms a password reset request and sets a new auth record password.
     * @param [collection] ID or name of the auth collection.
     * @param [passwordResetToken] The token from the password reset request email.
     * @param [password] The new auth record password to set
     * @param [passwordConfirm] New auth record password confirmation.
     */
    public suspend fun confirmPasswordReset(
        collection: String,
        passwordResetToken: String,
        password: String,
        passwordConfirm: String,
    ): Boolean {
        val params = mapOf(
            "token" to JsonPrimitive(passwordResetToken),
            "password" to JsonPrimitive(password),
            "passwordConfirm" to JsonPrimitive(passwordConfirm)
        )
        val response = client.httpClient.post {
            url {
                path(recordAuthFrom(collection), "confirm-password-reset")
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(params))
            }
        }
        PocketbaseException.handle(response)
        return true
    }

    @Untested("Requires SMTP server")
    /**
     * Sends an email change request for an authenticated record.
     * @param [collection] ID or name of the auth collection.
     * @param [newEmail] The new email address to send the change email request.
     */
    public suspend fun requestEmailChange(
        collection: String,
        newEmail: String
    ): Boolean {
        val params = mapOf(
            "newEmail" to JsonPrimitive(newEmail)
        )
        val response = client.httpClient.post {
            url {
                path(recordAuthFrom(collection), "request-email-change")
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(params))
            }
        }
        PocketbaseException.handle(response)
        return true
    }

    @Untested("Requires SMTP server")
    /**
     * Confirms email address change.
     * @param [collection] ID or name of the auth collection.
     * @param [emailChangeToken] The token from the change email request.
     * @param [password] The auth record password to confirm the email address change.
     */
    public suspend fun confirmEmailChange(
        collection: String, emailChangeToken: String, password: String
    ): Boolean {
        val params = mapOf(
            "token" to JsonPrimitive(emailChangeToken),
            "password" to JsonPrimitive(password)
        )
        val response = client.httpClient.post {
            url {
                path(recordAuthFrom(collection), "confirm-email-change")
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(params))
            }
        }
        PocketbaseException.handle(response)
        return true
    }
}