package io.github.agrevster.pocketbaseKotlin.models.utils

import io.github.agrevster.pocketbaseKotlin.PocketKtInternal
import kotlinx.serialization.Serializable

@Serializable
/**
 * The base class used for all [Pocketbase](https://pocketbase.io) models used
 *
 * @property [id] The unique ID of the model
 *
 */
public open class BaseModel(public open val id: String? = null)