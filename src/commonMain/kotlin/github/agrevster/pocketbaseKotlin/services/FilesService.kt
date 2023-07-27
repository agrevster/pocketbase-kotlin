package github.agrevster.pocketbaseKotlin.services


import github.agrevster.pocketbaseKotlin.PocketbaseClient
import github.agrevster.pocketbaseKotlin.PocketbaseException
import github.agrevster.pocketbaseKotlin.models.Record
import github.agrevster.pocketbaseKotlin.services.utils.BaseService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

public class FilesService(client: PocketbaseClient) : BaseService(client) {


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
     * @param [thumbFormat] the thumb variant of the requested file
     * @param [token] the file token used to access protected files
     */
    public fun getFileURL(record: Record, filename: String, thumbFormat: ThumbFormat? = null, token: String? = null): String {
        val url = URLBuilder()
        this.client.baseUrl(url)
        val authTokenIfValid: () -> String =  {if (token == null) "" else "?token=$token"}
        val thumbFormatIfValid: () -> String =  {if (thumbFormat == null) "" else "?thumb=$thumbFormat"}

        return "$url/api/files/${record.collectionId}/${record.id}/$filename${thumbFormatIfValid()}${authTokenIfValid()}"
    }

    /**
     * Generates a temporary token for accessing protected files
     * The client must be authenticated to use this function
     */
    public suspend fun generateProtectedFileToken(): String{
        @Serializable
        data class TokenResponse(val token: String?)
        val response = client.httpClient.post {
            url {
                path("api", "files","token")
            }
        }
        PocketbaseException.handle(response)
        return response.body<TokenResponse>().token!!
    }




}