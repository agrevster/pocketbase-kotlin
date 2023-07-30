package io.github.agrevster.pocketbaseKotlin.dsl.query

import io.github.agrevster.pocketbaseKotlin.models.Record
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * This allows a [Record]'s relations to be expanded and added to the response.
 * @param [relations] See [the Pocketbase website](https://pocketbase.io/docs/expanding-relations/) for a guide on how to format the [relations].
 */
public data class ExpandRelations(val relations: String? = null) {
    /**
     * Adds the current [ExpandRelations] to a request's parameters
     */
    public fun addTo(params: ParametersBuilder) {
        if (relations != null) params.append("expand", this.relations)
    }
}

@Serializable
/**
 * Used for when you wish to serialize a [Record] with expanded relations.
 * Type [T] is the [Serializable] class of the related record.
 * Be sure you include the [ExpandRelations] field in the request.
 *
 * For a full example of how to serialize a [Record] with expanded relations see [the docs]()
 *
 * @property [expand] the list of records that have been expanded.
 * Key: (expanded record name) -> Value: expanded record object of type [T]
 */
public open class ExpandRecord<T> : Record() {

    public val expand: Map<String, T>? = null

    override fun toString(): String {
        return "ExpandRecord(expand=$expand)"
    }
}

@Serializable
/**
 * Used for when you wish to serialize a [Record] with multiple expanded relations of different types or nested relations.
 * Be sure you include the [ExpandRelations] field in the request.
 *
 * For a full example of how to serialize a [Record] with multiple or nested expanded relations see [the docs]()
 *
 * @property [expand] the list of records that have been expanded.
 * Key: (expanded record name) -> Value: expanded record object's fields
 */
public open class ExpandJsonElement : Record() {
    public val expand: Map<String, JsonElement>? = null

    override fun toString(): String {
        return "ExpandJsonElement(expand=$expand)"
    }
}