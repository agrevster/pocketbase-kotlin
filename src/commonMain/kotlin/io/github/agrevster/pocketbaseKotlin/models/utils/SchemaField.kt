package io.github.agrevster.pocketbaseKotlin.models.utils

import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField.SchemaFieldType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
/**
 * The schema for a [Collection]'s field
 *
 * @property [name] the name given to the schema by the creator
 * @property [type] the [SchemaFieldType] assigned to the schema on creation
 * @property [required] weather or not the schema is required to have a value. If this is false the [Serializable] class should have an optional(?) type
 * @property [options] all of the possible options data for the schema. These all depend on the [SchemaFieldType], see our [documentation]() for a full explanation.
 * @property [system] weather or not the schema change be modified
 * @property [id] the unique ID of the schema
 * @TODO Link do docs
 */
public class SchemaField(
    public val name: String? = null,
    public val type: SchemaFieldType? = null,
    public val required: Boolean? = null,
    public val options: SchemaOptions? = SchemaOptions(),
    public val system: Boolean? = null,
    public val id: String? = null,
) {


    @Serializable
    /**
     * All the possible options data for the schema. These all depend on the [SchemaFieldType], see our [documentation]() for a full explanation.
     * @TODO Link do docs
     */
    public data class SchemaOptions(
        val min: JsonPrimitive? = null,
        val max: JsonPrimitive? = null,
        val pattern: String? = null,
        val exceptDomains: List<String>? = null,
        val onlyDomains: List<String>? = null,
        val values: List<String>? = null,
        val maxSelect: Int? = null,
        val collectionId: String? = null,
        val cascadeDelete: Boolean? = null,
        val maxSize: Int? = null,
        val mimeTypes: List<String>? = null,
        val thumbs: List<String>? = null,
        val minSelect: Int? = null,
        val displayFields: List<String>? = null,
        val protected: Boolean? = null,
        val noDecimal: Boolean? = null,
        val convertUrls: Boolean? = null
    )

    @Serializable
    /**
     * The type of Schema Field selected by the creator
     */
    public enum class SchemaFieldType {
        @SerialName("text")
        TEXT,

        @SerialName("number")
        NUMBER,

        @SerialName("bool")
        BOOL,

        @SerialName("email")
        EMAIL,

        @SerialName("url")
        URL,

        @SerialName("date")
        DATE,

        @SerialName("select")
        SELECT,

        @SerialName("json")
        JSON,

        @SerialName("file")
        FILE,

        @SerialName("relation")
        RELATION,

        @SerialName("editor")
        EDITOR
    }

    override fun toString(): String {
        return "SchemaField(system=$system, id=$id, name=$name, type=$type, required=$required, options=$options)"
    }

}