package io.github.agrevster.pocketbaseKotlin.models

import io.github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
/**
 * The object returned from the Pocketbase Log API.
 *
 * @param message The message content of the log.
 * @param level The log's level.
 * @param data Additional log data about the given request which can differ depending on the type of log.
 */
public data class Log(
    public val message: String? = null, public val level: Int? = null, public val data: Map<String, JsonElement>? = null
) : BaseModel()