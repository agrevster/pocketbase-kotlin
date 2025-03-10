package io.github.agrevster.pocketbaseKotlin.models

import io.github.agrevster.pocketbaseKotlin.models.Collection.CollectionType.*
import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import io.github.agrevster.pocketbaseKotlin.models.utils.TimestampedModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
/**
 * The object returned from the Pocketbase Collections API.
 * Depending on the collection type, if you would like access to addition methods see [CollectionType]'s comments.
 *
 * @param name the name of the collection.
 * @param type the type of the collection.
 * @param system weather or not the collection was created internally by Pocketbase.
 * @param fields the collection's [SchemaField]s which are used to determine what values are acceptable.
 * @param listRule the pocketbase API rule which determines who can view each [Record] in the collection.
 * @param viewRule the pocketbase API rule which determines who can view an individual [Record] in the collection.
 * @param createRule the pocketbase API rules which determines who can create a [Record] in the collection.
 * @param updateRule the pocketbase API rules which determines who can update a [Record] in the collection.
 * @param deleteRule the pocketbase API rules which determines who can delete a [Record] in the collection.
 * @param indexes the collections indexes which are used to determine which fields are unique.
 * */
public open class Collection(
    public val name: String? = null,
    public val type: CollectionType? = null,
    public val system: Boolean? = null,
    public val fields: List<SchemaField>? = null,
    public val listRule: String? = null,
    public val viewRule: String? = null,
    public val createRule: String? = null,
    public val updateRule: String? = null,
    public val deleteRule: String? = null,
    public val indexes: List<String>? = null,
    @Transient private val collectionId: String? = null
) : TimestampedModel(modelId = collectionId) {

    /**
     * All the supported collection types.
     * @property BASE the base collection type, no additional options. Use class: [Collection].
     * @property AUTH an authentication collection. Use class: [AuthCollection].
     * @property VIEW a view collection. Use class: [ViewCollection].
     */
    @Serializable
    public enum class CollectionType {
        @SerialName("base")
        BASE,

        @SerialName("auth")
        AUTH,

        @SerialName("view")
        VIEW;
    }

    override fun toString(): String {
        return "Collection(name=$name, type=$type, system=$system, fields=$fields, listRule=$listRule, viewRule=$viewRule, createRule=$createRule, updateRule=$updateRule, deleteRule=$deleteRule, indexes=$indexes, collectionId=$collectionId)"
    }

}