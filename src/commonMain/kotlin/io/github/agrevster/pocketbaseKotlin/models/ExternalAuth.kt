package io.github.agrevster.pocketbaseKotlin.models

import io.github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import kotlinx.serialization.Serializable

@Serializable
/**
 * A Pocketbase external authentication provider
 */
public open class ExternalAuth : BaseModel() {

    public val userId: String? = null
    public val collectionId: String? = null
    public val provider: String? = null
    public val providerId: String? = null
    override fun toString(): String {
        return "ExternalAuth(userId=$userId, collectionId=$collectionId, provider=$provider, providerId=$providerId)"
    }
}