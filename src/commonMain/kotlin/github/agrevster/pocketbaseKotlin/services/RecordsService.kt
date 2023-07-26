package github.agrevster.pocketbaseKotlin.services

import github.agrevster.pocketbaseKotlin.AuthResponse
import github.agrevster.pocketbaseKotlin.PocketbaseClient
import github.agrevster.pocketbaseKotlin.PocketbaseException
import github.agrevster.pocketbaseKotlin.Untested
import github.agrevster.pocketbaseKotlin.dsl.query.ExpandRelations
import github.agrevster.pocketbaseKotlin.models.Record
import github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import github.agrevster.pocketbaseKotlin.services.utils.AuthService
import github.agrevster.pocketbaseKotlin.services.utils.SubCrudService
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

public class RecordsService(client: PocketbaseClient) : SubCrudService<Record>(client), AuthService {
    public fun basePath(collectionId: String): String = "api/collections/$collectionId"
    override fun baseCrudPath(collectionId: String): String = "${basePath(collectionId)}/records"

    /**
     * The currently supported Pocketbase thumb formats
     */
    public enum class ThumbFormat {
        /**
         * (eg. 100x300) - crop to WxH viewbox (from center)
         */
        WxH,

        /**
         * (eg. 100x300t) - crop to WxH viewbox (from top)
         */
        WxHt,

        /**
         *  (eg. 100x300b) - crop to WxH viewbox (from bottom)
         */
        WxHb,

        /**
         *  (eg. 100x300f) - fit inside a WxH viewbox (without cropping)
         */
        WxHf,

        /**
         * (eg. 0x300) - resize to H height preserving the aspect ratio
         */
        `0xH`,

        /**
         * (eg. 100x0) - resize to W width preserving the aspect ratio
         */
        Wx0;
    }

    /**
     * Gets the url to a file in Pocketbase
     * @param [record] the record where the file is present
     * @param [filename] the file's name
     */
    public fun getFileURL(record: Record, filename: String, thumbFormat: ThumbFormat? = null): String {
        val url = URLBuilder()
        this.client.baseUrl(url)
        return if (thumbFormat != null) {
            "$url/api/files/${record.collectionId}/${record.id}/$filename?thumb=$thumbFormat"
        } else {
            "$url/api/files/${record.collectionId}/${record.id}/$filename"
        }
    }

    /**
     * Authenticate a single auth record by their username/email and password.
     * @param [collection] the ID or name of the auth collection
     * @param [email] the auth record username or email address
     * @param [password] the auth record password
     */
    public suspend inline fun <reified T : BaseModel> authWithPassword(
        collection: String, email: String, password: String, expandRelations: ExpandRelations = ExpandRelations()
    ): AuthResponse<T> {
        val params = mapOf(
            "identity" to JsonPrimitive(email),
            "password" to JsonPrimitive(password)
        )
        val response = client.httpClient.post {
            url {
                path(basePath(collection), "auth-with-password")
                expandRelations.addTo(parameters)
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
        expandRelations: ExpandRelations = ExpandRelations()
    ): AuthResponse<T> = authWithPassword(collection, username, password, expandRelations)


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
        expandRelations: ExpandRelations = ExpandRelations()
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
        expandRelations: ExpandRelations = ExpandRelations()
    ): AuthResponse<T> {
        val response = client.httpClient.post {
            url {
                path(basePath(collection), "auth-refresh")
                expandRelations.addTo(parameters)
            }
        }
        PocketbaseException.handle(response)
        return response.body()
    }
}