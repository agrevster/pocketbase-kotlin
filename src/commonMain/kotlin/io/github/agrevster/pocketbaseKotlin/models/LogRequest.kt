package io.github.agrevster.pocketbaseKotlin.models

import io.github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
@Deprecated("This this model is no longer used in Pocketbase 0.20.0+ please use Log instead", ReplaceWith("Log()"))
public open class LogRequest : BaseModel() {

    public val url: String? = null
    public val method: String? = null
    public val status: Int? = null
    public val auth: String? = null
    public val remoteIp: String? = null
    public val userIp: String? = null
    public val referer: String? = null
    public val userAgent: String? = null
    public val meta: Map<String, JsonElement>? = null
    override fun toString(): String {
        return "LogRequest(url=$url, method=$method, status=$status, auth=$auth, remoteIp=$remoteIp, userIp=$userIp, referer=$referer, userAgent=$userAgent, meta=$meta)"
    }
}