package io.github.agrevster.pocketbaseKotlin.models.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.ExperimentalTime

@Serializable
/**
 * A class that can be extended if you want to view the created and updated
 * time of a model.
 *
 * @param created the created field from the model.
 * @param updated the updated field from the model.
 */
public open class TimestampedModel(
    public val created: InstantPocketbase? = null,
    public val updated: InstantPocketbase? = null,
    @Transient private val modelId: String? = null,
) : BaseModel(modelId) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TimestampedModel) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return modelId?.hashCode() ?: 0
    }
}