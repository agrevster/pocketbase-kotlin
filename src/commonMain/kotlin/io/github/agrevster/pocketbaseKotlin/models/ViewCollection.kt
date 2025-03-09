package io.github.agrevster.pocketbaseKotlin.models

import kotlinx.serialization.Serializable

@Serializable
/**
 * Used to represent a Collection with the type of view. If you would like access to view collection fields extend this class.
 *
 * @param[viewQuery] The SQL query used to determine what is shown in the view collection.
 */
public open class ViewCollection(public val viewQuery: String? = null): Collection()