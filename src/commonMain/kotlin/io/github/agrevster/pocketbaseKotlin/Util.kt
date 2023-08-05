package io.github.agrevster.pocketbaseKotlin

import io.github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.coroutines.CoroutineContext

internal inline fun <reified T> className() = T::class::simpleName

@Serializable
public data class PocketbaseError(val code: Int, val message: String, val data: Map<String, JsonElement>)

@DslMarker
public annotation class PocketKtDSL

@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This function does not have an automated test associated with it. Use at your own risk!"
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
internal annotation class Untested(@Suppress("unused") val reason: String = "")

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This property only exists for serialization purposes. Please use the non internal property unless you really know what your doing!"
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
internal annotation class PocketKtInternal

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This API is experimental. It may be changed in the future without notice."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
internal annotation class PocketKtExperimental

public class PocketbaseException(public val reason: String) : Exception(reason) {
    public constructor(error: PocketbaseError) : this("${error.message}: ${error.code}\n ${Json.encodeToString(error.data)}")

    public companion object {
        public suspend fun handle(response: HttpResponse) {
            if (!response.status.isSuccess()) throw PocketbaseException(response.body<PocketbaseError>())
        }
    }
}

internal fun Array<out String>.toFieldList(): String {
    val builder = StringBuilder()
    for (i in indices) {
        if (i < this.size - 1) builder.append("${this[i]},")
        else builder.append(this[i])
    }
    return builder.toString()
}

internal suspend fun CoroutineContext.cancelAndJoin() {
    this[Job]?.cancelAndJoin()
}

@Serializable
public data class AuthResponse<T : BaseModel>(@SerialName("record") val record: T, val token: String)

internal fun recordAuthFrom(collection: String) = "/api/collections/$collection"

@OptIn(ExperimentalSerializationApi::class)
public fun nullJsonPrimitive(): JsonPrimitive = JsonPrimitive(null)

public fun String.toJsonPrimitive(): JsonPrimitive = JsonPrimitive(this)

public fun String?.toJsonPrimitiveOrNull(): JsonPrimitive = JsonPrimitive(this)

public fun Boolean.toJsonPrimitive(): JsonPrimitive = JsonPrimitive(this)

public fun Boolean?.toJsonPrimitiveOrNull(): JsonPrimitive = JsonPrimitive(this)

public fun Number.toJsonPrimitive(): JsonPrimitive = JsonPrimitive(this)

public fun Instant?.toJsonPrimitive(): JsonPrimitive? =
    if (this.toString() != "null") JsonPrimitive(this.toString().replace("T", " ")) else null

public fun JsonPrimitive.toInstant(): Instant = this.toString().replace(" ", "T").removeSurrounding("\"").toInstant()

public fun JsonPrimitive.toNumber(): Double = this.toString().toDouble()

public fun JsonPrimitive.isNumber(): Boolean = this.toString().all { c -> c.isDigit() || c == '.' }

public fun putIfNotNull(map: MutableMap<String, Any>, key: String, value: Any?) {
    if (value != null) map[key] = value
}

public fun JsonPrimitive.isInstant(): Boolean {
    var ret = true
    try {
        this.toInstant()
    } catch (e: Exception) {
        ret = false
    }
    return ret
}

public data class FileUpload(val field: String, val file: ByteArray?, val fileName: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FileUpload

        if (field != other.field) return false
        if (file != null) {
            if (other.file == null) return false
            if (!file.contentEquals(other.file)) return false
        } else if (other.file != null) return false
        if (fileName != other.fileName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = field.hashCode()
        result = 31 * result + (file?.contentHashCode() ?: 0)
        result = 31 * result + fileName.hashCode()
        return result
    }
}