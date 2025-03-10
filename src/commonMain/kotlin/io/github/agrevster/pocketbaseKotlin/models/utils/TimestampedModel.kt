package io.github.agrevster.pocketbaseKotlin.models.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
) : BaseModel(modelId)