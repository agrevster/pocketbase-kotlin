package io.github.agrevster.pocketbaseKotlin.services

import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.Untested
import io.github.agrevster.pocketbaseKotlin.dsl.query.ShowFields
import io.github.agrevster.pocketbaseKotlin.services.utils.BaseService
import io.github.agrevster.pocketbaseKotlin.toJsonPrimitive
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

public class SettingsService(client: io.github.agrevster.pocketbaseKotlin.PocketbaseClient) : BaseService(client) {
    /**
     * Returns a list with all available application settings.
     *
     * Secret/password fields are automatically redacted with ****** characters.
     */
    public suspend inline fun <reified T> getAll(fields: ShowFields = ShowFields()): T {
        val response = client.httpClient.get {
            url {
                path("/api/settings")
                fields.addTo(parameters)
            }
        }
        PocketbaseException.handle(response)
        return response.body()
    }

    /**
     * Bulk updates application settings and returns the updated settings list.
     * @param [body] the JSON body of the settings you want to tweak.
     */
    public suspend inline fun <reified T> update(body: String, fields: ShowFields = ShowFields()): T {
        val response = client.httpClient.patch {
            url {
                path("/api/settings")
                contentType(ContentType.Application.Json)
                fields.addTo(parameters)

            }
            setBody(body)
        }
        PocketbaseException.handle(response)
        return response.body()
    }

    @Untested("Requires an S3 Server. Will not be tested because it's just an http request without a body.")
    /**
     * Performs a S3 storage connection test.
     */
    public suspend fun testS3(): Boolean {
        val response = client.httpClient.post {
            url {
                path("/api/settings/test/s3")
            }
        }
        PocketbaseException.handle(response)
        return true
    }

    @Untested("Requires SMTP server")
    /**
     * Sends a test user email.
     * @param [toEmail] The receiver of the test email.
     * @param [emailTemplate] The test email template to send:
     * verification, password-reset or email-change.
     */
    public suspend fun testEmail(toEmail: String, emailTemplate: String): Boolean {
        val body = mapOf(
            "email" to toEmail.toJsonPrimitive(),
            "template" to emailTemplate.toJsonPrimitive()
        )
        val response = client.httpClient.post {
            url {
                path("/api/settings/test/email")
                setBody(Json.encodeToString(body))
            }
        }
        PocketbaseException.handle(response)
        return true
    }

    @Untested("Requires Apple OAuth2")
    /**
     * Generates a new Apple OAuth2 client secret key.
     * @param [clientId] the apple service id.
     * @param [teamId] 10-character string associated with your developer account (usually could be found next to your name in the Apple Developer site)
     * @param [keyId] 10-character key identifier generated for the "Sign in with Apple" private key associated with your developer account
     * @param [privateKey] the private key associated to your app
     * @param [duration] how long the generated JWT token should be considered valid. The specified value must be in seconds and max 15777000 (~6months).
     */
    public suspend fun generateAppleClientSecret(
        clientId: String,
        teamId: String,
        keyId: String,
        privateKey: String,
        duration: Long
    ): String {
        @Serializable
        data class AppleSecret(val secret: String)

        val body = mapOf(
            "clientId" to clientId.toJsonPrimitive(),
            "teamId" to teamId.toJsonPrimitive(),
            "keyId" to keyId.toJsonPrimitive(),
            "privateKey" to privateKey.toJsonPrimitive(),
            "duration" to duration.toJsonPrimitive()
        )
        val response = client.httpClient.post {
            url {
                path("/api/settings/apple/generate-client-secret")
                setBody(Json.encodeToString(body))
            }
        }
        PocketbaseException.handle(response)
        return response.body<AppleSecret>().secret
    }
}