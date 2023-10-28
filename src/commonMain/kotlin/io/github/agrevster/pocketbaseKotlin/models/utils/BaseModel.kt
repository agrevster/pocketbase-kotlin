package io.github.agrevster.pocketbaseKotlin.models.utils

import io.github.agrevster.pocketbaseKotlin.PocketKtInternal
import kotlinx.serialization.Serializable

@Serializable
/**
 * The base class used for all [Pocketbase](https://pocketbase.io) models used
 *
 * @property [id] The unique ID of the model
 * @property [created] the created field from the model.
 * @property [updated] the updated field from the model.
 *
 */
public open class BaseModel(public open val id: String? = null) {

    public val created: InstantPocketbase? = null

    public val updated: InstantPocketbase? = null

    @OptIn(PocketKtInternal::class)
    override fun toString(): String {
        return "BaseModel(id=$id, created=$created, updated=$updated)"
    }

}