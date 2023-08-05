package io.github.agrevster.pocketbaseKotlin.models.utils

import io.github.agrevster.pocketbaseKotlin.PocketKtInternal
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
/**
 * The base class used for all [Pocketbase](https://pocketbase.io) models used
 *
 * @property [id] The unique ID of the model
 * @property [initialCreated] the raw created field from the model formatted in the GO timestamp format.
 * Use [created] to access the type of [Instant]
 * @property [initialUpdated] the raw updated field from the model formatted in the GO timestamp format.
 * Use [updated] to access the type of [Instant]
 *
 */
public open class BaseModel(public open val id: String? = null) {


    @SerialName("created")
    @PocketKtInternal
    public val initialCreated: String? = null

    @SerialName("updated")
    @PocketKtInternal
    public val initialUpdated: String? = null

    @OptIn(PocketKtInternal::class)
    @Transient
    public val created: Instant? = initialCreated?.replace(" ", "T")?.toInstant()

    @OptIn(PocketKtInternal::class)
    @Transient
    public val updated: Instant? = initialUpdated?.replace(" ", "T")?.toInstant()


    @OptIn(PocketKtInternal::class)
    override fun toString(): String {
        return "BaseModel(id=$id, created=$initialCreated, updated=$initialUpdated)"
    }

}